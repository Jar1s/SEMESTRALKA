package sk.ikts.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.ikts.server.model.ChatMessage;

import java.util.List;

/**
 * Repository for chat message operations
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Find all messages for a group, ordered by sent time
     */
    List<ChatMessage> findByGroupIdOrderBySentAtAsc(Long groupId);
    
    /**
     * Find recent messages for a group (last N messages)
     */
    List<ChatMessage> findTop50ByGroupIdOrderBySentAtDesc(Long groupId);
}




