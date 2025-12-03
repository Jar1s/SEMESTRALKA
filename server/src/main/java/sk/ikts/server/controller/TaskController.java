package sk.ikts.server.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.ikts.server.dto.CreateTaskRequest;
import sk.ikts.server.dto.TaskDTO;
import sk.ikts.server.model.Task;
import sk.ikts.server.service.TaskService;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for task operations
 * Handles task CRUD operations and status updates
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * Create a new task
     * POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskDTO task = taskService.createTask(request);
        return ResponseEntity.ok(task);
    }

    /**
     * Get tasks by group
     * GET /api/tasks/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<TaskDTO>> getTasksByGroup(@PathVariable("groupId") Long groupId) {
        List<TaskDTO> tasks = taskService.getTasksByGroup(groupId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get task by ID
     * GET /api/tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable("id") Long id) {
        TaskDTO task = taskService.getTaskById(id);
        
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(task);
    }

    /**
     * Update task status
     * PUT /api/tasks/{id}/status
     * Body should be JSON: {"status": "OPEN"} or {"status": "IN_PROGRESS"} or {"status": "DONE"}
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().build();
            }
            Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(statusStr.toUpperCase());
            TaskDTO task = taskService.updateTaskStatus(id, taskStatus);
            
            if (task == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update task (full update)
     * PUT /api/tasks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable("id") Long id, @RequestBody Map<String, Object> request) {
        try {
            TaskDTO task = taskService.getTaskById(id);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }

            String title = request.containsKey("title") ? (String) request.get("title") : null;
            String description = request.containsKey("description") ? (String) request.get("description") : null;
            Task.TaskStatus status = null;
            if (request.containsKey("status")) {
                try {
                    status = Task.TaskStatus.valueOf(request.get("status").toString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            }
            
            java.time.LocalDateTime deadline = null;
            if (request.containsKey("deadline") && request.get("deadline") != null) {
                try {
                    String deadlineStr = request.get("deadline").toString();
                    if (!deadlineStr.isEmpty()) {
                        deadline = java.time.LocalDateTime.parse(deadlineStr, 
                            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                } catch (Exception e) {
                    // Ignore deadline parsing errors
                }
            }
            
            String reminders = null;
            if (request.containsKey("reminders") && request.get("reminders") != null) {
                reminders = request.get("reminders").toString();
            }

            TaskDTO updatedTask = taskService.updateTask(id, title, description, status, deadline, reminders);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

