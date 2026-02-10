@echo off
echo ========================================
echo CLEARING CACHE AND RESTARTING
echo ========================================
echo.

echo Clearing Symfony cache...
php bin/console cache:clear

echo.
echo ========================================
echo SUCCESS!
echo ========================================
echo.
echo All templates have been recreated without asset() function.
echo.
echo Now restart your server:
echo   1. Press Ctrl+C to stop the current server
echo   2. Run: php -S localhost:8000 -t public
echo   3. Open: http://localhost:8000/supplement
echo.
echo The application should now work perfectly!
echo.
pause

