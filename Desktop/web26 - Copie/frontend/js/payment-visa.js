// Visa/Mastercard Payment Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    loadOrderSummary();
    setupPaymentForm();
    formatCardInputs();
});

// Load order summary
function loadOrderSummary() {
    const orderSummary = JSON.parse(localStorage.getItem('orderSummary') || '{}');
    const cart = JSON.parse(localStorage.getItem('fitopiaCart') || '[]');
    
    if (cart.length === 0) {
        window.location.href = 'shop.html?cart=open';
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
    document.getElementById('paymentAmount').textContent = orderSummary.total.toFixed(2) + ' DT';
    
    if (orderSummary.discount > 0) {
        document.getElementById('summaryDiscountRow').style.display = 'flex';
        document.getElementById('summaryDiscount').textContent = `-${orderSummary.discount.toFixed(2)} DT`;
    }
}

// Setup payment form
function setupPaymentForm() {
    const form = document.getElementById('paymentForm');
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // Validate form
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }
        
        // Validate card number
        const cardNumber = document.getElementById('cardNumber').value.replace(/\s/g, '');
        if (cardNumber.length !== 16) {
            alert('Numéro de carte invalide');
            return;
        }
        
        // Validate expiry date
        const expiryDate = document.getElementById('expiryDate').value;
        if (!validateExpiryDate(expiryDate)) {
            alert('Date d\'expiration invalide');
            return;
        }
        
        // Validate CVV
        const cvv = document.getElementById('cvv').value;
        if (cvv.length !== 3) {
            alert('CVV invalide');
            return;
        }
        
        // Process payment
        processPayment();
    });
}

// Format card inputs
function formatCardInputs() {
    // Format card number
    const cardNumberInput = document.getElementById('cardNumber');
    cardNumberInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\s/g, '');
        value = value.replace(/\D/g, '');
        value = value.substring(0, 16);
        
        // Add spaces every 4 digits
        let formattedValue = '';
        for (let i = 0; i < value.length; i++) {
            if (i > 0 && i % 4 === 0) {
                formattedValue += ' ';
            }
            formattedValue += value[i];
        }
        
        e.target.value = formattedValue;
    });
    
    // Format expiry date
    const expiryInput = document.getElementById('expiryDate');
    expiryInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        
        if (value.length >= 2) {
            value = value.substring(0, 2) + '/' + value.substring(2, 4);
        }
        
        e.target.value = value;
    });
    
    // Format CVV (numbers only)
    const cvvInput = document.getElementById('cvv');
    cvvInput.addEventListener('input', function(e) {
        e.target.value = e.target.value.replace(/\D/g, '').substring(0, 3);
    });
}

// Validate expiry date
function validateExpiryDate(expiryDate) {
    const parts = expiryDate.split('/');
    if (parts.length !== 2) return false;
    
    const month = parseInt(parts[0]);
    const year = parseInt('20' + parts[1]);
    
    if (month < 1 || month > 12) return false;
    
    const now = new Date();
    const expiry = new Date(year, month - 1);
    
    return expiry > now;
}

// Process payment
function processPayment() {
    // Show loading
    const submitBtn = document.querySelector('.submit-payment-btn');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Traitement...';
    submitBtn.disabled = true;
    
    // Simulate payment processing
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
            paymentMethod: 'Carte bancaire'
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
