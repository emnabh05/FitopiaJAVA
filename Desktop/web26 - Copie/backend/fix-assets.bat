@echo off
echo ========================================
echo Enabling Asset Component
echo ========================================
echo.

echo Updating framework.yaml to enable assets...

(
echo framework:
echo     secret: '%%env^(APP_SECRET^)%%'
echo     http_method_override: false
echo     handle_all_throwables: true
echo     php_errors:
echo         log: true
echo     assets:
echo         enabled: true
echo.
echo when@dev:
echo     framework:
echo         router:
echo             strict_requirements: true
echo.
echo when@prod:
echo     framework:
echo         router:
echo             strict_requirements: null
) > config\packages\framework.yaml

echo Done!
echo.
echo Clearing cache...
php bin/console cache:clear

echo.
echo ========================================
echo ASSETS ENABLED!
echo ========================================
echo.
echo The asset^(^) function should work now.
echo.
echo Restart your server:
echo   Ctrl+C
echo   php -S localhost:8000 -t public
echo.
echo Then refresh: http://localhost:8000/supplement
echo.
pause

