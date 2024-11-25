package uk.gov.companieshouse.accounts.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.accounts.user.common.TestDataManager;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil;

@Tag( "unit-test" )
@WebMvcTest( OneloginController.class )
class OneloginControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    private StaticPropertyUtil staticPropertyUtil;

    @MockBean
    private UsersService usersService;

    private static final TestDataManager testDataManager = TestDataManager.getInstance();

    @BeforeEach
    void setup(){
        StaticPropertyUtil.APPLICATION_NAMESPACE = "accounts-user-api";
    }

    @Test
    void updateUserDetailsUnlinksOnelogin() throws Exception {
        final var requestingUserId = "CEOUSER001";
        final var targetUserId = "CEOUSER003";

        Mockito.doReturn( true ).when( usersService ).userExists( requestingUserId );
        Mockito.doReturn( Optional.of( testDataManager.fetchUserDtos( targetUserId ).getFirst() ) ).when( usersService ).fetchUser( targetUserId );

        mockMvc.perform( patch( String.format( "/internal/admin/users/%s", targetUserId ) )
                        .header( "X-Request-Id", "theId12345" )
                        .header( "ERIC-Identity", requestingUserId )
                        .header( "ERIC-Identity-Type", "oauth2" )
                        .header( "ERIC-Authorised-Roles", "/admin/user/unlinkonelogin" )
                        .content( "{ \"unlinkOneLogin\" : true }" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );

        Mockito.verify( usersService ).unlinkOnelogin( targetUserId, requestingUserId );
    }

    static Stream<Arguments> doNothingTestData(){
        return Stream.of(
                Arguments.of( "CEOUSER002", "true" ),
                Arguments.of( "CEOUSER003", "false" )
        );
    }

    @ParameterizedTest
    @MethodSource( "doNothingTestData" )
    void updateUserDetailsWithAnAlreadyUnlinkedUserOrUnlinkOneLoginSetToFalseDoesNothing( final String targetUserId, final String unlinkedOneLogin ) throws Exception {
        final var requestingUserId = "CEOUSER001";

        Mockito.doReturn( true ).when( usersService ).userExists( requestingUserId );
        Mockito.doReturn( Optional.of( testDataManager.fetchUserDtos( targetUserId ).getFirst() ) ).when( usersService ).fetchUser( targetUserId );

        mockMvc.perform( patch( String.format( "/internal/admin/users/%s", targetUserId ) )
                        .header( "X-Request-Id", "theId12345" )
                        .header( "ERIC-Identity", requestingUserId )
                        .header( "ERIC-Identity-Type", "oauth2" )
                        .header( "ERIC-Authorised-Roles", "/admin/user/unlinkonelogin" )
                        .content( String.format( "{ \"unlinkOneLogin\" : %s }", unlinkedOneLogin ) )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );

        Mockito.verify( usersService, Mockito.times( 0 ) ).unlinkOnelogin( targetUserId, requestingUserId );
    }

    static Stream<Arguments> badRequestTestData(){
        return Stream.of(
                Arguments.of( "theId12345", "£££", "{ \"unlinkOneLogin\" : true }" ),
                Arguments.of( "theId12345", "CEOUSER003", "" ),
                Arguments.of( "theId12345", "CEOUSER003", "{}" ),
                Arguments.of( "theId12345", "CEOUSER003", "{ \"unlinkOneLogin\" : no }" ),
                Arguments.of( "£££", "CEOUSER003", "{ \"unlinkOneLogin\" : true }" )
        );
    }

    @ParameterizedTest
    @MethodSource( "badRequestTestData" )
    void updateUserDetailsWithMalformedXRequestIdOrTargetUserIdOrRequestBodyReturnsBadRequest( final String xRequestId, final String targetUserId, final String requestBody ) throws Exception {
        final var requestingUserId = "CEOUSER001";

        mockMvc.perform( patch( String.format( "/internal/admin/users/%s", targetUserId ) )
                        .header( "X-Request-Id", xRequestId )
                        .header( "ERIC-Identity", requestingUserId )
                        .header( "ERIC-Identity-Type", "oauth2" )
                        .header( "ERIC-Authorised-Roles", "/admin/user/unlinkonelogin" )
                        .content( requestBody )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }

    @Test
    void updateUserDetailsWithNonexistentRequestingUserReturnsNotFound() throws Exception {
        final var requestingUserId = "CEOUSER001";
        final var targetUserId = "CEOUSER003";

        Mockito.doReturn( false ).when( usersService ).userExists( requestingUserId );
        mockMvc.perform( patch( String.format( "/internal/admin/users/%s", targetUserId ) )
                        .header( "X-Request-Id", "theId12345" )
                        .header( "ERIC-Identity", requestingUserId )
                        .header( "ERIC-Identity-Type", "oauth2" )
                        .header( "ERIC-Authorised-Roles", "/admin/user/unlinkonelogin" )
                        .content( "{ \"unlinkOneLogin\" : true }" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() );
    }


    @Test
    void updateUserDetailsWithNonexistentTargetUserReturnsNotFound() throws Exception {
        final var requestingUserId = "CEOUSER001";
        final var targetUserId = "CEOUSER003";

        Mockito.doReturn( true ).when( usersService ).userExists( requestingUserId );
        Mockito.doReturn( Optional.empty() ).when( usersService ).fetchUser( targetUserId );

        mockMvc.perform( patch( String.format( "/internal/admin/users/%s", targetUserId ) )
                        .header( "X-Request-Id", "theId12345" )
                        .header( "ERIC-Identity", requestingUserId )
                        .header( "ERIC-Identity-Type", "oauth2" )
                        .header( "ERIC-Authorised-Roles", "/admin/user/unlinkonelogin" )
                        .content( "{ \"unlinkOneLogin\" : true }" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() );
    }

}
