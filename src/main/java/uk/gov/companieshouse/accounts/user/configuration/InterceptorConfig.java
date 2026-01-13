package uk.gov.companieshouse.accounts.user.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil.APPLICATION_NAMESPACE;

import uk.gov.companieshouse.accounts.user.interceptor.AdminUserRolePermissionInterceptor;
import uk.gov.companieshouse.accounts.user.interceptor.EricAuthorisedKeyPrivilegesInterceptor;
import uk.gov.companieshouse.accounts.user.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.api.interceptor.RolePermissionInterceptor;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;

@Configuration
@ComponentScan("uk.gov.companieshouse.api.interceptor")
public class InterceptorConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final TokenPermissionsInterceptor tokenPermissionsInterceptor;
    public static final String USER_PROFILE = "/user/profile";

    private static final String USERS_ENDPOINTS = "/users/**";
    private static final String INTERNAL_USERS_ENDPOINTS = "/internal/users/**";
    private static final String ADMIN_ROLE_ENDPOINTS = "/internal/admin/roles/**";
    private static final String ADMIN_PERMISSION_ENDPOINTS = "/internal/admin/permissions/**";
    private static final String INTERNAL_ADMIN_USERS_ENDPOINTS = "/internal/admin/users/**";
    private static final String ADMIN_USER_SEARCH_PERMISSION = "/admin/user/search";

    private static final String WILDCARD = "/**";
    public InterceptorConfig( final LoggingInterceptor loggingInterceptor, final TokenPermissionsInterceptor tokenPermissionsInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.tokenPermissionsInterceptor = tokenPermissionsInterceptor;
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
        addRolePermissionInterceptor(registry, INTERNAL_USERS_ENDPOINTS, ADMIN_USER_SEARCH_PERMISSION);
        // Add role permission interceptor for admin role management endpoints
        addRolePermissionInterceptor(registry, ADMIN_ROLE_ENDPOINTS, "/admin/roles");
        addRolePermissionInterceptor(registry, ADMIN_PERMISSION_ENDPOINTS, "/admin/permissions");
        addTokenPermissionsInterceptor(registry);
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
        registry.addInterceptor(new RolePermissionInterceptor(APPLICATION_NAMESPACE, permission))
        .addPathPatterns(path);
    }

    private void addEricInterceptors( final InterceptorRegistry registry){
        registry.addInterceptor( new EricAuthorisedKeyPrivilegesInterceptor() ).addPathPatterns( USERS_ENDPOINTS );
        registry.addInterceptor( new AdminUserRolePermissionInterceptor() ).addPathPatterns( INTERNAL_ADMIN_USERS_ENDPOINTS );
    }
    /**
     * Interceptor for validating calls from ERIC to authenticate keys/tokens
     *
     * @param registry The spring interceptor registry
     */
    private void addTokenPermissionsInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(tokenPermissionsInterceptor).addPathPatterns(USER_PROFILE);
    }
}
