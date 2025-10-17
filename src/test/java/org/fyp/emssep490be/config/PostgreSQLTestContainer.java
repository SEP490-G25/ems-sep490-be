package org.fyp.emssep490be.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainers configuration for integration tests requiring actual PostgreSQL.
 *
 * Usage: Add @ContextConfiguration(initializers = PostgreSQLTestContainer.Initializer.class) to your test class.
 *
 * This provides a real PostgreSQL 16 instance with proper enum support,
 * which is critical for testing enum types defined in schema.sql.
 */
@TestConfiguration
public class PostgreSQLTestContainer {

    private static final PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("ems_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true); // Reuse container across tests for performance
        postgresContainer.start();
    }

    /**
     * Application context initializer to configure Spring Boot datasource
     * properties from the TestContainers PostgreSQL instance.
     */
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresContainer.getUsername(),
                    "spring.datasource.password=" + postgresContainer.getPassword(),
                    "spring.datasource.driver-class-name=org.postgresql.Driver",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
