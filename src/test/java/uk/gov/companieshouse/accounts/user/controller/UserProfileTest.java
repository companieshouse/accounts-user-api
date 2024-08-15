package uk.gov.companieshouse.accounts.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.companieshouse.accounts.user.models.Oauth2AuthorisationsDao;
import uk.gov.companieshouse.accounts.user.models.UserDetailsDao;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileTest {

    @InjectMocks
    private GetUserRecordController controller;

    @Mock
    UsersService usersService;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }


    @Test
    @DisplayName("UserControllerTests - getUserProfileSuccess")
    void getUserProfileSuccess() {

        var userDetails = new UserDetailsDao();
        userDetails.setForename("Fred");
        userDetails.setSurname("Bloggs");
        userDetails.setEmail("email@email.com");
        userDetails.setUserID("12345");

        var oauthAuthorisation = new Oauth2AuthorisationsDao();
        oauthAuthorisation.setUserDetails(userDetails);
        oauthAuthorisation.setRequestedScope("https://account.companieshouse.gov.uk/user.write-full");
        oauthAuthorisation.setPermissions(new HashMap<>());
        oauthAuthorisation.setTokenPermissions(new HashMap<>());

        request.setAttribute("oauth2_authorisation", oauthAuthorisation);

        Map<String, Object> userprofile = new HashMap<>();
        userprofile.put("forename", "Fred");
        userprofile.put("surname", "Bloggs");
        userprofile.put("email", "email@email.com");
        userprofile.put("id", "12345");
        userprofile.put("locale", "GB_en");
        userprofile.put("scope", "https://account.companieshouse.gov.uk/user.write-full");
        userprofile.put("permissions", new HashMap<>());
        userprofile.put("token_permissions", new HashMap<>());
        userprofile.put("private_beta_user", true);
        userprofile.put("account_type", "onelogin");

        var user = mock(User.class);
        when(usersService.fetchUser(any())).thenReturn((Optional.of(user)));
        when(user.getIsPrivateBetaUser()).thenReturn(true);
        when(user.getHasLinkedOneLogin()).thenReturn(true);

        var responseEntity = controller.getUserProfile(request, "X-Request-ID");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userprofile, responseEntity.getBody());
    }

    @Test
    @DisplayName("UserControllerTests - getUserProfileSuccessWithNullValue")
    void getUserProfileSuccessWithNullValue() {
        Oauth2AuthorisationsDao oauthAuthorisation = new Oauth2AuthorisationsDao();
        UserDetailsDao userDetails = new UserDetailsDao();

        userDetails.setForename(null);
        userDetails.setSurname(null);
        userDetails.setEmail("email@email.com");
        userDetails.setUserID("12345");

        oauthAuthorisation.setUserDetails(userDetails);
        oauthAuthorisation.setRequestedScope("https://account.companieshouse.gov.uk/user.write-full");
        oauthAuthorisation.setPermissions(new HashMap<>());
        oauthAuthorisation.setTokenPermissions(new HashMap<>());

        request.setAttribute("oauth2_authorisation", oauthAuthorisation);

        Map<String, Object> userprofile = new HashMap<>();
        userprofile.put("forename", null);
        userprofile.put("surname", null);
        userprofile.put("email", "email@email.com");
        userprofile.put("id", "12345");
        userprofile.put("locale", "GB_en");
        userprofile.put("scope", "https://account.companieshouse.gov.uk/user.write-full");
        userprofile.put("permissions", new HashMap<>());
        userprofile.put("token_permissions", new HashMap<>());
        userprofile.put("private_beta_user", true);
        userprofile.put("account_type", "companies_house");

        var user = mock(User.class);
        when(usersService.fetchUser(any())).thenReturn((Optional.of(user)));
        when(user.getIsPrivateBetaUser()).thenReturn(true);
        when(user.getHasLinkedOneLogin()).thenReturn(false);

        var responseEntity = controller.getUserProfile(request, "X-Request-ID");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userprofile, responseEntity.getBody());
    }

    @Test
    @DisplayName("UserControllerTests - getUserProfileNotFound")
    void getUserProfileNotFound() {
        Oauth2AuthorisationsDao oauthAuthorisation = new Oauth2AuthorisationsDao();
        request.setAttribute("oauth2_authorisation", oauthAuthorisation);

        Map<String, String> errorResponse = new HashMap<>(Map.of(
                "error", "Cannot locate account"));

        ResponseEntity<Object> responseEntity = controller.getUserProfile(request, "X-Request-ID");

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(errorResponse, responseEntity.getBody());
    }

    @Test
    @DisplayName("UserControllerTests - getUserProfile Optional NotFound")
    void getUserProfileOptionalNotFound() {
        var userDetails = new UserDetailsDao();
        userDetails.setForename("Fred");
        userDetails.setSurname("Bloggs");
        userDetails.setEmail("email@email.com");
        userDetails.setUserID("12345");

        var oauthAuthorisation = new Oauth2AuthorisationsDao();
        oauthAuthorisation.setUserDetails(userDetails);
        oauthAuthorisation.setRequestedScope("https://account.companieshouse.gov.uk/user.write-full");
        oauthAuthorisation.setPermissions(new HashMap<>());

        request.setAttribute("oauth2_authorisation", oauthAuthorisation);

        when(usersService.fetchUser(any())).thenReturn(Optional.empty());

        Map<String, String> errorResponse = new HashMap<>(Map.of(
                "error", "Cannot locate user"));

        ResponseEntity<Object> responseEntity = controller.getUserProfile(request, "X-Request-ID");

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(errorResponse, responseEntity.getBody());
    }

    @Test
    @DisplayName("UserControllerTests - getUserProfileException")
    void getUserProfileException() {
        Map<String, String> errorResponse = new HashMap<>(Map.of(
                "error", "Error locating account"));
        ResponseEntity<Object> responseEntity = controller.getUserProfile(request, "X-Request-ID");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(errorResponse, responseEntity.getBody());
    }

}
