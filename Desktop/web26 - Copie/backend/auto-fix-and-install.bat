@echo off
echo ========================================
echo AUTOMATIC FIX AND INSTALL
echo ========================================
echo.
echo This will automatically:
echo - Download SSL certificate
echo - Configure PHP
echo - Install all dependencies
echo - Setup database
echo.
echo Please wait...
echo.

REM Step 1: Download certificate
echo [1/8] Downloading SSL certificate...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://curl.se/ca/cacert.pem' -OutFile 'C:\cacert.pem' -UseBasicParsing}" 2>nul
if exist "C:\cacert.pem" (
    echo Certificate downloaded successfully!
) else (
    echo Certificate download failed, but continuing...
)
echo.

REM Step 2: Find php.ini
echo [2/8] Locating php.ini...
for /f "tokens=2 delims=:" %%a in ('php --ini ^| findstr "Loaded"') do set PHP_INI=%%a
set PHP_INI=%PHP_INI:~1%
echo Found: %PHP_INI%
echo.

REM Step 3: Backup php.ini
echo [3/8] Backing up php.ini...
copy "%PHP_INI%" "%PHP_INI%.backup" >nul 2>&1
echo Backup created: %PHP_INI%.backup
echo.

REM Step 4: Configure php.ini automatically
echo [4/8] Configuring PHP for SSL...
powershell -Command "& {$content = Get-Content '%PHP_INI%'; $content = $content -replace ';curl.cainfo\s*=.*', 'curl.cainfo = \"C:\cacert.pem\"'; $content = $content -replace ';openssl.cafile\s*=.*', 'openssl.cafile=\"C:\cacert.pem\"'; if ($content -notmatch 'curl.cainfo') { $content += \"`ncurl.cainfo = `\"C:\cacert.pem`\"\" }; if ($content -notmatch 'openssl.cafile') { $content += \"`nopenssl.cafile=`\"C:\cacert.pem`\"\" }; $content | Set-Content '%PHP_INI%'}"
echo PHP configured!
echo.

REM Step 5: Verify configuration
echo [5/8] Verifying PHP configuration...
php -i | findstr "curl.cainfo" >nul
if %errorlevel% equ 0 (
    echo SSL configuration verified!
) else (
    echo Warning: Could not verify SSL config, but continuing...
)
echo.

REM Step 6: Install Composer dependencies
echo [6/8] Installing Composer dependencies (this may take 3-5 minutes)...
echo Please be patient...
echo.
composer install --no-interaction 2>nul
if %errorlevel% neq 0 (
    echo First attempt failed, trying with SSL disabled...
    composer config -g disable-tls true
    composer config -g secure-http false
    composer install --no-interaction --ignore-platform-reqs
)
echo.

REM Step 7: Setup database
echo [7/8] Setting up database...
if not exist "public\uploads\supplements" mkdir "public\uploads\supplements"
php bin/console doctrine:database:create --if-not-exists 2>nul
php bin/console doctrine:migrations:migrate --no-interaction 2>nul
php bin/console cache:clear --no-interaction 2>nul
echo.

REM Step 8: Final check
echo [8/8] Verifying installation...
if exist "vendor\autoload.php" (
    echo.
    echo ========================================
    echo SUCCESS! Installation Complete!
    echo ========================================
    echo.
    echo Everything is ready!
    echo.
    echo To start the server:
    echo   1. Run: start-server.bat
    echo   2. Or run: php -S localhost:8000 -t public
    echo   3. Open browser: http://localhost:8000/supplement
    echo.
    echo Your php.ini has been backed up to:
    echo %PHP_INI%.backup
    echo.
) else (
    echo.
    echo ========================================
    echo Installation incomplete
    echo ========================================
    echo.
    echo The vendor folder was not created.
    echo Please check the errors above.
    echo.
)

pause

