@echo off
echo Fixing form templates...

REM Delete old templates
del /F templates\supplement\new.html.twig 2>nul
del /F templates\supplement\edit.html.twig 2>nul

echo Templates deleted. Now I'll create the new ones.
echo.
echo Please run the script again after I create the files.
pause

