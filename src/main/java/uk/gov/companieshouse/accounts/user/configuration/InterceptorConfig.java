package uk.gov.companieshouse.accounts.user.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication.applicationNameSpace;
import uk.gov.companieshouse.accounts.user.interceptor.EricAuthorisedKeyPrivilegesInterceptor;
import uk.gov.companieshouse.accounts.user.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.api.interceptor.RolePermissionInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    
    private static final String USERS_ENDPOINTS = "/users/**";
    private static final String INTERNAL_USERS_ENDPOINTS = "/internal/users/**";
    private static final String ADMIN_ROLE_ENDPOINTS = "/internal/admin/roles/**";

    private static final String WILDCARD = "/**";
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
        addRolePermissionInterceptor(registry, INTERNAL_USERS_ENDPOINTS, "/admin/user/search");
        addRolePermissionInterceptor(registry, ADMIN_ROLE_ENDPOINTS, "/admin/roles");
    }

    /**
     * Interceptor that logs all calls to endpoints
     *
     * @param registry The spring interceptor registry
     */
    private void addLoggingInterceptor( final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
        .addPathPatterns(WILDCARD);
    }
     
    private void addRolePermissionInterceptor(final InterceptorRegistry registry, final String path, final String permission){
        registry.addInterceptor(new RolePermissionInterceptor(applicationNameSpace, permission))
        .addPathPatterns(path);
    }

    private void addEricInterceptors( final InterceptorRegistry registry){
        registry.addInterceptor(
                new EricAuthorisedKeyPrivilegesInterceptor()
        ).addPathPatterns(USERS_ENDPOINTS);
    }
}
