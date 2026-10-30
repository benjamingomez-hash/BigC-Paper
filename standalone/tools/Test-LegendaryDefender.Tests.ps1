# Offline regression checks: imports functions only, never changes Defender settings.
$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot 'Test-LegendaryDefender.ps1'
$tokens = $null
$parseErrors = $null
$ast = [Management.Automation.Language.Parser]::ParseFile($source, [ref]$tokens, [ref]$parseErrors)
if ($parseErrors.Count -ne 0) { throw ($parseErrors | Out-String) }

function Import-Function([string]$Name) {
    $definition = $ast.Find({
        param($node)
        $node -is [Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -eq $Name
    }, $true)
    if (-not $definition) { throw "Missing production function: $Name" }
    # Return the source so the caller defines it in the test script's scope.
    return $definition.Extent.Text
}

Invoke-Expression (Import-Function 'Update-DefenderSignatures')
$script:delays = 0
function Start-Sleep { param([int]$Seconds) $script:delays++ }
function Invoke-DefenderCommand([string]$Arguments, [string]$LogName) {
    if ($Arguments -ne '-SignatureUpdate -MMPC') { throw "Unexpected update command: $Arguments" }
    if (-not $LogName.StartsWith('signature-update-')) { throw 'Missing update log.' }
    $result = $script:results.Dequeue()
    if ($result -is [Exception]) { throw $result }
    return [int]$result
}

function Set-Results([object[]]$Values) {
    $script:results = [Collections.Generic.Queue[object]]::new()
    foreach ($value in $Values) { $script:results.Enqueue($value) }
    $script:report = [ordered]@{ signatureUpdates = @(); cleanJar = $false; cleanDistribution = $false }
    $script:delays = 0
}

Set-Results @(0)
Update-DefenderSignatures
if ($report.signatureUpdates.Count -ne 1 -or $report.signatureUpdates[0].exitCode -ne 0 -or $delays -ne 0) {
    throw 'A successful update should finish after one attempt.'
}

Set-Results @([InvalidOperationException]::new('RPC unavailable'), 2, 0)
Update-DefenderSignatures
if ($report.signatureUpdates.Count -ne 3 -or $delays -ne 2 -or -not $report.signatureUpdates[0].error) {
    throw 'Transient update failures must be recorded before retrying.'
}

Set-Results @(2, 7, 2)
$failed = $false
try { Update-DefenderSignatures } catch { $failed = $true }
if (-not $failed -or $report.signatureUpdates.Count -ne 3 -or $report.cleanJar -or $report.cleanDistribution) {
    throw 'Exhausted update failures must block the scan and artifact.'
}

Set-Results @([InvalidOperationException]::new('RPC unavailable'),
    [InvalidOperationException]::new('RPC unavailable'), [InvalidOperationException]::new('RPC unavailable'))
$failed = $false
try { Update-DefenderSignatures } catch { $failed = $true }
if (-not $failed -or $report.signatureUpdates.Count -ne 3 -or $report.cleanJar) {
    throw 'Repeated RPC errors must fail closed.'
}
Write-Output 'Four signature-update regression cases passed.'

# Exercise actual process exit-code propagation with harmless PowerShell children.
Remove-Item Function:\Invoke-DefenderCommand
Invoke-Expression (Import-Function 'Invoke-DefenderCommand')
$OutputDirectory = Join-Path ([IO.Path]::GetTempPath()) ('legendary-defender-test-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $OutputDirectory | Out-Null
$script:scanner = [Diagnostics.Process]::GetCurrentProcess().MainModule.FileName
try {
    foreach ($exitCode in @(0, 2, 7)) {
        $actual = Invoke-DefenderCommand "-NoProfile -NonInteractive -Command exit $exitCode" "native-$exitCode"
        if ($actual -ne $exitCode) { throw "Native exit $exitCode was incorrectly read as $actual." }
    }
} finally {
    Remove-Item -LiteralPath $OutputDirectory -Recurse -Force
}
Write-Output 'Three native process exit-code checks passed. No Defender settings were changed.'
