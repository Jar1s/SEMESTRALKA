package sk.ikts.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.ikts.server.model.ActivityLog;
import sk.ikts.server.repository.ActivityLogRepository;

/**
 * Service for logging user activities
 * Records user actions for analytics and audit purposes
 */
@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    /**
     * Log a user activity
     * @param userId user ID who performed the action
     * @param action action type (e.g., "CREATE_GROUP", "CREATE_TASK", "LOGIN")
     * @param details additional details about the action
     */
    public void logActivity(Long userId, String action, String details) {
        try {
            ActivityLog log = new ActivityLog(userId, action, details);
            activityLogRepository.save(log);
        } catch (Exception e) {
            // Don't fail the main operation if logging fails
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }

    /**
     * Log a user activity without details
     * @param userId user ID who performed the action
     * @param action action type
     */
    public void logActivity(Long userId, String action) {
        logActivity(userId, action, null);
    }
}

