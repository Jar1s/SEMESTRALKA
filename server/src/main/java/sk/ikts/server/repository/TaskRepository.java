package sk.ikts.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sk.ikts.server.model.Task;

import java.time.LocalDateTime;
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

    /**
     * Find all tasks with deadlines between start and end time
     * @param start start time
     * @param end end time
     * @return List of tasks with deadlines in the specified range
     */
    @Query("SELECT t FROM Task t WHERE t.deadline IS NOT NULL AND t.deadline >= :start AND t.deadline <= :end AND t.status != 'DONE'")
    List<Task> findTasksWithDeadlinesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Find all tasks with deadlines in the next hours
     * @param now current time
     * @param hours number of hours ahead
     * @return List of tasks with deadlines within the specified hours
     */
    @Query("SELECT t FROM Task t WHERE t.deadline IS NOT NULL AND t.deadline > :now AND t.deadline <= :futureTime AND t.status != 'DONE'")
    List<Task> findTasksWithUpcomingDeadlines(@Param("now") LocalDateTime now, @Param("futureTime") LocalDateTime futureTime);
}










