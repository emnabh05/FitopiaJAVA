@echo off
echo ========================================
echo Adding Asset Component Manually
echo ========================================
echo.

echo Step 1: Backing up composer.json...
copy composer.json composer.json.backup >nul

echo Step 2: Adding symfony/asset to composer.json...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$json = Get-Content 'composer.json' -Raw | ConvertFrom-Json; $json.require | Add-Member -NotePropertyName 'symfony/asset' -NotePropertyValue '7.0.*' -Force; $json | ConvertTo-Json -Depth 10 | Set-Content 'composer.json'"

echo Step 3: Disabling SSL for this install...
composer config -g secure-http false
composer config -g disable-tls true

echo Step 4: Installing with SSL disabled...
composer update symfony/asset --no-interaction --no-scripts --prefer-dist

if %errorlevel% neq 0 (
    echo.
    echo Installation failed. Restoring backup...
    copy composer.json.backup composer.json >nul
    echo.
    echo Let me try a different approach...
    echo.
    pause
    exit /b 1
)

echo Step 5: Running full install to update autoloader...
composer install --no-interaction --no-scripts

echo Step 6: Clearing cache...
php bin/console cache:clear

echo.
echo ========================================
echo SUCCESS!
echo ========================================
echo.
echo Asset component installed!
echo.
echo Restart server:
echo   Ctrl+C
echo   php -S localhost:8000 -t public
echo.
pause

