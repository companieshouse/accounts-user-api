package uk.gov.companieshouse.accounts.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.accounts.user.models.Oauth2AuthorisationsDao;
import uk.gov.companieshouse.accounts.user.models.UserDetailsDao;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.GetUserRecordInterface;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GetUserRecordController implements GetUserRecordInterface {

    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger(AccountsUserServiceApplication.applicationNameSpace);

    private static final String FORENAME = "forename";
    private static final String SURNAME = "surname";
    private static final String EMAIL = "email";
    private static final String ID = "id";
    private static final String LOCALE = "locale";
    private static final String SCOPE = "scope";
    private static final String PERMISSIONS = "permissions";

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

    @GetMapping("/profile")
    @ResponseBody  //Spring will transform ResponseEntity body into JSON
    public ResponseEntity<Object> getUserProfile(HttpServletRequest request) {
        Map<String,Object> userProfile = new HashMap<>();
        try {
            // UserProfilePermissionInterceptor attaches to request
            Oauth2AuthorisationsDao oauthAuthorisation = (Oauth2AuthorisationsDao) request.getAttribute("oauth2_authorisation");
            UserDetailsDao userDetails = oauthAuthorisation.getUserDetails();
            if (userDetails == null){
                LOG.error("User Details not found");
                return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body(new HashMap<>(Map.of("error", "Cannot locate account")));
            }

            userProfile.put(FORENAME, userDetails.getForename());
            userProfile.put(SURNAME, userDetails.getSurname());
            userProfile.put(EMAIL, userDetails.getEmail());
            userProfile.put(ID, userDetails.getUserID());
            userProfile.put(LOCALE, "GB_en");  //Locale is always GB_en
            userProfile.put(SCOPE, oauthAuthorisation.getRequestedScope());
            userProfile.put(PERMISSIONS, oauthAuthorisation.getPermissions());

        } catch (Exception e) { //Can not use normal process for any uncaught errors of showing error page. Need to send response
            LOG.errorRequest(request, e.getMessage());
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).body(new HashMap<>(Map.of("error", "Error locating account")));
        }
        return ResponseEntity.status(HttpServletResponse.SC_OK).body(userProfile);
    }

}
