package sk.ikts.server.config;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import sk.ikts.server.dto.ChatMessageDTO;
import sk.ikts.server.dto.NotificationDTO;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Simple WebSocket handler for notifications
 * Handles direct WebSocket connections (non-STOMP)
 */
@Component
public class SimpleWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final Gson gson = GsonConfig.createGson();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages if needed
        System.out.println("Received message: " + message.getPayload());
    }

    /**
     * Broadcast notification to all connected clients
     */
    public void broadcastNotification(NotificationDTO notification) {
        String json = gson.toJson(notification);
        TextMessage message = new TextMessage(json);
        
        System.out.println("Broadcasting notification to " + sessions.size() + " connected sessions: " + notification.getType() + " - " + notification.getMessage());
        
        sessions.removeIf(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                    System.out.println("Sent notification to session: " + session.getId());
                    return false;
                } else {
                    System.out.println("Session " + session.getId() + " is closed");
                }
            } catch (IOException e) {
                System.err.println("Error sending WebSocket message: " + e.getMessage());
            }
            return true; // Remove closed sessions
        });
        
        System.out.println("Remaining active sessions: " + sessions.size());
    }

    /**
     * Broadcast chat message to all connected clients
     */
    public void broadcastChatMessage(ChatMessageDTO chatMessage) {
        String json = gson.toJson(chatMessage);
        TextMessage message = new TextMessage(json);
        
        sessions.removeIf(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                    return false;
                }
            } catch (IOException e) {
                System.err.println("Error sending chat message: " + e.getMessage());
            }
            return true; // Remove closed sessions
        });
    }
}

