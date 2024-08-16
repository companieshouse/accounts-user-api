package uk.gov.companieshouse.accounts.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileTest {

    @InjectMocks
    private GetUserRecordController controller;

    @Mock
    UsersService usersService;

    @Mock
    private HttpServletRequest mockRequest;

    private String xRequestId = "X-Request-Id";

    private final String testUserId = "0123456789";
    private final String testForename = "Fred";
    private final String testSurname = "Bloggs";
    private final String testEmail = "tester@test.com";

    private final String testScope = "https://account.companieshouse.gov.uk/user.write-full";
    private final String COMPANIES_HOUSE = "companies_house";
    private final String ONELOGIN = "onelogin";

    private User testUser = new User();
    private Map<String, Object> expectedUserProfile = new HashMap<>();

    @BeforeEach
    void setUp() {
        testUser.setUserId(testUserId);
        testUser.setForename(testForename);
        testUser.setSurname(testSurname);
        testUser.setEmail(testEmail);
        testUser.setIsPrivateBetaUser(false);

        expectedUserProfile.put("forename", testForename);
        expectedUserProfile.put("surname", testSurname);
        expectedUserProfile.put("email", testEmail);
        expectedUserProfile.put("id", testUserId);
        expectedUserProfile.put("locale", "GB_en");
        expectedUserProfile.put("scope", testScope);
        expectedUserProfile.put("permissions", new HashMap<>());
        expectedUserProfile.put("token_permissions", new HashMap<>(Map.of("user-profile", "read")));
        expectedUserProfile.put("private_beta_user", false);
        expectedUserProfile.put("account_type", COMPANIES_HOUSE);
    }

    @Test
    @DisplayName("UserControllerTests - getUserProfileSuccess")
    void getUserProfileSuccess() {

        try (MockedStatic<AuthorisationUtil> authorisationUtil = Mockito.mockStatic(AuthorisationUtil.class)) {
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedIdentityType(mockRequest)).thenReturn("oauth2");
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedIdentity(mockRequest)).thenReturn(testUserId);
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedRoles(mockRequest)).thenReturn(new ArrayList<String>());
            when(mockRequest.getHeader("eric-authorised-scope")).thenReturn(testScope);
            when(mockRequest.getHeader("eric-authorised-token-permissions")).thenReturn("user-profile=read");

            when(usersService.fetchUser(testUserId)).thenReturn((Optional.of(testUser)));

            var responseEntity = controller.getUserProfile(mockRequest, xRequestId);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(expectedUserProfile, responseEntity.getBody());
        }
    }

    @Test
    @DisplayName("UserControllerTests - getUserProfile Success with admin permissions")
    void getUserProfileSuccessWithPermissions() {

        var roles = new ArrayList<String>();
        roles.add("admin/role-A");
        roles.add("admin/role-B");
        var permissions = new HashMap<>(Map.of("admin/role-A", 1, "admin/role-B", 1));

        try (MockedStatic<AuthorisationUtil> authorisationUtil = Mockito.mockStatic(AuthorisationUtil.class)) {
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedIdentityType(mockRequest)).thenReturn("oauth2");
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedIdentity(mockRequest)).thenReturn(testUserId);
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedRoles(mockRequest)).thenReturn(roles);
            when(mockRequest.getHeader("eric-authorised-scope")).thenReturn(testScope);
            when(mockRequest.getHeader("eric-authorised-token-permissions")).thenReturn("user-profile=read");

            when(usersService.fetchUser(testUserId)).thenReturn((Optional.of(testUser)));

            var responseEntity = controller.getUserProfile(mockRequest, xRequestId);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            expectedUserProfile.put("permissions", permissions);
            assertEquals(expectedUserProfile, responseEntity.getBody());
        }
    }

    @Test
    @DisplayName("UserControllerTests - getUserProfileSuccessWithNullValue")
    void getUserProfileSuccessWithNullValue() {

        try (MockedStatic<AuthorisationUtil> authorisationUtil = Mockito.mockStatic(AuthorisationUtil.class)) {
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedIdentityType(mockRequest)).thenReturn("oauth2");
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedIdentity(mockRequest)).thenReturn(testUserId);
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedRoles(mockRequest)).thenReturn(new ArrayList<String>());
            when(mockRequest.getHeader("eric-authorised-scope")).thenReturn(testScope);
            when(mockRequest.getHeader("eric-authorised-token-permissions")).thenReturn("user-profile=read");

            testUser.setForename(null);
            testUser.setSurname(null);
            testUser.setIsPrivateBetaUser(true);
            testUser.setHasLinkedOneLogin(true);
            when(usersService.fetchUser(testUserId)).thenReturn((Optional.of(testUser)));

            var responseEntity = controller.getUserProfile(mockRequest, xRequestId);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            expectedUserProfile.put("forename", null);
            expectedUserProfile.put("surname", null);
            expectedUserProfile.put("private_beta_user", true);
            expectedUserProfile.put("account_type", ONELOGIN);
            assertEquals(expectedUserProfile, responseEntity.getBody());
        }
    }

    @Test
    @DisplayName("UserControllerTests - getUserProfile wrong identity type")
    void getUserProfileWrongIdentityType() {

        try (MockedStatic<AuthorisationUtil> authorisationUtil = Mockito.mockStatic(AuthorisationUtil.class)) {
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedIdentityType(mockRequest)).thenReturn("apikey");

            Map<String, String> errorResponse = new HashMap<>(Map.of(
                    "error", "wrong identity type"));

            ResponseEntity<Object> responseEntity = controller.getUserProfile(mockRequest, xRequestId);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
            assertEquals(errorResponse, responseEntity.getBody());
        }
    }

    @Test
    @DisplayName("UserControllerTests - getUserProfile Optional NotFound")
    void getUserProfileOptionalNotFound() {

        try (MockedStatic<AuthorisationUtil> authorisationUtil = Mockito.mockStatic(AuthorisationUtil.class)) {
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedIdentityType(mockRequest)).thenReturn("oauth2");
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedIdentity(mockRequest)).thenReturn(testUserId);
            authorisationUtil.when(() -> AuthorisationUtil.getAuthorisedRoles(mockRequest)).thenReturn(new ArrayList<String>());

            when(usersService.fetchUser(testUserId)).thenReturn((Optional.empty()));

            var responseEntity = controller.getUserProfile(mockRequest, xRequestId);

            assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
            Map<String, String> errorResponse = new HashMap<>(Map.of(
                    "error", "Cannot locate user"));
            assertEquals(errorResponse, responseEntity.getBody());
        }
    }
}
