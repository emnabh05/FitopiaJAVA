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
                        String barcode = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        
                        javafx.application.Platform.runLater(() -> {
                            if (onBarcodeReceived != null) {
                                onBarcodeReceived.accept(barcode);
                            }
                        });

                        String response = "OK";
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
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Impossible de lancer le serveur sur le port 8085 (port deja utilise ?)");
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }
}
