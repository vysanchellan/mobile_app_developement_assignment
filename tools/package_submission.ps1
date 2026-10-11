<#
.SYNOPSIS
    Builds the Moodle submission ZIP: project source + demo video + report.

.DESCRIPTION
    Creates submission\{STUDENTNUMBER}_{SURNAME}_MobileAppDev700_Assignment.zip containing
    - the full project source (only files tracked by git, so build/, .gradle/, .idea/ and
      local.properties are never included),
    - the video, renamed {STUDENTNUMBER}_{SURNAME}_MobileAppDev700_Video.<ext>,
    - the report, renamed {STUDENTNUMBER}_{SURNAME}_MobileAppDev700_Assignment.<docx|pdf>.
    Prints the final size and fails if it is over 50 MB.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File tools\package_submission.ps1 `
        -StudentNumber 402012345 -Surname Chellan -Video C:\rec\final.mp4 -Report C:\docs\report.docx
#>
param(
    [string]$StudentNumber,
    [string]$Surname,
    [string]$Video,
    [string]$Report
)

$ErrorActionPreference = 'Stop'
$MaxBytes = 50MB

function Fail([string]$message) {
    Write-Host "ERROR: $message" -ForegroundColor Red
    exit 1
}

if ([string]::IsNullOrWhiteSpace($StudentNumber)) { Fail 'Missing -StudentNumber.' }
if ([string]::IsNullOrWhiteSpace($Surname)) { Fail 'Missing -Surname.' }
if ([string]::IsNullOrWhiteSpace($Video) -or -not (Test-Path -LiteralPath $Video -PathType Leaf)) {
    Fail 'Missing -Video or the file does not exist.'
}
if ([string]::IsNullOrWhiteSpace($Report) -or -not (Test-Path -LiteralPath $Report -PathType Leaf)) {
    Fail 'Missing -Report or the file does not exist.'
}
$reportExt = [IO.Path]::GetExtension($Report).ToLower()
if ($reportExt -ne '.docx' -and $reportExt -ne '.pdf') { Fail 'The report must be a .docx or .pdf file.' }

$root = Split-Path -Parent $PSScriptRoot
$base = "${StudentNumber}_${Surname}_MobileAppDev700"
$outDir = Join-Path $root 'submission'
$staging = Join-Path $outDir $base
$zipPath = Join-Path $outDir "${base}_Assignment.zip"

if (Test-Path $staging) { Remove-Item -Recurse -Force $staging }
if (Test-Path $zipPath) { Remove-Item -Force $zipPath }
New-Item -ItemType Directory -Force (Join-Path $staging 'SmartPantryManager') | Out-Null

# Project source: exactly the files git tracks, which excludes build output and local settings.
Push-Location $root
try {
    $files = git ls-files
    if ($LASTEXITCODE -ne 0) { Fail 'git ls-files failed; run this from inside the repository.' }
} finally {
    Pop-Location
}
foreach ($file in $files) {
    $source = Join-Path $root $file
    $target = Join-Path (Join-Path $staging 'SmartPantryManager') $file
    New-Item -ItemType Directory -Force (Split-Path -Parent $target) | Out-Null
    Copy-Item -LiteralPath $source -Destination $target
}

Copy-Item -LiteralPath $Video -Destination (Join-Path $staging ("${base}_Video" + [IO.Path]::GetExtension($Video)))
Copy-Item -LiteralPath $Report -Destination (Join-Path $staging ("${base}_Assignment" + $reportExt))

# Written entry by entry with forward slashes: Compress-Archive in Windows PowerShell 5.1 uses
# backslashes, which unzip into odd file names on macOS and Linux.
Add-Type -AssemblyName System.IO.Compression, System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::Open($zipPath, 'Create')
try {
    Get-ChildItem -LiteralPath $staging -Recurse -File | ForEach-Object {
        $entry = $_.FullName.Substring($staging.Length + 1).Replace('\', '/')
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, $entry, 'Optimal') | Out-Null
    }
} finally {
    $zip.Dispose()
}
Remove-Item -Recurse -Force $staging

$size = (Get-Item $zipPath).Length
$sizeMb = [Math]::Round($size / 1MB, 2)
Write-Host "Created $zipPath ($sizeMb MB)"
if ($size -gt $MaxBytes) {
    Fail "The ZIP is $sizeMb MB, over the 50 MB limit. Compress the video further (see docs/VIDEO_SCRIPT.md)."
}
Write-Host 'Size OK (under 50 MB).' -ForegroundColor Green
