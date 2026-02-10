@echo off
echo.
echo ========================================
echo    FITOPIA - STARTING SERVER
echo ========================================
echo.

echo [1/2] Clearing cache...
php bin/console cache:clear

echo.
echo [2/2] Starting server...
echo.
echo ========================================
echo    SERVER RUNNING
echo ========================================
echo.
echo Open your browser: http://localhost:8000/supplement
echo.
echo Press Ctrl+C to stop the server
echo.

php -S localhost:8000 -t public

