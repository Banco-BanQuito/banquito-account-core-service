param(
    [Parameter(Mandatory = $true)]
    [string]$Token,

    [string]$HostUrl = "http://localhost:9000"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path ".\mvnw.cmd")) {
    throw "Run this script from the banquito-account-core-service directory."
}

.\mvnw.cmd clean verify sonar:sonar `
    "-Daccount-core.grpc.port=0" `
    "-Dsonar.host.url=$HostUrl" `
    "-Dsonar.token=$Token"

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
