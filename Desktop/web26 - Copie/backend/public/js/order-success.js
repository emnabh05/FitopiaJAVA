// Order Success Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    loadOrderDetails();
});

// Load order details
function loadOrderDetails() {
    const order = JSON.parse(localStorage.getItem('lastOrder') || '{}');
    
    if (!order.orderNumber) {
        // No order found, redirect to shop
        window.location.href = 'shop.html';
        return;
    }
    
    // Display order number
    document.getElementById('orderNumber').textContent = order.orderNumber;
    
    // Display order date
    const orderDate = new Date(order.orderDate);
    document.getElementById('orderDate').textContent = formatDate(orderDate);
    
    // Display payment method
    const paymentMethodText = getPaymentMethodText(order.paymentMethod);
    document.getElementById('paymentMethod').textContent = paymentMethodText;
    
    // Display total
    document.getElementById('orderTotal').textContent = order.summary.total.toFixed(2) + ' DT';
    
    // Calculate and display delivery date (3-5 business days)
    const deliveryDate = new Date(orderDate);
    deliveryDate.setDate(deliveryDate.getDate() + 5);
    document.getElementById('deliveryDate').textContent = formatDate(deliveryDate);
    
    // Display customer information
    document.getElementById('customerName').textContent = 
        `${order.customer.firstName} ${order.customer.lastName}`;
    document.getElementById('customerEmail').textContent = order.customer.email;
    document.getElementById('customerPhone').textContent = order.customer.phone;
    document.getElementById('customerAddress').textContent = 
        `${order.customer.address}, ${order.customer.city} ${order.customer.postalCode}`;
    
    // Display order items
    const orderItemsList = document.getElementById('orderItemsList');
    orderItemsList.innerHTML = order.items.map(item => `
        <div class="success-order-item">
            <img src="${item.image || 'images/image_1.jpg'}" alt="${item.name}" onerror="this.src='images/image_1.jpg'">
            <div class="success-order-item-details">
                <div class="success-order-item-name">${item.name}</div>
                <div class="success-order-item-quantity">Quantité: ${item.quantity}</div>
            </div>
            <div class="success-order-item-price">
                ${(parseFloat(item.price) * item.quantity).toFixed(2)} DT
            </div>
        </div>
    `).join('');
    
    // Send order to backend (optional)
    sendOrderToBackend(order);
}

// Format date
function formatDate(date) {
    const options = { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    };
    return date.toLocaleDateString('fr-FR', options);
}

// Get payment method text
function getPaymentMethodText(method) {
    const methods = {
        'Carte bancaire': 'Carte bancaire (Visa/Mastercard)',
        'PayPal': 'PayPal',
        'cod': 'Paiement à la livraison'
    };
    return methods[method] || method;
}

// Send order to backend
async function sendOrderToBackend(order) {
    try {
        const response = await fetch('http://localhost:8000/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(order)
        });
        
        if (response.ok) {
            console.log('Order saved to backend successfully');
        } else {
            console.error('Failed to save order to backend');
        }
    } catch (error) {
        console.error('Error sending order to backend:', error);
        // Don't show error to user, order is already saved in localStorage
    }
}

