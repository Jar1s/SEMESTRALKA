package sk.ikts.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.ikts.server.model.Membership;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Membership entity
 * Provides CRUD operations and custom queries
 */
@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    /**
     * Find all memberships for a specific user
     * @param userId user ID
     * @return List of memberships
     */
    List<Membership> findByUserId(Long userId);

    /**
     * Find all memberships for a specific group
     * @param groupId group ID
     * @return List of memberships
     */
    List<Membership> findByGroupId(Long groupId);

    /**
     * Find membership by user and group
     * @param userId user ID
     * @param groupId group ID
     * @return Optional Membership if found
     */
    Optional<Membership> findByUserIdAndGroupId(Long userId, Long groupId);

    /**
     * Check if user is member of group
     * @param userId user ID
     * @param groupId group ID
     * @return true if membership exists
     */
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);
}










