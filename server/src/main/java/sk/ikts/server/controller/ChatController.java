package sk.ikts.server.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import sk.ikts.server.config.SimpleWebSocketHandler;
import sk.ikts.server.dto.ChatMessageDTO;
import sk.ikts.server.dto.CreateChatMessageRequest;
import sk.ikts.server.service.ChatService;

import java.util.List;

/**
 * REST Controller for chat operations
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired(required = false)
    private SimpleWebSocketHandler simpleWebSocketHandler;

    /**
     * Get all messages for a group
     * GET /api/chat/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable("groupId") Long groupId) {
        try {
            List<ChatMessageDTO> messages = chatService.getRecentMessagesByGroup(groupId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Error getting messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send a chat message
     * POST /api/chat/send
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody CreateChatMessageRequest request) {
        try {
            ChatMessageDTO message = chatService.createMessage(request);
            
            // Broadcast message to all group members via STOMP
            messagingTemplate.convertAndSend("/topic/chat/group/" + request.getGroupId(), message);
            
            // Also broadcast via simple WebSocket for Java clients
            if (simpleWebSocketHandler != null) {
                simpleWebSocketHandler.broadcastChatMessage(message);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send message: " + e.getMessage());
        }
    }
}

