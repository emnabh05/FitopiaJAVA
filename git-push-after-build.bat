@echo off
REM Script pour faire le push Git automatique après chaque compilation réussie

echo.
echo ========================================
echo Push Git automatique apres compilation
echo ========================================
echo.

REM Vérifier si nous sommes dans un dépôt Git
if not exist ".git" (
    echo Erreur: Ce n'est pas un depot Git
    pause
    exit /b 1
)

REM Ajouter tous les fichiers modifiés
echo Ajout des fichiers modifies...
git add .

REM Vérifier s'il y a des changements à committer
git status --porcelain >nul 2>&1
if %errorlevel% neq 0 (
    echo Aucun changement a committer
    goto :end
)

REM Obtenir la date et heure actuelle pour le message de commit
for /f "tokens=1-6 delims= " %%a in ('date /t') do set date=%%a%%b
for /f "tokens=1-3 delims=: " %%a in ('time /t') do set time=%%a:%%b:%%c

set message=Auto-commit: Modifications et ajouts - %date% %time%

REM Committer les changements
echo Commit des changements...
git commit -m "%message%"

REM Push vers le dépôt distant
echo Push vers le depot distant...
git push

REM Vérifier si le push a réussi
if %errorlevel% equ 0 (
    echo.
    echo Push automatique reussi!
    echo Message: %message%
    echo.
) else (
    echo.
    echo Erreur lors du push automatique
    echo.
    pause
    exit /b 1
)

:end
echo Push automatique termine!
echo.
pause
