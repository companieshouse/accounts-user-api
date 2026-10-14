package uk.gov.companieshouse.accounts.user.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boots the application using the real {@code application.properties} (not a shadowed
 * test-resources copy) with OpenTelemetry explicitly enabled, to confirm the
 * {@code OpenTelemetryAppenderInitializer} bean wires up and the context starts successfully.
 */
@SpringBootTest
@Tag("integration-test")
class OpenTelemetryIntegrationTest extends BaseMongoIntegration {

    @Autowired
    private ApplicationContext applicationContext;

    @DynamicPropertySource
    static void setOpenTelemetryProperties( final DynamicPropertyRegistry registry ) {
        registry.add( "OTEL_LOG_ENABLED", () -> "true" );
    }

    @Test
    void contextStartsWithOpenTelemetryEnabled() {
        assertNotNull( applicationContext );
        assertTrue( applicationContext.containsBean( "openTelemetryAppenderInitializer" ) );
    }

}
