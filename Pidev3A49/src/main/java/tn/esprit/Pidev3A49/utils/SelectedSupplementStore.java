package tn.esprit.Pidev3A49.utils;

import tn.esprit.Pidev3A49.Models.Supplement;

public final class SelectedSupplementStore {

    private static final SelectedSupplementStore INSTANCE = new SelectedSupplementStore();

    private Supplement selectedSupplement;

    private SelectedSupplementStore() {
    }

    public static SelectedSupplementStore getInstance() {
        return INSTANCE;
    }

    public synchronized Supplement getSelectedSupplement() {
        return selectedSupplement;
    }

    public synchronized void setSelectedSupplement(Supplement selectedSupplement) {
        this.selectedSupplement = selectedSupplement;
    }

    public synchronized void clear() {
        selectedSupplement = null;
    }
}
