package sk.ikts.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.ikts.server.dto.ChatMessageDTO;
import sk.ikts.server.dto.CreateChatMessageRequest;
import sk.ikts.server.model.ChatMessage;
import sk.ikts.server.model.User;
import sk.ikts.server.repository.ChatMessageRepository;
import sk.ikts.server.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for chat message operations
 */
@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Create a new chat message
     */
    public ChatMessageDTO createMessage(CreateChatMessageRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessage chatMessage = new ChatMessage(
                request.getGroupId(),
                request.getUserId(),
                user.getName(),
                request.getMessage()
        );

        chatMessage = chatMessageRepository.save(chatMessage);
        
        // Log activity
        activityLogService.logActivity(request.getUserId(), "SEND_CHAT_MESSAGE", 
                "Sent message in group " + request.getGroupId() + ": " + 
                (request.getMessage().length() > 50 ? request.getMessage().substring(0, 50) + "..." : request.getMessage()));
        
        return convertToDTO(chatMessage);
    }

    /**
     * Get all messages for a group
     */
    public List<ChatMessageDTO> getMessagesByGroup(Long groupId) {
        List<ChatMessage> messages = chatMessageRepository.findByGroupIdOrderBySentAtAsc(groupId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent messages for a group (last 50)
     */
    public List<ChatMessageDTO> getRecentMessagesByGroup(Long groupId) {
        List<ChatMessage> messages = chatMessageRepository.findTop50ByGroupIdOrderBySentAtDesc(groupId);
        // Reverse to get chronological order
        return messages.stream()
                .sorted((m1, m2) -> m1.getSentAt().compareTo(m2.getSentAt()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     */
    private ChatMessageDTO convertToDTO(ChatMessage message) {
        return new ChatMessageDTO(
                message.getMessageId(),
                message.getGroupId(),
                message.getUserId(),
                message.getUserName(),
                message.getMessage(),
                message.getSentAt()
        );
    }
}




