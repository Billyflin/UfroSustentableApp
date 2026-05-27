$ErrorActionPreference = "Stop"

param(
    [string]$SonarScannerUrl = "http://172.17.0.1:9000",
    [string]$SonarLogin = $(if ($env:SONAR_LOGIN) { $env:SONAR_LOGIN } else { "admin" }),
    [string]$SonarPassword = $(if ($env:SONAR_PASSWORD) { $env:SONAR_PASSWORD } else { "admin" })
)

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$containerName = "ecosense-sonarqube"

Push-Location $root
try {
    $existing = docker ps -a --filter "name=^/$containerName$" --format "{{.Names}}"
    if ($existing -eq $containerName) {
        docker start $containerName | Out-Null
    } else {
        docker run -d --name $containerName -p 9000:9000 sonarqube:lts-community | Out-Null
    }

    $ready = $false
    for ($i = 1; $i -le 80; $i++) {
        try {
            $status = Invoke-RestMethod -Uri "http://localhost:9000/api/system/status" -TimeoutSec 5
            if ($status.status -eq "UP") {
                $ready = $true
                break
            }
        } catch {
            Start-Sleep -Seconds 3
        }
    }
    if (-not $ready) {
        throw "SonarQube did not become ready"
    }

    docker run --rm `
        -v "${PWD}:/usr/src" `
        sonarsource/sonar-scanner-cli:latest `
        "-Dsonar.host.url=$SonarScannerUrl" `
        "-Dsonar.login=$SonarLogin" `
        "-Dsonar.password=$SonarPassword"
}
finally {
    Pop-Location
}
