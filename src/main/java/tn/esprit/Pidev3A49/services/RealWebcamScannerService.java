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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RealWebcamScannerService {
    
    private boolean openCVLoaded = false;
    private VideoCapture camera;
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private final AtomicReference<String> lastBarcode = new AtomicReference<>();
    private Thread scanningThread;
    private volatile boolean cameraInitialized = false;
    
    // Callback pour mettre à jour l'interface
    public interface WebcamCallback {
        void onFrameReady(Image frame);
        void onBarcodeDetected(String barcode, BufferedImage capturedImage);
        void onError(String message);
        void onStatusUpdate(String status);
    }
    
    private WebcamCallback callback;
    
    public void setCallback(WebcamCallback callback) {
        this.callback = callback;
    }
    
    public boolean initializeCamera() {
        try {
            // Charger OpenCV seulement si ce n'est pas déjà fait
            if (!openCVLoaded) {
                try {
                    // Essayer de charger OpenCV localement
                    OpenCV.loadLocally();
                    openCVLoaded = true;
                    System.out.println("OpenCV chargé avec succès");
                } catch (Exception e) {
                    System.err.println("Impossible de charger OpenCV: " + e.getMessage());
                    if (callback != null) {
                        callback.onError("OpenCV non disponible. Installation requise.");
                    }
                    return false;
                }
            }
            
            camera = new VideoCapture();
            
            // Essayer d'abord la webcam par défaut (0)
            camera.open(0);
            
            if (!camera.isOpened()) {
                // Essayer d'autres indices si nécessaire
                for (int i = 1; i < 5; i++) {
                    camera.open(i);
                    if (camera.isOpened()) {
                        System.out.println("Webcam trouvée à l'index: " + i);
                        break;
                    }
                }
            }

            if (camera.isOpened()) {
                // Configurer la résolution
                camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
                camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
                
                // Vérifier que la résolution est correcte
                double width = camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
                double height = camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
                System.out.println("Résolution webcam: " + width + "x" + height);
                
                cameraInitialized = true;
                if (callback != null) {
                    callback.onStatusUpdate("Webcam initialisée avec succès");
                }
                return true;
            } else {
                if (callback != null) {
                    callback.onError("Aucune webcam détectée");
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la caméra: " + e.getMessage());
            if (callback != null) {
                callback.onError("Erreur webcam: " + e.getMessage());
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
            String previousBarcode = null;
            
            while (scanning.get() && camera.isOpened()) {
                if (camera.read(frame)) {
                    try {
                        // Convertir le frame OpenCV en BufferedImage
                        BufferedImage bufferedImage = matToBufferedImage(frame);
                        
                        if (bufferedImage != null) {
                            // Convertir en Image JavaFX pour l'affichage
                            Image fxImage = bufferedImageToFXImage(bufferedImage);
                            
                            // Mettre à jour l'interface avec le frame actuel
                            if (callback != null) {
                                Platform.runLater(() -> {
                                    callback.onFrameReady(fxImage);
                                });
                            }
                            
                            // Essayer de lire le code-barres
                            String barcode = readBarcode(bufferedImage);
                            
                            if (barcode != null && !barcode.equals(previousBarcode)) {
                                previousBarcode = barcode;
                                lastBarcode.set(barcode);
                                
                                // Faire une capture d'écran du moment de la détection
                                BufferedImage capturedImage = deepCopy(bufferedImage);
                                
                                // Sauvegarder la capture
                                saveBarcodeCapture(capturedImage, barcode);
                                
                                // Notifier le callback
                                if (callback != null) {
                                    Platform.runLater(() -> {
                                        callback.onBarcodeDetected(barcode, capturedImage);
                                    });
                                }
                                
                                // Pause pour éviter les lectures multiples
                                Thread.sleep(2000);
                            }
                        }
                        
                    } catch (Exception e) {
                        System.err.println("Erreur lors du traitement du frame: " + e.getMessage());
                    }
                }
                
                try {
                    Thread.sleep(50); // 20 FPS pour un flux fluide
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            frame.release();
        });

        scanningThread.start();
    }
    
    private String readBarcode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Reader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap);
            
            return result.getText();
        } catch (NotFoundException e) {
            // Pas de code-barres trouvé, c'est normal
            return null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du code-barres: " + e.getMessage());
            return null;
        }
    }
    
    private BufferedImage matToBufferedImage(Mat mat) {
        try {
            MatOfByte mob = new MatOfByte();
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
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
            // Créer le répertoire de captures s'il n'existe pas
            File captureDir = new File("barcode_captures");
            if (!captureDir.exists()) {
                captureDir.mkdir();
            }
            
            // Nom de fichier avec timestamp et code-barres
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
