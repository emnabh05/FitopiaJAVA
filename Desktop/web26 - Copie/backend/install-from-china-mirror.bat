@echo off
echo ========================================
echo Install Using Alternative Repository
echo ========================================
echo.
echo This uses a mirror that doesn't require SSL.
echo.

REM Use Aliyun mirror (China) - works without SSL
composer config -g repos.packagist composer https://mirrors.aliyun.com/composer/
composer config -g secure-http false
composer config -g disable-tls true

echo Clearing cache...
composer clear-cache

echo.
echo Installing from mirror...
echo This may take 5-10 minutes...
echo.

composer install --no-interaction --ignore-platform-reqs

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo SUCCESS!
    echo ========================================
    echo.
    
    REM Setup database
    if not exist "public\uploads\supplements" mkdir "public\uploads\supplements"
    php bin/console doctrine:database:create --if-not-exists
    php bin/console doctrine:migrations:migrate --no-interaction
    php bin/console cache:clear
    
    echo.
    echo Ready! Start server with:
    echo   php -S localhost:8000 -t public
    echo.
) else (
    echo.
    echo Mirror install failed too.
    echo.
    echo Let me try one more thing...
    echo.
    
    REM Reset to default and try HTTP
    composer config -g --unset repos.packagist
    composer config -g repo.packagist composer http://packagist.org
    composer install --no-interaction --ignore-platform-reqs
)

pause

