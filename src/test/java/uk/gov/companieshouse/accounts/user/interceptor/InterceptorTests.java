package uk.gov.companieshouse.accounts.user.interceptor;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.User;


@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
@Tag("integration-test")
public class InterceptorTests {

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
    InterceptorConfig interceptorConfig;

    @BeforeEach
    public void setup() {

        final var eminem = new Users();
        eminem.setId( "111" );
        eminem.setLocale( "GB_en" );
        eminem.setForename( "Marshall" );
        eminem.setSurname( "Mathers" );
        eminem.setDisplayName( "Eminem" );
        eminem.setEmail( "eminem@rap.com" );
        eminem.setRoles( List.of("supervisor") );
        eminem.setCreated( LocalDateTime.now().minusDays( 1 ) );
        eminem.setUpdated( LocalDateTime.now() );

        final var theRock = new Users();
        theRock.setId( "222" );
        theRock.setLocale( "GB_en" );
        theRock.setForename( "Dwayne" );
        theRock.setSurname( "Johnson" );
        theRock.setDisplayName( "The Rock" );
        theRock.setEmail( "the.rock@wrestling.com" );
        theRock.setRoles( List.of( "bados_user", "restricted_word" ) );
        theRock.setCreated( LocalDateTime.now().minusDays( 4 ) );
        theRock.setUpdated( LocalDateTime.now().minusDays( 2 ) );

        final var harleyQuinn = new Users();
        harleyQuinn.setId( "333" );
        harleyQuinn.setLocale( "GB_en" );
        harleyQuinn.setForename( "Harleen" );
        harleyQuinn.setSurname( "Quinzel" );
        harleyQuinn.setDisplayName( "Harley Quinn" );
        harleyQuinn.setEmail( "harley.quinn@gotham.city" );
        harleyQuinn.setRoles( List.of( "appeals_team" ) );
        harleyQuinn.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harleyQuinn.setUpdated( LocalDateTime.now().minusDays( 5 ) );

        usersRepository.insert( List.of( eminem, theRock, harleyQuinn ) );
    }

    @Test
    void healthCheck() throws Exception {
        String healthStatus = mockMvc.perform( get( "/accounts-user-api/healthcheck" ))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        assertEquals("{\"status\":\"UP\"}", healthStatus);
    }

    @Test
    void userSearchCorretPrivileges() throws Exception {
        final var responseBody =
        mockMvc.perform( 
                    get( "/users/search?user_email=harley.quinn@gotham.city" )
                        .header("X-Request-Id", "theId123")
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key") 
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "user-data,internal-app")
                    )
               .andExpect(status().isOk())
               .andReturn()
               .getResponse()
               .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var users = objectMapper.readValue(responseBody, new TypeReference<List<User>>(){} );

        Assertions.assertEquals( 1, users.size() );
        Assertions.assertEquals( "Harley Quinn", users.get(0).getDisplayName() );
    }

    @Test
    void userSearchInternlNoPrivileges() throws Exception {
        mockMvc.perform( 
                    get( "/users/search?user_email=harley.quinn@gotham.city" )
                        .header("X-Request-Id", "theId123")
                       )
               .andExpect(status().is(HttpServletResponse.SC_UNAUTHORIZED));
    }

    @Test
    void userSearchInternlPrivilegesOnly() throws Exception {
        mockMvc.perform( 
                    get( "/users/search?user_email=harley.quinn@gotham.city" )
                        .header("X-Request-Id", "theId123")
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key") 
                        .header("ERIC-Authorised-Key-Roles", "*")
                    )
               .andExpect(status().is(HttpServletResponse.SC_UNAUTHORIZED));
    }

    @Test
    void internalUserSearchMissingInternlDataPrivileges() throws Exception {
        mockMvc.perform( 
                    get( "/internal/users/search?partial_email=harley.quinn@gotham.city" )
                        .header("X-Request-Id", "theId123")
                    )
                    .andExpect(status().is(HttpServletResponse.SC_FORBIDDEN));
    }

    @Test
    void internalUserSearchIncorrectPrivileges() throws Exception {
        mockMvc.perform( 
                    get( "/users/search?user_email=harley.quinn@gotham.city" )
                        .header("X-Request-Id", "theId123")
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key") 
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app")                    
                    )
                        .andExpect(status().is(HttpServletResponse.SC_UNAUTHORIZED));

    }

    @Test
    void internalUserSearchCorrectPrivileges() throws Exception {
        final var responseBody =
        mockMvc.perform( 
                    get( "/internal/users/search?partial_email=harley.quinn@gotham.city" )
                        .header("X-Request-Id", "theId123")
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "oauth2") 
                        .header("ERIC-Authorised-Roles", "/admin/user/search")
                    )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
         
                 final var objectMapper = new ObjectMapper();
                 final var users = objectMapper.readValue(responseBody, new TypeReference<List<User>>(){} );
         
                 Assertions.assertEquals( 1, users.size() );
                 Assertions.assertEquals( "Harley Quinn", users.get(0).getDisplayName() );

    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }

}