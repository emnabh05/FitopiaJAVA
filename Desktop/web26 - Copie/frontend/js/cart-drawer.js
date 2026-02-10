// Cart drawer (pop-out) for frontend pages
(function() {
    const CART_KEY = 'fitopiaCart';
    const DISCOUNT_CODES = {
        'ali123': { percent: 10, description: '10% de reduction' }
    };
    const SHIPPING_COST = 7.00;
    const FREE_SHIPPING_THRESHOLD = 100.00;

    let appliedDiscount = null;
    let appliedDiscountCode = null;

    function getEl(id) {
        return document.getElementById(id);
    }

    function getCart() {
        return JSON.parse(localStorage.getItem(CART_KEY) || '[]');
    }

    function saveCart(cart) {
        localStorage.setItem(CART_KEY, JSON.stringify(cart));
        updateCartCount();
    }

    function updateCartCount() {
        const cart = getCart();
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        document.querySelectorAll('.cart-count').forEach(el => {
            el.textContent = totalItems;
        });
    }

    function formatMoney(value) {
        return value.toFixed(2) + ' DT';
    }

    function loadDiscountFromStorage() {
        const saved = localStorage.getItem('appliedDiscount');
        if (!saved) return;
        try {
            appliedDiscount = JSON.parse(saved);
            if (appliedDiscount && appliedDiscount.percent) {
                const codeKey = Object.keys(DISCOUNT_CODES).find(key =>
                    DISCOUNT_CODES[key].percent === appliedDiscount.percent
                );
                appliedDiscountCode = codeKey || null;
                const input = getEl('drawerDiscountCode');
                const msg = getEl('drawerDiscountMessage');
                if (input && appliedDiscountCode) {
                    input.value = appliedDiscountCode;
                }
                if (msg && appliedDiscount) {
                    msg.textContent = 'Code applique: ' + appliedDiscount.description;
                    msg.className = 'drawer-discount-message success';
                }
            }
        } catch (e) {
            appliedDiscount = null;
            appliedDiscountCode = null;
        }
    }

    function applyDiscountCode() {
        const input = getEl('drawerDiscountCode');
        const msg = getEl('drawerDiscountMessage');
        if (!input || !msg) return;

        const code = input.value.trim().toLowerCase();
        if (!code) {
            msg.textContent = 'Veuillez entrer un code promo';
            msg.className = 'drawer-discount-message error';
            return;
        }

        if (DISCOUNT_CODES[code]) {
            appliedDiscount = DISCOUNT_CODES[code];
            appliedDiscountCode = code;
            msg.textContent = 'Code applique: ' + appliedDiscount.description;
            msg.className = 'drawer-discount-message success';
            localStorage.setItem('appliedDiscount', JSON.stringify(appliedDiscount));
            renderDrawer();
        } else {
            msg.textContent = 'Code promo invalide';
            msg.className = 'drawer-discount-message error';
            appliedDiscount = null;
            appliedDiscountCode = null;
            localStorage.removeItem('appliedDiscount');
            renderDrawer();
        }
    }

    function updateSummary(cart) {
        const subtotal = cart.reduce((sum, item) => sum + (parseFloat(item.price) * item.quantity), 0);
        const shipping = cart.length === 0 ? 0 : (subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_COST);
        let discountAmount = 0;

        if (appliedDiscount) {
            discountAmount = (subtotal * appliedDiscount.percent) / 100;
        }

        const total = Math.max(0, subtotal + shipping - discountAmount);

        const subtotalEl = getEl('drawerSubtotal');
        const shippingEl = getEl('drawerShipping');
        const totalEl = getEl('drawerTotal');
        const discountRow = getEl('drawerDiscountRow');
        const discountPercent = getEl('drawerDiscountPercent');
        const discountAmountEl = getEl('drawerDiscountAmount');

        if (subtotalEl) subtotalEl.textContent = formatMoney(subtotal);
        if (shippingEl) shippingEl.textContent = shipping === 0 ? 'GRATUIT' : formatMoney(shipping);
        if (totalEl) totalEl.textContent = formatMoney(total);

        if (discountRow && discountAmountEl && discountPercent) {
            if (appliedDiscount && discountAmount > 0) {
                discountRow.style.display = 'flex';
                discountPercent.textContent = '(' + appliedDiscount.percent + '%)';
                discountAmountEl.textContent = '-' + formatMoney(discountAmount);
            } else {
                discountRow.style.display = 'none';
            }
        }
    }

    function renderItems(cart) {
        const itemsContainer = getEl('cartDrawerItems');
        const emptyState = getEl('cartDrawerEmpty');
        const checkoutBtn = getEl('drawerCheckoutBtn');

        if (!itemsContainer || !emptyState) return;

        if (cart.length === 0) {
            itemsContainer.innerHTML = '';
            emptyState.style.display = 'block';
            if (checkoutBtn) checkoutBtn.disabled = true;
            return;
        }

        emptyState.style.display = 'none';
        if (checkoutBtn) checkoutBtn.disabled = false;

        itemsContainer.innerHTML = cart.map(item => `
            <div class="cart-drawer-item" data-id="${item.id}">
                <div class="cart-drawer-item-image">
                    <img src="${item.image || 'images/image_1.jpg'}" alt="${item.name}" onerror="this.src='images/image_1.jpg'">
                </div>
                <div class="cart-drawer-item-details">
                    <h4>${item.name}</h4>
                    <span>${item.category || 'Supplement'}</span>
                    <div class="cart-drawer-qty">
                        <button class="drawer-qty-minus" data-id="${item.id}">-</button>
                        <span>${item.quantity}</span>
                        <button class="drawer-qty-plus" data-id="${item.id}">+</button>
                    </div>
                    <button class="cart-drawer-remove" data-id="${item.id}">Supprimer</button>
                </div>
                <div class="cart-drawer-item-price">
                    ${formatMoney(parseFloat(item.price) * item.quantity)}
                </div>
            </div>
        `).join('');

        itemsContainer.querySelectorAll('.drawer-qty-plus').forEach(btn => {
            btn.addEventListener('click', () => updateQuantity(btn.dataset.id, 1));
        });
        itemsContainer.querySelectorAll('.drawer-qty-minus').forEach(btn => {
            btn.addEventListener('click', () => updateQuantity(btn.dataset.id, -1));
        });
        itemsContainer.querySelectorAll('.cart-drawer-remove').forEach(btn => {
            btn.addEventListener('click', () => removeItem(btn.dataset.id));
        });
    }

    function updateQuantity(productId, delta) {
        const cart = getCart();
        const item = cart.find(i => i.id == productId);
        if (!item) return;

        item.quantity += delta;
        if (item.quantity <= 0) {
            removeItem(productId);
            return;
        }

        saveCart(cart);
        renderDrawer();
    }

    function removeItem(productId) {
        const cart = getCart().filter(item => item.id != productId);
        saveCart(cart);
        renderDrawer();
    }

    function proceedToCheckout() {
        const cart = getCart();
        if (cart.length === 0) {
            return;
        }

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
            discountCode: appliedDiscountCode,
            total,
            items: cart
        };

        localStorage.setItem('orderSummary', JSON.stringify(orderSummary));
        window.location.href = 'checkout.html';
    }

    function openDrawer() {
        const drawer = getEl('cartDrawer');
        const overlay = getEl('cartOverlay');
        if (!drawer || !overlay) return;

        drawer.classList.add('is-open');
        overlay.classList.add('is-open');
        document.body.classList.add('cart-open');
        renderDrawer();
    }

    function closeDrawer() {
        const drawer = getEl('cartDrawer');
        const overlay = getEl('cartOverlay');
        if (!drawer || !overlay) return;

        drawer.classList.remove('is-open');
        overlay.classList.remove('is-open');
        document.body.classList.remove('cart-open');
    }

    function renderDrawer() {
        const cart = getCart();
        renderItems(cart);
        updateSummary(cart);
        updateCartCount();
    }

    function bindEvents() {
        const toggles = document.querySelectorAll('.cart-toggle');
        toggles.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                openDrawer();
            });
        });

        const overlay = getEl('cartOverlay');
        if (overlay) {
            overlay.addEventListener('click', closeDrawer);
        }

        const closeBtn = getEl('cartDrawerClose');
        if (closeBtn) {
            closeBtn.addEventListener('click', closeDrawer);
        }

        const checkoutBtn = getEl('drawerCheckoutBtn');
        if (checkoutBtn) {
            checkoutBtn.addEventListener('click', proceedToCheckout);
        }

        const applyBtn = getEl('drawerApplyDiscount');
        if (applyBtn) {
            applyBtn.addEventListener('click', applyDiscountCode);
        }

        const discountInput = getEl('drawerDiscountCode');
        if (discountInput) {
            discountInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    applyDiscountCode();
                }
            });
        }

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                closeDrawer();
            }
        });
    }

    function maybeOpenFromUrl() {
        const params = new URLSearchParams(window.location.search);
        if (params.get('cart') === 'open' || window.location.hash === '#cart') {
            openDrawer();
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        if (!getEl('cartDrawer')) return;
        loadDiscountFromStorage();
        bindEvents();
        updateCartCount();
        maybeOpenFromUrl();

        window.refreshCartDrawer = renderDrawer;
    });
})();
