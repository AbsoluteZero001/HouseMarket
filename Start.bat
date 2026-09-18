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

echo [1/3] Building and starting containers...
docker compose up -d --build
if errorlevel 1 (
    echo [ERROR] Failed to start containers. See messages above.
    pause
    exit /b 1
)

echo [2/3] Waiting for services...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ok=$false; for($i=0;$i -lt 90;$i++){ try { $r=Invoke-RestMethod -Uri 'http://localhost:5173/api/public/stats' -TimeoutSec 2; if($r.data.houses -gt 0){$ok=$true; break} } catch {}; Start-Sleep -Seconds 2 }; if(-not $ok){ Write-Host 'Backend did not become ready in time or seed data is empty.'; exit 1 }"
if errorlevel 1 (
    echo [ERROR] Services did not become ready in time.
    echo Run "docker compose ps" and "docker compose logs backend" to debug.
    pause
    exit /b 1
)

echo [3/3] Starting browser...
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
