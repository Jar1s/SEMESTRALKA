package sk.ikts.server.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Database initializer that ensures required columns exist
 * Handles SQLite limitations with ALTER TABLE
 */
@Component
@Order(1)
public class DatabaseInitializer {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        if (jdbcTemplate == null) {
            return; // JdbcTemplate not available
        }

        try {
            // Check if users table exists
            String checkTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='users'";
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(checkTable);
            
            if (tables.isEmpty()) {
                // Table doesn't exist, Hibernate will create it
                return;
            }

            // Database schema is managed by Hibernate
            
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

