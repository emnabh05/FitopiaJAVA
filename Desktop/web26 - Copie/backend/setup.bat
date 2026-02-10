@echo off
echo ========================================
echo Fitopia Symfony 7 Setup Script
echo ========================================
echo.

echo Step 1: Installing Composer dependencies...
call composer install
if %errorlevel% neq 0 (
    echo ERROR: Composer install failed. Make sure Composer is installed.
    echo Download from: https://getcomposer.org/download/
    pause
    exit /b 1
)
echo.

echo Step 2: Creating upload directory...
if not exist "public\uploads\supplements" mkdir "public\uploads\supplements"
echo Upload directory created.
echo.

echo Step 3: Creating database...
php bin/console doctrine:database:create --if-not-exists
echo.

echo Step 4: Running migrations...
php bin/console doctrine:migrations:migrate --no-interaction
echo.

echo Step 5: Clearing cache...
php bin/console cache:clear
echo.

echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo To start the server, run:
echo   php -S localhost:8000 -t public
echo.
echo Or if you have Symfony CLI:
echo   symfony server:start
echo.
echo Then visit: http://localhost:8000/supplement
echo.
pause

