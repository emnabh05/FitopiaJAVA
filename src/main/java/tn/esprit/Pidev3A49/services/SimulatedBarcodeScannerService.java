package tn.esprit.Pidev3A49.services;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service de simulation de scan de codes-barres pour la démonstration.
 * Évite les problèmes d'installation d'OpenCV tout en montrant la fonctionnalité.
 */
public class SimulatedBarcodeScannerService {
    
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private final AtomicReference<String> lastBarcode = new AtomicReference<>();
    private Timer scanTimer;
    
    // Quelques codes-barres de produits réels pour la démonstration
    private static final String[] SAMPLE_BARCODES = {
        "7622210419288", // Prince biscuits
        "3175681205309", // Nutella
        "5449000131836", // Coca-Cola
        "3017620422003", // Evian
        "3560070443074",  // Lu Petit Beurre
        "3228857000906",  // Kinder Bueno
        "8000500214024",  // Barilla Pasta
        "4000500114024"   // Milka Chocolate
    };
    
    public void startScanning(BarcodeCallback callback) {
        if (scanning.get()) {
            return; // Déjà en cours de scan
        }
        
        scanning.set(true);
        lastBarcode.set(null);
        
        // Simuler un scan après 3-5 secondes pour l'effet "industrie"
        scanTimer = new Timer();
        scanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (scanning.get()) {
                    // Choisir un code-barres aléatoire
                    String randomBarcode = SAMPLE_BARCODES[(int)(Math.random() * SAMPLE_BARCODES.length)];
                    lastBarcode.set(randomBarcode);
                    
                    // Notifier le callback
                    javafx.application.Platform.runLater(() -> {
                        callback.onBarcodeDetected(randomBarcode);
                    });
                    
                    // Arrêter le scan après détection
                    stopScanning();
                }
            }
        }, 3000 + (int)(Math.random() * 2000)); // 3-5 secondes
    }
    
    public void stopScanning() {
        scanning.set(false);
        if (scanTimer != null) {
            scanTimer.cancel();
            scanTimer = null;
        }
    }
    
    public String getLastBarcode() {
        return lastBarcode.get();
    }
    
    public boolean isScanning() {
        return scanning.get();
    }
    
    public void release() {
        stopScanning();
    }
    
    public interface BarcodeCallback {
        void onBarcodeDetected(String barcode);
    }
}
