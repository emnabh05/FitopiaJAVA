// Shop functionality
const API_BASE_URL = 'http://localhost:8000/api';

document.addEventListener('DOMContentLoaded', function() {
    // Load products from backend
    loadProducts();

    // Load categories and brands
    loadCategories();
    loadBrands();

    // Initialize filters
    initializeFilters();

    // Initialize cart
    initializeCart();
});

// Load products from Symfony backend
async function loadProducts(filters = {}) {
    try {
        // Build query string
        const params = new URLSearchParams();
        if (filters.category) params.append('category', filters.category);
        if (filters.brand) params.append('brand', filters.brand);
        if (filters.maxPrice) params.append('maxPrice', filters.maxPrice);

        const url = `${API_BASE_URL}/supplements${params.toString() ? '?' + params.toString() : ''}`;

        // Fetch products from backend API
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error('Failed to fetch products');
        }

        const products = await response.json();

        // Render products
        renderProducts(products);

        // Update results count
        updateResultsCount(products.length);
    } catch (error) {
        console.error('Error loading products:', error);
        // Show error message
        showError('Unable to load products from backend. Make sure the Symfony server is running on http://localhost:8000');
    }
}

// Load categories from backend
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE_URL}/categories`);
        const categories = await response.json();

        const container = document.querySelector('.filter-options');
        if (container && categories.length > 0) {
            // Clear existing checkboxes
            const categoryFilters = container.parentElement;
            if (categoryFilters.querySelector('h3').textContent === 'Catégories') {
                container.innerHTML = '';

                categories.forEach(cat => {
                    const label = document.createElement('label');
                    label.innerHTML = `
                        <input type="checkbox" name="category" value="${cat.name}">
                        ${cat.name} <span>(${cat.count})</span>
                    `;
                    container.appendChild(label);
                });

                // Re-initialize filters
                initializeFilters();
            }
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Load brands from backend
async function loadBrands() {
    try {
        const response = await fetch(`${API_BASE_URL}/brands`);
        const brands = await response.json();

        const filterGroups = document.querySelectorAll('.filter-group');
        filterGroups.forEach(group => {
            if (group.querySelector('h3').textContent === 'Marques') {
                const container = group.querySelector('.filter-options');
                if (container && brands.length > 0) {
                    container.innerHTML = '';

                    brands.forEach(brand => {
                        const label = document.createElement('label');
                        label.innerHTML = `
                            <input type="checkbox" name="brand" value="${brand.name}">
                            ${brand.name} <span>(${brand.count})</span>
                        `;
                        container.appendChild(label);
                    });

                    // Re-initialize filters
                    initializeFilters();
                }
            }
        });
    } catch (error) {
        console.error('Error loading brands:', error);
    }
}

// Update results count
function updateResultsCount(count) {
    const resultsCount = document.querySelector('.results-count');
    if (resultsCount) {
        const showing = Math.min(12, count);
        resultsCount.innerHTML = `Affichage de <strong>1-${showing}</strong> sur <strong>${count}</strong> résultats`;
    }
}

// Show error message
function showError(message) {
    const grid = document.getElementById('productsGrid');
    grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px;">
            <i class="fas fa-exclamation-triangle" style="font-size: 48px; color: #de2a2a; margin-bottom: 20px;"></i>
            <h3 style="color: #282828; margin-bottom: 10px;">Erreur de chargement</h3>
            <p style="color: #666;">${message}</p>
            <button onclick="location.reload()" style="margin-top: 20px; padding: 12px 30px; background: #1639CA; color: white; border: none; border-radius: 30px; cursor: pointer; font-weight: 600;">
                Réessayer
            </button>
        </div>
    `;
}

// Render products to grid
function renderProducts(products) {
    const grid = document.getElementById('productsGrid');
    grid.innerHTML = '';
    
    products.forEach(product => {
        const productCard = createProductCard(product);
        grid.appendChild(productCard);
    });
}

// Create product card HTML
function createProductCard(product) {
    const card = document.createElement('div');
    card.className = 'product-card';

    const hasDiscount = product.oldPrice && product.oldPrice > product.price;
    const isNew = product.isNew || false;
    const inStock = product.stock > 0;

    // Use placeholder if no image
    const imageUrl = product.image || 'images/image_1.jpg';

    card.innerHTML = `
        <div class="product-image">
            ${hasDiscount ? '<span class="product-badge">SALE</span>' : ''}
            ${isNew && !hasDiscount ? '<span class="product-badge new">NEW</span>' : ''}
            <img src="${imageUrl}" alt="${product.name}" onerror="this.src='images/image_1.jpg'">
        </div>
        <div class="product-info">
            <div class="product-category">${product.category || 'Supplement'}</div>
            <h3 class="product-title">${product.name}</h3>
            <div class="product-price">
                <span class="price-current">${parseFloat(product.price).toFixed(2)} DT</span>
                ${product.oldPrice ? `<span class="price-old">${parseFloat(product.oldPrice).toFixed(2)} DT</span>` : ''}
            </div>
            <div class="product-stock ${inStock ? 'in-stock' : 'out-of-stock'}">
                ${inStock ? `✓ En stock (${product.stock})` : '✗ Rupture de stock'}
            </div>
            <button class="add-to-cart-btn"
                    data-id="${product.id}"
                    data-name="${product.name}"
                    data-price="${product.price}"
                    data-image="${imageUrl}"
                    ${!inStock ? 'disabled' : ''}>
                <i class="fas fa-shopping-cart"></i> ${inStock ? 'Ajouter au panier' : 'Indisponible'}
            </button>
        </div>
    `;

    // Add to cart event
    const addBtn = card.querySelector('.add-to-cart-btn');
    if (inStock) {
        addBtn.addEventListener('click', () => addToCart(product));
    }

    return card;
}



// Initialize filters
function initializeFilters() {
    const checkboxes = document.querySelectorAll('.filter-options input[type="checkbox"]');
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', applyFilters);
    });
    
    const priceSlider = document.querySelector('.price-slider');
    if (priceSlider) {
        priceSlider.addEventListener('input', applyFilters);
    }
}

// Apply filters
function applyFilters() {
    // Get selected categories
    const selectedCategories = Array.from(document.querySelectorAll('input[name="category"]:checked'))
        .map(cb => cb.value);

    // Get selected brands
    const selectedBrands = Array.from(document.querySelectorAll('input[name="brand"]:checked'))
        .map(cb => cb.value);

    // Get price range
    const maxPrice = document.querySelector('.price-slider')?.value || 500;

    // Build filters object
    const filters = {};

    // For now, we can only filter by one category and one brand at a time
    // You can enhance the backend to support multiple values
    if (selectedCategories.length > 0) {
        filters.category = selectedCategories[0];
    }
    if (selectedBrands.length > 0) {
        filters.brand = selectedBrands[0];
    }
    if (maxPrice < 500) {
        filters.maxPrice = maxPrice;
    }

    // Reload products with filters
    loadProducts(filters);
}

// Initialize cart
function initializeCart() {
    updateCartCount();
}

// Add product to cart
function addToCart(product) {
    // Get cart from localStorage
    let cart = JSON.parse(localStorage.getItem('fitopiaCart') || '[]');

    // Check if product already in cart
    const existingItem = cart.find(item => item.id === product.id);

    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            id: product.id,
            name: product.name,
            category: product.category,
            price: product.price,
            image: product.image,
            quantity: 1
        });
    }

    // Save cart
    localStorage.setItem('fitopiaCart', JSON.stringify(cart));

    // Update cart count
    updateCartCount();

    // Refresh drawer if present
    if (typeof window.refreshCartDrawer === 'function') {
        window.refreshCartDrawer();
    }

    // Show success message
    showNotification(`${product.name} ajouté au panier!`);
}

// Update cart count badge
function updateCartCount() {
    const cart = JSON.parse(localStorage.getItem('fitopiaCart') || '[]');
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);

    const cartCountElement = document.querySelector('.cart-count');
    if (cartCountElement) {
        cartCountElement.textContent = totalItems;
    }
}

// Show notification
function showNotification(message) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.innerHTML = `
        <i class="fas fa-check-circle"></i>
        <span>${message}</span>
    `;

    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 100px;
        right: 20px;
        background: #2e9e7b;
        color: white;
        padding: 15px 25px;
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0,0,0,0.2);
        z-index: 10000;
        display: flex;
        align-items: center;
        gap: 10px;
        font-weight: 600;
        animation: slideIn 0.3s ease;
    `;

    document.body.appendChild(notification);

    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);
