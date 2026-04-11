package tn.esprit.Pidev3A49.Models;

import java.math.BigDecimal;

public class SupplementOrderItem {

    private int supplementId;
    private String supplementName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;

    public SupplementOrderItem() {
    }

    public SupplementOrderItem(int supplementId, String supplementName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
        this.supplementId = supplementId;
        this.supplementName = supplementName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public int getSupplementId() {
        return supplementId;
    }

    public void setSupplementId(int supplementId) {
        this.supplementId = supplementId;
    }

    public String getSupplementName() {
        return supplementName;
    }

    public void setSupplementName(String supplementName) {
        this.supplementName = supplementName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
