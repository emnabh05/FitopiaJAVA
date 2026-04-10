package tn.esprit.Pidev3A49.Models;

import java.math.BigDecimal;

public class CartItem {

    private final Supplement supplement;
    private int quantity;

    public CartItem(Supplement supplement, int quantity) {
        this.supplement = supplement;
        this.quantity = quantity;
    }

    public Supplement getSupplement() {
        return supplement;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return supplement.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
