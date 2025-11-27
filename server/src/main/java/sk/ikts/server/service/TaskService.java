package sk.ikts.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.ikts.server.dto.CreateTaskRequest;
import sk.ikts.server.dto.TaskDTO;
import sk.ikts.server.model.Task;
import sk.ikts.server.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for task management operations
 * Handles task creation, retrieval, and status updates
 * Added by Cursor AI - service layer for task business logic
 */
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a new task
     * @param request task creation request
     * @return TaskDTO of created task
     */
    public TaskDTO createTask(CreateTaskRequest request) {
        Task task = new Task(request.getGroupId(), request.getCreatedBy(), 
                            request.getTitle(), request.getDescription());
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        task = taskRepository.save(task);
        
        // Send notification about new task
        notificationService.notifyNewTask(request.getGroupId(), task.getTaskId(), task.getTitle());
        
        return convertToDTO(task);
    }

    /**
     * Get all tasks for a group
     * @param groupId group ID
     * @return List of tasks
     */
    public List<TaskDTO> getTasksByGroup(Long groupId) {
        return taskRepository.findByGroupId(groupId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get task by ID
     * @param taskId task ID
     * @return TaskDTO or null if not found
     */
    public TaskDTO getTaskById(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return null;
        }
        return convertToDTO(taskOpt.get());
    }

    /**
     * Update task status
     * @param taskId task ID
     * @param status new status
     * @return TaskDTO or null if task doesn't exist
     */
    public TaskDTO updateTaskStatus(Long taskId, Task.TaskStatus status) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return null;
        }

        Task task = taskOpt.get();
        task.setStatus(status);
        task = taskRepository.save(task);

        // Send notification about status change
        notificationService.notifyTaskStatusChange(
                task.getGroupId(), 
                task.getTaskId(), 
                task.getTitle(), 
                status.toString()
        );

        return convertToDTO(task);
    }

    /**
     * Update task (title, description, status, deadline)
     * @param taskId task ID
     * @param title new title (can be null to keep existing)
     * @param description new description (can be null to keep existing)
     * @param status new status (can be null to keep existing)
     * @param deadline new deadline (can be null to keep existing)
     * @return TaskDTO or null if task doesn't exist
     */
    public TaskDTO updateTask(Long taskId, String title, String description, Task.TaskStatus status, LocalDateTime deadline) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return null;
        }

        Task task = taskOpt.get();
        
        if (title != null && !title.trim().isEmpty()) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (status != null) {
            task.setStatus(status);
            // Send notification about status change
            notificationService.notifyTaskStatusChange(
                    task.getGroupId(), 
                    task.getTaskId(), 
                    task.getTitle(), 
                    status.toString()
            );
        }
        if (deadline != null) {
            task.setDeadline(deadline);
        }
        
        task = taskRepository.save(task);
        return convertToDTO(task);
    }

    /**
     * Convert Task entity to DTO
     */
    private TaskDTO convertToDTO(Task task) {
        return new TaskDTO(
                task.getTaskId(),
                task.getGroupId(),
                task.getCreatedBy(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDeadline(),
                task.getCreatedAt()
        );
    }
}

