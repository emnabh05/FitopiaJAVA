@echo off
echo ========================================
echo Installing Asset Package (Offline Method)
echo ========================================
echo.

echo This will download the asset package directly from GitHub
echo and install it manually, bypassing Composer SSL issues.
echo.

set "ASSET_VERSION=v7.0.3"
set "ASSET_URL=https://github.com/symfony/asset/archive/refs/tags/%ASSET_VERSION%.zip"
set "TEMP_ZIP=%TEMP%\symfony-asset.zip"
set "TEMP_DIR=%TEMP%\symfony-asset-temp"
set "TARGET_DIR=vendor\symfony\asset"

echo [1/6] Downloading symfony/asset from GitHub...
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; [Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}; try { Invoke-WebRequest -Uri '%ASSET_URL%' -OutFile '%TEMP_ZIP%' -UseBasicParsing; Write-Host 'Downloaded successfully' } catch { Write-Host 'Download failed:' $_.Exception.Message; exit 1 }"

if not exist "%TEMP_ZIP%" (
    echo.
    echo ERROR: Download failed.
    echo.
    echo Trying alternative method...
    echo Please manually download from:
    echo https://github.com/symfony/asset/releases/download/%ASSET_VERSION%/asset-%ASSET_VERSION%.zip
    echo.
    pause
    exit /b 1
)

echo [2/6] Extracting package...
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%TEMP_ZIP%' -DestinationPath '%TEMP_DIR%' -Force"

echo [3/6] Installing to vendor folder...
if exist "%TARGET_DIR%" rmdir /s /q "%TARGET_DIR%"
if not exist "vendor\symfony" mkdir "vendor\symfony"

REM Find the extracted folder (it will be named asset-7.0.3 or similar)
for /d %%i in ("%TEMP_DIR%\asset-*") do (
    xcopy "%%i" "%TARGET_DIR%\" /E /I /Y >nul
)

echo [4/6] Updating composer.json...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$json = Get-Content 'composer.json' -Raw | ConvertFrom-Json; if (-not $json.require.'symfony/asset') { $json.require | Add-Member -NotePropertyName 'symfony/asset' -NotePropertyValue '7.0.*' -Force; $json | ConvertTo-Json -Depth 10 | Set-Content 'composer.json' }"

echo [5/6] Regenerating autoloader...
composer dump-autoload --no-interaction

echo [6/6] Clearing cache...
php bin/console cache:clear

echo.
echo Cleaning up...
del "%TEMP_ZIP%" >nul 2>&1
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"

if exist "%TARGET_DIR%\composer.json" (
    echo.
    echo ========================================
    echo SUCCESS!
    echo ========================================
    echo.
    echo symfony/asset installed successfully!
    echo.
) else (
    echo.
    echo ========================================
    echo INSTALLATION FAILED
    echo ========================================
    echo.
    echo The package was not installed correctly.
    echo.
)

echo Restart server:
echo   Ctrl+C
echo   php -S localhost:8000 -t public
echo.
pause

