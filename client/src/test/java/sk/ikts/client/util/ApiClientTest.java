package sk.ikts.client.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testy pre ApiClient
 * 
 * Poznámka: ApiClient používa statické metódy s hardcoded BASE_URL,
 * takže plné testovanie by vyžadovalo refaktoring na dependency injection.
 * Tento test overuje základnú funkcionalitu Gson objektu.
 */
class ApiClientTest {

    @Test
    void testGetGson_NotNull() {
        // Test that Gson instance is available
        assertNotNull(ApiClient.getGson(), "Gson instance should not be null");
    }

    @Test
    void testGetGson_ReturnsSameInstance() {
        // Test that getGson returns the same instance (singleton pattern)
        var gson1 = ApiClient.getGson();
        var gson2 = ApiClient.getGson();
        assertSame(gson1, gson2, "Gson should return the same instance");
    }
}

