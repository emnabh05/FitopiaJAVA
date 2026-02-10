@echo off
echo ========================================
echo Install Using Symfony CLI
echo ========================================
echo.
echo Checking if Symfony CLI is installed...
echo.

symfony version >nul 2>&1
if %errorlevel% neq 0 (
    echo Symfony CLI is NOT installed.
    echo.
    echo Downloading Symfony CLI installer...
    echo.
    
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://get.symfony.com/cli/installer' -OutFile 'symfony-installer.exe' -UseBasicParsing}"
    
    if exist "symfony-installer.exe" (
        echo Running installer...
        symfony-installer.exe
        del symfony-installer.exe
        echo.
        echo Please restart this script after installation.
        pause
        exit /b 0
    ) else (
        echo.
        echo Auto-download failed.
        echo.
        echo Please manually install Symfony CLI:
        echo 1. Go to: https://symfony.com/download
        echo 2. Download the Windows installer
        echo 3. Run it
        echo 4. Run this script again
        echo.
        pause
        exit /b 1
    )
)

echo Symfony CLI found!
echo.

echo Installing Composer dependencies via Symfony CLI...
symfony composer install --no-interaction

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo SUCCESS!
    echo ========================================
    echo.
    
    if not exist "public\uploads\supplements" mkdir "public\uploads\supplements"
    symfony console doctrine:database:create --if-not-exists
    symfony console doctrine:migrations:migrate --no-interaction
    symfony console cache:clear
    
    echo.
    echo Ready! Start server with:
    echo   symfony server:start
    echo.
    echo Or:
    echo   php -S localhost:8000 -t public
    echo.
) else (
    echo.
    echo Installation failed.
    echo.
    echo Try running manually:
    echo   symfony composer install
    echo.
)

pause

