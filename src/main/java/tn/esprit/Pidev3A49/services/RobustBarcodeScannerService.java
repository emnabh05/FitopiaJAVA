package tn.esprit.Pidev3A49.services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelFormat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RobustBarcodeScannerService {
    
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private final AtomicReference<String> lastScannedBarcode = new AtomicReference<>();
    private Thread scanningThread;
    private volatile boolean cameraActive = false;
    private volatile boolean frozenOnBarcode = false;
    private BufferedImage frozenFrame = null;
    
    // Callback pour l'interface
    public interface ScannerCallback {
        void onFrameReady(Image frame, boolean frozen);
        void onBarcodeDetected(String barcode, BufferedImage capturedImage);
        void onError(String message);
        void onStatusUpdate(String status);
        void onScanEffect(boolean active);
    }
    
    private ScannerCallback callback;
    
    public void setCallback(ScannerCallback callback) {
        this.callback = callback;
    }
    
    public boolean initializeCamera() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Utiliser Java AWT Robot sans OpenCV - pas d'erreurs MSMF
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = ge.getScreenDevices();
            
            if (devices.length == 0) {
                if (callback != null) {
                    Platform.runLater(() -> {
                        callback.onError("Aucun écran détecté");
                    });
                }
                return false;
            }
            
            cameraActive = true;
            long initTime = System.currentTimeMillis() - startTime;
            
            if (callback != null) {
                Platform.runLater(() -> {
                    callback.onStatusUpdate("Scanner robuste prêt! (" + initTime + "ms) - Sans OpenCV");
                });
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du scanner robuste: " + e.getMessage());
            if (callback != null) {
                Platform.runLater(() -> {
                    callback.onError("Erreur d'initialisation: " + e.getMessage());
                });
            }
            return false;
        }
    }
    
    public void startScanning() {
        if (scanning.get() || !cameraActive) {
            return;
        }

        scanning.set(true);
        lastScannedBarcode.set(null);
        frozenOnBarcode = false;
        frozenFrame = null;

        scanningThread = new Thread(() -> {
            Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                if (callback != null) {
                    Platform.runLater(() -> callback.onError("Impossible de créer le scanner robuste"));
                }
                return;
            }
            
            // Zone de scan optimisée pour codes-barres
            Rectangle screenRect = new Rectangle(640, 480);
            screenRect.x = (Toolkit.getDefaultToolkit().getScreenSize().width - 640) / 2;
            screenRect.y = (Toolkit.getDefaultToolkit().getScreenSize().height - 480) / 2;
            
            while (scanning.get() && cameraActive) {
                try {
                    BufferedImage currentFrame;
                    
                    // Si on est gelé sur un code-barres, utiliser l'image gelée
                    if (frozenOnBarcode && frozenFrame != null) {
                        currentFrame = frozenFrame;
                        
                        // Afficher l'image gelée avec effet de focus
                        if (callback != null) {
                            Image fxImage = addFocusEffect(currentFrame);
                            Platform.runLater(() -> {
                                callback.onFrameReady(fxImage, true);
                            });
                        }
                        
                        Thread.sleep(100);
                        continue;
                    }
                    
                    // Capturer une nouvelle image
                    currentFrame = robot.createScreenCapture(screenRect);
                    
                    // Essayer de lire un code-barres avec TOUS les formats internationaux
                    String barcode = readBarcodeInternational(currentFrame);
                    
                    if (barcode != null && isValidInternationalBarcode(barcode)) {
                        String lastBarcode = lastScannedBarcode.get();
                        long currentTime = System.currentTimeMillis();
                        long lastTime = lastScanTime.get();
                        
                        // Éviter les scans multiples du même code (délai de 3 secondes)
                        if (!barcode.equals(lastBarcode) || (currentTime - lastTime) > 3000) {
                            // Geler sur ce code-barres
                            frozenOnBarcode = true;
                            frozenFrame = deepCopy(currentFrame);
                            lastScannedBarcode.set(barcode);
                            lastScanTime.set(currentTime);
                            
                            // Effet de scan visuel
                            if (callback != null) {
                                Platform.runLater(() -> {
                                    callback.onScanEffect(true);
                                    callback.onStatusUpdate("FOCUS SUR: " + barcode + " - VALIDATION...");
                                });
                            }
                            
                            // Double validation pour éviter les faux positifs
                            String validation1 = readBarcodeInternational(frozenFrame);
                            String validation2 = readBarcodeInternational(frozenFrame);
                            
                            if (validation1 != null && validation1.equals(barcode) && 
                                validation2 != null && validation2.equals(barcode)) {
                                
                                // Sauvegarder la capture
                                saveBarcodeCapture(frozenFrame, barcode);
                                
                                // Notifier la détection
                                if (callback != null) {
                                    Platform.runLater(() -> {
                                        callback.onBarcodeDetected(barcode, frozenFrame);
                                    });
                                }
                                
                                // Pause de 2 secondes gelé sur le code
                                Thread.sleep(2000);
                            } else {
                                // Échec validation
                                Thread.sleep(500);
                            }
                            
                            // Dégeler après la pause
                            frozenOnBarcode = false;
                            frozenFrame = null;
                            
                            if (callback != null) {
                                Platform.runLater(() -> {
                                    callback.onScanEffect(false);
                                    callback.onStatusUpdate("Scan actif - Recherche codes-barres internationaux...");
                                });
                            }
                        }
                    }
                    
                    // Mettre à jour l'affichage du flux si on n'est pas gelé
                    if (!frozenOnBarcode && callback != null) {
                        Image fxImage = bufferedImageToFXImage(currentFrame);
                        Platform.runLater(() -> {
                            callback.onFrameReady(fxImage, false);
                        });
                    }
                    
                    // Libérer la mémoire
                    currentFrame.flush();
                    
                    Thread.sleep(50); // 20 FPS pour une bonne réactivité
                    
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("Erreur lors du scan robuste: " + e.getMessage());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        });

        scanningThread.start();
    }
    
    private String readBarcodeInternational(BufferedImage image) {
        try {
            Reader reader = new MultiFormatReader();
            
            // Configuration pour TOUS les formats internationaux
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
            hints.put(DecodeHintType.ASSUME_CODE_39_CHECK_DIGIT, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, 
                Arrays.asList(
                    // Formats internationaux complets
                    BarcodeFormat.EAN_13,      // Europe, Asie, Amérique
                    BarcodeFormat.EAN_8,       // Petits produits
                    BarcodeFormat.UPC_A,        // Amérique du Nord
                    BarcodeFormat.UPC_E,        // Amérique du Nord (compact)
                    BarcodeFormat.CODE_128,     // Logistique mondial
                    BarcodeFormat.CODE_39,      // Industrie, militaire
                    BarcodeFormat.CODE_93,      // Amérique du Nord
                    BarcodeFormat.ITF,          // Logistique (interleaved 2 of 5)
                    BarcodeFormat.CODABAR,      // Bibliothèques, sang
                    BarcodeFormat.RSS_14,       // Retail (GS1 Databar)
                    BarcodeFormat.RSS_EXPANDED, // Retail étendu
                    BarcodeFormat.QR_CODE,      // Asie, mondial
                    BarcodeFormat.DATA_MATRIX,  // Industrie, médical
                    BarcodeFormat.AZTEC,        // Transport, billets
                    BarcodeFormat.PDF_417,      // Documents, transport
                    BarcodeFormat.MAXICODE      // Logistique UPS
                ));
            
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Result result = reader.decode(bitmap, hints);
            return result.getText();
            
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean isValidInternationalBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return false;
        }
        
        barcode = barcode.trim();
        
        // Accepter les codes-barres avec caractères alphanumériques (CODE_39, CODE_128, etc.)
        if (!barcode.matches("[A-Za-z0-9\\-\\.\\s]+")) {
            return false;
        }
        
        int length = barcode.length();
        
        // Formats internationaux acceptés
        if (barcode.matches("\\d+")) {
            // Codes numériques seulement
            return length == 8    // EAN-8, UPC-E
                || length == 10   // UPC-A (sans checksum)
                || length == 12   // UPC-A
                || length == 13   // EAN-13
                || length == 14   // ITF-14, GS1-128
                || length == 18;  // SSCC, GS1-128 étendu
        } else {
            // Codes alphanumériques (CODE_39, CODE_128, etc.)
            return length >= 1 && length <= 48; // Longueur variable pour formats alphanumériques
        }
    }
    
    private Image addFocusEffect(BufferedImage original) {
        // Ajouter un effet de focus visuel (rectangle vert autour de la zone de scan)
        BufferedImage withFocus = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = withFocus.createGraphics();
        
        // Dessiner l'image originale
        g2d.drawImage(original, 0, 0, null);
        
        // Ajouter un rectangle de focus vert
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(10, 10, original.getWidth() - 20, original.getHeight() - 20);
        
        // Ajouter un effet de luminosité
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, original.getWidth(), original.getHeight());
        
        g2d.dispose();
        return bufferedImageToFXImage(withFocus);
    }
    
    private BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }
    
    private Image bufferedImageToFXImage(BufferedImage bufferedImage) {
        // Conversion directe de BufferedImage vers Image JavaFX
        WritableImage wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        int[] pixels = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());
        wr.getPixelWriter().setPixels(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), 
            PixelFormat.getIntArgbInstance(), pixels, 0, bufferedImage.getWidth());
        return wr;
    }
    
    private void saveBarcodeCapture(BufferedImage image, String barcode) {
        try {
            File captureDir = new File("barcode_captures");
            if (!captureDir.exists()) {
                captureDir.mkdir();
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = sdf.format(new Date());
            String filename = "robust_scan_" + barcode + "_" + timestamp + ".jpg";
            
            File outputFile = new File(captureDir, filename);
            ImageIO.write(image, "jpg", outputFile);
            
            System.out.println("Capture robuste sauvegardée: " + outputFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de la capture: " + e.getMessage());
        }
    }
    
    public void stopScanning() {
        scanning.set(false);
        frozenOnBarcode = false;
        frozenFrame = null;
        if (scanningThread != null) {
            scanningThread.interrupt();
        }
    }
    
    public void releaseCamera() {
        stopScanning();
        cameraActive = false;
    }
    
    public String getLastScannedBarcode() {
        return lastScannedBarcode.get();
    }
    
    public boolean isScanning() {
        return scanning.get();
    }
    
    public boolean isFrozenOnBarcode() {
        return frozenOnBarcode;
    }
    
    private final AtomicReference<Long> lastScanTime = new AtomicReference<>(0L);
}
