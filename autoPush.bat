@echo off
setlocal

set /p msg="Message commit: "
if "%msg%"=="" set "msg=update auto - progression metiers avances"

git add .
git commit -m "%msg%"
git push origin crudEvenement

endlocal
