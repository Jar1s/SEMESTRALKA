package sk.ikts.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sk.ikts.server.model.Task;
import sk.ikts.server.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for checking upcoming task deadlines and sending reminders
 * Runs periodically to notify users about approaching deadlines
 */
@Service
public class DeadlineReminderService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationService notificationService;

    // Track which tasks have already been notified at which threshold to avoid duplicate notifications
    // Format: "taskId_threshold" where threshold is 24, 6, or 1
    private final Set<String> notifiedTasks = new HashSet<>();

    /**
     * Check for upcoming deadlines every hour
     * Sends daily notifications for tasks with deadlines within 3 days (72 hours)
     * Each day, users receive one reminder notification
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void checkUpcomingDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDateTime in3Days = now.plusDays(3); // 3 days = 72 hours
        
        // Find tasks with deadlines within the next 3 days
        List<Task> upcomingTasks = taskRepository.findTasksWithUpcomingDeadlines(now, in3Days);
        
        for (Task task : upcomingTasks) {
            if (task.getDeadline() == null || task.getStatus() == Task.TaskStatus.DONE || task.getTaskId() == null) {
                continue;
            }
            
            long hoursUntilDeadline = ChronoUnit.HOURS.between(now, task.getDeadline());
            long minutesUntilDeadline = ChronoUnit.MINUTES.between(now, task.getDeadline());
            
            String taskKey = task.getTaskId().toString();
            String dailyKey = taskKey + "_daily_" + today; // Key for daily notification
            
            // Check if we already notified today
            boolean alreadyNotifiedToday = notifiedTasks.contains(dailyKey);
            
            // Send notification based on time remaining
            if (hoursUntilDeadline <= 1 && minutesUntilDeadline > 0) {
                // Less than 1 hour remaining - urgent notification (only once)
                String urgentKey = taskKey + "_urgent";
                if (!notifiedTasks.contains(urgentKey)) {
                    notificationService.notifyDeadlineApproaching(
                        task.getGroupId(),
                        task.getTaskId(),
                        task.getTitle(),
                        minutesUntilDeadline,
                        true
                    );
                    notifiedTasks.add(urgentKey);
                    notifiedTasks.add(dailyKey); // Mark as notified today
                }
            } else if (hoursUntilDeadline <= 6 && hoursUntilDeadline > 1) {
                // Less than 6 hours remaining - warning notification (once per day)
                if (!alreadyNotifiedToday) {
                    notificationService.notifyDeadlineApproaching(
                        task.getGroupId(),
                        task.getTaskId(),
                        task.getTitle(),
                        hoursUntilDeadline,
                        false
                    );
                    notifiedTasks.add(dailyKey);
                }
            } else if (hoursUntilDeadline <= 24 && hoursUntilDeadline > 6) {
                // Less than 24 hours remaining - reminder notification (once per day)
                if (!alreadyNotifiedToday) {
                    notificationService.notifyDeadlineReminder(
                        task.getGroupId(),
                        task.getTaskId(),
                        task.getTitle(),
                        hoursUntilDeadline
                    );
                    notifiedTasks.add(dailyKey);
                }
            } else if (hoursUntilDeadline <= 72 && hoursUntilDeadline > 24) {
                // Between 24 and 72 hours (1-3 days) - daily reminder
                if (!alreadyNotifiedToday) {
                    notificationService.notifyDeadlineReminder(
                        task.getGroupId(),
                        task.getTaskId(),
                        task.getTitle(),
                        hoursUntilDeadline
                    );
                    notifiedTasks.add(dailyKey);
                }
            }
        }
        
        // Clean up notified tasks that are past their deadline or completed
        // Also clean up old daily keys (older than today)
        notifiedTasks.removeIf(key -> {
            // Handle daily keys: taskId_daily_date
            if (key.contains("_daily_")) {
                String[] parts = key.split("_daily_");
                if (parts.length == 2) {
                    try {
                        Long taskId = Long.parseLong(parts[0]);
                        LocalDate notificationDate = LocalDate.parse(parts[1]);
                        // Remove if task is done or deadline passed, or if notification date is old
                        Task task = taskRepository.findById(taskId).orElse(null);
                        if (task == null || 
                            task.getStatus() == Task.TaskStatus.DONE || 
                            (task.getDeadline() != null && task.getDeadline().isBefore(now))) {
                            return true;
                        }
                        // Remove old daily keys (not today)
                        return !notificationDate.equals(today);
                    } catch (Exception e) {
                        return true; // Remove invalid keys
                    }
                }
            }
            // Handle other keys (urgent, overdue, etc.)
            String taskIdStr = key.split("_")[0];
            try {
                Long taskId = Long.parseLong(taskIdStr);
                Task task = taskRepository.findById(taskId).orElse(null);
                return task == null || 
                       task.getStatus() == Task.TaskStatus.DONE || 
                       (task.getDeadline() != null && task.getDeadline().isBefore(now));
            } catch (NumberFormatException e) {
                return true; // Remove invalid keys
            }
        });
    }

    /**
     * Check for overdue tasks every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void checkOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find all tasks that are not done and have passed their deadline
        List<Task> allTasks = taskRepository.findAll();
        
        for (Task task : allTasks) {
            if (task.getDeadline() != null && 
                task.getDeadline().isBefore(now) && 
                task.getStatus() != Task.TaskStatus.DONE &&
                task.getTaskId() != null) {
                
                // Send overdue notification (only once per day)
                String overdueKey = task.getTaskId() + "_overdue_" + now.toLocalDate();
                if (!notifiedTasks.contains(overdueKey)) {
                    notificationService.notifyDeadlineOverdue(
                        task.getGroupId(),
                        task.getTaskId(),
                        task.getTitle()
                    );
                    notifiedTasks.add(overdueKey);
                }
            }
        }
    }
}

