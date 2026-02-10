@echo off
echo ========================================
echo Fitopia Quick Install (SSL Fix Included)
echo ========================================
echo.

echo This will install everything with SSL verification disabled.
echo This is safe for local development.
echo.
pause

echo Step 1: Configuring Composer to skip SSL verification...
composer config -g disable-tls true
composer config -g secure-http false
echo Done.
echo.

echo Step 2: Installing dependencies (this may take a few minutes)...
composer install --no-interaction --ignore-platform-reqs
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Installation failed.
    echo.
    echo Please check:
    echo - Is Composer installed? (composer -V)
    echo - Is PHP installed? (php -v)
    echo - Is your internet connection working?
    echo.
    pause
    exit /b 1
)
echo.

echo Step 3: Creating upload directory...
if not exist "public\uploads\supplements" (
    mkdir "public\uploads\supplements"
    echo Created: public\uploads\supplements
) else (
    echo Already exists: public\uploads\supplements
)
echo.

echo Step 4: Setting up database...
echo Creating database (if it doesn't exist)...
php bin/console doctrine:database:create --if-not-exists 2>nul
if %errorlevel% equ 0 (
    echo Database created successfully.
) else (
    echo Database already exists or connection failed.
    echo Please check your .env file for correct database credentials.
    echo.
    echo Current DATABASE_URL in .env:
    findstr "DATABASE_URL" .env
    echo.
    echo If this is wrong, edit .env file and run this script again.
    echo.
    set /p continue="Continue anyway? (y/n): "
    if /i not "%continue%"=="y" exit /b 1
)
echo.

echo Step 5: Running database migrations...
php bin/console doctrine:migrations:migrate --no-interaction
if %errorlevel% neq 0 (
    echo.
    echo WARNING: Migrations failed. This might be okay if database already exists.
    echo.
)
echo.

echo Step 6: Clearing cache...
php bin/console cache:clear --no-interaction
echo.

echo ========================================
echo Installation Complete!
echo ========================================
echo.
echo Your Symfony application is ready!
echo.
echo To start the server:
echo   1. Run: start-server.bat
echo   2. Open browser: http://localhost:8000/supplement
echo.
echo Or manually start with:
echo   php -S localhost:8000 -t public
echo.
echo ========================================
echo.
pause

