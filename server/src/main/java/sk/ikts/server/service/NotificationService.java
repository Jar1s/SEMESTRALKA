package sk.ikts.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sk.ikts.server.config.SimpleWebSocketHandler;
import sk.ikts.server.dto.NotificationDTO;

/**
 * Service for sending WebSocket notifications
 * Handles real-time notifications to clients
 */
@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired(required = false)
    private SimpleWebSocketHandler simpleWebSocketHandler;

    /**
     * Send notification to all members of a group
     * @param groupId group ID
     * @param notification notification to send
     */
    public void notifyGroup(Long groupId, NotificationDTO notification) {
        System.out.println("Sending notification to group " + groupId + ": " + notification.getType() + " - " + notification.getMessage());
        messagingTemplate.convertAndSend("/topic/group/" + groupId, notification);
        // Also send via simple WebSocket (all clients receive group notifications)
        if (simpleWebSocketHandler != null) {
            System.out.println("Broadcasting notification via SimpleWebSocketHandler");
            simpleWebSocketHandler.broadcastNotification(notification);
        } else {
            System.out.println("WARNING: SimpleWebSocketHandler is null!");
        }
    }

    /**
     * Send notification to all connected clients
     * @param notification notification to send
     */
    public void notifyAll(NotificationDTO notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
        // Also send via simple WebSocket
        if (simpleWebSocketHandler != null) {
            simpleWebSocketHandler.broadcastNotification(notification);
        }
    }

    /**
     * Notify about new task
     */
    public void notifyNewTask(Long groupId, Long taskId, String taskTitle) {
        NotificationDTO notification = new NotificationDTO(
                "NEW_TASK",
                "New task created: " + taskTitle,
                groupId
        );
        notification.setTaskId(taskId);
        notifyGroup(groupId, notification);
    }

    /**
     * Notify about task status change
     */
    public void notifyTaskStatusChange(Long groupId, Long taskId, String taskTitle, String newStatus) {
        NotificationDTO notification = new NotificationDTO(
                "TASK_STATUS_CHANGED",
                "Task '" + taskTitle + "' status changed to: " + newStatus,
                groupId
        );
        notification.setTaskId(taskId);
        notifyGroup(groupId, notification);
    }

    /**
     * Notify about new group member
     */
    public void notifyNewMember(Long groupId, String memberName) {
        NotificationDTO notification = new NotificationDTO(
                "NEW_MEMBER",
                memberName + " joined the group",
                groupId
        );
        notifyGroup(groupId, notification);
    }

    /**
     * Notify about new group
     */
    public void notifyNewGroup(Long groupId, String groupName) {
        NotificationDTO notification = new NotificationDTO(
                "NEW_GROUP",
                "New group created: " + groupName,
                groupId
        );
        notifyAll(notification);
    }

    /**
     * Notify about approaching deadline (urgent - less than 1 hour)
     */
    public void notifyDeadlineApproaching(Long groupId, Long taskId, String taskTitle, long timeRemaining, boolean urgent) {
        String timeText;
        if (urgent) {
            timeText = timeRemaining <= 1 ? "less than 1 minute" : timeRemaining + " minutes";
        } else {
            timeText = timeRemaining + " hours";
        }
        
        NotificationDTO notification = new NotificationDTO(
                urgent ? "DEADLINE_URGENT" : "DEADLINE_WARNING",
                "Task '" + taskTitle + "' deadline approaching! " + 
                (urgent ? "Only " + timeText + " remaining!" : timeText + " remaining."),
                groupId
        );
        notification.setTaskId(taskId);
        notifyGroup(groupId, notification);
    }

    /**
     * Notify about deadline reminder
     * Used for tasks with deadlines within 3 days
     */
    public void notifyDeadlineReminder(Long groupId, Long taskId, String taskTitle, long hoursRemaining) {
        String message;
        if (hoursRemaining >= 24) {
            long days = hoursRemaining / 24;
            message = "Reminder: Task '" + taskTitle + "' deadline in " + days + " day" + (days > 1 ? "s" : "");
        } else {
            message = "Reminder: Task '" + taskTitle + "' deadline in " + hoursRemaining + " hours";
        }
        
        NotificationDTO notification = new NotificationDTO(
                "DEADLINE_REMINDER",
                message,
                groupId
        );
        notification.setTaskId(taskId);
        notifyGroup(groupId, notification);
    }

    /**
     * Notify about overdue deadline
     */
    public void notifyDeadlineOverdue(Long groupId, Long taskId, String taskTitle) {
        NotificationDTO notification = new NotificationDTO(
                "DEADLINE_OVERDUE",
                "⚠️ Task '" + taskTitle + "' is overdue!",
                groupId
        );
        notification.setTaskId(taskId);
        notifyGroup(groupId, notification);
    }
}





