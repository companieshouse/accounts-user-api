package uk.gov.companieshouse.accounts.user.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetrySdkAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@Tag("unit-test")
class OpenTelemetryAppenderInitializerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(OpenTelemetryAppenderInitializer.class)
            .withConfiguration(AutoConfigurations.of(OpenTelemetrySdkAutoConfiguration.class));

    @Test
    void afterPropertiesSetInstallsOpenTelemetryAppender() {
        final var openTelemetry = Mockito.mock(OpenTelemetry.class);
        final var initializer = new OpenTelemetryAppenderInitializer(openTelemetry);

        try (MockedStatic<OpenTelemetryAppender> appender = mockStatic(OpenTelemetryAppender.class)) {
            initializer.afterPropertiesSet();
            appender.verify(() -> OpenTelemetryAppender.install(openTelemetry));
        }
    }

    @Test
    void beanIsAbsentWhenOpenTelemetryDisabledByDefault() {
        contextRunner.run(context ->
                assertFalse(context.containsBean("openTelemetryAppenderInitializer")));
    }

    @Test
    void beanIsPresentWhenOpenTelemetryExplicitlyEnabled() {
        contextRunner
                .withPropertyValues("management.opentelemetry.enabled=true")
                .run(context ->
                        assertTrue(context.containsBean("openTelemetryAppenderInitializer")));
    }

}
