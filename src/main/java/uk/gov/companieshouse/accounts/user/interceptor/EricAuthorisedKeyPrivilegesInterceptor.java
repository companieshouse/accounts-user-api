package uk.gov.companieshouse.accounts.user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.RequestLogger;

@Component
public class EricAuthorisedKeyPrivilegesInterceptor implements HandlerInterceptor, RequestLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger( AccountsUserServiceApplication.applicationNameSpace );

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        final var privileges = request.getHeader( "eric-authorised-key-privileges" );

        if ( Objects.isNull( privileges ) || !privileges.contains("sensitive-data") ) {
            LOGGER.error( "Caller does not have sensitive-data privileges" );
            response.setStatus( 401 );
            return false;
        }

        LOGGER.debug( "Caller authorised with sensitive-data privileges" );
        return true;
    }

}
