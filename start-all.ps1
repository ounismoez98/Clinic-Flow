# Clinic-Flow - Start all services in order
$Root = $PSScriptRoot

function Wait-ForHttp {
    param([string]$Url, [string]$Name, [int]$TimeoutSec = 120)
    Write-Host "  Waiting for $Name at $Url ..." -ForegroundColor Yellow
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $r = Invoke-RestMethod $Url -TimeoutSec 3 -ErrorAction Stop
            Write-Host "  $Name is UP" -ForegroundColor Green
            return $true
        } catch { Start-Sleep 3 }
    }
    Write-Host "  ERROR: $Name did not start in time" -ForegroundColor Red
    return $false
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Clinic-Flow - Starting all services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. RabbitMQ
Write-Host "[1/6] Starting RabbitMQ (Docker)..." -ForegroundColor Cyan
Set-Location $Root
docker compose up -d
if (-not (Wait-ForHttp "http://localhost:15672" "RabbitMQ")) { exit 1 }

# 2. Eureka
Write-Host "[2/6] Starting Eureka Server..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root\demoEurekaServer'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
if (-not (Wait-ForHttp "http://localhost:8761/actuator/health" "Eureka")) { exit 1 }

# 3. Config Server
Write-Host "[3/6] Starting Config Server..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root\demoConfigServer'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
if (-not (Wait-ForHttp "http://localhost:8888/actuator/health" "Config Server")) { exit 1 }

# 4. MSUser
Write-Host "[4/6] Starting MSUser..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root\MSUser'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
if (-not (Wait-ForHttp "http://localhost:8083/users/hello" "MSUser")) { exit 1 }

# 5. MSPatientMedcin
Write-Host "[5/6] Starting MSPatientMedcin..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root\MSPatientMedcin'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
if (-not (Wait-ForHttp "http://localhost:8082/medecins/hello" "MSPatientMedcin")) { exit 1 }

# 6. API Gateway
Write-Host "[6/6] Starting API Gateway..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root\demoApiGateway'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
Start-Sleep 15
if (-not (Wait-ForHttp "http://localhost:8085/medecins/hello" "API Gateway")) { exit 1 }

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  All services are UP!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Eureka dashboard   : http://localhost:8761" -ForegroundColor White
Write-Host "  RabbitMQ dashboard : http://localhost:15672  (guest/guest)" -ForegroundColor White
Write-Host "  API Gateway        : http://localhost:8085" -ForegroundColor White
Write-Host ""
Write-Host "  Test endpoints:" -ForegroundColor White
Write-Host "    GET http://localhost:8085/medecins/hello" -ForegroundColor Gray
Write-Host "    GET http://localhost:8085/medecins" -ForegroundColor Gray
Write-Host "    GET http://localhost:8085/patients" -ForegroundColor Gray
Write-Host "    GET http://localhost:8085/users" -ForegroundColor Gray
Write-Host ""
