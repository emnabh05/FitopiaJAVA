@echo off
setlocal enabledelayedexpansion

echo ========================================
echo AUTOMATIC FIX AND INSTALL v2
echo ========================================
echo.

REM Step 1: Download certificate
echo [1/7] Downloading SSL certificate...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri 'https://curl.se/ca/cacert.pem' -OutFile 'C:\cacert.pem' -UseBasicParsing; Write-Host 'Downloaded!' } catch { Write-Host 'Failed - will try alternative method' }"
echo.

REM Step 2: Find php.ini using a better method
echo [2/7] Finding php.ini location...
set "PHP_INI_PATH="
for /f "delims=" %%i in ('php -r "echo php_ini_loaded_file();"') do set "PHP_INI_PATH=%%i"
echo Found: %PHP_INI_PATH%
echo.

REM Step 3: Backup php.ini
echo [3/7] Backing up php.ini...
if exist "%PHP_INI_PATH%" (
    copy "%PHP_INI_PATH%" "%PHP_INI_PATH%.backup" >nul 2>&1
    echo Backup created!
) else (
    echo Warning: Could not find php.ini
)
echo.

REM Step 4: Configure php.ini using PowerShell
echo [4/7] Configuring PHP for SSL...
if exist "%PHP_INI_PATH%" (
    powershell -Command "$file='%PHP_INI_PATH%'; $content=Get-Content $file -Raw; if($content -notmatch 'curl.cainfo.*cacert.pem'){$content=$content+\"`r`ncurl.cainfo = \`"C:\cacert.pem\`"`r`n\"}; if($content -notmatch 'openssl.cafile.*cacert.pem'){$content=$content+\"`r`nopenssl.cafile=\`"C:\cacert.pem\`"`r`n\"}; Set-Content $file $content"
    echo PHP configured!
) else (
    echo Skipping php.ini configuration
)
echo.

REM Step 5: Clear Composer cache
echo [5/7] Clearing Composer cache...
composer clear-cache 2>nul
echo Cache cleared!
echo.

REM Step 6: Install dependencies with multiple fallback methods
echo [6/7] Installing Composer dependencies...
echo This may take 3-5 minutes. Please wait...
echo.

REM Try method 1: Normal install
composer install --no-interaction 2>nul
if %errorlevel% equ 0 goto install_success

REM Try method 2: Disable SSL
echo Trying with SSL disabled...
composer config -g disable-tls true
composer config -g secure-http false
composer install --no-interaction 2>nul
if %errorlevel% equ 0 goto install_success

REM Try method 3: With ignore platform reqs
echo Trying with platform requirements ignored...
composer install --no-interaction --ignore-platform-reqs 2>nul
if %errorlevel% equ 0 goto install_success

REM Try method 4: Prefer source
echo Trying with prefer-source...
composer install --prefer-source --no-interaction 2>nul
if %errorlevel% equ 0 goto install_success

echo.
echo Installation failed. Checking if vendor folder exists...
if exist "vendor\autoload.php" (
    echo Vendor folder found! Continuing...
    goto install_success
) else (
    echo ERROR: Could not install dependencies.
    echo.
    echo Please try:
    echo 1. Temporarily disable your antivirus/firewall
    echo 2. Run this script again
    echo.
    pause
    exit /b 1
)

:install_success
echo Dependencies installed successfully!
echo.

REM Step 7: Setup database and finalize
echo [7/7] Setting up database and finalizing...
if not exist "public\uploads\supplements" mkdir "public\uploads\supplements" 2>nul
php bin/console doctrine:database:create --if-not-exists 2>nul
php bin/console doctrine:migrations:migrate --no-interaction 2>nul
php bin/console cache:clear --no-interaction 2>nul
echo.

REM Final verification
if exist "vendor\autoload.php" (
    echo ========================================
    echo SUCCESS! Installation Complete!
    echo ========================================
    echo.
    echo Your Symfony application is ready!
    echo.
    echo To start the server:
    echo   php -S localhost:8000 -t public
    echo.
    echo Then open browser:
    echo   http://localhost:8000/supplement
    echo.
    if exist "%PHP_INI_PATH%.backup" (
        echo Your php.ini backup is at:
        echo   %PHP_INI_PATH%.backup
        echo.
    )
) else (
    echo ========================================
    echo Installation Incomplete
    echo ========================================
    echo.
    echo The vendor folder was not created.
    echo Please check errors above.
    echo.
)

pause

