param(
    [string]$HostAddress = 'localhost',
    [switch]$BuildOnly
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$buildDir = Join-Path $projectRoot 'build'
$sources = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src') -Filter '*.java' | ForEach-Object FullName)

if ($sources.Count -eq 0) { throw 'No Java source files found.' }
New-Item -ItemType Directory -Path $buildDir -Force | Out-Null
& javac -d $buildDir $sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
Copy-Item -LiteralPath (Join-Path $projectRoot 'src\assets') -Destination $buildDir -Recurse -Force

if ($BuildOnly) { return }
Push-Location $projectRoot
try {
    & java -cp $buildDir Main $HostAddress
} finally {
    Pop-Location
}
