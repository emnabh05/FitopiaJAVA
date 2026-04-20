package tn.esprit.Pidev3A49.services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BarcodeScannerService {
    private boolean openCVLoaded = false;

    private VideoCapture camera;
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private final AtomicReference<String> lastBarcode = new AtomicReference<>();
    private Thread scanningThread;

    public boolean initializeCamera() {
        try {
            // Charger OpenCV seulement si ce n'est pas déjà fait
            if (!openCVLoaded) {
                try {
                    OpenCV.loadLocally();
                    openCVLoaded = true;
                    System.out.println("OpenCV chargé avec succès");
                } catch (Exception e) {
                    System.err.println("Impossible de charger OpenCV: " + e.getMessage());
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
                        break;
                    }
                }
            }

            if (camera.isOpened()) {
                // Configurer la résolution
                camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
                camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la caméra: " + e.getMessage());
        }
        return false;
    }

    public void startScanning(BarcodeCallback callback) {
        if (scanning.get()) {
            return; // Déjà en cours de scan
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
                        
                        // Essayer de lire le code-barres
                        String barcode = readBarcode(bufferedImage);
                        
                        if (barcode != null && !barcode.equals(previousBarcode)) {
                            previousBarcode = barcode;
                            lastBarcode.set(barcode);
                            
                            // Notifier le callback
                            javafx.application.Platform.runLater(() -> {
                                callback.onBarcodeDetected(barcode);
                            });
                            
                            // Pause pour éviter les lectures multiples
                            Thread.sleep(2000);
                        }
                        
                    } catch (Exception e) {
                        System.err.println("Erreur lors du scan: " + e.getMessage());
                    }
                }
                
                try {
                    Thread.sleep(100); // 10 FPS
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            frame.release();
        });

        scanningThread.start();
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
            return ImageIO.read(bis);
        } catch (IOException e) {
            System.err.println("Erreur de conversion Mat vers BufferedImage: " + e.getMessage());
            return null;
        }
    }

    public String getLastBarcode() {
        return lastBarcode.get();
    }

    public boolean isScanning() {
        return scanning.get();
    }

    public interface BarcodeCallback {
        void onBarcodeDetected(String barcode);
    }
}
