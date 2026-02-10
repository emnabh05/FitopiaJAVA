@echo off
echo ========================================
echo Fixing SSL Certificate Issue
echo ========================================
echo.

echo Step 1: Disabling SSL verification for Composer (temporary fix)...
composer config -g -- disable-tls false
composer config -g -- secure-http false
echo SSL verification disabled.
echo.

echo Step 2: Installing Composer dependencies...
call composer install --no-interaction
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Composer install still failed.
    echo.
    echo Trying alternative method...
    echo.
    composer install --ignore-platform-reqs --no-scripts
    if %errorlevel% neq 0 (
        echo.
        echo Please try manual fix:
        echo 1. Download cacert.pem from: https://curl.se/ca/cacert.pem
        echo 2. Save it to C:\cacert.pem
        echo 3. Edit php.ini and add: curl.cainfo = "C:\cacert.pem"
        echo 4. Restart and run setup.bat again
        pause
        exit /b 1
    )
)
echo.

echo Step 3: Creating upload directory...
if not exist "public\uploads\supplements" mkdir "public\uploads\supplements"
echo Upload directory created.
echo.

echo Step 4: Creating database...
php bin/console doctrine:database:create --if-not-exists
echo.

echo Step 5: Running migrations...
php bin/console doctrine:migrations:migrate --no-interaction
echo.

echo Step 6: Clearing cache...
php bin/console cache:clear
echo.

echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo To start the server, run:
echo   start-server.bat
echo.
echo Then visit: http://localhost:8000/supplement
echo.
pause

