package tn.esprit.Pidev3A49.services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class OptimizedBarcodeScannerService {
    
    private boolean openCVLoaded = false;
    private VideoCapture camera;
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private final AtomicReference<String> lastBarcode = new AtomicReference<>();
    private Thread scanningThread;
    private volatile boolean cameraInitialized = false;
    private volatile boolean analyzingBarcode = false;
    private BufferedImage lockedFrame = null;
    
    // Callback pour mettre à jour l'interface
    public interface WebcamCallback {
        void onFrameReady(Image frame, boolean barcodeDetected);
        void onBarcodeDetected(String barcode, BufferedImage capturedImage);
        void onError(String message);
        void onStatusUpdate(String status);
        void onScanEffect(boolean active);
    }
    
    private WebcamCallback callback;
    
    public void setCallback(WebcamCallback callback) {
        this.callback = callback;
    }
    
    public boolean initializeCamera() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Charger OpenCV avec gestion d'erreur améliorée
            if (!openCVLoaded) {
                try {
                    nu.pattern.OpenCV.loadLocally();
                    openCVLoaded = true;
                    long loadTime = System.currentTimeMillis() - startTime;
                    System.out.println("OpenCV chargé en " + loadTime + "ms");
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement d'OpenCV: " + e.getMessage());
                    if (callback != null) {
                        Platform.runLater(() -> {
                            callback.onError("OpenCV non disponible: " + e.getMessage());
                        });
                    }
                    return false;
                }
            }
            
            // Initialiser la caméra avec résolution optimisée et timeout
            camera = new VideoCapture();
            
            // Timeout pour éviter les blocages
            long timeout = 5000; // 5 secondes max
            long initStartTime = System.currentTimeMillis();
            
            // Essayer avec la caméra par défaut d'abord (contourner les erreurs MSMF)
            boolean cameraOpened = false;
            try {
                cameraOpened = camera.open(0);
            } catch (Exception e) {
                System.err.println("Erreur ouverture caméra 0: " + e.getMessage());
            }
            
            if (!cameraOpened) {
                // Essayer avec d'autres indices et backends
                for (int camIndex = 1; camIndex < 5; camIndex++) {
                    try {
                        if (camera.open(camIndex)) {
                            cameraOpened = true;
                            break;
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur ouverture caméra " + camIndex + ": " + e.getMessage());
                    }
                }
                
                // Essayer avec DirectShow si MSMF échoue
                if (!cameraOpened) {
                    try {
                        // Forcer DirectShow sur Windows
                        System.setProperty("opencv.videoio.backends", "DSHOW");
                        cameraOpened = camera.open(0);
                    } catch (Exception e) {
                        System.err.println("Erreur DirectShow: " + e.getMessage());
                    }
                }
                
                if (!cameraOpened) {
                    if (callback != null) {
                        Platform.runLater(() -> {
                            callback.onError("Aucune webcam détectée (vérifiez les permissions)");
                        });
                    }
                    return false;
                }
            }
            
            // Appliquer une résolution optimisée pour la vitesse
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 360);
            
            // Activer l'autofocus si disponible
            camera.set(Videoio.CAP_PROP_AUTOFOCUS, 1);
            
            // Réduire le FPS pour la stabilité
            camera.set(Videoio.CAP_PROP_FPS, 15);
            
            // Vérifier que la caméra fonctionne
            Mat testFrame = new Mat();
            if (camera.read(testFrame)) {
                testFrame.release();
                cameraInitialized = true;
                
                long initTime = System.currentTimeMillis() - startTime;
                System.out.println("Webcam initialisée en " + initTime + "ms");
                
                // Afficher les informations de la caméra
                double actualWidth = camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
                double actualHeight = camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
                double fps = camera.get(Videoio.CAP_PROP_FPS);
                System.out.println("Résolution webcam: " + actualWidth + "x" + actualHeight + " @ " + fps + " FPS");
                
                if (callback != null) {
                    Platform.runLater(() -> {
                        callback.onStatusUpdate("Webcam prête! (" + initTime + "ms)");
                    });
                }
                
                return true;
            } else {
                testFrame.release();
                camera.release();
                if (callback != null) {
                    Platform.runLater(() -> {
                        callback.onError("La webcam ne produit pas d'images");
                    });
                }
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la webcam: " + e.getMessage());
            if (callback != null) {
                Platform.runLater(() -> {
                    callback.onError("Erreur webcam: " + e.getMessage());
                });
            }
            return false;
        }
    }
    
    public void startScanning() {
        if (scanning.get() || !cameraInitialized) {
            return;
        }

        scanning.set(true);
        lastBarcode.set(null);

        scanningThread = new Thread(() -> {
            Mat frame = new Mat();
            Mat processedFrame = new Mat();
            String previousBarcode = null;
            int scanCount = 0;
            
            while (scanning.get() && camera.isOpened()) {
                if (camera.read(frame)) {
                    try {
                        scanCount++;
                        
                        // Si nous sommes en mode d'analyse, afficher l'image bloquée
                        if (analyzingBarcode && lockedFrame != null) {
                            Image fxImage = bufferedImageToFXImage(lockedFrame);
                            if (callback != null) {
                                Platform.runLater(() -> {
                                    callback.onFrameReady(fxImage, true);
                                });
                            }
                            Thread.sleep(100);
                            continue;
                        }
                        
                        // Prétraitement optimisé pour la détection de codes-barres
                        Mat enhancedFrame = preprocessForBarcode(frame);
                        
                        // Convertir le frame OpenCV en BufferedImage
                        BufferedImage enhancedImage = matToBufferedImage(enhancedFrame);
                        BufferedImage originalImage = matToBufferedImage(frame); // Image originale sans traitement
                        
                        if (enhancedImage != null && originalImage != null) {
                            // Convertir en Image JavaFX pour l'affichage (utiliser l'image originale pour le preview)
                            Image fxImage = bufferedImageToFXImage(originalImage);
                            
                            // Détection rapide initiale
                            String initialBarcode = readBarcodeOptimized(enhancedImage);
                            if (initialBarcode == null || !isValidBarcode(initialBarcode)) {
                                initialBarcode = readBarcodeOptimized(originalImage);
                            }
                            
                            boolean initialDetection = initialBarcode != null && !initialBarcode.equals(previousBarcode);
                            
                            if (initialDetection) {
                                // BLOQUER l'image immédiatement et démarrer l'analyse approfondie
                                analyzingBarcode = true;
                                lockedFrame = deepCopy(originalImage);
                                
                                if (callback != null) {
                                    Platform.runLater(() -> {
                                        callback.onStatusUpdate("Code-barres détecté! Analyse approfondie en cours...");
                                        callback.onScanEffect(true);
                                    });
                                }
                                
                                // Faire l'analyse approfondie sur l'image bloquée
                                String confirmedBarcode = analyzeBarcodeDeeply(lockedFrame);
                                
                                if (confirmedBarcode != null && !confirmedBarcode.equals(previousBarcode)) {
                                    previousBarcode = confirmedBarcode;
                                    lastBarcode.set(confirmedBarcode);
                                    final String finalBarcode = confirmedBarcode;
                                    
                                    // Faire une capture d'écran du moment de la détection
                                    final BufferedImage capturedImage = deepCopy(lockedFrame);
                                    
                                    // Sauvegarder la capture
                                    saveBarcodeCapture(capturedImage, finalBarcode);
                                    
                                    // Notifier le callback
                                    if (callback != null) {
                                        Platform.runLater(() -> {
                                            callback.onBarcodeDetected(finalBarcode, capturedImage);
                                            callback.onScanEffect(false);
                                        });
                                    }
                                    
                                    // Pause plus longue après une détection confirmée
                                    Thread.sleep(2000);
                                } else {
                                    // Code-barres non confirmé, reprendre le scan
                                    if (callback != null) {
                                        Platform.runLater(() -> {
                                            callback.onStatusUpdate("Code-barres non validé. Reprendre le scan...");
                                            callback.onScanEffect(false);
                                        });
                                    }
                                    Thread.sleep(500);
                                }
                                
                                // Libérer et reprendre le scan
                                analyzingBarcode = false;
                                lockedFrame = null;
                            }
                            
                            // Mettre à jour l'interface avec le frame actuel seulement si nous ne sommes pas en analyse
                            if (!analyzingBarcode && callback != null) {
                                Platform.runLater(() -> {
                                    callback.onFrameReady(fxImage, false);
                                });
                            }
                        }
                        
                        // Libérer la mémoire
                        enhancedFrame.release();
                        
                    } catch (Exception e) {
                        System.err.println("Erreur lors du traitement du frame " + scanCount + ": " + e.getMessage());
                    }
                }
                
                try {
                    Thread.sleep(50); // 20 FPS pour une meilleure réactivité
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            frame.release();
            processedFrame.release();
        });

        scanningThread.start();
    }
    
    private Mat preprocessForBarcode(Mat frame) {
        Mat gray = new Mat();
        Mat enhanced = new Mat();
        
        // Convertir en niveaux de gris
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        
        // Traitement plus doux pour préserver les codes-barres
        // Légère amélioration du contraste sans égalisation d'histogramme agressive
        Core.convertScaleAbs(gray, enhanced, 1.1, 5);
        
        // Flou très léger uniquement si nécessaire
        Imgproc.GaussianBlur(enhanced, enhanced, new Size(1, 1), 0);
        
        // Seuil adaptatif pour mieux gérer les variations de lumière
        Imgproc.adaptiveThreshold(enhanced, enhanced, 255, 
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        
        gray.release();
        return enhanced;
    }
    
    private String readBarcodeOptimized(BufferedImage image) {
        // Essayer plusieurs approches de détection
        String[] results = new String[3];
        
        // Approche 1: Détection standard
        results[0] = tryDecodeWithHints(image, false, false);
        
        // Approche 2: TRY_HARDER activé
        results[1] = tryDecodeWithHints(image, true, false);
        
        // Approche 3: PURE_BARCODE activé
        results[2] = tryDecodeWithHints(image, false, true);
        
        // Valider les résultats
        for (String result : results) {
            if (isValidBarcode(result)) {
                return result;
            }
        }
        
        return null;
    }
    
    private String tryDecodeWithHints(BufferedImage image, boolean tryHarder, boolean pureBarcode) {
        try {
            Reader reader = new MultiFormatReader();
            
            Map<DecodeHintType, Object> hints = new java.util.HashMap<>();
            hints.put(DecodeHintType.TRY_HARDER, tryHarder);
            hints.put(DecodeHintType.PURE_BARCODE, pureBarcode);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, 
                java.util.Arrays.asList(
                    // Formats internationaux
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
            System.err.println("Erreur lors de la lecture du code-barres: " + e.getMessage());
            return null;
        }
    }
    
    private boolean isValidBarcode(String barcode) {
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
    
    private boolean isValidBarcodeFormat(String barcode) {
        if (barcode == null || barcode.length() < 8 || barcode.length() > 13) {
            return false;
        }
        
        int length = barcode.length();
        
        // Validation spécifique selon le format
        switch (length) {
            case 8: // EAN-8
                return validateEAN8(barcode);
            case 12: // UPC-A
                return validateUPCA(barcode);
            case 13: // EAN-13
                return validateEAN13(barcode);
            case 10: // CODE-128 (vérification simple)
                return barcode.matches("\\d{10}");
            default:
                return false;
        }
    }
    
    private boolean validateEAN8(String barcode) {
        if (!barcode.matches("\\d{8}")) return false;
        
        // Calcul du checksum EAN-8
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit * 3 : digit;
        }
        int checksum = (10 - (sum % 10)) % 10;
        
        return checksum == Character.getNumericValue(barcode.charAt(7));
    }
    
    private boolean validateEAN13(String barcode) {
        if (!barcode.matches("\\d{13}")) return false;
        
        // Calcul du checksum EAN-13
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checksum = (10 - (sum % 10)) % 10;
        
        return checksum == Character.getNumericValue(barcode.charAt(12));
    }
    
    private boolean validateUPCA(String barcode) {
        if (!barcode.matches("\\d{12}")) return false;
        
        // Calcul du checksum UPC-A (similaire à EAN-13)
        int sum = 0;
        for (int i = 0; i < 11; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit * 3 : digit;
        }
        int checksum = (10 - (sum % 10)) % 10;
        
        return checksum == Character.getNumericValue(barcode.charAt(11));
    }
    
    private String analyzeBarcodeDeeply(BufferedImage image) {
        if (callback != null) {
            Platform.runLater(() -> {
                callback.onStatusUpdate("Analyse approfondie du code-barres...");
            });
        }
        
        // Tableau pour stocker les résultats des différentes tentatives
        java.util.List<String> results = new java.util.ArrayList<>();
        
        // Essayer avec plusieurs configurations et plusieurs fois
        for (int i = 0; i < 3; i++) {
            // Approche 1: Image originale
            String result1 = tryDecodeWithHints(image, false, false);
            if (result1 != null && isValidBarcode(result1)) {
                results.add(result1);
            }
            
            // Approche 2: TRY_HARDER
            String result2 = tryDecodeWithHints(image, true, false);
            if (result2 != null && isValidBarcode(result2)) {
                results.add(result2);
            }
            
            // Approche 3: PURE_BARCODE
            String result3 = tryDecodeWithHints(image, false, true);
            if (result3 != null && isValidBarcode(result3)) {
                results.add(result3);
            }
            
            // Pause entre les tentatives
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        // Analyser les résultats pour trouver le code-barres le plus probable
        return findMostProbableBarcode(results);
    }
    
    private String findMostProbableBarcode(java.util.List<String> results) {
        if (results.isEmpty()) {
            return null;
        }
        
        // Compter les occurrences de chaque code-barres
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (String barcode : results) {
            counts.put(barcode, counts.getOrDefault(barcode, 0) + 1);
        }
        
        // Trouver le code-barres avec le plus d'occurrences
        String mostProbable = null;
        int maxCount = 0;
        
        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostProbable = entry.getKey();
            }
        }
        
        // Valider que le code-barres a été détecté au moins 3 fois pour plus de fiabilité
        if (maxCount >= 3) {
            final String finalMostProbable = mostProbable;
            final int finalMaxCount = maxCount;
            if (callback != null) {
                Platform.runLater(() -> {
                    callback.onStatusUpdate("Code-barres confirmé: " + finalMostProbable + " (détecté " + finalMaxCount + " fois)");
                });
            }
            return mostProbable;
        } else if (maxCount == 2) {
            // Validation supplémentaire pour les codes détectés 2 fois
            if (isValidBarcodeFormat(mostProbable)) {
                final String finalMostProbable = mostProbable;
                final int finalMaxCount = maxCount;
                if (callback != null) {
                    Platform.runLater(() -> {
                        callback.onStatusUpdate("Code-barres confirmé: " + finalMostProbable + " (détecté " + finalMaxCount + " fois - validation format)");
                    });
                }
                return mostProbable;
            }
        }
        
        if (callback != null) {
            Platform.runLater(() -> {
                callback.onStatusUpdate("Code-barres non confirmé (détection inconsistante)");
            });
        }
        return null;
    }
    
    private BufferedImage matToBufferedImage(Mat mat) {
        try {
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, mob);
            byte[] byteArray = mob.toArray();
            
            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
            BufferedImage image = ImageIO.read(bis);
            mob.release();
            
            return image;
        } catch (IOException e) {
            System.err.println("Erreur de conversion Mat vers BufferedImage: " + e.getMessage());
            return null;
        }
    }
    
    private BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }
    
    private Image bufferedImageToFXImage(BufferedImage bufferedImage) {
        WritableImage wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        int[] pixels = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());
        wr.getPixelWriter().setPixels(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), 
            javafx.scene.image.PixelFormat.getIntArgbInstance(), pixels, 0, bufferedImage.getWidth());
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
            String filename = "barcode_" + barcode + "_" + timestamp + ".jpg";
            
            File outputFile = new File(captureDir, filename);
            ImageIO.write(image, "jpg", outputFile);
            
            System.out.println("Capture sauvegardée: " + outputFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de la capture: " + e.getMessage());
        }
    }
    
    public void stopScanning() {
        scanning.set(false);
        if (scanningThread != null) {
            scanningThread.interrupt();
        }
    }
    
    public void releaseCamera() {
        stopScanning();
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        cameraInitialized = false;
    }
    
    public String getLastBarcode() {
        return lastBarcode.get();
    }
    
    public boolean isScanning() {
        return scanning.get();
    }
    
    public boolean isCameraInitialized() {
        return cameraInitialized;
    }
}
