package tn.esprit.Pidev3A49.test;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FaceIdController {

    @FXML
    private ImageView cameraPreview;

    @FXML
    private Label statusLabel;

    private Webcam webcam;
    private ScheduledExecutorService executor;
    private BufferedImage currentFrame;

    @FXML
    public void initialize() {
        openCamera();
    }

    @FXML
    private void handleCapture() {
        if (currentFrame == null) {
            statusLabel.setText("Aucune image disponible. Verifiez la camera.");
            return;
        }

        try {
            Path facesDir = Paths.get("faces");
            Files.createDirectories(facesDir);
            String fileName = "face_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";
            Path output = facesDir.resolve(fileName);
            ImageIO.write(currentFrame, "PNG", output.toFile());
            statusLabel.setText("Visage capture: " + output.toAbsolutePath());
            openCrudPage();
        } catch (IOException e) {
            statusLabel.setText("Erreur de sauvegarde: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetry() {
        statusLabel.setText("Camera relancee.");
        stopCamera();
        openCamera();
    }

    @FXML
    private void handleCancel() {
        stopCamera();
        openLoginPage();
    }

    private void openCamera() {
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                statusLabel.setText("Aucune camera detectee.");
                return;
            }

            webcam.setViewSize(new Dimension(640, 480));
            webcam.open();
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                if (webcam != null && webcam.isOpen()) {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        currentFrame = image;
                        Platform.runLater(() -> cameraPreview.setImage(SwingFXUtils.toFXImage(image, null)));
                    }
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
            statusLabel.setText("Camera active. Positionnez votre visage puis capturez.");
        } catch (Exception e) {
            statusLabel.setText("Impossible d'ouvrir la camera: " + e.getMessage());
        }
    }

    private void stopCamera() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        if (webcam != null) {
            webcam.close();
            webcam = null;
        }
    }

    private void openCrudPage() {
        stopCamera();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserCrud.fxml"));
            Scene scene = new Scene(loader.load(), 1450, 920);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setTitle("Fitopia User CRUD");
            stage.setScene(scene);
            stage.setMinWidth(1200);
            stage.setMinHeight(820);
        } catch (IOException e) {
            statusLabel.setText("Impossible d'ouvrir la page user: " + e.getMessage());
        }
    }

    private void openLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 640);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setTitle("Fitopia Login");
            stage.setScene(scene);
            stage.setMinWidth(1100);
            stage.setMinHeight(620);
        } catch (IOException e) {
            statusLabel.setText("Impossible de revenir au login: " + e.getMessage());
        }
    }
}
