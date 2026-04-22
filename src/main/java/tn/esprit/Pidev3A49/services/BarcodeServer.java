package tn.esprit.Pidev3A49.services;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class BarcodeServer {
    private static BarcodeServer instance;
    private HttpServer server;
    private java.util.function.Consumer<String> onBarcodeReceived;

    private BarcodeServer() {}

    public static BarcodeServer getInstance() {
        if (instance == null) {
            instance = new BarcodeServer();
        }
        return instance;
    }

    public void setOnBarcodeReceived(java.util.function.Consumer<String> onBarcodeReceived) {
        this.onBarcodeReceived = onBarcodeReceived;
    }

    public void startServer() {
        if (server != null) return; // Déjà démarré
        try {
            server = HttpServer.create(new InetSocketAddress(8085), 0);
            server.createContext("/api/barcode", new HttpHandler() {
                @Override
                public void handle(HttpExchange t) throws IOException {
                    t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    t.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS, GET");
                    
                    if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
                        t.sendResponseHeaders(204, -1);
                        return;
                    }

                    if ("POST".equals(t.getRequestMethod())) {
                        InputStream is = t.getRequestBody();
                        byte[] bodyBytes = is.readAllBytes();
                        String barcode = new String(bodyBytes, StandardCharsets.UTF_8).trim();
                        
                        System.out.println("[🛰️ Serveur] Données reçues du téléphone : " + barcode);
                        
                        javafx.application.Platform.runLater(() -> {
                            try {
                                if (onBarcodeReceived != null) {
                                    onBarcodeReceived.accept(barcode);
                                }
                            } catch (Exception e) {
                                System.err.println("[❌] Erreur pendant le traitement du barcode: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });

                        String response = "OK";
                        t.sendResponseHeaders(200, response.length());
                        OutputStream os = t.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } else if ("GET".equals(t.getRequestMethod())) {
                        String response = "Le serveur de scan est en ligne ! Tapez un code-barres via POST pour l'envoyer à l'app.";
                        t.sendResponseHeaders(200, response.length());
                        OutputStream os = t.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } else {
                         t.sendResponseHeaders(204, -1);
                    }
                }
            });
            server.setExecutor(null);
            server.start();
            System.out.println("✅ Serveur Barcode pret sur le port 8085");
        } catch (java.net.BindException e) {
            System.err.println("[❌] ERREUR : Le port 8085 est déjà utilisé. Une autre instance de l'application tourne probablement en arrière-plan.");
            System.err.println("[💡] CONSEIL : Fermez toutes les fenêtres de l'application et vérifiez le gestionnaire des tâches pour arrêter les processus Java.");
        } catch (IOException e) {
            System.err.println("⚠️ Information : Impossible de lancer le serveur réseau (" + e.getMessage() + ").");
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }
}
