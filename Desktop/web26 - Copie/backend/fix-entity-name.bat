@echo off
echo Renaming Entity file...

move src\Entity\SupplementComplete.php src\Entity\Supplement.php

echo Fixed!
echo.
echo Clearing cache...
php bin/console cache:clear

echo.
echo ========================================
echo DONE! Entity file renamed!
echo ========================================
echo.
echo The server should work now!
echo.
echo If server is running, restart it:
echo   Ctrl+C to stop
echo   php -S localhost:8000 -t public
echo.
echo Then open: http://localhost:8000/supplement
echo.
pause

