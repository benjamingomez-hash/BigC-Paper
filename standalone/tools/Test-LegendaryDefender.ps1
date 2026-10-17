# Diagnostic only: never executes the downloaded distribution or changes antivirus exclusions.
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$JarPath,
    [Parameter(Mandatory = $true)][string]$BaselineZip,
    [Parameter(Mandatory = $true)][string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$OutputDirectory = (Resolve-Path $OutputDirectory).Path
$report = [ordered]@{
    commit = $env:GITHUB_SHA
    startedUtc = [DateTime]::UtcNow.ToString('o')
    completed = $false
    cleanJar = $false
    baselineError = $null
    engine = $null
    scans = @()
    error = $null
}
$scanIndex = 0

function Invoke-FileScan([string]$Path, [string]$Label) {
    $script:scanIndex++
    $result = [ordered]@{ label = $Label; sha256 = $null; result = 'Unavailable'; exitCode = $null }
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        $result.result = 'MissingOrBlocked'
        return [pscustomobject]$result
    }
    $Path = (Resolve-Path -LiteralPath $Path).Path
    $result.sha256 = (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
    $prefix = Join-Path $OutputDirectory ('scan-{0:d2}' -f $script:scanIndex)
    # DisableRemediation applies only to this diagnostic custom scan, so a detection
    # is reported as nonzero instead of being mistaken for a clean, remediated scan.
    # Real-time protection, signatures, exclusions and sample contents are untouched.
    $arguments = '-Scan -ScanType 3 -File "{0}" -DisableRemediation' -f $Path
    $process = Start-Process -FilePath $script:scanner -ArgumentList $arguments `
        -RedirectStandardOutput "$prefix.stdout.txt" -RedirectStandardError "$prefix.stderr.txt" `
        -NoNewWindow -PassThru
    $null = $process.Handle
    if (-not $process.WaitForExit(180000)) {
        $process.Kill()
        throw "Defender timed out while scanning $Label."
    }
    $process.WaitForExit()
    $result.exitCode = $process.ExitCode
    if ($process.ExitCode -eq 0 -and (Test-Path -LiteralPath $Path)) {
        $after = (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($after -ne $result.sha256) { throw "Artifact changed during scan: $Label." }
        $result.result = 'Clean'
    } else {
        # MpCmdRun can return 2 both for a detection and for some scan errors.
        $result.result = 'DetectionOrScanError'
    }
    return [pscustomobject]$result
}

try {
    Get-Command Get-MpComputerStatus, Update-MpSignature -ErrorAction Stop | Out-Null
    $status = Get-MpComputerStatus
    if (-not $status.AMServiceEnabled -or -not $status.AntivirusEnabled) {
        throw 'Microsoft Defender is not active on this runner; no verified artifact can be produced.'
    }
    Update-MpSignature -ErrorAction Stop
    $status = Get-MpComputerStatus
    if (-not $status.AMServiceEnabled -or -not $status.AntivirusEnabled -or $status.AntivirusSignatureAge -gt 2) {
        throw 'Defender is unavailable or its signatures are stale.'
    }
    $report.engine = [ordered]@{
        productVersion = $status.AMProductVersion
        engineVersion = $status.AMEngineVersion
        signatureVersion = $status.AntivirusSignatureVersion
        signatureUpdatedUtc = $status.AntivirusSignatureLastUpdated.ToUniversalTime().ToString('o')
        realTimeProtectionEnabled = $status.RealTimeProtectionEnabled
    }
    $platform = Join-Path $env:ProgramData 'Microsoft/Windows Defender/Platform'
    $script:scanner = Get-ChildItem "$platform/*/MpCmdRun.exe" -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTimeUtc -Descending | Select-Object -First 1 -ExpandProperty FullName
    if (-not $script:scanner) { $script:scanner = Join-Path $env:ProgramFiles 'Windows Defender/MpCmdRun.exe' }
    if (-not (Test-Path -LiteralPath $script:scanner -PathType Leaf)) { throw 'MpCmdRun.exe is unavailable.' }

    $members = @()
    if (Test-Path -LiteralPath $BaselineZip -PathType Leaf) {
        $hash = (Get-FileHash -LiteralPath $BaselineZip -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($hash -ne 'fa789ababf4a84d0cbcdc5834e1216f0c37fa684956cf9b95d6810e360f62734') {
            throw 'The baseline ZIP does not match the reported artifact.'
        }
        # The expected, previously audited hash fixes the exact eight archive paths.
        # Samples stay outside the report directory and are never uploaded or executed.
        $samples = Join-Path ([IO.Path]::GetTempPath()) ([Guid]::NewGuid().ToString('N'))
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        try {
            [IO.Compression.ZipFile]::ExtractToDirectory((Resolve-Path $BaselineZip).Path, $samples)
        } catch {
            # Real-time protection may block an original member during extraction.
            # Record that limitation and still independently scan the new build.
            $report.baselineError = $_.Exception.Message
        }
        if (Test-Path -LiteralPath $samples -PathType Container) {
            $members = @(Get-ChildItem $samples -File -Recurse | Sort-Object FullName)
        }
    }
    $report.scans += Invoke-FileScan $BaselineZip 'Original published ZIP'
    foreach ($member in $members) {
        $report.scans += Invoke-FileScan $member.FullName ("Original member: " + $member.Name)
    }
    $candidate = Invoke-FileScan $JarPath 'Newly built production JAR'
    $report.scans += $candidate
    $report.completed = $true
    $report.cleanJar = $candidate.result -eq 'Clean'
    if (-not $report.cleanJar) { throw 'The newly built JAR did not pass a completed clean Defender scan.' }
    "$($candidate.sha256)  $([IO.Path]::GetFileName($JarPath))" |
        Set-Content (Join-Path $OutputDirectory 'SHA256SUMS.txt') -Encoding ASCII
} catch {
    $report.cleanJar = $false
    $report.error = $_.Exception.Message
    throw
} finally {
    $report.finishedUtc = [DateTime]::UtcNow.ToString('o')
    $report | ConvertTo-Json -Depth 8 | Set-Content (Join-Path $OutputDirectory 'defender-report.json') -Encoding UTF8
    if ($env:GITHUB_OUTPUT) {
        $value = 'clean_jar=' + $report.cleanJar.ToString().ToLowerInvariant() + "`n"
        [IO.File]::AppendAllText($env:GITHUB_OUTPUT, $value, [Text.UTF8Encoding]::new($false))
    }
}
