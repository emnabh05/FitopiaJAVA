package tn.esprit.Pidev3A49.utils;

import tn.esprit.Pidev3A49.Models.SupplementOrder;

public final class ConfirmedOrderStore {

    private static final ConfirmedOrderStore INSTANCE = new ConfirmedOrderStore();

    private SupplementOrder confirmedOrder;

    private ConfirmedOrderStore() {
    }

    public static ConfirmedOrderStore getInstance() {
        return INSTANCE;
    }

    public synchronized SupplementOrder getConfirmedOrder() {
        return confirmedOrder;
    }

    public synchronized void setConfirmedOrder(SupplementOrder confirmedOrder) {
        this.confirmedOrder = confirmedOrder;
    }

    public synchronized void clear() {
        this.confirmedOrder = null;
    }
}
