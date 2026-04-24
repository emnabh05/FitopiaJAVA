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
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8085), 0);
            System.out.println("[🚀] Serveur demarre sur toutes les interfaces (0.0.0.0:8085)");
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange t) throws IOException {
                    System.out.println("[🛰️ INFO] Connexion recue de : " + t.getRemoteAddress());
                    System.out.println("[🔍 Diagnostic] Methode : " + t.getRequestMethod() + " | URI : " + t.getRequestURI());
                    
                    t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    t.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS, GET");
                    t.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
                    
                    if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
                        t.sendResponseHeaders(204, -1);
                        return;
                    }

                    if ("POST".equals(t.getRequestMethod())) {
                        InputStream is = t.getRequestBody();
                        byte[] bodyBytes = is.readAllBytes();
                        String barcode = new String(bodyBytes, StandardCharsets.UTF_8).trim();
                        
                        System.out.println("[✅ SUCCES] Code recu : " + barcode);
                        
                        javafx.application.Platform.runLater(() -> {
                            if (onBarcodeReceived != null) onBarcodeReceived.accept(barcode);
                        });

                        String response = "OK";
                        t.sendResponseHeaders(200, response.length());
                        OutputStream os = t.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } else {
                        String response = "Le serveur est pret ! Utilisez un POST pour envoyer le code.";
                        t.sendResponseHeaders(200, response.length());
                        OutputStream os = t.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
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
