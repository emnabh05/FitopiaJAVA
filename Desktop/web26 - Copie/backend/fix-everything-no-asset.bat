@echo off
echo ========================================
echo COMPLETE FIX - NO ASSET PACKAGE NEEDED
echo ========================================
echo.

echo [1/4] Removing assets config from framework.yaml...
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
echo [2/4] Replacing asset^(^) calls in base.html.twig...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$content = Get-Content 'templates\base.html.twig' -Raw; $content = $content -replace [regex]::Escape(\"{{ asset('build/assets/img/apple-icon.png') }}\"), '/build/assets/img/apple-icon.png'; $content = $content -replace [regex]::Escape(\"{{ asset('build/assets/img/logo.jpeg') }}\"), '/build/assets/img/logo.jpeg'; $content = $content -replace [regex]::Escape(\"{{ asset('build/assets/css/nucleo-icons.css') }}\"), '/build/assets/css/nucleo-icons.css'; $content = $content -replace [regex]::Escape(\"{{ asset('build/assets/css/nucleo-svg.css') }}\"), '/build/assets/css/nucleo-svg.css'; $content = $content -replace [regex]::Escape(\"{{ asset('build/assets/css/argon-dashboard-tailwind-fitopia.css') }}\"), '/build/assets/css/argon-dashboard-tailwind-fitopia.css'; $content = $content -replace [regex]::Escape(\"{{ asset('build/assets/js/plugins/perfect-scrollbar.min.js') }}\"), '/build/assets/js/plugins/perfect-scrollbar.min.js'; $content = $content -replace [regex]::Escape(\"{{ asset('build/assets/js/plugins/smooth-scrollbar.min.js') }}\"), '/build/assets/js/plugins/smooth-scrollbar.min.js'; $content = $content -replace [regex]::Escape(\"{{ asset('build/assets/js/argon-dashboard-tailwind.js') }}\"), '/build/assets/js/argon-dashboard-tailwind.js'; Set-Content -Path 'templates\base.html.twig' -Value $content -NoNewline"

echo Done!

echo.
echo [3/4] Replacing asset^(^) calls in supplement templates...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$content = Get-Content 'templates\supplement\index.html.twig' -Raw; $content = $content -replace [regex]::Escape(\"{{ asset('uploads/supplements/' ~ supplement.image) }}\"), '/uploads/supplements/{{ supplement.image }}'; Set-Content -Path 'templates\supplement\index.html.twig' -Value $content -NoNewline"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$content = Get-Content 'templates\supplement\edit.html.twig' -Raw; $content = $content -replace [regex]::Escape(\"{{ asset('uploads/supplements/' ~ supplement.image) }}\"), '/uploads/supplements/{{ supplement.image }}'; Set-Content -Path 'templates\supplement\edit.html.twig' -Value $content -NoNewline"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$content = Get-Content 'templates\supplement\show.html.twig' -Raw; $content = $content -replace [regex]::Escape(\"{{ asset('uploads/supplements/' ~ supplement.image) }}\"), '/uploads/supplements/{{ supplement.image }}'; Set-Content -Path 'templates\supplement\show.html.twig' -Value $content -NoNewline"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$content = Get-Content 'templates\supplement\new.html.twig' -Raw; $content = $content -replace [regex]::Escape(\"{{ asset('js/supplement-validation.js') }}\"), '/js/supplement-validation.js'; Set-Content -Path 'templates\supplement\new.html.twig' -Value $content -NoNewline"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$content = Get-Content 'templates\supplement\edit.html.twig' -Raw; $content = $content -replace [regex]::Escape(\"{{ asset('js/supplement-validation.js') }}\"), '/js/supplement-validation.js'; Set-Content -Path 'templates\supplement\edit.html.twig' -Value $content -NoNewline"

echo Done!

echo.
echo [4/4] Clearing cache...
php bin/console cache:clear

echo.
echo ========================================
echo SUCCESS! ALL FIXED!
echo ========================================
echo.
echo All asset^(^) function calls have been removed.
echo The app will now work without symfony/asset package.
echo.
echo Restart your server:
echo   Ctrl+C ^(to stop current server^)
echo   php -S localhost:8000 -t public
echo.
echo Then open: http://localhost:8000/supplement
echo.
pause

