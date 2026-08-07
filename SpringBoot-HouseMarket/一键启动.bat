@echo off
setlocal
cd /d "%~dp0"

echo ============================================
echo  HouseMarket Docker One-Click Launcher
echo ============================================
echo.

where docker >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Docker is not installed or not in PATH.
    pause
    exit /b 1
)

docker info >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Docker daemon is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

echo [1/4] Checking database volume...
docker volume inspect housemarket-mysql-data >nul 2>nul
if not errorlevel 1 (
    echo An existing database volume was found.
    choice /C YN /T 10 /D N /M "Reset database and re-run seed data"
    if errorlevel 2 goto START
    echo Resetting database volume...
    docker compose down -v
)

:START
echo [2/4] Building and starting containers...
docker compose up -d --build
if errorlevel 1 (
    echo [ERROR] Failed to start containers. See messages above.
    pause
    exit /b 1
)

echo [3/4] Waiting for services...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ok=$false; for($i=0;$i -lt 90;$i++){ try { $r=Invoke-WebRequest -UseBasicParsing 'http://localhost:5173/api/public/stats' -TimeoutSec 2; if($r.StatusCode -eq 200){$ok=$true; break} } catch {}; Start-Sleep -Seconds 2 }; if(-not $ok){ Write-Host 'Backend did not become ready in time.'; exit 1 }"
if errorlevel 1 (
    echo [ERROR] Services did not become ready in time.
    echo Run "docker compose ps" and "docker compose logs backend" to debug.
    pause
    exit /b 1
)

echo [4/4] Starting browser...
start "" http://localhost:5173

echo.
echo ============================================
echo  HouseMarket is ready:
echo    Frontend : http://localhost:5173
echo    Backend  : http://localhost:8082
echo    Swagger  : http://localhost:8082/swagger-ui.html
echo    Admin    : admin / admin123
echo    Landlord : landlord1 / 123456
echo    Tenant   : tenant1 / 123456
echo ============================================
echo.
docker compose ps
echo.
pause
