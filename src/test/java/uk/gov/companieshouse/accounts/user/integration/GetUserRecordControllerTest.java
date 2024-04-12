package uk.gov.companieshouse.accounts.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.OneLoginDataDao;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
@Tag("integration-test")
public class GetUserRecordControllerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer container = new MongoDBContainer("mongo:5");

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    UsersRepository usersRepository;

    @MockBean
    InterceptorConfig interceptorConfig;

    @BeforeEach
    public void setup() {

        final var appealsTeam = new ArrayList<String>();
        appealsTeam.add( "appeals_team" );

        final var harleyQuinn = new Users();
        harleyQuinn.setId( "333" );
        harleyQuinn.setLocale( "GB_en" );
        harleyQuinn.setForename( "Harleen" );
        harleyQuinn.setSurname( "Quinzel" );
        harleyQuinn.setDisplayName( "Harley Quinn" );
        harleyQuinn.setEmail( "harley.quinn@gotham.city" );
        harleyQuinn.setRoles( appealsTeam );
        harleyQuinn.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harleyQuinn.setUpdated( LocalDateTime.now().minusDays( 5 ) );
        OneLoginDataDao oneLoginDataDao = new OneLoginDataDao();
        oneLoginDataDao.setOneLoginUserId("333");
        harleyQuinn.setOneLoginData(oneLoginDataDao);
        harleyQuinn.setPrivateBetaUser(true);

        usersRepository.insert( List.of( harleyQuinn ) );

        Mockito.doNothing().when(interceptorConfig).addInterceptors( any() );
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
        mockMvc.perform( get( "/users/{user_id}", "999" ).header( "X-Request-Id", "theId123" ) ).andExpect( status().isNotFound() );
    }

    @Test
    void getUserDetailsFetchesUserDetails() throws Exception {
        final var responseBody =
        mockMvc.perform( get( "/users/{user_id}", "333" ).header( "X-Request-Id", "theId123" ) )
                .andExpect( status().isOk() )
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var user = objectMapper.readValue( responseBody, User.class );

        Assertions.assertEquals( "Harley Quinn", user.getDisplayName() );
        Assertions.assertTrue(user.getHasLinkedOneLogin());
        Assertions.assertTrue(user.getIsPrivateBetaUser());
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }

}
