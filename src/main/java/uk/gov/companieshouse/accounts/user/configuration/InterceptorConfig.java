package uk.gov.companieshouse.accounts.user.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication.applicationNameSpace;
import uk.gov.companieshouse.accounts.user.interceptor.EricAuthorisedKeyPrivilegesInterceptor;
import uk.gov.companieshouse.accounts.user.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.accounts.user.interceptor.UserProfilePermissionInterceptor;
import uk.gov.companieshouse.api.interceptor.RolePermissionInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final UserProfilePermissionInterceptor userProfilePermissionInterceptor;
    public static final String USER_PROFILE = "/user/profile";
    
    private static final String USERS_ENDPOINTS = "/users/**";
    private static final String INTERNAL_USERS_ENDPOINTS = "/internal/users/**";
    private static final String ADMIN_ROLE_ENDPOINTS = "/internal/admin/roles/**";
    private static final String INTERNAL_GET_USER_RECORD_ENDPOINT = "/internal/admin/users/**";
    private static final String ADMIN_USER_SEARCH_PERMISSION = "/admin/user/search";
    
    private static final String WILDCARD = "/**";
    public InterceptorConfig(final LoggingInterceptor loggingInterceptor, final UserProfilePermissionInterceptor userProfilePermissionInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.userProfilePermissionInterceptor = userProfilePermissionInterceptor;
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
        addRolePermissionInterceptor(registry, INTERNAL_GET_USER_RECORD_ENDPOINT,ADMIN_USER_SEARCH_PERMISSION );
        addRolePermissionInterceptor(registry, INTERNAL_USERS_ENDPOINTS, ADMIN_USER_SEARCH_PERMISSION);
        addRolePermissionInterceptor(registry, ADMIN_ROLE_ENDPOINTS, "/admin/roles");
        addUserProfileInterceptor(registry);
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

    /**
     * Interceptor that checks for user profile read permission
     * @param registry The spring interceptor registry
     */
    private void addUserProfileInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(userProfilePermissionInterceptor).addPathPatterns("/user/profile");
    }
}
