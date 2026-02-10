@echo off
echo ========================================
echo Installing Symfony Without SSL
echo ========================================
echo.

echo Step 1: Downloading certificate file...
powershell -Command "try { Invoke-WebRequest -Uri 'https://curl.se/ca/cacert.pem' -OutFile 'C:\cacert.pem' -UseBasicParsing; Write-Host 'Certificate downloaded successfully' } catch { Write-Host 'Download failed - you may need to download manually' }"
echo.

echo Step 2: Finding php.ini location...
php --ini | findstr "Loaded"
echo.
echo Please note the php.ini location above.
echo.

echo Step 3: Configuring PHP to use certificate...
echo.
echo MANUAL STEP REQUIRED:
echo 1. Open the php.ini file shown above in a text editor
echo 2. Find the line: ;curl.cainfo =
echo 3. Change it to: curl.cainfo = "C:\cacert.pem"
echo 4. Find the line: ;openssl.cafile=
echo 5. Change it to: openssl.cafile="C:\cacert.pem"
echo 6. Save the file
echo.
echo After editing php.ini, press any key to continue...
pause
echo.

echo Step 4: Installing Composer dependencies...
composer install
if %errorlevel% neq 0 (
    echo.
    echo Installation failed. Trying alternative method...
    composer config -g disable-tls true
    composer config -g secure-http false
    composer install --ignore-platform-reqs --no-scripts
)
echo.

echo Step 5: Creating upload directory...
if not exist "public\uploads\supplements" mkdir "public\uploads\supplements"
echo.

echo Step 6: Creating database...
php bin/console doctrine:database:create --if-not-exists
echo.

echo Step 7: Running migrations...
php bin/console doctrine:migrations:migrate --no-interaction
echo.

echo Step 8: Clearing cache...
php bin/console cache:clear
echo.

echo ========================================
echo Setup Complete!
echo ========================================
echo.
pause

