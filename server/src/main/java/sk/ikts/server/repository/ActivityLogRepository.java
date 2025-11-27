package sk.ikts.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.ikts.server.model.ActivityLog;

import java.util.List;

/**
 * Repository interface for ActivityLog entity
 * Provides CRUD operations and custom queries
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Find all activity logs for a specific user
     * @param userId user ID
     * @return List of activity logs
     */
    List<ActivityLog> findByUserId(Long userId);

    /**
     * Find all activity logs with specific action
     * @param action action type
     * @return List of activity logs
     */
    List<ActivityLog> findByAction(String action);
}










