package uk.gov.companieshouse.accounts.user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.ArrayUtils;
import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class EricAuthorisedKeyPrivilegesInterceptor extends UserAuthenticationInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(AccountsUserServiceApplication.applicationNameSpace);

    public EricAuthorisedKeyPrivilegesInterceptor(List<String> externalMethods,
            List<String> otherAllowedIdentityTypes,
            InternalUserInterceptor internalUserInterceptor) {
        super(externalMethods, otherAllowedIdentityTypes, internalUserInterceptor);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ( super.preHandle( request, response, handler ) ) {
            final var privileges =
            Optional.ofNullable( request.getHeader("ERIC-Authorised-Key-Privileges") )
                    .map(s -> s.split(","))
                    .orElse(new String[]{});

            final var hasInternalPrivilege = ArrayUtils.contains(privileges, "internal-app");
            final var hasUserDataPrivilege = ArrayUtils.contains(privileges, "user-data-privilege");

            if( hasInternalPrivilege && hasUserDataPrivilege ){
                LOG.debug( "Caller authorised with internal-app and user-data-privilege privileges" );
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
