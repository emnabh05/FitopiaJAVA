@echo off
echo Creating framework.yaml...

(
echo framework:
echo     secret: '%%env^(APP_SECRET^)%%'
echo     http_method_override: false
echo     handle_all_throwables: true
echo     php_errors:
echo         log: true
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
echo Framework.yaml created!
echo.
echo Restart server:
echo   php -S localhost:8000 -t public
echo.
pause

