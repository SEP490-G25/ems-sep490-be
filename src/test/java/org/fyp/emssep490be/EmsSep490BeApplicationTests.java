package org.fyp.emssep490be;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test to verify Spring Boot application context loads successfully.
 *
 * NOTE: This test requires full PostgreSQL database with enum types created.
 * Disabled in CI/CD as it requires complex infrastructure setup.
 * Use repository integration tests (@DataJpaTest) and unit tests instead.
 *
 * To run locally: Ensure PostgreSQL is running with 'ems' database and schema.sql executed.
 */
@SpringBootTest
@Disabled("Skip context load check until Spring config stabilized")
class EmsSep490BeApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that Spring context loads successfully with all beans
    }

}
