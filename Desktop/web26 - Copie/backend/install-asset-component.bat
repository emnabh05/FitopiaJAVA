@echo off
echo ========================================
echo Installing Symfony Asset Component
echo ========================================
echo.

echo This will install the missing asset component...
echo.

REM Set environment variables for SSL
set "COMPOSER_CAFILE=%CD%\.certs\cacert.pem"
set "SSL_CERT_FILE=%CD%\.certs\cacert.pem"
set "CURL_CA_BUNDLE=%CD%\.certs\cacert.pem"

echo Installing symfony/asset...
symfony composer require symfony/asset --no-interaction

if %errorlevel% neq 0 (
    echo.
    echo Symfony CLI failed, trying regular composer...
    composer require symfony/asset --no-interaction
)

echo.
echo Clearing cache...
php bin/console cache:clear

echo.
echo ========================================
echo DONE!
echo ========================================
echo.
echo The asset component is now installed.
echo.
echo Restart your server:
echo   Ctrl+C (to stop current server)
echo   php -S localhost:8000 -t public
echo.
echo Then refresh: http://localhost:8000/supplement
echo.
pause

