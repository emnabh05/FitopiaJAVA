@echo off
echo ========================================
echo DATABASE SETUP
echo ========================================
echo.

echo [1/5] Renaming Entity file...
if exist "src\Entity\SupplementComplete.php" (
    move src\Entity\SupplementComplete.php src\Entity\Supplement.php
    echo Entity renamed!
) else (
    echo Entity already named correctly.
)

echo.
echo [2/5] Creating uploads directory...
if not exist "public\uploads\supplements" mkdir "public\uploads\supplements"
echo Done!

echo.
echo [3/5] Creating database...
php bin/console doctrine:database:create --if-not-exists
if %errorlevel% neq 0 (
    echo.
    echo WARNING: Database creation failed.
    echo This might mean:
    echo - MySQL/MariaDB is not running
    echo - Database already exists (which is OK)
    echo.
    echo Continuing anyway...
)

echo.
echo [4/5] Running migrations (creating tables)...
php bin/console doctrine:migrations:migrate --no-interaction

echo.
echo [5/5] Clearing cache...
php bin/console cache:clear

echo.
echo ========================================
echo DATABASE SETUP COMPLETE!
echo ========================================
echo.
echo Next steps:
echo.
echo 1. Start the server:
echo    php -S localhost:8000 -t public
echo.
echo 2. Open browser:
echo    http://localhost:8000/supplement
echo.
pause

