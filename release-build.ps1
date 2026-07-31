param(
    [ValidateSet("android", "desktop", "web", "js", "wasm", "all")]
    [string]$Target = "android",
    [switch]$Clean,
    [switch]$SkipTests,
    [switch]$UnsignedAndroid,
    [switch]$GenerateKeystore
)

$ErrorActionPreference = "Stop"

function Invoke-Gradle {
    param([string[]]$Tasks)

    $arguments = @()
    if ($Clean) {
        $arguments += "clean"
    }
    $arguments += $Tasks

    Write-Host "Running: .\gradlew.bat $($arguments -join ' ')"
    & .\gradlew.bat @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle failed with exit code $LASTEXITCODE."
    }
}

function Test-Command {
    param([string]$Name)
    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Convert-SecureStringToPlainText {
    param([securestring]$Value)

    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Value)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

function Ensure-AndroidSigning {
    if ($UnsignedAndroid) {
        Write-Warning "Building an unsigned Android release. Do not distribute this artifact."
        return
    }

    if (Test-Path "keystore.properties") {
        return
    }

    if (-not $GenerateKeystore) {
        throw "keystore.properties is missing. Run '.\release-build.ps1 -Target android -GenerateKeystore' or pass -UnsignedAndroid for local validation only."
    }

    if (-not (Test-Command "keytool")) {
        throw "keytool was not found on PATH. Install/configure a JDK, then rerun this command."
    }

    $storePassword = Read-Host "Keystore password" -AsSecureString
    $keyPassword = Read-Host "Key password" -AsSecureString
    $storePasswordText = Convert-SecureStringToPlainText $storePassword
    $keyPasswordText = Convert-SecureStringToPlainText $keyPassword
    $keyAlias = "echolist"
    $storeFile = "release.keystore"

    & keytool `
        -genkeypair `
        -v `
        -keystore $storeFile `
        -alias $keyAlias `
        -keyalg RSA `
        -keysize 2048 `
        -validity 10000 `
        -storepass $storePasswordText `
        -keypass $keyPasswordText `
        -dname "CN=EchoList, OU=Release, O=EchoList, L=Unknown, S=Unknown, C=DE"

    @"
storeFile=$storeFile
storePassword=$storePasswordText
keyAlias=$keyAlias
keyPassword=$keyPasswordText
"@ | Set-Content -Path "keystore.properties" -Encoding UTF8
}

$qualityTasks = @()
if (-not $SkipTests) {
    $qualityTasks = @(":composeApp:jvmTest")
}

$androidTasks = @(":composeApp:assembleRelease", ":composeApp:bundleRelease")
$desktopTasks = @(":composeApp:packageReleaseDistributionForCurrentOS")
$jsTasks = @(":composeApp:jsBrowserDistribution")
$wasmTasks = @(":composeApp:wasmJsBrowserDistribution")

switch ($Target) {
    "android" {
        Ensure-AndroidSigning
        Invoke-Gradle ($qualityTasks + $androidTasks)
    }
    "desktop" {
        Invoke-Gradle ($qualityTasks + $desktopTasks)
    }
    "js" {
        Invoke-Gradle ($qualityTasks + $jsTasks)
    }
    "wasm" {
        Invoke-Gradle ($qualityTasks + $wasmTasks)
    }
    "web" {
        Invoke-Gradle ($qualityTasks + $jsTasks + $wasmTasks)
    }
    "all" {
        Ensure-AndroidSigning
        Invoke-Gradle ($qualityTasks + $androidTasks + $desktopTasks + $jsTasks + $wasmTasks)
    }
}
