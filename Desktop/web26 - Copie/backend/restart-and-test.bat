@echo off
echo ========================================
echo FITOPIA DASHBOARD - RESTART
echo ========================================
echo.

echo [1/2] Clearing Symfony cache...
php bin/console cache:clear

echo.
echo [2/2] Cache cleared successfully!
echo.
echo ========================================
echo READY TO GO!
echo ========================================
echo.
echo Your Fitopia Dashboard is ready!
echo.
echo Next steps:
echo   1. Stop your current server (Ctrl+C)
echo   2. Start server: php -S localhost:8000 -t public
echo   3. Open: http://localhost:8000/supplement
echo.
echo The custom Fitopia design is now active!
echo.
pause

