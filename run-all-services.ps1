<#
.SYNOPSIS
    Starts Docker infra (optional), then launches services in order and waits until each one is reachable before starting the next.

.DESCRIPTION
    Run from the Clinic-Flow repo root (same folder as docker-compose.yml).
    Requires JDK 17 and Docker Desktop when using Docker services.

.PARAMETER SkipDocker
    Do not run docker compose (use when Rabbit/MySQL already running).

.PARAMETER SkipPatient
    Do not start MSPatientMedcin (skip if you only test catalogue / ordonnances without dispense).

.PARAMETER JavaHome
    Absolute path to a JDK 17+ install (folder that contains bin\java.exe). Use this when java -version still shows Java 8.

.PARAMETER WaitTimeoutSec
    Max seconds to wait for each readiness probe after a window is started (default 240).

.PARAMETER SkipWait
    Skip HTTP/TCP readiness polling; only opens windows (not recommended).

.EXAMPLE
    .\run-all-services.ps1

.EXAMPLE
    .\run-all-services.ps1 -SkipDocker -SkipPatient

.EXAMPLE
    .\run-all-services.ps1 -JavaHome "C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot"

.EXAMPLE
    .\run-all-services.ps1 -WaitTimeoutSec 360
#>

param(
    [switch] $SkipDocker,
    [switch] $SkipPatient,
    [string] $JavaHome = "",
    [int] $WaitTimeoutSec = 240,
    [switch] $SkipWait
)

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot

function Get-JavaBootstrapLines {
    param([string] $JdkRoot)
    $jh = $JdkRoot.TrimEnd('\')
    $javaExe = Join-Path $jh "bin\java.exe"
    if (-not (Test-Path $javaExe)) {
        throw "JAVA_HOME invalid (missing bin\java.exe): $jh"
    }
    # Escape single quotes for embedding in single-quoted segments of child script
    $jhEsc = $jh -replace "'", "''"
    return @(
        "`$env:JAVA_HOME = '$jhEsc'",
        "`$env:PATH = `"`$env:JAVA_HOME\bin;`$env:PATH`""
    )
}

function Start-MicroserviceWindow {
    param(
        [Parameter(Mandatory = $true)][string] $DisplayName,
        [Parameter(Mandatory = $true)][string] $RelativeModulePath,
        [string[]] $JavaBootstrapLines = @()
    )

    $moduleFullPath = Join-Path $RepoRoot $RelativeModulePath
    if (-not (Test-Path $moduleFullPath)) {
        Write-Warning "Skipped (path not found): $RelativeModulePath"
        return
    }

    $mvnw = Join-Path $moduleFullPath "mvnw.cmd"
    if (-not (Test-Path $mvnw)) {
        Write-Warning "Skipped (no mvnw.cmd): $RelativeModulePath"
        return
    }

    $prefix = ""
    if ($JavaBootstrapLines.Count -gt 0) {
        $prefix = ($JavaBootstrapLines -join "`n") + "`n"
    }

    $command = @"
$prefix
Set-Location -LiteralPath '$moduleFullPath'
`$Host.UI.RawUI.WindowTitle = '$DisplayName'
Write-Host '=== $DisplayName ===' -ForegroundColor Cyan
Write-Host "JAVA_HOME=`$env:JAVA_HOME"
java -version
.\mvnw.cmd spring-boot:run
"@
    Start-Process powershell.exe -ArgumentList @("-NoExit", "-Command", $command) | Out-Null
    Write-Host "Started window: $DisplayName"
}

function Wait-TcpOpen {
    param(
        [string] $ComputerName = "127.0.0.1",
        [Parameter(Mandatory = $true)][int] $Port,
        [string] $Description,
        [int] $TimeoutSec = 120,
        [int] $IntervalSec = 2
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    Write-Host "Waiting for TCP $ComputerName`:$Port ($Description)..." -ForegroundColor Yellow
    while ((Get-Date) -lt $deadline) {
        try {
            $t = Test-NetConnection -ComputerName $ComputerName -Port $Port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
            if ($t.TcpTestSucceeded) {
                Write-Host "  OK: $Description (port $Port)" -ForegroundColor Green
                return
            }
        }
        catch { }
        Start-Sleep -Seconds $IntervalSec
    }
    throw "Timeout after ${TimeoutSec}s waiting for TCP port $Port ($Description)"
}

function Test-HttpReachable {
    param([Parameter(Mandatory)][string]$Uri)

    try {
        if ($PSVersionTable.PSVersion.Major -ge 7) {
            $resp = Invoke-WebRequest -Uri $Uri -UseBasicParsing -TimeoutSec 6 -MaximumRedirection 2 -SkipHttpErrorCheck -ErrorAction Stop
            return ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500)
        }
        try {
            $resp = Invoke-WebRequest -Uri $Uri -UseBasicParsing -TimeoutSec 6 -MaximumRedirection 2 -ErrorAction Stop
            return ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500)
        }
        catch [System.Net.WebException] {
            $code = $null
            if ($null -ne $_.Exception.Response) {
                $code = [int]$_.Exception.Response.StatusCode
            }
            return ($null -ne $code -and $code -gt 0 -and $code -lt 500)
        }
    }
    catch {
        return $false
    }
}

function Wait-HttpReady {
    param(
        [Parameter(Mandatory)][string]$Description,
        [Parameter(Mandatory)][string[]]$Urls,
        [int]$TimeoutSec = 240,
        [int]$IntervalSec = 3
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    Write-Host "Waiting for HTTP: $Description..." -ForegroundColor Yellow
    while ((Get-Date) -lt $deadline) {
        foreach ($url in $Urls) {
            if (Test-HttpReachable -Uri $url) {
                Write-Host "  OK: $Description → $url" -ForegroundColor Green
                return
            }
        }
        Write-Host "  ... still waiting ($Description)" -ForegroundColor DarkGray
        Start-Sleep -Seconds $IntervalSec
    }
    throw "Timeout after ${TimeoutSec}s waiting for: $Description"
}

Write-Host "Clinic-Flow root: $RepoRoot" -ForegroundColor Green

$javaBootstrapLines = @()
if ($JavaHome) {
    $javaBootstrapLines = Get-JavaBootstrapLines -JdkRoot $JavaHome
    Write-Host "Using JDK from -JavaHome: $($JavaHome.TrimEnd('\'))" -ForegroundColor Cyan
}
else {
    try {
        $versionLine = ((java -version 2>&1) | Select-Object -First 1) | Out-String
        # Java 8 and older report as version "1.8..." — Spring Boot 4 needs 17+
        if ($versionLine -match 'version "1\.[0-8]\.') {
            Write-Host ""
            Write-Host "This project requires JDK 17+, but your default java reports Java 8 or older:" -ForegroundColor Red
            Write-Host "  $($versionLine.Trim())" -ForegroundColor Red
            Write-Host ""
            Write-Host "Install JDK 17 (e.g. Eclipse Temurium: https://adoptium.net/) then either:" -ForegroundColor Yellow
            Write-Host '  1) Set user JAVA_HOME to that JDK and put %JAVA_HOME%\bin before other Java entries on PATH, or' -ForegroundColor Yellow
            Write-Host '  2) Run this script with: -JavaHome "C:\Path\to\jdk-17"' -ForegroundColor Yellow
            Write-Host ""
            exit 1
        }
    }
    catch {
        Write-Warning "Could not run java -version. Ensure JDK 17+ is installed and on PATH."
    }
}

if (-not $SkipDocker) {
    Write-Host "Starting Docker Compose (RabbitMQ + MySQL)..." -ForegroundColor Yellow
    Push-Location $RepoRoot
    try {
        docker compose up -d
    }
    finally {
        Pop-Location
    }
    if (-not $SkipWait) {
        Wait-TcpOpen -Port 5672 -Description "RabbitMQ" -TimeoutSec $WaitTimeoutSec
        Wait-TcpOpen -Port 3306 -Description "MySQL" -TimeoutSec $WaitTimeoutSec
    }
    else {
        Write-Host "SkipWait: assuming Docker ports are already accepting connections." -ForegroundColor DarkYellow
        Start-Sleep -Seconds 5
    }
}

Write-Host "Opening service windows (one per microservice)..." -ForegroundColor Yellow

Start-MicroserviceWindow -DisplayName "Eureka" -RelativeModulePath "demoEurekaServer" -JavaBootstrapLines $javaBootstrapLines
if (-not $SkipWait) {
    Wait-HttpReady -Description "Eureka dashboard" -Urls @("http://127.0.0.1:8761/") -TimeoutSec $WaitTimeoutSec
}

Start-MicroserviceWindow -DisplayName "ConfigServer" -RelativeModulePath "demoConfigServer" -JavaBootstrapLines $javaBootstrapLines
if (-not $SkipWait) {
    Wait-HttpReady -Description "Config Server (MSPharmacie config)" -Urls @("http://127.0.0.1:8888/MSPharmacie/default") -TimeoutSec $WaitTimeoutSec
}

Start-MicroserviceWindow -DisplayName "MSPharmacie" -RelativeModulePath "MSPharmacie" -JavaBootstrapLines $javaBootstrapLines
if (-not $SkipWait) {
    Wait-HttpReady -Description "MSPharmacie REST" -Urls @(
        "http://127.0.0.1:8086/medicaments",
        "http://127.0.0.1:8086/actuator/health"
    ) -TimeoutSec $WaitTimeoutSec
}

Start-MicroserviceWindow -DisplayName "MSOrdonnance" -RelativeModulePath "MSOrdonnance" -JavaBootstrapLines $javaBootstrapLines
if (-not $SkipWait) {
    Wait-HttpReady -Description "MSOrdonnance REST" -Urls @(
        "http://127.0.0.1:8081/ordonnances",
        "http://127.0.0.1:8081/actuator/health"
    ) -TimeoutSec $WaitTimeoutSec
}

if (-not $SkipPatient) {
    Start-MicroserviceWindow -DisplayName "MSPatientMedcin" -RelativeModulePath "MSPatientMedcin" -JavaBootstrapLines $javaBootstrapLines
    if (-not $SkipWait) {
        Wait-HttpReady -Description "MSPatientMedcin REST" -Urls @(
            "http://127.0.0.1:8082/patients",
            "http://127.0.0.1:8082/actuator/health"
        ) -TimeoutSec $WaitTimeoutSec
    }
}

Start-MicroserviceWindow -DisplayName "ApiGateway" -RelativeModulePath "demoApiGateway" -JavaBootstrapLines $javaBootstrapLines
if (-not $SkipWait) {
    Wait-HttpReady -Description "API Gateway (port listening)" -Urls @("http://127.0.0.1:8085/") -TimeoutSec $WaitTimeoutSec
}

Write-Host ""
Write-Host "Done. Check Eureka: http://localhost:8761" -ForegroundColor Green
Write-Host "Gateway: http://localhost:8085" -ForegroundColor Green
Write-Host "Close each PowerShell window (or Ctrl+C) to stop a service." -ForegroundColor Gray
Write-Host "Docker: docker compose down  (from repo root)" -ForegroundColor Gray
