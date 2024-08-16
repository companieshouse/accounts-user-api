package uk.gov.companieshouse.accounts.user.integration;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
@Tag("integration-test")
class UserRolesControllerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer container = new MongoDBContainer("mongo:5");

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    RolesRepository userRolesRepository;

    @MockBean
    InterceptorConfig interceptorConfig;

    @BeforeEach
    public void setup() {

        final var supervisor = new ArrayList<String>();
        supervisor.add( "supervisor" );

        final var eminem = new Users();
        eminem.setId( "111" );
        eminem.setLocale( "GB_en" );
        eminem.setForename( "Marshall" );
        eminem.setSurname( "Mathers" );
        eminem.setDisplayName( "Eminem" );
        eminem.setEmail( "eminem@rap.com" );
        eminem.setRoles( supervisor );
        eminem.setCreated( LocalDateTime.now().minusDays( 1 ) );
        eminem.setUpdated( LocalDateTime.now() );

        final var badosUserAndRestrictedWord = new ArrayList<String>();
        badosUserAndRestrictedWord.addAll( List.of( "bados_user", "restricted_word" ) );

        final var theRock = new Users();
        theRock.setId( "222" );
        theRock.setLocale( "GB_en" );
        theRock.setForename( "Dwayne" );
        theRock.setSurname( "Johnson" );
        theRock.setDisplayName( "The Rock" );
        theRock.setEmail( "the.rock@wrestling.com" );
        theRock.setRoles( badosUserAndRestrictedWord );
        theRock.setCreated( LocalDateTime.now().minusDays( 4 ) );
        theRock.setUpdated( LocalDateTime.now().minusDays( 2 ) );


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

        final var harryPotter = new Users();
        harryPotter.setId( "444" );
        harryPotter.setLocale( "GB_en" );
        harryPotter.setForename( "Daniel" );
        harryPotter.setSurname( "Radcliff" );
        harryPotter.setDisplayName( "Harry Potter" );
        harryPotter.setEmail( "harry.potter@under-the-stairs.com" );
        harryPotter.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harryPotter.setUpdated( LocalDateTime.now().minusDays( 5 ) );

        usersRepository.insert( List.of( eminem, theRock, harleyQuinn, harryPotter ) );

        UserRole supervisor1 = new UserRole();
        supervisor1.setId("supervisor");

        UserRole badosUser = new UserRole();
        badosUser.setId("bados_user");


        UserRole restrictedWord = new UserRole();
        restrictedWord.setId("restricted_word");


        UserRole supportMember = new UserRole();
        supportMember.setId("support-member");

        UserRole csiSupport = new UserRole();
        csiSupport.setId("csi_support");

        userRolesRepository.insert(List.of(supervisor1,badosUser,restrictedWord,supportMember,csiSupport));



        Mockito.doNothing().when(interceptorConfig).addInterceptors( any() );
    }
    @Test
    void getUserRolesWithoutPathVariableReturnsNotFound() throws Exception {
        mockMvc.perform( get( "/users/roles" ).header( "X-Request-Id", "theId123" ) )
                .andExpect( status().isNotFound() );
    }

    @Test
    void getUserRolesWithMalformedInputReturnsBadRequest() throws Exception {
        mockMvc.perform( get( "/users/{user_id}/roles", "$" ).header( "X-Request-Id", "theId123" ) )
                .andExpect( status().isBadRequest() );
    }
    @Test
    void getUserRolesWithNonexistentUserIDReturnsNotFound() throws Exception {
        mockMvc.perform( get( "/users/roles").header("X-Request-Id", "theId123") ).
                andExpect(status().isNotFound());
    }

    @Test
    void getUserRolesForValidUserDetails() throws Exception {

        final var responseBody =
                mockMvc.perform( get( "/users/{user_id}/roles", "222" ).header( "X-Request-Id", "theId123" ) )
                        .andExpect( status().isOk() )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        final var roles = objectMapper.readValue(responseBody, new TypeReference<Set<String>>(){} );
        Assertions.assertEquals( 2, roles.size() );
        Assertions.assertTrue( roles.containsAll( Set.of( "bados_user", "restricted_word" )));

    }
    @Test
    void setUserRolesWithMalformedInputReturnsBadRequest() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("supervisor") );

        mockMvc.perform( put( "/users/{user_id}/roles", "$" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isBadRequest() );
    }

    @Test
    void setUserRolesWithoutRolesReturnsBadRequest() throws Exception {
        mockMvc.perform( put( "/users/{user_id}/roles", "999" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" ) )
                .andExpect( status().isBadRequest() );
    }

    @Test
    void setUserRolesWithEmptyListOfRolesReturnsBadRequest() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of() );

        mockMvc.perform( put( "/users/{user_id}/roles", "999" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isBadRequest() );
    }

    @Test
    void setUserRolesWithNonexistentUserReturnsNotFound() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("support-member") );

        mockMvc.perform( put( "/users/{user_id}/roles", "999" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isNotFound() );
    }

    @Test
    void setUserRolesWithOneRoleSetsTheUsersRole() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("supervisor") );

        mockMvc.perform( put( "/users/{user_id}/roles", "333" )
                         .header( "X-Request-Id", "theId123" )
                         .contentType( "application/json" )
                         .content( roles ) )
               .andExpect( status().isOk() );

        Assertions.assertEquals( List.of("supervisor"), usersRepository.findUsersById( "333" ).get().getRoles() );
    }

    @Test
    void setUserRolesWithMultipleRolesSetsTheUsersRoles() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("support-member", "csi_support" ) );

        mockMvc.perform( put( "/users/{user_id}/roles", "333" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isOk() );

        final var actualRoles = usersRepository.findUsersById( "333" ).get().getRoles();
        Assertions.assertEquals( 2, actualRoles.size() );
        Assertions.assertTrue( actualRoles.containsAll( List.of("support-member", "csi_support" ) ) );
    }

    @Test
    void setUserRolesWithDuplicatesRolesSetsTheRoleOnce() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("support-member", "support-member" ) );

        mockMvc.perform( put( "/users/{user_id}/roles", "333" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isOk() );

        Assertions.assertEquals( List.of("support-member"), usersRepository.findUsersById( "333" ).get().getRoles() );
    }

    @Test
    void setUserRolesWithDummyRolesSetsShouldThrowBadRequest() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("dummy", "support-member" ) );
        final String error = "{\"errors\":[{\"error\":\"dummy not valid role(s)\",\"location\":\"accounts_user_api\",\"location_type\":\"request-body\",\"type\":\"ch:validation\"}]}";
        var response = mockMvc.perform( put( "/users/{user_id}/roles", "333" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isBadRequest() )
                .andReturn();

        Assertions.assertEquals(error, response.getResponse().getContentAsString());
        Assertions.assertEquals( List.of("appeals_team"), usersRepository.findUsersById( "333" ).get().getRoles() );
    }


    @Test
    void setUserRolesCreatesNewRoleFieldWhenNotPresent() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("support-member") );

        mockMvc.perform( put( "/users/{user_id}/roles", "444" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isOk() );

        Assertions.assertEquals( List.of("support-member"), usersRepository.findUsersById( "444" ).get().getRoles() );
    }

    @Test
    void testBadRequestExceptionWhenRolesNotSet() throws Exception {
        mockMvc.perform( put( "/users/{user_id}/roles", "444" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                       )
                .andExpect( status().isBadRequest() );    }


    @Test
    void getUserProfile() throws Exception {

        mockMvc.perform( get( "/user/profile" )
                        .header( "eric-identity-type", "oauth2" )
                        .header( "eric-identity", "111" )
                        .header( "eric-authorised-roles", "" )
                        .header( "eric-authorised-scope", "test-scope" )
                        .header( "eric-authorised-token-permissions", "user-profile=read" )
                )
                .andExpect( status().isOk())
                .andExpect( jsonPath("$.forename").value("Marshall"))
                .andExpect( jsonPath("$.surname").value("Mathers"))
                .andExpect( jsonPath("$.email").value("eminem@rap.com"))
                .andExpect( jsonPath("$.id").value("111"))
                .andExpect( jsonPath("$.locale").value("GB_en"))
                .andExpect( jsonPath("$.scope").value("test-scope"))
                .andExpect( jsonPath("$.permissions").value(""))
                .andExpect( jsonPath("$.token_permissions").value(new HashMap<>(Map.of("user-profile", "read"))))
                .andExpect( jsonPath("$.private_beta_user").value(false))
                .andExpect( jsonPath("$.account_type").value("companies_house"))
        ;

    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection(UserRole.class);
        mongoTemplate.dropCollection( Users.class );
    }

}
