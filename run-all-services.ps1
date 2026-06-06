<#
.SYNOPSIS
    Starts Docker infra (optional), then launches microservices.
    By default only Eureka, Config Server and API Gateway are waited on; other services
    start in quick succession (use -WaitAll for the old strict one-by-one readiness wait).

.DESCRIPTION
    Run from the Clinic-Flow repo root (same folder as docker-compose.yml).
    Requires JDK 17 and Docker Desktop when using Docker services.

    Only RabbitMQ + MySQL are started in Docker; microservices run locally via mvnw.
    Host ports must match docker-compose.yml (default RabbitMQ 5673, MySQL 3307).

.PARAMETER SkipDocker
    Do not run docker compose (use when Rabbit/MySQL already running).

.PARAMETER SkipPatient
    Do not start MSPatientMedcin (skip if you only test catalogue / ordonnances without dispense).

.PARAMETER SkipFacture
    Do not start MsFacture.

.PARAMETER SkipRendezVous
    Do not start MSRendezVous (runs on host port 8088 to avoid clashing with MSOrdonnance on 8081).

.PARAMETER JavaHome
    Absolute path to a JDK 17+ install (folder that contains bin\java.exe). Use this when java -version still shows Java 8.

.PARAMETER RabbitMqPort
    Host port mapped to RabbitMQ AMQP (docker-compose default: 5673).

.PARAMETER MySqlPort
    Host port mapped to MySQL (docker-compose default: 3307).

.PARAMETER WaitTimeoutSec
    Max seconds to wait for a service readiness probe (default 120).

.PARAMETER WaitAll
    Wait until each service is fully ready before starting the next one (slower, old behavior).

.PARAMETER StaggerSec
    Seconds between launching non-critical services in fast mode (default 2).

.PARAMETER SkipWait
    Skip HTTP/TCP readiness polling; only opens windows (not recommended).

.EXAMPLE
    .\run-all-services.ps1

.EXAMPLE
    .\run-all-services.ps1 -SkipDocker -SkipPatient

.EXAMPLE
    .\run-all-services.ps1 -JavaHome "C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot"

.EXAMPLE
    .\run-all-services.ps1 -MySqlPort 3306 -RabbitMqPort 5673
#>

param(
    [switch] $SkipDocker,
    [switch] $SkipPatient,
    [switch] $SkipFacture,
    [switch] $SkipRendezVous,
    [string] $JavaHome = "",
    [int] $RabbitMqPort = 5673,
    [int] $MySqlPort = 3307,
    [int] $WaitTimeoutSec = 120,
    [switch] $WaitAll,
    [int] $StaggerSec = 2,
    [switch] $SkipWait
)

$CriticalWaitServices = @('Eureka', 'ConfigServer', 'ApiGateway')

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot

function Get-JavaBootstrapLines {
    param([string] $JdkRoot)
    $jh = $JdkRoot.TrimEnd('\')
    $javaExe = Join-Path $jh "bin\java.exe"
    if (-not (Test-Path $javaExe)) {
        throw "JAVA_HOME invalid (missing bin\java.exe): $jh"
    }
    $jhEsc = $jh -replace "'", "''"
    return @(
        "`$env:JAVA_HOME = '$jhEsc'",
        "`$env:PATH = `"`$env:JAVA_HOME\bin;`$env:PATH`""
    )
}

function Get-RabbitMqEnvLines {
    param([int] $Port)
    return @(
        "`$env:SPRING_RABBITMQ_PORT = '$Port'",
        "`$env:RABBITMQ_PORT = '$Port'"
    )
}

function Get-MySqlPharmacieEnvLines {
    param([int] $Port)
    $url = "jdbc:mysql://localhost:$Port/clinic_pharmacie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    $urlEsc = $url -replace "'", "''"
    return @(
        "`$env:SPRING_DATASOURCE_URL = '$urlEsc'",
        "`$env:SPRING_DATASOURCE_USERNAME = 'root'",
        "`$env:SPRING_DATASOURCE_PASSWORD = 'root'"
    )
}

function Get-MySqlFactureEnvLines {
    param([int] $Port)
    $url = "jdbc:mysql://localhost:$Port/clinique?createDatabaseIfNotExist=true&serverTimezone=UTC"
    $urlEsc = $url -replace "'", "''"
    return @(
        "`$env:SPRING_DATASOURCE_URL = '$urlEsc'",
        "`$env:SPRING_DATASOURCE_USERNAME = 'root'",
        "`$env:SPRING_DATASOURCE_PASSWORD = 'root'"
    )
}

function Get-RendezVousEnvLines {
    # In-memory H2 for the local launcher — avoids stale file DB credential mismatches.
    # Credentials must match MSRendezVous application.properties (username Moez, empty password).
    return @(
        "`$env:SERVER_PORT = '8088'",
        "`$env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:rendezvous_local;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'",
        "`$env:SPRING_DATASOURCE_USERNAME = 'Moez'",
        "`$env:SPRING_DATASOURCE_PASSWORD = ''"
    )
}

function Get-OrdonnanceH2EnvLines {
    # Prevent MySQL SPRING_DATASOURCE_* vars (from shell profile) overriding H2 credentials.
    return @(
        "`$env:SPRING_DATASOURCE_USERNAME = 'Maroua'",
        "`$env:SPRING_DATASOURCE_PASSWORD = ''"
    )
}

function Get-GeminiApiKeyEnvLines {
    param([string] $RepoRootPath)

    $key = $env:GEMINI_API_KEY
    if ([string]::IsNullOrWhiteSpace($key)) {
        $envFile = Join-Path $RepoRootPath ".env"
        if (Test-Path $envFile) {
            foreach ($line in Get-Content $envFile -ErrorAction SilentlyContinue) {
                if ($line -match '^\s*GEMINI_API_KEY\s*=\s*(.+)\s*$') {
                    $key = $Matches[1].Trim().Trim('"').Trim("'")
                    break
                }
            }
        }
    }

    if ([string]::IsNullOrWhiteSpace($key)) {
        return @()
    }

    $keyEsc = $key -replace "'", "''"
    return @("`$env:GEMINI_API_KEY = '$keyEsc'")
}

function Start-MicroserviceWindow {
    param(
        [Parameter(Mandatory = $true)][string] $DisplayName,
        [Parameter(Mandatory = $true)][string] $RelativeModulePath,
        [string[]] $JavaBootstrapLines = @(),
        [string[]] $ExtraEnvLines = @()
    )

    $moduleFullPath = Join-Path $RepoRoot $RelativeModulePath
    if (-not (Test-Path $moduleFullPath)) {
        Write-Warning "Skipped (path not found): $RelativeModulePath"
        return $false
    }

    $mvnw = Join-Path $moduleFullPath "mvnw.cmd"
    if (-not (Test-Path $mvnw)) {
        Write-Warning "Skipped (no mvnw.cmd): $RelativeModulePath"
        return $false
    }

    $prefixLines = @()
    if ($JavaBootstrapLines.Count -gt 0) {
        $prefixLines += $JavaBootstrapLines
    }
    if ($ExtraEnvLines.Count -gt 0) {
        $prefixLines += $ExtraEnvLines
    }

    $prefix = ""
    if ($prefixLines.Count -gt 0) {
        $prefix = ($prefixLines -join "`n") + "`n"
    }

    $command = @"
$prefix
Set-Location -LiteralPath '$moduleFullPath'
`$Host.UI.RawUI.WindowTitle = '$DisplayName'
Write-Host '=== $DisplayName ===' -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run
"@
    Start-Process powershell.exe -ArgumentList @("-NoExit", "-Command", $command) | Out-Null
    Write-Host "  Started window: $DisplayName" -ForegroundColor White
    return $true
}

function Test-TcpOpenFast {
    param(
        [string] $ComputerName = "127.0.0.1",
        [Parameter(Mandatory)][int] $Port,
        [int] $TimeoutMs = 600
    )
    $client = $null
    try {
        $client = [System.Net.Sockets.TcpClient]::new()
        $task = $client.ConnectAsync($ComputerName, $Port)
        if (-not $task.Wait($TimeoutMs)) {
            return $false
        }
        return $client.Connected
    }
    catch {
        return $false
    }
    finally {
        if ($null -ne $client) {
            $client.Dispose()
        }
    }
}

function Test-SpringActuatorUp {
    param(
        [Parameter(Mandatory)][string]$Uri,
        [int]$TimeoutSec = 2
    )

    try {
        $resp = Invoke-RestMethod -Uri $Uri -TimeoutSec $TimeoutSec -ErrorAction Stop
        if ($resp.status -eq "UP") {
            return $true
        }
    }
    catch { }

    return $false
}

function Wait-ServiceReady {
    param(
        [Parameter(Mandatory)][string]$Description,
        [int]$TcpPort = 0,
        [string[]]$ActuatorUrls = @(),
        [string[]]$FallbackUrls = @(),
        [int]$TimeoutSec = 120,
        [int]$IntervalSec = 1,
        [int]$HttpTimeoutSec = 2
    )

    if ($SkipWait) {
        Write-Host "  SkipWait: not waiting for $Description." -ForegroundColor DarkYellow
        Start-Sleep -Seconds 1
        return
    }

    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    $readyUrls = @($FallbackUrls + $ActuatorUrls | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Unique)

    Write-Host "  Waiting for $Description..." -ForegroundColor Yellow

    while ((Get-Date) -lt $deadline) {
        if ($TcpPort -gt 0 -and -not (Test-TcpOpenFast -Port $TcpPort)) {
            Start-Sleep -Seconds $IntervalSec
            continue
        }

        foreach ($url in $readyUrls) {
            if ($url -match '/actuator/health') {
                if (Test-SpringActuatorUp -Uri $url -TimeoutSec $HttpTimeoutSec) {
                    Write-Host "  OK: $Description is UP → $url" -ForegroundColor Green
                    return
                }
            }
            elseif (Test-HttpReachable -Uri $url -TimeoutSec $HttpTimeoutSec) {
                Write-Host "  OK: $Description is reachable → $url" -ForegroundColor Green
                return
            }
        }

        Start-Sleep -Seconds $IntervalSec
    }

    throw "Timeout after ${TimeoutSec}s waiting for $Description to become ready."
}

function Start-ServiceAndWait {
    param(
        [Parameter(Mandatory = $true)][int] $Step,
        [Parameter(Mandatory = $true)][int] $TotalSteps,
        [Parameter(Mandatory = $true)][string] $DisplayName,
        [Parameter(Mandatory = $true)][string] $RelativeModulePath,
        [int] $TcpPort = 0,
        [string[]] $ActuatorUrls = @(),
        [string[]] $FallbackUrls = @(),
        [string[]] $JavaBootstrapLines = @(),
        [string[]] $ExtraEnvLines = @()
    )

    $started = Start-MicroserviceWindow `
        -DisplayName $DisplayName `
        -RelativeModulePath $RelativeModulePath `
        -JavaBootstrapLines $JavaBootstrapLines `
        -ExtraEnvLines $ExtraEnvLines

    if (-not $started) {
        return
    }

    Wait-ServiceReady `
        -Description $DisplayName `
        -TcpPort $TcpPort `
        -ActuatorUrls $ActuatorUrls `
        -FallbackUrls $FallbackUrls `
        -TimeoutSec $WaitTimeoutSec
}

function Wait-TcpOpen {
    param(
        [string] $ComputerName = "127.0.0.1",
        [Parameter(Mandatory = $true)][int] $Port,
        [string] $Description,
        [int] $TimeoutSec = 120,
        [int] $IntervalSec = 1
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    Write-Host "Waiting for TCP $ComputerName`:$Port ($Description)..." -ForegroundColor Yellow
    while ((Get-Date) -lt $deadline) {
        if (Test-TcpOpenFast -ComputerName $ComputerName -Port $Port) {
            Write-Host "  OK: $Description (port $Port)" -ForegroundColor Green
            return
        }
        Start-Sleep -Seconds $IntervalSec
    }
    throw "Timeout after ${TimeoutSec}s waiting for TCP port $Port ($Description)"
}

function Test-HttpReachable {
    param(
        [Parameter(Mandatory)][string]$Uri,
        [int]$TimeoutSec = 2
    )

    try {
        if ($PSVersionTable.PSVersion.Major -ge 7) {
            $resp = Invoke-WebRequest -Uri $Uri -UseBasicParsing -TimeoutSec $TimeoutSec -MaximumRedirection 2 -SkipHttpErrorCheck -ErrorAction Stop
            return ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500)
        }
        try {
            $resp = Invoke-WebRequest -Uri $Uri -UseBasicParsing -TimeoutSec $TimeoutSec -MaximumRedirection 2 -ErrorAction Stop
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

$rabbitEnvLines = Get-RabbitMqEnvLines -Port $RabbitMqPort
$mysqlPharmacieEnvLines = Get-MySqlPharmacieEnvLines -Port $MySqlPort
$mysqlFactureEnvLines = Get-MySqlFactureEnvLines -Port $MySqlPort
$geminiEnvLines = Get-GeminiApiKeyEnvLines -RepoRootPath $RepoRoot

$servicePlan = @(
    @{
        DisplayName = "Eureka"
        RelativeModulePath = "demoEurekaServer"
        TcpPort = 8761
        ActuatorUrls = @()
        FallbackUrls = @("http://127.0.0.1:8761/")
        ExtraEnvLines = @()
    },
    @{
        DisplayName = "ConfigServer"
        RelativeModulePath = "demoConfigServer"
        TcpPort = 8888
        ActuatorUrls = @("http://127.0.0.1:8888/actuator/health")
        FallbackUrls = @("http://127.0.0.1:8888/MSPharmacie/default")
        ExtraEnvLines = @()
    },
    @{
        DisplayName = "MSPharmacie"
        RelativeModulePath = "MSPharmacie"
        TcpPort = 8086
        ActuatorUrls = @("http://127.0.0.1:8086/actuator/health")
        FallbackUrls = @("http://127.0.0.1:8086/medicaments")
        ExtraEnvLines = ($rabbitEnvLines + $mysqlPharmacieEnvLines + $geminiEnvLines)
    },
    @{
        DisplayName = "MSOrdonnance"
        RelativeModulePath = "MSOrdonnance"
        TcpPort = 8081
        ActuatorUrls = @("http://127.0.0.1:8081/actuator/health")
        FallbackUrls = @("http://127.0.0.1:8081/ordonnances")
        ExtraEnvLines = ($rabbitEnvLines + (Get-OrdonnanceH2EnvLines))
    },
    @{
        DisplayName = "MSUser"
        RelativeModulePath = "MSUser"
        TcpPort = 8083
        ActuatorUrls = @("http://127.0.0.1:8083/actuator/health")
        FallbackUrls = @("http://127.0.0.1:8083/users/hello")
        ExtraEnvLines = $rabbitEnvLines
    },
    @{
        DisplayName = "MSPatientMedcin"
        RelativeModulePath = "MSPatientMedcin"
        TcpPort = 8082
        ActuatorUrls = @("http://127.0.0.1:8082/actuator/health")
        FallbackUrls = @("http://127.0.0.1:8082/medecins/hello", "http://127.0.0.1:8082/patients")
        ExtraEnvLines = $rabbitEnvLines
        Skip = $SkipPatient
    },
    @{
        DisplayName = "MsFacture"
        RelativeModulePath = "MsFacture"
        TcpPort = 8084
        ActuatorUrls = @("http://127.0.0.1:8084/actuator/health")
        FallbackUrls = @("http://127.0.0.1:8084/factures")
        ExtraEnvLines = $mysqlFactureEnvLines
        Skip = $SkipFacture
    },
    @{
        DisplayName = "MSRendezVous"
        RelativeModulePath = "MSRendezVous"
        TcpPort = 8088
        ActuatorUrls = @("http://127.0.0.1:8088/actuator/health")
        FallbackUrls = @("http://127.0.0.1:8088/rendezvous/hello")
        ExtraEnvLines = (Get-RendezVousEnvLines)
        Skip = $SkipRendezVous
    },
    @{
        DisplayName = "ApiGateway"
        RelativeModulePath = "demoApiGateway"
        TcpPort = 8085
        ActuatorUrls = @("http://127.0.0.1:8085/actuator/health")
        FallbackUrls = @("http://127.0.0.1:8085/", "http://127.0.0.1:8085/medecins/hello")
        ExtraEnvLines = @()
    }
)

$activeServices = @($servicePlan | Where-Object { -not $_.Skip })
$totalSteps = $activeServices.Count

if (-not $SkipDocker) {
    Write-Host ""
    Write-Host "[Docker] Starting infra (rabbitmq + mysql)..." -ForegroundColor Cyan
    Push-Location $RepoRoot
    try {
        docker compose up -d rabbitmq mysql
    }
    finally {
        Pop-Location
    }
    if (-not $SkipWait) {
        Wait-TcpOpen -Port $RabbitMqPort -Description "RabbitMQ AMQP" -TimeoutSec 60 -IntervalSec 1
        Wait-TcpOpen -Port $MySqlPort -Description "MySQL" -TimeoutSec 90 -IntervalSec 1
    }
    else {
        Write-Host "SkipWait: assuming Docker ports are already accepting connections." -ForegroundColor DarkYellow
        Start-Sleep -Seconds 5
    }
}

if ($WaitAll) {
    Write-Host ""
    Write-Host "Starting microservices sequentially (waiting for each to be ready)..." -ForegroundColor Yellow
}
else {
    Write-Host ""
    Write-Host "Fast start: wait only for Eureka, Config Server, then API Gateway." -ForegroundColor Yellow
    Write-Host "Other services launch with a ${StaggerSec}s stagger (use -WaitAll for strict mode)." -ForegroundColor DarkGray
}

$step = 0
foreach ($svc in $activeServices) {
    $step++
    $mustWait = $WaitAll -or ($CriticalWaitServices -contains $svc.DisplayName)

    Write-Host ""
    Write-Host "[$step/$totalSteps] $($svc.DisplayName)" -ForegroundColor Cyan

    if ($mustWait) {
        Start-ServiceAndWait `
            -Step $step `
            -TotalSteps $totalSteps `
            -DisplayName $svc.DisplayName `
            -RelativeModulePath $svc.RelativeModulePath `
            -TcpPort $svc.TcpPort `
            -ActuatorUrls $svc.ActuatorUrls `
            -FallbackUrls $svc.FallbackUrls `
            -JavaBootstrapLines $javaBootstrapLines `
            -ExtraEnvLines $svc.ExtraEnvLines
    }
    else {
        $started = Start-MicroserviceWindow `
            -DisplayName $svc.DisplayName `
            -RelativeModulePath $svc.RelativeModulePath `
            -JavaBootstrapLines $javaBootstrapLines `
            -ExtraEnvLines $svc.ExtraEnvLines
        if ($started -and -not $SkipWait -and $StaggerSec -gt 0) {
            Start-Sleep -Seconds $StaggerSec
        }
    }
}

Write-Host ""
Write-Host "Done. Services are running locally." -ForegroundColor Green
Write-Host "  Eureka dashboard   : http://localhost:8761" -ForegroundColor Green
Write-Host "  RabbitMQ dashboard : http://localhost:15673  (guest/guest)" -ForegroundColor Green
Write-Host "  Gateway            : http://localhost:8085" -ForegroundColor Green
Write-Host ""
Write-Host "  Test via Gateway:" -ForegroundColor White
Write-Host "    GET http://localhost:8085/medicaments" -ForegroundColor Gray
Write-Host "    GET http://localhost:8085/ordonnances" -ForegroundColor Gray
Write-Host "    GET http://localhost:8085/patients" -ForegroundColor Gray
Write-Host "    GET http://localhost:8085/users" -ForegroundColor Gray
Write-Host "    GET http://localhost:8085/factures" -ForegroundColor Gray
Write-Host "    GET http://localhost:8085/rendezvous/hello" -ForegroundColor Gray
Write-Host ""
Write-Host "Close each PowerShell window (or Ctrl+C) to stop a service." -ForegroundColor Gray
Write-Host "Docker infra: docker compose stop rabbitmq mysql  (from repo root)" -ForegroundColor Gray
