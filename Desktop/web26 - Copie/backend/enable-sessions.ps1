# Enable sessions in Symfony
Write-Host "Enabling sessions in framework.yaml..." -ForegroundColor Green

Remove-Item -Path "config\packages\framework.yaml" -Force -ErrorAction SilentlyContinue

@"
framework:
    secret: '%env(APP_SECRET)%'
    http_method_override: false
    handle_all_throwables: true
    php_errors:
        log: true
    session:
        handler_id: null
        cookie_secure: auto
        cookie_samesite: lax
        storage_factory_id: session.storage.factory.native

when@dev:
    framework:
        router:
            strict_requirements: true

when@prod:
    framework:
        router:
            strict_requirements: null
"@ | Out-File -FilePath "config\packages\framework.yaml" -Encoding UTF8

Write-Host "Sessions enabled!" -ForegroundColor Green
Write-Host "Clearing cache..." -ForegroundColor Yellow
php bin/console cache:clear

Write-Host ""
Write-Host "Done! Restart your server." -ForegroundColor Green
Write-Host ""

