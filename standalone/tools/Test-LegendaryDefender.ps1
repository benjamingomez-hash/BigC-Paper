# Diagnostic only: restores protection on the disposable runner; never executes samples or adds exclusions.
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$JarPath,
    [Parameter(Mandatory = $true)][string]$DistributionZip,
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [string]$ReferenceManifest
)

$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$OutputDirectory = (Resolve-Path $OutputDirectory).Path
$report = [ordered]@{
    commit = $env:GITHUB_SHA
    startedUtc = [DateTime]::UtcNow.ToString('o')
    completed = $false
    cleanJar = $false
    cleanDistribution = $false
    engine = $null
    scans = @()
    references = @()
    error = $null
}
$scanIndex = 0

function Invoke-DefenderCommand([string]$Arguments, [string]$LogName) {
    $prefix = Join-Path $OutputDirectory $LogName
    $process = Start-Process -FilePath $script:scanner -ArgumentList $Arguments `
        -RedirectStandardOutput "$prefix.stdout.txt" -RedirectStandardError "$prefix.stderr.txt" `
        -NoNewWindow -PassThru
    $null = $process.Handle
    if (-not $process.WaitForExit(180000)) {
        $process.Kill()
        throw "Defender timed out: $LogName."
    }
    $process.WaitForExit()
    return $process.ExitCode
}

function Invoke-FileScan([string]$Path, [string]$Label) {
    $script:scanIndex++
    $result = [ordered]@{ label = $Label; sha256 = $null; result = 'Unavailable'; exitCode = $null }
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        $result.result = 'MissingOrBlocked'
        return [pscustomobject]$result
    }
    $Path = (Resolve-Path -LiteralPath $Path).Path
    # Preserve the Internet origin boundary of a downloaded file rather than
    # treating it as a trusted local build. This does not change its content hash.
    Set-Content -LiteralPath $Path -Stream Zone.Identifier -Value "[ZoneTransfer]`r`nZoneId=3" -Encoding ASCII
    $result.sha256 = (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
    # DisableRemediation applies only to this diagnostic custom scan, so a detection
    # is reported as nonzero instead of being mistaken for a clean, remediated scan.
    # Real-time protection stays enabled; exclusions and sample contents are untouched.
    $arguments = '-Scan -ScanType 3 -File "{0}" -DisableRemediation' -f $Path
    $result.exitCode = Invoke-DefenderCommand $arguments ('scan-{0:d2}' -f $script:scanIndex)
    if ($result.exitCode -eq 0 -and (Test-Path -LiteralPath $Path)) {
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
    Get-Command Get-MpComputerStatus, Set-MpPreference, Get-MpPreference, Remove-MpPreference, Update-MpSignature -ErrorAction Stop | Out-Null
    $status = Get-MpComputerStatus
    if (-not $status.AMServiceEnabled -or -not $status.AntivirusEnabled) {
        throw 'Microsoft Defender is not active on this runner; no verified artifact can be produced.'
    }
    $report.realTimeProtectionInitiallyEnabled = $status.RealTimeProtectionEnabled
    $initialPreferences = Get-MpPreference
    $report.initialExclusionCounts = [ordered]@{
        paths = @($initialPreferences.ExclusionPath | Where-Object { $_ }).Count
        extensions = @($initialPreferences.ExclusionExtension | Where-Object { $_ }).Count
        processes = @($initialPreferences.ExclusionProcess | Where-Object { $_ }).Count
    }
    # Hosted images exclude entire drives and disable several scanning features.
    # Remove those omissions on this disposable runner before inspecting samples.
    if ($initialPreferences.ExclusionPath) { Remove-MpPreference -ExclusionPath $initialPreferences.ExclusionPath }
    if ($initialPreferences.ExclusionExtension) { Remove-MpPreference -ExclusionExtension $initialPreferences.ExclusionExtension }
    if ($initialPreferences.ExclusionProcess) { Remove-MpPreference -ExclusionProcess $initialPreferences.ExclusionProcess }
    Set-MpPreference -DisableArchiveScanning $false -DisableBehaviorMonitoring $false `
        -DisableIOAVProtection $false -DisableScriptScanning $false -ErrorAction Stop
    if (-not $status.RealTimeProtectionEnabled) {
        Set-MpPreference -DisableRealtimeMonitoring $false -ErrorAction Stop
        for ($attempt = 0; $attempt -lt 15; $attempt++) {
            $status = Get-MpComputerStatus
            if ($status.RealTimeProtectionEnabled) { break }
            Start-Sleep -Seconds 1
        }
    }
    if (-not $status.RealTimeProtectionEnabled) { throw 'Real-time protection could not be enabled on the runner.' }
    # Samples are this public repository's distribution and build, on a disposable
    # runner. Enable normal cloud protection instead of bypassing its ML checks.
    Set-MpPreference -MAPSReporting Advanced -SubmitSamplesConsent SendSafeSamples -DisableBlockAtFirstSeen $false -ErrorAction Stop
    Update-MpSignature -ErrorAction Stop
    $status = Get-MpComputerStatus
    if (-not $status.AMServiceEnabled -or -not $status.AntivirusEnabled -or -not $status.RealTimeProtectionEnabled -or $status.AntivirusSignatureAge -gt 2) {
        throw 'Defender is unavailable or its signatures are stale.'
    }
    $preferences = Get-MpPreference
    if ([int]$preferences.MAPSReporting -eq 0 -or $preferences.DisableBlockAtFirstSeen -or [int]$preferences.SubmitSamplesConsent -ne 1) {
        throw 'Cloud protection, safe sample consent or Block at First Sight could not be enabled.'
    }
    foreach ($name in @('DisableArchiveScanning', 'DisableBehaviorMonitoring', 'DisableIOAVProtection', 'DisableScriptScanning')) {
        if ($preferences.$name -ne $false) { throw "Required scanning feature is still disabled: $name." }
    }
    if (@($preferences.ExclusionPath | Where-Object { $_ }).Count -ne 0 `
        -or @($preferences.ExclusionExtension | Where-Object { $_ }).Count -ne 0 `
        -or @($preferences.ExclusionProcess | Where-Object { $_ }).Count -ne 0) {
        throw 'Inherited file scanning exclusions could not be cleared.'
    }
    $report.engine = [ordered]@{
        productVersion = $status.AMProductVersion
        engineVersion = $status.AMEngineVersion
        signatureVersion = $status.AntivirusSignatureVersion
        signatureUpdatedUtc = $status.AntivirusSignatureLastUpdated.ToUniversalTime().ToString('o')
        realTimeProtectionEnabled = $status.RealTimeProtectionEnabled
        mapsReporting = [string]$preferences.MAPSReporting
        disableBlockAtFirstSeen = $preferences.DisableBlockAtFirstSeen
        submitSamplesConsent = [string]$preferences.SubmitSamplesConsent
        archiveScanningEnabled = -not $preferences.DisableArchiveScanning
        behaviorMonitoringEnabled = -not $preferences.DisableBehaviorMonitoring
        ioavProtectionEnabled = -not $preferences.DisableIOAVProtection
        scriptScanningEnabled = -not $preferences.DisableScriptScanning
        pathExclusions = @($preferences.ExclusionPath | Where-Object { $_ })
        extensionExclusions = @($preferences.ExclusionExtension | Where-Object { $_ })
        processExclusions = @($preferences.ExclusionProcess | Where-Object { $_ })
    }
    $platform = Join-Path $env:ProgramData 'Microsoft/Windows Defender/Platform'
    $script:scanner = Get-ChildItem "$platform/*/MpCmdRun.exe" -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTimeUtc -Descending | Select-Object -First 1 -ExpandProperty FullName
    if (-not $script:scanner) { $script:scanner = Join-Path $env:ProgramFiles 'Windows Defender/MpCmdRun.exe' }
    if (-not (Test-Path -LiteralPath $script:scanner -PathType Leaf)) { throw 'MpCmdRun.exe is unavailable.' }
    $report.cloudConnectionExitCode = Invoke-DefenderCommand '-ValidateMapsConnection' 'cloud-connection'
    if ($report.cloudConnectionExitCode -ne 0) { throw 'Defender cloud connectivity validation failed.' }

    if ($ReferenceManifest) {
        $referenceIndex = 0
        foreach ($reference in (Get-Content -LiteralPath $ReferenceManifest -Raw | ConvertFrom-Json)) {
            $referenceIndex++
            $comparison = [ordered]@{ label = $reference.label; url = $reference.url; expectedSha256 = $reference.sha256; scans = @(); error = $null }
            try {
                $uri = [Uri]$reference.url
                if ($uri.Scheme -ne 'https' -or $uri.Host -ne 'raw.githubusercontent.com') { throw 'Unexpected reference origin.' }
                $referenceRoot = Join-Path ([IO.Path]::GetTempPath()) ([Guid]::NewGuid().ToString('N'))
                New-Item -ItemType Directory -Path $referenceRoot | Out-Null
                $referencePath = Join-Path $referenceRoot 'reference.zip'
                Invoke-WebRequest -Uri $uri -OutFile $referencePath -UseBasicParsing
                if ((Get-FileHash -LiteralPath $referencePath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $reference.sha256) {
                    throw 'Downloaded reference does not match the pinned hash.'
                }
                $archiveScan = Invoke-FileScan $referencePath $reference.label
                $comparison.scans += $archiveScan
                if ($archiveScan.result -ne 'Clean') { throw 'Reference archive blocked or scan failed; it is not cleared for use.' }
                Add-Type -AssemblyName System.IO.Compression.FileSystem
                $referenceMembers = Join-Path $referenceRoot 'members'
                [IO.Compression.ZipFile]::ExtractToDirectory($referencePath, $referenceMembers)
                foreach ($member in (Get-ChildItem $referenceMembers -Recurse -File | Sort-Object FullName)) {
                    $comparison.scans += Invoke-FileScan $member.FullName ($reference.label + ': ' + $member.Name)
                }
            } catch {
                # Reference diagnostics must not be mistaken for a local-user verdict.
                $comparison.error = $_.Exception.Message
            }
            $report.references += [pscustomobject]$comparison
        }
    }

    $report.scans += Invoke-FileScan $DistributionZip 'Rebuilt distribution ZIP'
    if ($report.scans[-1].result -ne 'Clean') { throw 'Distribution ZIP was blocked or its scan failed.' }
    $samples = Join-Path ([IO.Path]::GetTempPath()) ([Guid]::NewGuid().ToString('N'))
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [IO.Compression.ZipFile]::ExtractToDirectory((Resolve-Path $DistributionZip).Path, $samples)
    $members = @(Get-ChildItem $samples -File -Recurse | Sort-Object FullName)
    if ($members.Count -ne 3) { throw 'Expected exactly three distribution members.' }
    $packedJar = @($members | Where-Object { $_.Name -eq [IO.Path]::GetFileName($JarPath) })
    if ($packedJar.Count -ne 1 -or (Get-FileHash $packedJar[0].FullName).Hash -ne (Get-FileHash $JarPath).Hash) {
        throw 'The distributed JAR does not match the source build.'
    }
    foreach ($member in $members) {
        $report.scans += Invoke-FileScan $member.FullName ("Distribution member: " + $member.Name)
    }
    $report.cleanDistribution = @($report.scans | Where-Object { $_.result -ne 'Clean' }).Count -eq 0
    $candidate = Invoke-FileScan $JarPath 'Newly built production JAR'
    $report.scans += $candidate
    $report.completed = $true
    $finalPreferences = Get-MpPreference
    $report.cleanJar = $candidate.result -eq 'Clean' -and $report.cleanDistribution -and (Get-MpComputerStatus).RealTimeProtectionEnabled `
        -and [int]$finalPreferences.MAPSReporting -ne 0 -and -not $finalPreferences.DisableBlockAtFirstSeen `
        -and -not $finalPreferences.DisableArchiveScanning -and -not $finalPreferences.DisableBehaviorMonitoring `
        -and -not $finalPreferences.DisableIOAVProtection -and -not $finalPreferences.DisableScriptScanning
    if (-not $report.cleanJar) { throw 'The newly built JAR did not pass a completed clean Defender scan.' }
    @("$($candidate.sha256)  $([IO.Path]::GetFileName($JarPath))",
      "$($report.scans[0].sha256)  $([IO.Path]::GetFileName($DistributionZip))") |
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
