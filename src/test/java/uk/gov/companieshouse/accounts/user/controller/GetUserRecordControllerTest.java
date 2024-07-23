package uk.gov.companieshouse.accounts.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.Oauth2AuthorisationsDao;
import uk.gov.companieshouse.accounts.user.models.UserDetailsDao;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.SessionImpl;
import uk.gov.companieshouse.session.handler.SessionHandler;
import uk.gov.companieshouse.session.store.Store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit-test")
@WebMvcTest(GetUserRecordController.class)
public class GetUserRecordControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    UsersService usersService;

    @MockBean
    InterceptorConfig interceptorConfig;

    @InjectMocks
    private GetUserRecordController controller;

    @Mock
    private Store store;
    private MockHttpServletRequest request;

    private User userEminem;
    private User userTheRock;
    private User userHarleyQuinn;

    @BeforeEach
    void setup() {

        final var supervisor = new RolesList();
        supervisor.add( "supervisor" );

        userEminem = new User();
        userEminem.userId("111")
                .forename("Marshall")
                .surname("Mathers")
                .displayName("Eminem")
                .email("eminem@rap.com")
                .roles( supervisor );

        final var badosUserAndRestrictedWord = new RolesList();
        badosUserAndRestrictedWord.addAll( List.of( "bados_user", "restricted_user" ) );

        userTheRock = new User();
        userTheRock.userId("222")
                .forename("Dwayne")
                .surname("Johnson")
                .displayName("The Rock")
                .email("the.rock@wrestling.com")
                .roles( badosUserAndRestrictedWord );

        final var appealsTeam = new RolesList();
        appealsTeam.add( "appeals_team");

        userHarleyQuinn = new User();
        userHarleyQuinn.userId("333")
                .forename("Harleen")
                .surname("Quinzel")
                .displayName("Harley Quinn")
                .email("harley.quinn@gotham.city")
                .roles( appealsTeam );

        Mockito.doNothing().when(interceptorConfig).addInterceptors( any() );

        request = new MockHttpServletRequest();
        //Session session = new SessionImpl(store, "1234", new HashMap<>());
        //request.setAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY, session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void getUserDetailsWithoutPathVariableReturnsNotFound() throws Exception {
        mockMvc.perform( get( "/users/" ).header( "X-Request-Id", "theId123" ) ).andExpect( status().isNotFound() );
    }

    @Test
    void getUserDetailsWithMalformedInputReturnsBadRequest() throws Exception {
        mockMvc.perform( get( "/users/{user_id}", "$" ).header( "X-Request-Id", "theId123" ) ).andExpect( status().isBadRequest() );
    }

    @Test
    void getUserDetailsWithNonexistentUserIdReturnsNotFound() throws Exception {
        Mockito.doReturn( Optional.empty() ).when( usersService ).fetchUser( any() );

        mockMvc.perform( get( "/users/{user_id}", "999" ).header( "X-Request-Id", "theId123" ) ).andExpect( status().isNotFound() );
    }

    @Test
    void getUserDetailsFetchesUserDetails() throws Exception {
        Mockito.doReturn( Optional.of( userHarleyQuinn ) ).when( usersService ).fetchUser( any() );

        final var responseBody =
                mockMvc.perform( get( "/users/{user_id}", "333" ).header( "X-Request-Id", "theId123" ) )
                        .andExpect( status().isOk() )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var user = objectMapper.readValue( responseBody, User.class );

        Assertions.assertEquals( "Harley Quinn", user.getDisplayName() );
    }

    @Test
    @DisplayName("GetUserRecordControllerTests - getUserProfileSuccess")
    void getUserProfileSuccess() {
        Oauth2AuthorisationsDao oauthAuthorisation = new Oauth2AuthorisationsDao();
        UserDetailsDao userDetails = new UserDetailsDao();

        userDetails.setForename("Fred");
        userDetails.setSurname("Bloggs");
        userDetails.setEmail("email@email.com");
        userDetails.setUserID("12345");

        oauthAuthorisation.setUserDetails(userDetails);
        oauthAuthorisation.setRequestedScope("https://account.companieshouse.gov.uk/user.write-full");
        oauthAuthorisation.setPermissions(new HashMap<>());

        request.setAttribute("oauth2_authorisation", oauthAuthorisation);

        Map<String, Object> userprofile = new HashMap<>();
        userprofile.put("forename", "Fred");
        userprofile.put("surname", "Bloggs");
        userprofile.put("email", "email@email.com");
        userprofile.put("id", "12345");
        userprofile.put("locale", "GB_en");
        userprofile.put("scope", "https://account.companieshouse.gov.uk/user.write-full");
        userprofile.put("permissions", new HashMap<>());

        ResponseEntity responseEntity = controller.getUserProfile(request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userprofile, responseEntity.getBody());
    }

    @Test
    @DisplayName("GetUserRecordControllerTests - getUserProfileSuccessWithNullValue")
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

        request.setAttribute("oauth2_authorisation", oauthAuthorisation);

        Map<String, Object> userprofile = new HashMap<>();
        userprofile.put("forename", null);
        userprofile.put("surname", null);
        userprofile.put("email", "email@email.com");
        userprofile.put("id", "12345");
        userprofile.put("locale", "GB_en");
        userprofile.put("scope", "https://account.companieshouse.gov.uk/user.write-full");
        userprofile.put("permissions", new HashMap<>());

        ResponseEntity responseEntity = controller.getUserProfile(request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userprofile, responseEntity.getBody());
    }

    @Test
    @DisplayName("GetUserRecordControllerTests - getUserProfileNotFound")
    void getUserProfileNotFound() {
        Oauth2AuthorisationsDao oauthAuthorisation = new Oauth2AuthorisationsDao();
        request.setAttribute("oauth2_authorisation", oauthAuthorisation);

        Map<String, String> errorResponse = new HashMap<>(Map.of(
                "error", "Cannot locate account"));

        ResponseEntity<Object> responseEntity = controller.getUserProfile(request);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(errorResponse, responseEntity.getBody());
    }

    @Test
    @DisplayName("GetUserRecordControllerTests - getUserProfileException")
    void getUserProfileException() {
        Map<String, String> errorResponse = new HashMap<>(Map.of(
                "error", "Error locating account"));
        ResponseEntity<Object> responseEntity = controller.getUserProfile(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(errorResponse, responseEntity.getBody());
    }

}
