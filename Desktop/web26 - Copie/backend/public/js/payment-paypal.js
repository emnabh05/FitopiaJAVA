// PayPal Payment Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    loadOrderSummary();
    setupPaymentForm();
});

// Load order summary
function loadOrderSummary() {
    const orderSummary = JSON.parse(localStorage.getItem('orderSummary') || '{}');
    const cart = JSON.parse(localStorage.getItem('fitopiaCart') || '[]');
    
    if (cart.length === 0) {
        window.location.href = 'cart.html';
        return;
    }
    
    // Render order items
    const orderItemsContainer = document.getElementById('orderItems');
    orderItemsContainer.innerHTML = cart.map(item => `
        <div class="order-item">
            <div class="order-item-image">
                <img src="${item.image || 'images/image_1.jpg'}" alt="${item.name}" onerror="this.src='images/image_1.jpg'">
            </div>
            <div class="order-item-details">
                <div class="order-item-name">${item.name}</div>
                <div class="order-item-quantity">Quantité: ${item.quantity}</div>
            </div>
            <div class="order-item-price">
                ${(parseFloat(item.price) * item.quantity).toFixed(2)} DT
            </div>
        </div>
    `).join('');
    
    // Update summary totals
    document.getElementById('summarySubtotal').textContent = orderSummary.subtotal.toFixed(2) + ' DT';
    document.getElementById('summaryShipping').textContent = orderSummary.shipping === 0 ? 'GRATUIT' : orderSummary.shipping.toFixed(2) + ' DT';
    document.getElementById('summaryTotal').textContent = orderSummary.total.toFixed(2) + ' DT';
    document.getElementById('paypalAmount').textContent = orderSummary.total.toFixed(2) + ' DT';
    
    if (orderSummary.discount > 0) {
        document.getElementById('summaryDiscountRow').style.display = 'flex';
        document.getElementById('summaryDiscount').textContent = `-${orderSummary.discount.toFixed(2)} DT`;
    }
}

// Setup payment form
function setupPaymentForm() {
    const form = document.getElementById('paypalForm');
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // Validate form
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }
        
        // Validate email
        const email = document.getElementById('paypalEmail').value;
        if (!validateEmail(email)) {
            alert('Email invalide');
            return;
        }
        
        // Process payment
        processPayment();
    });
}

// Validate email
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// Process payment
function processPayment() {
    // Show loading
    const submitBtn = document.querySelector('.submit-payment-btn');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Connexion à PayPal...';
    submitBtn.disabled = true;
    
    // Simulate PayPal authentication and payment
    setTimeout(() => {
        const orderSummary = JSON.parse(localStorage.getItem('orderSummary') || '{}');
        const customerInfo = JSON.parse(localStorage.getItem('customerInfo') || '{}');
        const cart = JSON.parse(localStorage.getItem('fitopiaCart') || '[]');
        
        const orderData = {
            customer: customerInfo,
            items: cart,
            summary: orderSummary,
            orderDate: new Date().toISOString(),
            orderNumber: 'FIT-' + Date.now(),
            status: 'paid',
            paymentMethod: 'PayPal'
        };
        
        // Save order
        localStorage.setItem('lastOrder', JSON.stringify(orderData));
        
        // Clear cart
        localStorage.removeItem('fitopiaCart');
        localStorage.removeItem('orderSummary');
        localStorage.removeItem('appliedDiscount');
        localStorage.removeItem('customerInfo');
        
        // Redirect to success page
        window.location.href = 'order-success.html';
    }, 2000);
}

