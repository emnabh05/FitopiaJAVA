# Delete old templates if they exist
Remove-Item -Path "templates\supplement\new.html.twig" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "templates\supplement\edit.html.twig" -Force -ErrorAction SilentlyContinue

Write-Host "Creating new.html.twig..." -ForegroundColor Green

# Create new.html.twig
@"
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
"@ | Out-File -FilePath "templates\supplement\new.html.twig" -Encoding UTF8

Write-Host "Creating edit.html.twig..." -ForegroundColor Green

# Create edit.html.twig
@"
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
"@ | Out-File -FilePath "templates\supplement\edit.html.twig" -Encoding UTF8

Write-Host ""
Write-Host "Templates created successfully!" -ForegroundColor Green
Write-Host "Clearing Symfony cache..." -ForegroundColor Yellow
php bin/console cache:clear

Write-Host ""
Write-Host "Done! Restart your server and try again." -ForegroundColor Green
Write-Host ""

