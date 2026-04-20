# Script Git pour push automatique après chaque modification
# À exécuter après chaque compilation réussie

Write-Host "Début du push automatique Git..." -ForegroundColor Green

# Vérifier si nous sommes dans un dépôt Git
if (-not (Test-Path ".git")) {
    Write-Host "Erreur: Ce n'est pas un dépôt Git" -ForegroundColor Red
    exit 1
}

# Ajouter tous les fichiers modifiés
Write-Host "Ajout des fichiers modifiés..." -ForegroundColor Yellow
git add .

# Vérifier s'il y a des changements à committer
$changes = git status --porcelain
if (-not $changes) {
    Write-Host "Aucun changement à committer" -ForegroundColor Yellow
    exit 0
}

# Obtenir la date et heure actuelle pour le message de commit
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$message = "Auto-commit: Modifications et ajouts - $timestamp"

# Committer les changements
Write-Host "Commit des changements..." -ForegroundColor Yellow
git commit -m $message

# Push vers le dépôt distant
Write-Host "Push vers le dépôt distant..." -ForegroundColor Yellow
git push

# Vérifier si le push a réussi
if ($LASTEXITCODE -eq 0) {
    Write-Host "Push automatique réussi!" -ForegroundColor Green
    Write-Host "Message: $message" -ForegroundColor Cyan
} else {
    Write-Host "Erreur lors du push automatique" -ForegroundColor Red
    exit 1
}

Write-Host "Push automatique terminé!" -ForegroundColor Green
