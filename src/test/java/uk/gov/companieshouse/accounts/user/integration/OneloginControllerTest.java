package uk.gov.companieshouse.accounts.user.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.accounts.user.common.ParsingUtils.localDateTimeToNormalisedString;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.accounts.user.common.TestDataManager;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.User;

@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
@Tag( "integration-test" )
class OneloginControllerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer container = new MongoDBContainer("mongo:6");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    private static final TestDataManager testDataManager = TestDataManager.getInstance();

    @Test
    void updateUserDetailsUnlinksOnelogin() throws Exception {
        final var requestingUserId = "CEOUSER001";
        final var targetUserId = "CEOUSER003";

        usersRepository.insert( testDataManager.fetchUsersDaos( requestingUserId, targetUserId ) );

        final var responseContent =
        mockMvc.perform( patch( String.format( "/internal/admin/users/%s", targetUserId ) )
                .header( "X-Request-Id", "theId12345" )
                .header( "ERIC-Identity", requestingUserId )
                .header( "ERIC-Identity-Type", "oauth2" )
                .header( "ERIC-Authorised-Roles", "/admin/user/unlinkonelogin" )
                .content( "{ \"unlinkOneLogin\" : true }" )
                .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule( new JavaTimeModule() );
        final var response = objectMapper.readValue( responseContent, User.class );

        final var updatedUserDao = usersRepository.findById( targetUserId ).get();

        Assertions.assertEquals( targetUserId, response.getUserId() );
        Assertions.assertFalse( response.getHasLinkedOneLogin() );
        Assertions.assertNull( updatedUserDao.getOneLoginData() );
        Assertions.assertEquals( requestingUserId, updatedUserDao.getOneLoginLinkRemovedBy() );
        Assertions.assertNotNull( updatedUserDao.getOneLoginLinkRemovedAt() );
    }

    @Test
    void updateUserDetailsWithAnAlreadyUnlinkedUserDoesNothing() throws Exception {
        final var requestingUserId = "CEOUSER001";
        final var targetUserId = "CEOUSER002";

        final var originalUserDaos = testDataManager.fetchUsersDaos( requestingUserId, targetUserId );
        usersRepository.insert( originalUserDaos );

        final var responseContent =
                mockMvc.perform( patch( String.format( "/internal/admin/users/%s", targetUserId ) )
                                .header( "X-Request-Id", "theId12345" )
                                .header( "ERIC-Identity", requestingUserId )
                                .header( "ERIC-Identity-Type", "oauth2" )
                                .header( "ERIC-Authorised-Roles", "/admin/user/unlinkonelogin" )
                                .content( "{ \"unlinkOneLogin\" : true }" )
                                .contentType( MediaType.APPLICATION_JSON ) )
                        .andExpect( status().isOk() )
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray();

        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule( new JavaTimeModule() );
        final var response = objectMapper.readValue( responseContent, User.class );

        final var originalUserDao = originalUserDaos.getLast();
        final var updatedUserDao = usersRepository.findById( targetUserId ).get();

        Assertions.assertEquals( targetUserId, response.getUserId() );
        Assertions.assertFalse( response.getHasLinkedOneLogin() );
        Assertions.assertEquals( originalUserDao.getOneLoginData(), updatedUserDao.getOneLoginData() );
        Assertions.assertEquals( originalUserDao.getOneLoginLinkRemovedBy(), updatedUserDao.getOneLoginLinkRemovedBy() );
        Assertions.assertEquals( localDateTimeToNormalisedString( originalUserDao.getOneLoginLinkRemovedAt() ), localDateTimeToNormalisedString( updatedUserDao.getOneLoginLinkRemovedAt() ) );
    }

    @Test
    void updateUserDetailsWithUnlinkOneLoginSetToFalseDoesNothing() throws Exception {
        final var requestingUserId = "CEOUSER001";
        final var targetUserId = "CEOUSER003";

        final var originalUserDaos = testDataManager.fetchUsersDaos( requestingUserId, targetUserId );
        usersRepository.insert( originalUserDaos );

        final var responseContent =
                mockMvc.perform( patch( String.format( "/internal/admin/users/%s", targetUserId ) )
                                .header( "X-Request-Id", "theId12345" )
                                .header( "ERIC-Identity", requestingUserId )
                                .header( "ERIC-Identity-Type", "oauth2" )
                                .header( "ERIC-Authorised-Roles", "/admin/user/unlinkonelogin" )
                                .content( "{ \"unlinkOneLogin\" : false }" )
                                .contentType( MediaType.APPLICATION_JSON ) )
                        .andExpect( status().isOk() )
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray();

        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule( new JavaTimeModule() );
        final var response = objectMapper.readValue( responseContent, User.class );

        final var originalUserDao = originalUserDaos.getLast();
        final var updatedUserDao = usersRepository.findById( targetUserId ).get();

        Assertions.assertEquals( targetUserId, response.getUserId() );
        Assertions.assertTrue( response.getHasLinkedOneLogin() );
        Assertions.assertEquals( originalUserDao.getOneLoginData().getOneLoginUserId(), updatedUserDao.getOneLoginData().getOneLoginUserId() );
        Assertions.assertEquals( originalUserDao.getOneLoginLinkRemovedBy(), updatedUserDao.getOneLoginLinkRemovedBy() );
        Assertions.assertEquals( originalUserDao.getOneLoginLinkRemovedAt(), updatedUserDao.getOneLoginLinkRemovedAt() );
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

    static Stream<Arguments> notFoundTestData(){
        return Stream.of(
                Arguments.of( testDataManager.fetchUsersDaos( "CEOUSER001" ) ),
                Arguments.of( testDataManager.fetchUsersDaos( "CEOUSER003" ) )
        );
    }

    @ParameterizedTest
    @MethodSource( "notFoundTestData" )
    void updateUserDetailsWithNonexistentUsersReturnsNotFound( final List<Users> users ) throws Exception {
        final var requestingUserId = "CEOUSER001";
        final var targetUserId = "CEOUSER003";

        usersRepository.insert( users );

        mockMvc.perform( patch( String.format( "/internal/admin/users/%s", targetUserId ) )
                        .header( "X-Request-Id", "theId12345" )
                        .header( "ERIC-Identity", requestingUserId )
                        .header( "ERIC-Identity-Type", "oauth2" )
                        .header( "ERIC-Authorised-Roles", "/admin/user/unlinkonelogin" )
                        .content( "{ \"unlinkOneLogin\" : true }" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() );
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }
}
