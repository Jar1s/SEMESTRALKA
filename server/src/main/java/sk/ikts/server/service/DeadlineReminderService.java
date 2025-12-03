package sk.ikts.server.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sk.ikts.server.model.Task;
import sk.ikts.server.repository.TaskRepository;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    
    private final Gson gson = new Gson();

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
            
            // Get custom reminders or use defaults
            List<Integer> reminders = getRemindersForTask(task);
            
            // Check each reminder threshold
            for (Integer reminderHours : reminders) {
                if (reminderHours == null) continue;
                
                // Check if we're within the reminder window (within 1 hour of the reminder time)
                boolean isWithinReminderWindow = hoursUntilDeadline <= reminderHours && 
                                                hoursUntilDeadline > (reminderHours - 1);
                
                if (isWithinReminderWindow) {
                    String reminderKey = taskKey + "_reminder_" + reminderHours;
                    
                    // Only send if we haven't sent this reminder yet
                    if (!notifiedTasks.contains(reminderKey)) {
                        if (reminderHours <= 1 && minutesUntilDeadline > 0) {
                            // Less than 1 hour - urgent notification
                            notificationService.notifyDeadlineApproaching(
                                task.getGroupId(),
                                task.getTaskId(),
                                task.getTitle(),
                                minutesUntilDeadline,
                                true
                            );
                        } else if (reminderHours <= 6) {
                            // Warning notification
                            notificationService.notifyDeadlineApproaching(
                                task.getGroupId(),
                                task.getTaskId(),
                                task.getTitle(),
                                hoursUntilDeadline,
                                false
                            );
                        } else {
                            // Regular reminder
                            notificationService.notifyDeadlineReminder(
                                task.getGroupId(),
                                task.getTaskId(),
                                task.getTitle(),
                                hoursUntilDeadline
                            );
                        }
                        notifiedTasks.add(reminderKey);
                    }
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
     * Get reminders for a task - either custom reminders or default ones
     */
    private List<Integer> getRemindersForTask(Task task) {
        if (task.getReminders() != null && !task.getReminders().isEmpty()) {
            try {
                Type listType = new TypeToken<List<Integer>>(){}.getType();
                List<Integer> customReminders = gson.fromJson(task.getReminders(), listType);
                if (customReminders != null && !customReminders.isEmpty()) {
                    return customReminders;
                }
            } catch (Exception e) {
                // If parsing fails, fall back to defaults
            }
        }
        // Default reminders: 24h, 6h, 1h
        List<Integer> defaults = new ArrayList<>();
        defaults.add(24);
        defaults.add(6);
        defaults.add(1);
        return defaults;
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

