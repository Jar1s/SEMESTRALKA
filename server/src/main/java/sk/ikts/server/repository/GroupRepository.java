package sk.ikts.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.ikts.server.model.Group;

import java.util.List;

/**
 * Repository interface for Group entity
 * Provides CRUD operations and custom queries
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * Find all groups created by a specific user
     * @param createdBy user ID
     * @return List of groups
     */
    List<Group> findByCreatedBy(Long createdBy);
}










