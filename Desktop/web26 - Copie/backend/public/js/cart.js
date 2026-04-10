// Cart Management
const DISCOUNT_CODES = {
    'ali123': { percent: 10, description: '10% de réduction' }
};

const SHIPPING_COST = 7.00;
const FREE_SHIPPING_THRESHOLD = 100.00;

let appliedDiscount = null;

document.addEventListener('DOMContentLoaded', function() {
    loadCart();
    setupCartEventListeners();
});

// Load cart from localStorage
function loadCart() {
    const cart = getCart();
    
    if (cart.length === 0) {
        showEmptyCart();
    } else {
        renderCartItems(cart);
        updateCartSummary();
    }
    
    updateCartCount();
}

// Get cart from localStorage
function getCart() {
    return JSON.parse(localStorage.getItem('fitopiaCart') || '[]');
}

// Save cart to localStorage
function saveCart(cart) {
    localStorage.setItem('fitopiaCart', JSON.stringify(cart));
    updateCartCount();
}

// Render cart items
function renderCartItems(cart) {
    const cartItemsContainer = document.getElementById('cartItems');
    const emptyCart = document.getElementById('emptyCart');
    
    if (cart.length === 0) {
        cartItemsContainer.style.display = 'none';
        emptyCart.style.display = 'block';
        return;
    }
    
    cartItemsContainer.style.display = 'flex';
    emptyCart.style.display = 'none';
    
    cartItemsContainer.innerHTML = cart.map(item => `
        <div class="cart-item" data-id="${item.id}">
            <div class="cart-item-image">
                <img src="${item.image || 'images/image_1.jpg'}" alt="${item.name}" onerror="this.src='images/image_1.jpg'">
            </div>
            <div class="cart-item-details">
                <h3 class="cart-item-name">${item.name}</h3>
                <p class="cart-item-category">${item.category || 'Supplement'}</p>
                <p class="cart-item-price">${parseFloat(item.price).toFixed(2)} DT</p>
            </div>
            <div class="cart-item-actions">
                <div class="quantity-controls">
                    <button class="quantity-btn decrease-btn" data-id="${item.id}">
                        <i class="fas fa-minus"></i>
                    </button>
                    <span class="quantity-value">${item.quantity}</span>
                    <button class="quantity-btn increase-btn" data-id="${item.id}">
                        <i class="fas fa-plus"></i>
                    </button>
                </div>
                <button class="remove-item-btn" data-id="${item.id}">
                    <i class="fas fa-trash"></i> Supprimer
                </button>
                <div class="cart-item-total">
                    ${(parseFloat(item.price) * item.quantity).toFixed(2)} DT
                </div>
            </div>
        </div>
    `).join('');
    
    // Add event listeners
    document.querySelectorAll('.increase-btn').forEach(btn => {
        btn.addEventListener('click', () => updateQuantity(btn.dataset.id, 1));
    });
    
    document.querySelectorAll('.decrease-btn').forEach(btn => {
        btn.addEventListener('click', () => updateQuantity(btn.dataset.id, -1));
    });
    
    document.querySelectorAll('.remove-item-btn').forEach(btn => {
        btn.addEventListener('click', () => removeItem(btn.dataset.id));
    });
}

// Update quantity
function updateQuantity(productId, change) {
    const cart = getCart();
    const item = cart.find(i => i.id == productId);
    
    if (item) {
        item.quantity += change;
        
        if (item.quantity <= 0) {
            removeItem(productId);
            return;
        }
        
        saveCart(cart);
        loadCart();
    }
}

// Remove item from cart
function removeItem(productId) {
    const cart = getCart();
    const newCart = cart.filter(item => item.id != productId);
    
    saveCart(newCart);
    loadCart();
    
    showNotification('Produit retiré du panier', 'info');
}

// Show empty cart
function showEmptyCart() {
    document.getElementById('cartItems').style.display = 'none';
    document.getElementById('emptyCart').style.display = 'block';
    document.getElementById('checkoutBtn').disabled = true;
}

// Update cart summary
function updateCartSummary() {
    const cart = getCart();
    const subtotal = cart.reduce((sum, item) => sum + (parseFloat(item.price) * item.quantity), 0);
    
    // Calculate shipping
    const shipping = subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_COST;
    
    // Calculate discount
    let discountAmount = 0;
    if (appliedDiscount) {
        discountAmount = (subtotal * appliedDiscount.percent) / 100;
    }
    
    // Calculate total
    const total = subtotal + shipping - discountAmount;
    
    // Update UI
    document.getElementById('subtotal').textContent = subtotal.toFixed(2) + ' DT';
    document.getElementById('shipping').textContent = shipping === 0 ? 'GRATUIT' : shipping.toFixed(2) + ' DT';
    document.getElementById('total').textContent = total.toFixed(2) + ' DT';
    
    if (appliedDiscount) {
        document.getElementById('discountRow').style.display = 'flex';
        document.getElementById('discountPercent').textContent = `(${appliedDiscount.percent}%)`;
        document.getElementById('discountAmount').textContent = `-${discountAmount.toFixed(2)} DT`;
    } else {
        document.getElementById('discountRow').style.display = 'none';
    }
}

// Setup event listeners
function setupCartEventListeners() {
    // Apply discount code
    const applyDiscountBtn = document.getElementById('applyDiscount');
    const discountInput = document.getElementById('discountCode');

    if (applyDiscountBtn) {
        applyDiscountBtn.addEventListener('click', applyDiscountCode);
    }

    if (discountInput) {
        discountInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                applyDiscountCode();
            }
        });
    }

    // Checkout button
    const checkoutBtn = document.getElementById('checkoutBtn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', proceedToCheckout);
    }
}

// Apply discount code
function applyDiscountCode() {
    const code = document.getElementById('discountCode').value.trim().toLowerCase();
    const messageEl = document.getElementById('discountMessage');

    if (!code) {
        messageEl.textContent = 'Veuillez entrer un code promo';
        messageEl.className = 'discount-message error';
        return;
    }

    if (DISCOUNT_CODES[code]) {
        appliedDiscount = DISCOUNT_CODES[code];
        messageEl.textContent = `✓ Code appliqué: ${appliedDiscount.description}`;
        messageEl.className = 'discount-message success';
        updateCartSummary();

        // Save discount to localStorage
        localStorage.setItem('appliedDiscount', JSON.stringify(appliedDiscount));
    } else {
        messageEl.textContent = '✗ Code promo invalide';
        messageEl.className = 'discount-message error';
        appliedDiscount = null;
        localStorage.removeItem('appliedDiscount');
        updateCartSummary();
    }
}

// Proceed to checkout
function proceedToCheckout() {
    const cart = getCart();

    if (cart.length === 0) {
        showNotification('Votre panier est vide', 'error');
        return;
    }

    // Save cart summary to localStorage
    const subtotal = cart.reduce((sum, item) => sum + (parseFloat(item.price) * item.quantity), 0);
    const shipping = subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_COST;
    let discountAmount = 0;

    if (appliedDiscount) {
        discountAmount = (subtotal * appliedDiscount.percent) / 100;
    }

    const total = subtotal + shipping - discountAmount;

    const orderSummary = {
        subtotal,
        shipping,
        discount: discountAmount,
        discountCode: appliedDiscount ? document.getElementById('discountCode').value : null,
        total,
        items: cart
    };

    localStorage.setItem('orderSummary', JSON.stringify(orderSummary));

    // Redirect to checkout
    window.location.href = 'checkout.html';
}

// Update cart count in header
function updateCartCount() {
    const cart = getCart();
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);

    const cartCountElements = document.querySelectorAll('.cart-count');
    cartCountElements.forEach(el => {
        el.textContent = totalItems;
    });
}

// Show notification
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = 'notification';

    const icon = type === 'success' ? 'fa-check-circle' : 'fa-info-circle';
    const bgColor = type === 'success' ? '#2e9e7b' : '#1639CA';

    notification.innerHTML = `
        <i class="fas ${icon}"></i>
        <span>${message}</span>
    `;

    notification.style.cssText = `
        position: fixed;
        top: 100px;
        right: 20px;
        background: ${bgColor};
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

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Load applied discount from localStorage on page load
const savedDiscount = localStorage.getItem('appliedDiscount');
if (savedDiscount) {
    appliedDiscount = JSON.parse(savedDiscount);
    const discountInput = document.getElementById('discountCode');
    if (discountInput && appliedDiscount) {
        // Find the code key
        const codeKey = Object.keys(DISCOUNT_CODES).find(key =>
            DISCOUNT_CODES[key].percent === appliedDiscount.percent
        );
        if (codeKey) {
            discountInput.value = codeKey;
            document.getElementById('discountMessage').textContent = `✓ Code appliqué: ${appliedDiscount.description}`;
            document.getElementById('discountMessage').className = 'discount-message success';
        }
    }
}

