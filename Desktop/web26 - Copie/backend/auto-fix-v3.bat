@echo off
setlocal

echo ========================================
echo AUTOMATIC FIX AND INSTALL v3 (Windows)
echo ========================================
echo.

REM Always run from backend folder
cd /d "%~dp0"

set "CERT_DIR=%CD%\.certs"
set "CERT_FILE=%CERT_DIR%\cacert.pem"

echo [1/7] Preparing certificate folder...
if not exist "%CERT_DIR%" mkdir "%CERT_DIR%" >nul 2>&1

echo [2/7] Downloading CA bundle to:
echo   %CERT_FILE%
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; [Net.ServicePointManager]::ServerCertificateValidationCallback={ $true }; try { Invoke-WebRequest -Uri 'https://curl.se/ca/cacert.pem' -OutFile '%CERT_FILE%' -UseBasicParsing; Write-Host 'Downloaded via PowerShell' } catch { Write-Host 'PowerShell download failed' }"

if not exist "%CERT_FILE%" (
  echo Trying fallback download via certutil...
  certutil -urlcache -split -f https://curl.se/ca/cacert.pem "%CERT_FILE%" >nul 2>&1
)

if not exist "%CERT_FILE%" (
  echo.
  echo ERROR: Could not download the CA bundle.
  echo - This is usually antivirus/firewall HTTPS scanning.
  echo - Try temporarily disabling Avast HTTPS scanning for 5 minutes.
  echo - Then re-run this script.
  echo.
  pause
  exit /b 1
)

echo [3/7] Adding local antivirus/proxy root certs if found (Avast/Zscaler/etc)...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$target='%CERT_FILE%'; $names='Avast|Zscaler|Bitdefender|ESET|Kaspersky|Fortinet|Sophos|Blue Coat|Cisco Umbrella'; $stores=@('Cert:\\CurrentUser\\Root','Cert:\\LocalMachine\\Root'); $certs=@(); foreach($s in $stores){ if(Test-Path $s){ $certs += Get-ChildItem $s | Where-Object { $_.Subject -match $names } } }; $certs = $certs | Select-Object -Unique; foreach($c in $certs){ $bytes=$c.Export([System.Security.Cryptography.X509Certificates.X509ContentType]::Cert); $b=[Convert]::ToBase64String($bytes,'InsertLineBreaks'); $nl=[Environment]::NewLine; $pem='-----BEGIN CERTIFICATE-----'+$nl+$b+$nl+'-----END CERTIFICATE-----'+$nl; Add-Content -Encoding Ascii -Path $target -Value $pem }"

echo [4/7] Locating php.ini...
set "PHP_INI_PATH="
for /f "delims=" %%i in ('php -r "echo php_ini_loaded_file();"') do set "PHP_INI_PATH=%%i"
echo   php.ini = %PHP_INI_PATH%

if not "%PHP_INI_PATH%"=="" if exist "%PHP_INI_PATH%" (
  echo Backing up php.ini...
  copy "%PHP_INI_PATH%" "%PHP_INI_PATH%.backup" >nul 2>&1
  echo Patching php.ini to use CA file...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ini='%PHP_INI_PATH%'; $ca='%CERT_FILE%'; $lines=Get-Content -LiteralPath $ini; $foundCurl=$false; $foundOpen=$false; for($i=0;$i -lt $lines.Count;$i++){ if($lines[$i] -match '^\s*;?\s*curl\.cainfo\s*='){ $lines[$i]='curl.cainfo = '+$ca; $foundCurl=$true } if($lines[$i] -match '^\s*;?\s*openssl\.cafile\s*='){ $lines[$i]='openssl.cafile = '+$ca; $foundOpen=$true } }; if(-not $foundCurl){ $lines += 'curl.cainfo = '+$ca }; if(-not $foundOpen){ $lines += 'openssl.cafile = '+$ca }; Set-Content -LiteralPath $ini -Value $lines -Encoding ASCII"
) else (
  echo WARNING: php.ini not found. Will rely on env vars for Composer.
)

echo [5/7] Setting env vars for this terminal...
set "COMPOSER_CAFILE=%CERT_FILE%"
set "SSL_CERT_FILE=%CERT_FILE%"
set "CURL_CA_BUNDLE=%CERT_FILE%"

echo [6/7] Installing Composer dependencies...
composer clear-cache
composer install --no-interaction --prefer-dist
if %errorlevel% neq 0 (
  echo.
  echo Composer install failed.
  echo - If you have Avast, disable HTTPS scanning temporarily then retry.
  echo - Log a screenshot of the first error lines and send it to me.
  echo.
  pause
  exit /b 1
)

echo [7/7] Finishing (uploads + database)...
if not exist "public\uploads\supplements" mkdir "public\uploads\supplements" >nul 2>&1
php bin/console doctrine:database:create --if-not-exists
php bin/console doctrine:migrations:migrate --no-interaction
php bin/console cache:clear

if exist "vendor\autoload_runtime.php" (
  echo.
  echo ========================================
  echo SUCCESS! vendor is installed.
  echo ========================================
  echo Start server:
  echo   php -S localhost:8000 -t public
  echo Open:
  echo   http://localhost:8000/supplement
  echo.
) else (
  echo.
  echo WARNING: vendor seems incomplete. Check composer output.
)

pause

