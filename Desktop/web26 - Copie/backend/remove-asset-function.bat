@echo off
echo ========================================
echo Removing asset() Function Calls
echo ========================================
echo.
echo This will replace all asset() calls with simple paths
echo that work without the symfony/asset package.
echo.

echo Backing up templates...
if not exist "templates_backup" mkdir templates_backup
xcopy templates templates_backup\ /E /I /Y >nul

echo.
echo Replacing asset() calls in templates...

REM Use PowerShell to do the replacements
powershell -NoProfile -ExecutionPolicy Bypass -Command "$files = Get-ChildItem -Path 'templates' -Filter '*.twig' -Recurse; foreach ($file in $files) { $content = Get-Content $file.FullName -Raw; $content = $content -replace \"{{ asset\('build/assets/([^']+)'\) }}\", '/build/assets/$1'; $content = $content -replace \"{{ asset\('uploads/supplements/' ~ supplement\.image\) }}\", '/uploads/supplements/{{ supplement.image }}'; $content = $content -replace \"{{ asset\('js/([^']+)'\) }}\", '/js/$1'; Set-Content -Path $file.FullName -Value $content -NoNewline }"

echo Done!
echo.
echo Clearing cache...
php bin/console cache:clear

echo.
echo ========================================
echo SUCCESS!
========================================
echo.
echo All asset() calls have been replaced with simple paths.
echo Your templates backup is in: templates_backup\
echo.
echo Restart your server:
echo   Ctrl+C
echo   php -S localhost:8000 -t public
echo.
echo Then open: http://localhost:8000/supplement
echo.
pause

