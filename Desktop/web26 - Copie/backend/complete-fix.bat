@echo off
echo ========================================
echo COMPLETE FIX - Recreating Templates
echo ========================================
echo.

echo Step 1: Deleting old templates...
del /F /Q templates\supplement\new.html.twig 2>nul
del /F /Q templates\supplement\edit.html.twig 2>nul
timeout /t 2 /nobreak >nul

echo Step 2: Creating new.html.twig...
(
echo ^{% extends 'base.html.twig' %%^}
echo.
echo ^{% block title %%^}Add New Supplement - Fitopia Admin^{% endblock %%^}
echo ^{% block page_title %%^}Add New Supplement^{% endblock %%^}
echo ^{% block breadcrumb %%^}New Supplement^{% endblock %%^}
echo.
echo ^{% block body %%^}
echo ^<div class="card"^>
echo     ^<div class="card-header"^>
echo         ^<h3 class="card-title"^>Supplement Information^</h3^>
echo     ^</div^>
echo     ^<div class="card-body"^>
echo         {{ form_start^(form, {'attr': {'id': 'supplementForm', 'novalidate': 'novalidate'}}^) }}
echo.        
echo         ^<div class="form-row"^>
echo             ^<div class="form-group"^>
echo                 ^<label class="form-label required"^>Name^</label^>
echo                 {{ form_widget^(form.name, {'attr': {'class': 'form-control'}}^) }}
echo             ^</div^>
echo             ^<div class="form-group"^>
echo                 ^<label class="form-label required"^>Category^</label^>
echo                 {{ form_widget^(form.category, {'attr': {'class': 'form-control'}}^) }}
echo             ^</div^>
echo         ^</div^>
echo.
echo         ^<div class="form-row"^>
echo             ^<div class="form-group"^>
echo                 ^<label class="form-label required"^>Brand^</label^>
echo                 {{ form_widget^(form.brand, {'attr': {'class': 'form-control'}}^) }}
echo             ^</div^>
echo             ^<div class="form-group"^>
echo                 ^<label class="form-label required"^>Price ^($^)^</label^>
echo                 {{ form_widget^(form.price, {'attr': {'class': 'form-control', 'step': '0.01'}}^) }}
echo             ^</div^>
echo         ^</div^>
echo.
echo         ^<div class="form-row"^>
echo             ^<div class="form-group"^>
echo                 ^<label class="form-label required"^>Stock^</label^>
echo                 {{ form_widget^(form.stock, {'attr': {'class': 'form-control'}}^) }}
echo             ^</div^>
echo             ^<div class="form-group"^>
echo                 ^<label class="form-label"^>Calories^</label^>
echo                 {{ form_widget^(form.calories, {'attr': {'class': 'form-control'}}^) }}
echo             ^</div^>
echo         ^</div^>
echo.
echo         ^<div class="form-group"^>
echo             ^<label class="form-label"^>Description^</label^>
echo             {{ form_widget^(form.description, {'attr': {'class': 'form-control', 'rows': '4'}}^) }}
echo         ^</div^>
echo.
echo         ^<div class="form-group"^>
echo             ^<label class="form-label"^>Product Image^</label^>
echo             {{ form_widget^(form.imageFile, {'attr': {'class': 'form-control'}}^) }}
echo         ^</div^>
echo.
echo         ^<div style="display: flex; gap: 1rem; margin-top: 2rem;"^>
echo             ^<button type="submit" class="btn btn-primary"^>^<i class="fas fa-save"^>^</i^> Save^</button^>
echo             ^<a href="{{ path^('app_supplement_index'^) }}" class="btn btn-secondary"^>Cancel^</a^>
echo         ^</div^>
echo.
echo         {{ form_end^(form^) }}
echo     ^</div^>
echo ^</div^>
echo ^{% endblock %%^}
echo.
echo ^{% block javascripts %%^}
echo ^<script src="/js/supplement-validation.js"^>^</script^>
echo ^{% endblock %%^}
) > templates\supplement\new.html.twig

echo Step 3: Creating edit.html.twig...
(
echo ^{% extends 'base.html.twig' %%^}
echo.
echo ^{% block title %%^}Edit Supplement^{% endblock %%^}
echo ^{% block page_title %%^}Edit Supplement^{% endblock %%^}
echo ^{% block breadcrumb %%^}Edit^{% endblock %%^}
echo.
echo ^{% block body %%^}
echo ^<div class="card"^>
echo     ^<div class="card-header"^>
echo         ^<h3 class="card-title"^>Edit Supplement^</h3^>
echo     ^</div^>
echo     ^<div class="card-body"^>
echo         {{ form_start^(form^) }}
echo         ^<div class="form-row"^>
echo             ^<div class="form-group"^>^<label class="form-label"^>Name^</label^>{{ form_widget^(form.name, {'attr': {'class': 'form-control'}}^) }}^</div^>
echo             ^<div class="form-group"^>^<label class="form-label"^>Category^</label^>{{ form_widget^(form.category, {'attr': {'class': 'form-control'}}^) }}^</div^>
echo         ^</div^>
echo         ^<div class="form-row"^>
echo             ^<div class="form-group"^>^<label class="form-label"^>Brand^</label^>{{ form_widget^(form.brand, {'attr': {'class': 'form-control'}}^) }}^</div^>
echo             ^<div class="form-group"^>^<label class="form-label"^>Price^</label^>{{ form_widget^(form.price, {'attr': {'class': 'form-control'}}^) }}^</div^>
echo         ^</div^>
echo         ^<div class="form-row"^>
echo             ^<div class="form-group"^>^<label class="form-label"^>Stock^</label^>{{ form_widget^(form.stock, {'attr': {'class': 'form-control'}}^) }}^</div^>
echo             ^<div class="form-group"^>^<label class="form-label"^>Calories^</label^>{{ form_widget^(form.calories, {'attr': {'class': 'form-control'}}^) }}^</div^>
echo         ^</div^>
echo         ^<div class="form-group"^>^<label class="form-label"^>Description^</label^>{{ form_widget^(form.description, {'attr': {'class': 'form-control'}}^) }}^</div^>
echo         ^<div class="form-group"^>^<label class="form-label"^>Image^</label^>{{ form_widget^(form.imageFile, {'attr': {'class': 'form-control'}}^) }}^</div^>
echo         ^<button type="submit" class="btn btn-primary"^>Update^</button^>
echo         ^<a href="{{ path^('app_supplement_index'^) }}" class="btn btn-secondary"^>Cancel^</a^>
echo         {{ form_end^(form^) }}
echo     ^</div^>
echo ^</div^>
echo ^{% endblock %%^}
) > templates\supplement\edit.html.twig

echo.
echo Step 4: Clearing cache...
php bin/console cache:clear

echo.
echo ========================================
echo DONE!
echo ========================================
echo.
echo Templates recreated successfully!
echo Restart your server and try again.
echo.
pause

