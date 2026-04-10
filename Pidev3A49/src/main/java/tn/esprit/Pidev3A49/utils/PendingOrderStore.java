package tn.esprit.Pidev3A49.utils;

import tn.esprit.Pidev3A49.Models.SupplementOrder;

public final class PendingOrderStore {

    private static final PendingOrderStore INSTANCE = new PendingOrderStore();

    private SupplementOrder pendingOrder;

    private PendingOrderStore() {
    }

    public static PendingOrderStore getInstance() {
        return INSTANCE;
    }

    public synchronized SupplementOrder getPendingOrder() {
        return pendingOrder;
    }

    public synchronized void setPendingOrder(SupplementOrder pendingOrder) {
        this.pendingOrder = pendingOrder;
    }

    public synchronized void clear() {
        this.pendingOrder = null;
    }
}
