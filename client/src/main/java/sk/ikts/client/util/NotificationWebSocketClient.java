package sk.ikts.client.util;

import com.google.gson.Gson;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import sk.ikts.client.model.Notification;

import java.net.URI;
import java.util.function.Consumer;

/**
 * WebSocket client for real-time notifications
 * Connects to server WebSocket endpoint and handles notifications
 */
public class NotificationWebSocketClient {
    private static final String WS_URL = "ws://127.0.0.1:8081/ws/simple";
    private WebSocketClient ws;
    private Consumer<Notification> onNotificationCallback;
    private final Gson gson = ApiClient.getGson();
    private boolean connected = false;

    /**
     * Connect to WebSocket server
     * @param onNotification callback for received notifications
     */
    public void connect(Consumer<Notification> onNotification) {
        this.onNotificationCallback = onNotification;
        
        try {
            URI serverUri = new URI(WS_URL);
            ws = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    connected = true;
                    System.out.println("WebSocket connected");
                }

                @Override
                public void onMessage(String message) {
                    try {
                        Notification notification = gson.fromJson(message, Notification.class);
                        if (onNotificationCallback != null && notification != null) {
                            Platform.runLater(() -> onNotificationCallback.accept(notification));
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing notification: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    System.out.println("WebSocket disconnected: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                    connected = false;
                }
            };
            
            ws.connect();
        } catch (Exception e) {
            System.err.println("Failed to connect WebSocket: " + e.getMessage());
            connected = false;
        }
    }

    /**
     * Disconnect from WebSocket server
     */
    public void disconnect() {
        if (ws != null && connected) {
            try {
                ws.close();
            } catch (Exception e) {
                System.err.println("Error disconnecting WebSocket: " + e.getMessage());
            }
            connected = false;
        }
    }

    /**
     * Check if connected
     */
    public boolean isConnected() {
        return connected && ws != null && ws.isOpen();
    }
}

