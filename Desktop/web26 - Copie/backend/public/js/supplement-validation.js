/**
 * Supplement Form Validation
 * All validation is handled client-side with JavaScript
 * Symfony only handles data persistence and file uploads
 */

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('supplementForm');
    if (!form) return;

    const fields = {
        name: document.getElementById('supplement_name'),
        category: document.getElementById('supplement_category'),
        brand: document.getElementById('supplement_brand'),
        price: document.getElementById('supplement_price'),
        stock: document.getElementById('supplement_stock'),
        calories: document.getElementById('supplement_calories'),
        description: document.getElementById('supplement_description'),
        imageFile: document.getElementById('supplement_imageFile')
    };

    const errors = {
        name: document.getElementById('name-error'),
        category: document.getElementById('category-error'),
        brand: document.getElementById('brand-error'),
        price: document.getElementById('price-error'),
        stock: document.getElementById('stock-error'),
        calories: document.getElementById('calories-error'),
        description: document.getElementById('description-error'),
        image: document.getElementById('image-error')
    };

    function showError(field, message) {
        const errorElement = errors[field];
        const fieldElement = fields[field];
        if (errorElement && fieldElement) {
            errorElement.textContent = message;
            errorElement.classList.add('show');
            fieldElement.classList.add('error');
        }
    }

    function clearError(field) {
        const errorElement = errors[field];
        const fieldElement = fields[field];
        if (errorElement && fieldElement) {
            errorElement.textContent = '';
            errorElement.classList.remove('show');
            fieldElement.classList.remove('error');
        }
    }

    function validateField(fieldName) {
        clearError(fieldName);
        let value = fieldName === 'imageFile' ? fields[fieldName].files[0] : fields[fieldName].value;
        let errorMessage = null;

        switch(fieldName) {
            case 'name':
                if (!value || value.trim() === '') errorMessage = 'Product name is required';
                else if (value.length < 3) errorMessage = 'Product name must be at least 3 characters';
                else if (value.length > 255) errorMessage = 'Product name must not exceed 255 characters';
                else if (/[0-9]/.test(value)) errorMessage = 'Product name should not contain numbers';
                else if (/[^a-zA-Z\s\-']/.test(value)) errorMessage = 'Product name contains invalid characters';
                break;

            case 'category':
                if (!value || value.trim() === '') errorMessage = 'Category is required';
                else if (!/^[a-zA-Z\s]+$/.test(value)) errorMessage = 'Category must contain only letters';
                else if (value.length > 100) errorMessage = 'Category must not exceed 100 characters';
                break;

            case 'brand':
                if (!value || value.trim() === '') errorMessage = 'Brand is required';
                else if (!/^[a-zA-Z\s]+$/.test(value)) errorMessage = 'Brand must contain only letters';
                else if (value.length > 100) errorMessage = 'Brand must not exceed 100 characters';
                break;

            case 'price':
                if (!value || value.trim() === '') errorMessage = 'Price is required';
                else {
                    const numValue = parseFloat(value);
                    if (isNaN(numValue)) errorMessage = 'Price must be a valid number';
                    else if (numValue <= 0) errorMessage = 'Price must be greater than 0';
                    else if (!/^\d+(\.\d{1,2})?$/.test(value)) errorMessage = 'Price must have maximum 2 decimal places';
                    else if (numValue > 9999999.99) errorMessage = 'Price is too large';
                }
                break;

            case 'stock':
                if (!value || value.trim() === '') errorMessage = 'Stock quantity is required';
                else {
                    const numValue = parseInt(value);
                    if (isNaN(numValue)) errorMessage = 'Stock must be a valid integer';
                    else if (numValue < 0) errorMessage = 'Stock cannot be negative';
                    else if (!Number.isInteger(parseFloat(value))) errorMessage = 'Stock must be a whole number';
                }
                break;

            case 'calories':
                if (value && value.trim() !== '') {
                    const numValue = parseInt(value);
                    if (isNaN(numValue)) errorMessage = 'Calories must be a valid integer';
                    else if (numValue < 0) errorMessage = 'Calories cannot be negative';
                    else if (!Number.isInteger(parseFloat(value))) errorMessage = 'Calories must be a whole number';
                }
                break;

            case 'description':
                if (!value || value.trim() === '') errorMessage = 'Description is required';
                else if (value.length < 10) errorMessage = 'Description must be at least 10 characters';
                else if (value.length > 5000) errorMessage = 'Description must not exceed 5000 characters';
                break;

            case 'imageFile':
                if (value) {
                    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
                    const maxSize = 5 * 1024 * 1024;
                    if (!allowedTypes.includes(value.type)) errorMessage = 'Only JPG, PNG, and WEBP images are allowed';
                    else if (value.size > maxSize) errorMessage = 'Image size must not exceed 5MB';
                }
                break;
        }

        if (errorMessage) {
            showError(fieldName, errorMessage);
            return false;
        }
        return true;
    }

    // Real-time validation
    Object.keys(fields).forEach(fieldName => {
        if (fields[fieldName]) {
            fields[fieldName].addEventListener('blur', () => validateField(fieldName));
            fields[fieldName].addEventListener('input', () => {
                if (errors[fieldName].classList.contains('show')) {
                    validateField(fieldName);
                }
            });
        }
    });

    // Form submission
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        let isValid = true;
        ['name', 'category', 'brand', 'price', 'stock', 'calories', 'description', 'imageFile'].forEach(field => {
            if (!validateField(field)) isValid = false;
        });

        if (isValid) {
            form.submit();
        } else {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    });
});

