@echo off
echo ========================================
echo Starting Fitopia Symfony Server
echo ========================================
echo.
echo Server will start at: http://localhost:8000
echo Supplement module: http://localhost:8000/supplement
echo.
echo Press Ctrl+C to stop the server
echo.
php -S localhost:8000 -t public

