Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   FITOPIA - COMPLETE FIX & START" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Fix framework.yaml
Write-Host "[1/5] Fixing framework.yaml..." -ForegroundColor Green
Remove-Item -Path "config\packages\framework.yaml" -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 300

$frameworkYaml = @"
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
"@

$frameworkYaml | Out-File -FilePath "config\packages\framework.yaml" -Encoding UTF8 -NoNewline
Write-Host "      ✓ Sessions enabled" -ForegroundColor Gray

# Step 2: Create new.html.twig
Write-Host "[2/5] Creating new.html.twig..." -ForegroundColor Green
Remove-Item -Path "templates\supplement\new.html.twig" -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 300

$newTemplate = @"
{% extends 'base.html.twig' %}

{% block title %}Add New Supplement - Fitopia Admin{% endblock %}
{% block page_title %}Add New Supplement{% endblock %}
{% block breadcrumb %}New Supplement{% endblock %}

{% block body %}
<div class="card">
    <div class="card-header">
        <h3 class="card-title">Supplement Information</h3>
    </div>
    <div class="card-body">
        {{ form_start(form, {'attr': {'id': 'supplementForm', 'novalidate': 'novalidate'}}) }}
        
        <div class="form-row">
            <div class="form-group">
                <label class="form-label required">Name</label>
                {{ form_widget(form.name, {'attr': {'class': 'form-control'}}) }}
            </div>
            <div class="form-group">
                <label class="form-label required">Category</label>
                {{ form_widget(form.category, {'attr': {'class': 'form-control'}}) }}
            </div>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label class="form-label required">Brand</label>
                {{ form_widget(form.brand, {'attr': {'class': 'form-control'}}) }}
            </div>
            <div class="form-group">
                <label class="form-label required">Price (`$)</label>
                {{ form_widget(form.price, {'attr': {'class': 'form-control', 'step': '0.01'}}) }}
            </div>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label class="form-label required">Stock</label>
                {{ form_widget(form.stock, {'attr': {'class': 'form-control'}}) }}
            </div>
            <div class="form-group">
                <label class="form-label">Calories</label>
                {{ form_widget(form.calories, {'attr': {'class': 'form-control'}}) }}
            </div>
        </div>

        <div class="form-group">
            <label class="form-label">Description</label>
            {{ form_widget(form.description, {'attr': {'class': 'form-control', 'rows': '4'}}) }}
        </div>

        <div class="form-group">
            <label class="form-label">Product Image</label>
            {{ form_widget(form.imageFile, {'attr': {'class': 'form-control'}}) }}
            <small class="form-text">JPG, PNG, WEBP - Max 2MB</small>
        </div>

        <div style="display: flex; gap: 1rem; margin-top: 2rem;">
            <button type="submit" class="btn btn-primary">
                <i class="fas fa-save"></i> Save Supplement
            </button>
            <a href="{{ path('app_supplement_index') }}" class="btn btn-secondary">
                <i class="fas fa-times"></i> Cancel
            </a>
        </div>

        {{ form_end(form) }}
    </div>
</div>
{% endblock %}

{% block javascripts %}
<script src="/js/supplement-validation.js"></script>
{% endblock %}
"@

$newTemplate | Out-File -FilePath "templates\supplement\new.html.twig" -Encoding UTF8 -NoNewline
Write-Host "      ✓ Form template created" -ForegroundColor Gray

# Step 3: Create edit.html.twig
Write-Host "[3/5] Creating edit.html.twig..." -ForegroundColor Green
Remove-Item -Path "templates\supplement\edit.html.twig" -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 300

$editTemplate = @"
{% extends 'base.html.twig' %}

{% block title %}Edit Supplement - Fitopia Admin{% endblock %}
{% block page_title %}Edit Supplement{% endblock %}
{% block breadcrumb %}Edit{% endblock %}

{% block body %}
<div class="card">
    <div class="card-header">
        <h3 class="card-title">Edit: {{ supplement.name }}</h3>
    </div>
    <div class="card-body">
        {{ form_start(form) }}
        
        <div class="form-row">
            <div class="form-group">
                <label class="form-label">Name</label>
                {{ form_widget(form.name, {'attr': {'class': 'form-control'}}) }}
            </div>
            <div class="form-group">
                <label class="form-label">Category</label>
                {{ form_widget(form.category, {'attr': {'class': 'form-control'}}) }}
            </div>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label class="form-label">Brand</label>
                {{ form_widget(form.brand, {'attr': {'class': 'form-control'}}) }}
            </div>
            <div class="form-group">
                <label class="form-label">Price</label>
                {{ form_widget(form.price, {'attr': {'class': 'form-control'}}) }}
            </div>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label class="form-label">Stock</label>
                {{ form_widget(form.stock, {'attr': {'class': 'form-control'}}) }}
            </div>
            <div class="form-group">
                <label class="form-label">Calories</label>
                {{ form_widget(form.calories, {'attr': {'class': 'form-control'}}) }}
            </div>
        </div>

        <div class="form-group">
            <label class="form-label">Description</label>
            {{ form_widget(form.description, {'attr': {'class': 'form-control'}}) }}
        </div>

        <div class="form-group">
            <label class="form-label">Image</label>
            {% if supplement.image %}
                <div style="margin-bottom: 1rem;">
                    <img src="/uploads/supplements/{{ supplement.image }}" alt="{{ supplement.name }}" style="width: 120px; height: 120px; object-fit: cover; border-radius: 0.5rem; border: 2px solid #e2e8f0;">
                </div>
            {% endif %}
            {{ form_widget(form.imageFile, {'attr': {'class': 'form-control'}}) }}
            <small class="form-text">Leave empty to keep current image</small>
        </div>

        <div style="display: flex; gap: 1rem; margin-top: 2rem;">
            <button type="submit" class="btn btn-primary">
                <i class="fas fa-save"></i> Update
            </button>
            <a href="{{ path('app_supplement_show', {'id': supplement.id}) }}" class="btn btn-secondary">
                <i class="fas fa-times"></i> Cancel
            </a>
        </div>

        {{ form_end(form) }}
    </div>
</div>
{% endblock %}

{% block javascripts %}
<script src="/js/supplement-validation.js"></script>
{% endblock %}
"@

$editTemplate | Out-File -FilePath "templates\supplement\edit.html.twig" -Encoding UTF8 -NoNewline
Write-Host "      ✓ Edit template created" -ForegroundColor Gray

# Step 4: Clear cache
Write-Host "[4/5] Clearing cache..." -ForegroundColor Green
php bin/console cache:clear 2>&1 | Out-Null
Write-Host "      ✓ Cache cleared" -ForegroundColor Gray

# Step 5: Verify files
Write-Host "[5/5] Verifying setup..." -ForegroundColor Green
$allGood = $true

if (!(Test-Path "public\index.php")) {
    Write-Host "      ✗ public/index.php missing!" -ForegroundColor Red
    $allGood = $false
} else {
    Write-Host "      ✓ public/index.php exists" -ForegroundColor Gray
}

if (!(Test-Path "config\packages\framework.yaml")) {
    Write-Host "      ✗ framework.yaml missing!" -ForegroundColor Red
    $allGood = $false
} else {
    Write-Host "      ✓ framework.yaml exists" -ForegroundColor Gray
}

if (!(Test-Path "templates\supplement\new.html.twig")) {
    Write-Host "      ✗ new.html.twig missing!" -ForegroundColor Red
    $allGood = $false
} else {
    Write-Host "      ✓ new.html.twig exists" -ForegroundColor Gray
}

if (!(Test-Path "templates\supplement\edit.html.twig")) {
    Write-Host "      ✗ edit.html.twig missing!" -ForegroundColor Red
    $allGood = $false
} else {
    Write-Host "      ✓ edit.html.twig exists" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

if ($allGood) {
    Write-Host "   ALL CHECKS PASSED! ✓" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Starting server on http://localhost:8000..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Gray
    Write-Host ""
    
    # Start the server
    php -S localhost:8000 -t public
} else {
    Write-Host "   ERRORS FOUND!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Please check the errors above." -ForegroundColor Yellow
}

