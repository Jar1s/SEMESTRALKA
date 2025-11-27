package sk.ikts.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.ikts.server.model.Task;

import java.util.List;

/**
 * Repository interface for Task entity
 * Provides CRUD operations and custom queries
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks for a specific group
     * @param groupId group ID
     * @return List of tasks
     */
    List<Task> findByGroupId(Long groupId);

    /**
     * Find all tasks created by a specific user
     * @param createdBy user ID
     * @return List of tasks
     */
    List<Task> findByCreatedBy(Long createdBy);

    /**
     * Find all tasks for a group with specific status
     * @param groupId group ID
     * @param status task status
     * @return List of tasks
     */
    List<Task> findByGroupIdAndStatus(Long groupId, Task.TaskStatus status);
}










