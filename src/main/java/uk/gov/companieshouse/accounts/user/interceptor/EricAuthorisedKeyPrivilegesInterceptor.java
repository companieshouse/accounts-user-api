package uk.gov.companieshouse.accounts.user.interceptor;

import static uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil.APPLICATION_NAMESPACE;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class EricAuthorisedKeyPrivilegesInterceptor extends InternalUserInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    public EricAuthorisedKeyPrivilegesInterceptor() {
        super();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (AuthorisationUtil.isOauth2User(request) && AuthorisationUtil.getAuthorisedRoles(request).contains("/admin/user/roles")) {
            return true;
        } else if ( super.preHandle( request, response, handler ) ) {
            final var privileges =
            Optional.ofNullable( request.getHeader("ERIC-Authorised-Key-Privileges") )
                    .map(s -> s.split(","))
                    .orElse(new String[]{});

            final var hasInternalPrivilege = ArrayUtils.contains(privileges, "internal-app");
            final var hasUserDataPrivilege = ArrayUtils.contains(privileges, "user-data");

            if( hasInternalPrivilege && hasUserDataPrivilege ){
                LOG.debug( "Caller authorised with internal-app and user-data privileges" );
                return true;
            } else {
                LOG.error( "Caller does not have required privileges" );
                response.setStatus( 401 );
                return false;
            }
        }
        return false;
    }
}
