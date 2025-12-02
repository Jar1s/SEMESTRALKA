package sk.ikts.client.util;

import com.google.gson.Gson;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import sk.ikts.client.model.ChatMessage;

import java.net.URI;
import java.util.function.Consumer;

/**
 * WebSocket client for real-time chat messages
 */
public class ChatWebSocketClient {
    private WebSocketClient ws;
    private Consumer<ChatMessage> onMessageCallback;
    private final Gson gson = ApiClient.getGson();
    private boolean connected = false;
    private Long groupId;

    /**
     * Connect to chat WebSocket for a specific group
     */
    public void connect(Long groupId, Consumer<ChatMessage> onMessage) {
        this.groupId = groupId;
        this.onMessageCallback = onMessage;
        
        try {
            // Use simple WebSocket endpoint for chat
            String wsUrl = "ws://127.0.0.1:8081/ws/simple";
            URI serverUri = new URI(wsUrl);
            
            ws = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    connected = true;
                    System.out.println("Chat WebSocket connected for group: " + groupId);
                }

                @Override
                public void onMessage(String message) {
                    try {
                        // Try to parse as chat message
                        ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
                        if (chatMessage != null && chatMessage.getGroupId() != null && 
                            chatMessage.getGroupId().equals(ChatWebSocketClient.this.groupId)) {
                            if (onMessageCallback != null) {
                                Platform.runLater(() -> onMessageCallback.accept(chatMessage));
                            }
                        }
                    } catch (Exception e) {
                        // Not a chat message, ignore
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    System.out.println("Chat WebSocket disconnected: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("Chat WebSocket error: " + ex.getMessage());
                    connected = false;
                }
            };
            
            ws.connect();
        } catch (Exception e) {
            System.err.println("Failed to connect chat WebSocket: " + e.getMessage());
            connected = false;
        }
    }

    /**
     * Disconnect from WebSocket
     */
    public void disconnect() {
        if (ws != null && connected) {
            try {
                ws.close();
            } catch (Exception e) {
                System.err.println("Error disconnecting chat WebSocket: " + e.getMessage());
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

