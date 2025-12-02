package sk.ikts.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.ikts.server.model.User;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations plus email lookups used during auth flows.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Check if a user exists with the provided email.
     * @param email user email
     * @return true if a user already uses this email
     */
    boolean existsByEmail(String email);

    /**
     * Find a user by email.
     * @param email user email
     * @return optional user if found
     */
    Optional<User> findByEmail(String email);

}






