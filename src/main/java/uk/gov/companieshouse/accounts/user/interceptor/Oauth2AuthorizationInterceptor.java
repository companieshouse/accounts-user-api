package uk.gov.companieshouse.accounts.user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.accounts.user.models.Oauth2AuthorisationsDao;
import uk.gov.companieshouse.accounts.user.repositories.OauthRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.RequestLogger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication.applicationNameSpace;

// Java implementation of CH::MojoX::Bridge::Authorisation::check_OAuth2
@Component
public class Oauth2AuthorizationInterceptor implements HandlerInterceptor, RequestLogger {

    private static final String REQUEST_METHOD_OPTIONS = "OPTIONS";
    private static final String INVALID_AUTHORIZATION_HEADER = "invalid-authorization-header";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final Logger logger = LoggerFactory.getLogger(applicationNameSpace);

    OauthRepository oauthRepository;

    @Autowired
    public Oauth2AuthorizationInterceptor(OauthRepository oauthRepository) {
        this.oauthRepository = oauthRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws IOException {

        // Skip authentication where request method is OPTIONS
        String requestMethod = request.getMethod();
        if (requestMethod.equals(REQUEST_METHOD_OPTIONS)) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return true;
        }

        // Start of AccountChGovUk::Bridges::OAuth2ClientCheck::get_client_credentials()
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isBlank()) {
            logger.trace("no authorisation supplied");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access denied");
            return false;
        }

        String[] splitHeader = authHeader.split("\\s");
        var authType = splitHeader[0];
        String authValue;
        if (authType.equalsIgnoreCase("basic")) {
            authValue = new String(Base64.getDecoder().decode(splitHeader[1]));

            if (!authValue.endsWith(":")) {
                logger.trace("Invalid username:password string");
                response.addHeader(WWW_AUTHENTICATE, "Basic error='invalid_literal' error-desc='Invalid username:password string' realm='https://identity.company-information.service.gov.uk'");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_AUTHORIZATION_HEADER);
                return false;
            }
        } else {
            authValue = splitHeader[1];
        }

        return validateAccessToken(authType, authValue, request, response);
    }

    /**
     * Perform validation on retrieved access token.
     *
     * @param authType the authorisation type set in the header (currently on basic accepted)
     * @param authValue the decoded authorisation value set in the header which should be the token
     * @param request http request
     * @param response http response
     * @return a boolean determining whether to allow request to controller.
     * @throws IOException
     */
    private boolean validateAccessToken(String authType, String authValue, HttpServletRequest request, HttpServletResponse response) throws IOException {

        var tokenType = "Bearer";
        String token;

        var paramToken = request.getParameter("access_token");

        if (paramToken == null && authType.equalsIgnoreCase(tokenType)) {
            token = authValue;
        } else {
            token = paramToken;
        }

        // Comment in account.ch.gov.uk perl code:
        // FIXME The www-authenticate response for read-only resource _could_ also be Basic in addition to Bearer

        // Perl code allows either Bearer token with (or without) access_token parameter or
        // ANY basic authorisation header (can be any username & password in the username:password string) with access_token parameter
        // Commented out line below is current perl code logic
        // if (tokenType == null || token == null || !"Bearer".equals(tokenType)) {
        if (token == null) {
            logger.trace("Invalid or no authorization header provided");
            response.addHeader(WWW_AUTHENTICATE, "Bearer error='invalid_token' error-desc='Invalid or no authorization header provided' realm='https://identity.company-information.service.gov.uk'");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_AUTHORIZATION_HEADER);
            return false;
        }

        // Get oauth2_authorisation
        Oauth2AuthorisationsDao oad = oauthRepository.findByToken(token);

        if (oad == null) {
            logger.traceRequest(request, "No authorisation document found for token",
                    new HashMap<>(Map.of("token", token)));
            response.addHeader(WWW_AUTHENTICATE, "Bearer error='invalid_token' error-desc='Invalid or no authorization header provided' realm='https://identity.company-information.service.gov.uk'");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_AUTHORIZATION_HEADER);
            return false;
        }

        if (oad.getTokenValidUntil() < LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {
            logger.traceRequest(request, "Token expired", null);
            response.addHeader(WWW_AUTHENTICATE, "Bearer error='expired_token' error-desc='Access token has expired' realm='https://identity.company-information.service.gov.uk'");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_AUTHORIZATION_HEADER);
            return false;
        }

        request.setAttribute(ACCESS_CONTROL_ALLOW_ORIGIN, request.getHeaders(HttpHeaders.ORIGIN));

        //All checks passed, store oauth2_authorisation in the request and allow to continue
        request.setAttribute("oauth2_authorisation", oad);
        return true;
    }
}
