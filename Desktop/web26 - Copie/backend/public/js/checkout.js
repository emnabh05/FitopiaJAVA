// Checkout Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    loadOrderSummary();
    setupCheckoutForm();
});

// Load order summary from localStorage
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
    
    if (orderSummary.discount > 0) {
        document.getElementById('summaryDiscountRow').style.display = 'flex';
        document.getElementById('summaryDiscount').textContent = `-${orderSummary.discount.toFixed(2)} DT`;
    }
}

// Setup checkout form
function setupCheckoutForm() {
    const form = document.getElementById('checkoutForm');
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // Validate form
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }
        
        // Get form data
        const formData = {
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            address: document.getElementById('address').value,
            city: document.getElementById('city').value,
            postalCode: document.getElementById('postalCode').value,
            notes: document.getElementById('notes').value,
            paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value
        };
        
        // Save customer info to localStorage
        localStorage.setItem('customerInfo', JSON.stringify(formData));
        
        // Route to appropriate payment page
        routeToPayment(formData.paymentMethod);
    });
}

// Route to payment page based on payment method
function routeToPayment(paymentMethod) {
    switch(paymentMethod) {
        case 'visa':
        case 'mastercard':
            window.location.href = 'payment-visa.html';
            break;
        case 'paypal':
            window.location.href = 'payment-paypal.html';
            break;
        case 'cod':
            // Cash on delivery - go directly to order processing
            processOrder();
            break;
        default:
            alert('Méthode de paiement invalide');
    }
}

// Process order (for cash on delivery)
function processOrder() {
    const orderSummary = JSON.parse(localStorage.getItem('orderSummary') || '{}');
    const customerInfo = JSON.parse(localStorage.getItem('customerInfo') || '{}');
    const cart = JSON.parse(localStorage.getItem('fitopiaCart') || '[]');
    
    const orderData = {
        customer: customerInfo,
        items: cart,
        summary: orderSummary,
        orderDate: new Date().toISOString(),
        orderNumber: 'FIT-' + Date.now(),
        status: 'pending'
    };
    
    // Save order to localStorage (will be sent to backend later)
    localStorage.setItem('lastOrder', JSON.stringify(orderData));
    
    // Clear cart
    localStorage.removeItem('fitopiaCart');
    localStorage.removeItem('orderSummary');
    localStorage.removeItem('appliedDiscount');
    
    // Redirect to success page
    window.location.href = 'order-success.html';
}

