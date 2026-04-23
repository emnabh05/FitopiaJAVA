package tn.esprit.Pidev3A49.utils;

import tn.esprit.Pidev3A49.Models.CartItem;
import tn.esprit.Pidev3A49.Models.Supplement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CartStore {

    private static final BigDecimal STANDARD_SHIPPING_COST = new BigDecimal("7.00");
    private static final CartStore INSTANCE = new CartStore();

    private final Map<Integer, CartItem> itemsBySupplementId = new LinkedHashMap<>();
    private String lastCheckoutEmail;

    private CartStore() {
    }

    public static CartStore getInstance() {
        return INSTANCE;
    }

    public synchronized void addSupplement(Supplement supplement) {
        if (supplement == null) {
            throw new IllegalArgumentException("Le supplement est introuvable.");
        }
        if (supplement.getStock() <= 0) {
            throw new IllegalStateException("Ce supplement n'est plus en stock.");
        }

        CartItem item = itemsBySupplementId.get(supplement.getId());
        if (item == null) {
            itemsBySupplementId.put(supplement.getId(), new CartItem(supplement, 1));
            return;
        }

        if (item.getQuantity() >= supplement.getStock()) {
            throw new IllegalStateException("Quantite maximale atteinte pour ce supplement.");
        }

        item.setQuantity(item.getQuantity() + 1);
    }

    public synchronized void increaseQuantity(int supplementId) {
        CartItem item = requireItem(supplementId);
        if (item.getQuantity() >= item.getSupplement().getStock()) {
            throw new IllegalStateException("Stock insuffisant pour ajouter une unite de plus.");
        }
        item.setQuantity(item.getQuantity() + 1);
    }

    public synchronized void decreaseQuantity(int supplementId) {
        CartItem item = requireItem(supplementId);
        int newQuantity = item.getQuantity() - 1;
        if (newQuantity <= 0) {
            itemsBySupplementId.remove(supplementId);
            return;
        }
        item.setQuantity(newQuantity);
    }

    public synchronized void removeSupplement(int supplementId) {
        itemsBySupplementId.remove(supplementId);
    }

    public synchronized List<CartItem> getItems() {
        return new ArrayList<>(itemsBySupplementId.values());
    }

    public synchronized boolean isEmpty() {
        return itemsBySupplementId.isEmpty();
    }

    public synchronized int getTotalQuantity() {
        return itemsBySupplementId.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public synchronized BigDecimal getSubtotal() {
        return itemsBySupplementId.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public synchronized BigDecimal getShippingCost() {
        return isEmpty() ? BigDecimal.ZERO : STANDARD_SHIPPING_COST;
    }

    public synchronized BigDecimal getTotal(BigDecimal discountAmount) {
        return getSubtotal()
                .add(getShippingCost())
                .subtract(discountAmount == null ? BigDecimal.ZERO : discountAmount);
    }

    public synchronized void clear() {
        itemsBySupplementId.clear();
    }

    public synchronized String getLastCheckoutEmail() {
        return lastCheckoutEmail;
    }

    public synchronized void setLastCheckoutEmail(String lastCheckoutEmail) {
        this.lastCheckoutEmail = lastCheckoutEmail;
    }

    private CartItem requireItem(int supplementId) {
        CartItem item = itemsBySupplementId.get(supplementId);
        if (item == null) {
            throw new IllegalStateException("Le supplement n'est plus present dans le panier.");
        }
        return item;
    }
}
