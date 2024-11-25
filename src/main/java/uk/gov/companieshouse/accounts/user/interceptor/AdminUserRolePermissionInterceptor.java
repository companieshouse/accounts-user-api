package uk.gov.companieshouse.accounts.user.interceptor;

import static uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil.APPLICATION_NAMESPACE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.interceptor.RolePermissionInterceptor;

public class AdminUserRolePermissionInterceptor implements HandlerInterceptor {

    private static final String ADMIN_USER_SEARCH_PERMISSION = "/admin/user/search";
    private static final String ADMIN_USER_UNLINKONELOGIN_PERMISSION = "/admin/user/unlinkonelogin";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_PATCH = "PATCH";

    @Override
    public boolean preHandle( final HttpServletRequest request, final HttpServletResponse response, final Object handler ) throws IOException {
        final var method = request.getMethod().toUpperCase();
        return switch ( method ) {
            case METHOD_GET -> new RolePermissionInterceptor( APPLICATION_NAMESPACE, ADMIN_USER_SEARCH_PERMISSION ).preHandle( request, response, handler );
            case METHOD_PATCH -> new RolePermissionInterceptor( APPLICATION_NAMESPACE, ADMIN_USER_UNLINKONELOGIN_PERMISSION ).preHandle( request, response, handler );
            default -> true;
        };
    }

}
