@echo off
echo ========================================
echo SIMPLE INSTALL (Skip SSL Config)
echo ========================================
echo.
echo This will install dependencies without
echo modifying your php.ini file.
echo.
pause

echo [1/4] Clearing Composer cache...
composer clear-cache
echo Done!
echo.

echo [2/4] Configuring Composer to skip SSL...
composer config -g disable-tls true
composer config -g secure-http false
echo Done!
echo.

echo [3/4] Installing dependencies...
echo This will take 3-5 minutes. Please wait...
echo.

composer install --no-interaction --ignore-platform-reqs --prefer-dist
if %errorlevel% neq 0 (
    echo.
    echo First method failed, trying alternative...
    echo.
    composer install --prefer-source --no-scripts --no-interaction
)

if %errorlevel% neq 0 (
    echo.
    echo Still failing. Trying one more method...
    echo.
    composer update --no-interaction --ignore-platform-reqs
)

echo.

echo [4/4] Setting up database...
if not exist "public\uploads\supplements" mkdir "public\uploads\supplements"
php bin/console doctrine:database:create --if-not-exists
php bin/console doctrine:migrations:migrate --no-interaction
php bin/console cache:clear
echo.

if exist "vendor\autoload.php" (
    echo ========================================
    echo SUCCESS!
    echo ========================================
    echo.
    echo To start the server, run:
    echo   php -S localhost:8000 -t public
    echo.
    echo Then open: http://localhost:8000/supplement
    echo.
) else (
    echo ========================================
    echo FAILED
    echo ========================================
    echo.
    echo Vendor folder not created.
    echo.
    echo Please:
    echo 1. Temporarily disable Avast/antivirus
    echo 2. Run this script again
    echo.
)

pause

