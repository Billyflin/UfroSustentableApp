$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Push-Location $root
try {
    .\gradlew.bat :app:testDebugUnitTest --tests "com.ecosense.integration.EcoSenseIntegrationSpec" --rerun-tasks
}
finally {
    Pop-Location
}
