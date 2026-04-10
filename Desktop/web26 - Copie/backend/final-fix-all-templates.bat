@echo off
echo ========================================
echo FINAL FIX - Removing ALL asset() calls
echo ========================================
echo.

echo Deleting old templates...
del /Q templates\base.html.twig 2>nul
del /Q templates\supplement\*.twig 2>nul

echo.
echo Creating new templates without asset() function...
echo.

REM This PowerShell script will fix all templates
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
"$files = @('templates\base.html.twig', 'templates\supplement\index.html.twig', 'templates\supplement\new.html.twig', 'templates\supplement\edit.html.twig', 'templates\supplement\show.html.twig'); ^
foreach ($file in $files) { ^
    if (Test-Path $file) { ^
        $content = Get-Content $file -Raw; ^
        $content = $content -replace \"\{\{ asset\('([^']+)'\) \}\}\", '/$1'; ^
        $content = $content -replace \"\{\{ asset\('uploads/supplements/' ~ supplement\.image\) \}\}\", '/uploads/supplements/{{ supplement.image }}'; ^
        Set-Content -Path $file -Value $content -NoNewline; ^
        Write-Host \"Fixed: $file\"; ^
    } ^
}"

echo.
echo Clearing cache...
php bin/console cache:clear

echo.
echo ========================================
echo DONE!
echo ========================================
echo.
echo All templates have been fixed.
echo.
echo Restart server:
echo   php -S localhost:8000 -t public
echo.
pause

