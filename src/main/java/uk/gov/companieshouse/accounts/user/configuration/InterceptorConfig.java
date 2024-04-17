package uk.gov.companieshouse.accounts.user.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.accounts.user.interceptor.EricAuthorisedKeyPrivilegesInterceptor;
import uk.gov.companieshouse.accounts.user.interceptor.LoggingInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {


    private final LoggingInterceptor loggingInterceptor;

    public InterceptorConfig( final LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    /**
     * Setup the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in the order they are added to the registry
     *
     * @param registry The spring interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull final InterceptorRegistry registry) {
        addLoggingInterceptor(registry);
        addEricInterceptors(registry);
    }

    /**
     * Interceptor that logs all calls to endpoints
     *
     * @param registry The spring interceptor registry
     */
    private void addLoggingInterceptor( final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
    }

    private void addEricInterceptors( final InterceptorRegistry registry){
        registry.addInterceptor(
                new EricAuthorisedKeyPrivilegesInterceptor()
        ).excludePathPatterns("/*/healthcheck");
    }

}
