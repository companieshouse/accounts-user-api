package uk.gov.companieshouse.accounts.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.accounts.user.exceptions.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.GetUserRecordInterface;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.accounts.user.controller.ControllerAdvice.X_REQUEST_ID;
import static uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil.APPLICATION_NAMESPACE;

@RestController
public class GetUserRecordController implements GetUserRecordInterface {

    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private static final String FORENAME = "forename";
    private static final String SURNAME = "surname";
    private static final String EMAIL = "email";
    private static final String ID = "id";
    private static final String LOCALE = "locale";
    private static final String SCOPE = "scope";
    private static final String PERMISSIONS = "permissions";
    private static final String TOKEN_PERMISSIONS = "token_permissions";

    private static final String PRIVATE_BETA_USER = "private_beta_user";
    private static final String ACCOUNT_TYPE = "account_type";
    private static final String ERROR = "error";

    public GetUserRecordController(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<User> getUserDetails( final String userId, final String xRequestId) {

        LOG.debug( String.format( "%s: Attempting to search for the details of user: %s", xRequestId, userId ) );

        final var userOptional = usersService.fetchUser( userId );

        if ( userOptional.isEmpty() ){
            LOG.debug( String.format( "%s: Could not find user: %s", xRequestId, userId ) );
            return new ResponseEntity<>( HttpStatus.NOT_FOUND );
        }

        final var user = userOptional.get();

        LOG.debug( String.format( "%s: Successfully fetched user: %s", xRequestId, userId ) );

        return new ResponseEntity<>( user, HttpStatus.OK );
    }

    @GetMapping("/user/profile")
    public ResponseEntity<Object> getUserProfile(HttpServletRequest request,
                                                 @RequestHeader(value = X_REQUEST_ID, required = false) String xRequestId) {
        LOG.debugContext(xRequestId,"Get User Profile", null);
        Map<String,Object> userProfile = new HashMap<>();
        try {
            if (!"oauth2".equals(AuthorisationUtil.getAuthorisedIdentityType(request))) {
                throw new InternalServerErrorRuntimeException("wrong identity type");
            }
            var userId = AuthorisationUtil.getAuthorisedIdentity(request);

            final var userOptional = usersService.fetchUser(userId);
            if (userOptional.isEmpty()){
                LOG.errorContext(xRequestId, "User Details not found", null,null);
                return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body(new HashMap<>(Map.of(ERROR, "Cannot locate user")));
            }
            final var user = userOptional.get();

            userProfile.put(FORENAME, user.getForename());
            userProfile.put(SURNAME, user.getSurname());
            userProfile.put(EMAIL, user.getEmail());
            userProfile.put(ID, userId);
            userProfile.put(PRIVATE_BETA_USER, user.getIsPrivateBetaUser());
            userProfile.put(ACCOUNT_TYPE, Boolean.TRUE.equals(user.getHasLinkedOneLogin()) ? "onelogin" : "companies_house");
            userProfile.put(LOCALE, "GB_en");  //Locale is always GB_en
            userProfile.put(SCOPE, request.getHeader("eric-authorised-scope"));

            // Authorised roles are the list of admin permissions set when user signs in with admin* prefix
            var roles = AuthorisationUtil.getAuthorisedRoles(request);
            var permissions = new HashMap<String, Integer>();
            for (String role : roles) {
                if (!role.isEmpty()) {
                    permissions.put(role, 1);
                }
            }
            userProfile.put(PERMISSIONS, permissions);

            userProfile.put(TOKEN_PERMISSIONS, getTokenPermissions(request));

        } catch (Exception e) { //Can not use normal process for any uncaught errors of showing error page. Need to send response
            LOG.errorContext(xRequestId, e, null);
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).body(new HashMap<>(Map.of(ERROR, e.getMessage())));
        }
        return ResponseEntity.status(HttpServletResponse.SC_OK).body(userProfile);
    }

    private HashMap<String, String> getTokenPermissions(HttpServletRequest request) {
        // Get token permissions from ERIC
        var tokenPermissionsString = request.getHeader("eric-authorised-token-permissions");
        var tokenPermissions = tokenPermissionsString.split(" ");

        var tokenPermissionMap = new HashMap<String, String>();
        for (String permission : tokenPermissions ) {
            var keyValue = permission.split("=");
            tokenPermissionMap.put(keyValue[0], keyValue[1]);
        }
        return tokenPermissionMap;
    }
}
