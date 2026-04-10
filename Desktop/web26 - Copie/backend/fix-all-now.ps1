Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FITOPIA - COMPLETE FIX" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Fix framework.yaml
Write-Host "[1/4] Fixing framework.yaml..." -ForegroundColor Green
Remove-Item -Path "config\packages\framework.yaml" -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 500

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
Write-Host "   ✓ framework.yaml created" -ForegroundColor Gray

# Step 2: Create new.html.twig
Write-Host "[2/4] Creating new.html.twig..." -ForegroundColor Green
Remove-Item -Path "templates\supplement\new.html.twig" -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 500

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
                <div class="error-message" id="name-error"></div>
            </div>
            <div class="form-group">
                <label class="form-label required">Category</label>
                {{ form_widget(form.category, {'attr': {'class': 'form-control'}}) }}
                <div class="error-message" id="category-error"></div>
            </div>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label class="form-label required">Brand</label>
                {{ form_widget(form.brand, {'attr': {'class': 'form-control'}}) }}
                <div class="error-message" id="brand-error"></div>
            </div>
            <div class="form-group">
                <label class="form-label required">Price (`$)</label>
                {{ form_widget(form.price, {'attr': {'class': 'form-control', 'step': '0.01'}}) }}
                <div class="error-message" id="price-error"></div>
            </div>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label class="form-label required">Stock</label>
                {{ form_widget(form.stock, {'attr': {'class': 'form-control'}}) }}
                <div class="error-message" id="stock-error"></div>
            </div>
            <div class="form-group">
                <label class="form-label">Calories</label>
                {{ form_widget(form.calories, {'attr': {'class': 'form-control'}}) }}
                <div class="error-message" id="calories-error"></div>
            </div>
        </div>

        <div class="form-group">
            <label class="form-label">Description</label>
            {{ form_widget(form.description, {'attr': {'class': 'form-control', 'rows': '4'}}) }}
            <div class="error-message" id="description-error"></div>
        </div>

        <div class="form-group">
            <label class="form-label">Product Image</label>
            {{ form_widget(form.imageFile, {'attr': {'class': 'form-control'}}) }}
            <div class="error-message" id="image-error"></div>
            <small class="form-text">Allowed formats: JPG, PNG, WEBP. Max size: 2MB</small>
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
Write-Host "   ✓ new.html.twig created" -ForegroundColor Gray

# Step 3: Create edit.html.twig
Write-Host "[3/4] Creating edit.html.twig..." -ForegroundColor Green
Remove-Item -Path "templates\supplement\edit.html.twig" -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 500

$editTemplate = @"
{% extends 'base.html.twig' %}

{% block title %}Edit {{ supplement.name }} - Fitopia Admin{% endblock %}
{% block page_title %}Edit Supplement{% endblock %}
{% block breadcrumb %}Edit Supplement{% endblock %}

{% block body %}
<div class="card">
    <div class="card-header">
        <h3 class="card-title">Edit: {{ supplement.name }}</h3>
    </div>
    <div class="card-body">
        {{ form_start(form, {'attr': {'id': 'supplementForm', 'novalidate': 'novalidate'}}) }}
        
        <div class="form-row">
            <div class="form-group">
                <label class="form-label required">Name</label>
                {{ form_widget(form.name, {'attr': {'class': 'form-control'}}) }}
                <div class="error-message" id="name-error"></div>
            </div>
            <div class="form-group">
                <label class="form-label required">Category</label>
                {{ form_widget(form.category, {'attr': {'class': 'form-control'}}) }}
                <div class="error-message" id="category-error"></div>
            </div>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label class="form-label required">Brand</label>
                {{ form_widget(form.brand, {'attr': {'class': 'form-control'}}) }}
                <div class="error-message" id="brand-error"></div>
            </div>
            <div class="form-group">
                <label class="form-label required">Price (`$)</label>
                {{ form_widget(form.price, {'attr': {'class': 'form-control', 'step': '0.01'}}) }}
                <div class="error-message" id="price-error"></div>
            </div>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label class="form-label required">Stock</label>
                {{ form_widget(form.stock, {'attr': {'class': 'form-control'}}) }}
                <div class="error-message" id="stock-error"></div>
            </div>
            <div class="form-group">
                <label class="form-label">Calories</label>
                {{ form_widget(form.calories, {'attr': {'class': 'form-control'}}) }}
                <div class="error-message" id="calories-error"></div>
            </div>
        </div>

        <div class="form-group">
            <label class="form-label">Description</label>
            {{ form_widget(form.description, {'attr': {'class': 'form-control', 'rows': '4'}}) }}
            <div class="error-message" id="description-error"></div>
        </div>

        <div class="form-group">
            <label class="form-label">Product Image</label>
            {% if supplement.image %}
                <div style="margin-bottom: 1rem;">
                    <p style="font-size: 13px; color: #64748b; margin-bottom: 0.5rem;">Current image:</p>
                    <img src="/uploads/supplements/{{ supplement.image }}" alt="{{ supplement.name }}" style="width: 120px; height: 120px; object-fit: cover; border-radius: 0.5rem; border: 2px solid #e2e8f0;">
                </div>
            {% endif %}
            {{ form_widget(form.imageFile, {'attr': {'class': 'form-control'}}) }}
            <div class="error-message" id="image-error"></div>
            <small class="form-text">Leave empty to keep current image</small>
        </div>

        <div style="display: flex; gap: 1rem; margin-top: 2rem;">
            <button type="submit" class="btn btn-primary">
                <i class="fas fa-save"></i> Update Supplement
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
Write-Host "   ✓ edit.html.twig created" -ForegroundColor Gray

# Step 4: Clear cache
Write-Host "[4/4] Clearing Symfony cache..." -ForegroundColor Green
php bin/console cache:clear
Write-Host "   ✓ Cache cleared" -ForegroundColor Gray

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ALL FIXED! ✓" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Restart your server: php -S localhost:8000 -t public" -ForegroundColor White
Write-Host "  2. Open: http://localhost:8000/supplement" -ForegroundColor White
Write-Host ""

