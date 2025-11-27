package sk.ikts.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.ikts.server.model.Resource;

import java.util.List;

/**
 * Repository interface for Resource entity
 * Provides CRUD operations and custom queries
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    /**
     * Find all resources for a specific group
     * @param groupId group ID
     * @return List of resources
     */
    List<Resource> findByGroupId(Long groupId);

    /**
     * Find all resources uploaded by a specific user
     * @param uploadedBy user ID
     * @return List of resources
     */
    List<Resource> findByUploadedBy(Long uploadedBy);
}










