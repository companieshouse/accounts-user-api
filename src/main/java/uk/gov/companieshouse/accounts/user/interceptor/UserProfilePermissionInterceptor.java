package uk.gov.companieshouse.accounts.user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.accounts.user.models.Oauth2AuthorisationsDao;
import uk.gov.companieshouse.accounts.user.repositories.OauthRepository;

/**
 * Interceptor that checks permission to access user profile.
 */
@Component
public class UserProfilePermissionInterceptor implements HandlerInterceptor {

    private final OauthRepository oauthRepository;

    public UserProfilePermissionInterceptor(OauthRepository oauthRepository) {
        this.oauthRepository = oauthRepository;
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws Exception {

        String header = request.getHeader("authorization");
        String accessToken = header.split(" ")[1];
        if (!getOauth2Authorization(accessToken, request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Oauth2Authorization not found");
            return false;
        }
        return true;
    }

    private boolean getOauth2Authorization(String accessToken, HttpServletRequest request) {
        if (accessToken == null){
            return false;
        }

        Oauth2AuthorisationsDao oauthAuthorisation = oauthRepository.findByToken(accessToken);
        if (oauthAuthorisation == null){
            return false;
        }

        // All users have user profile read permission so no need to check permissions
        //Add oauth2_authorisation to request
        request.setAttribute("oauth2_authorisation", oauthAuthorisation);
        return true;
    }
}
