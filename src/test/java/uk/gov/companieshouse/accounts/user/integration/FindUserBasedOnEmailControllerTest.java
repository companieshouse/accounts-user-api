package uk.gov.companieshouse.accounts.user.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.User;

@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
@Tag("integration-test")
public class FindUserBasedOnEmailControllerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer container = new MongoDBContainer("mongo:4.4.22");

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
        final var eminem = new Users();
        eminem.setId( "111" );
        eminem.setLocale( "GB_en" );
        eminem.setForename( "Marshall" );
        eminem.setSurname( "Mathers" );
        eminem.setDisplayName( "Eminem" );
        eminem.setEmail( "eminem@rap.com" );
        eminem.setRoles( Set.of( Role.SUPERVISOR ) );
        eminem.setCreated( LocalDateTime.now().minusDays( 1 ) );
        eminem.setUpdated( LocalDateTime.now() );

        final var theRock = new Users();
        theRock.setId( "222" );
        theRock.setLocale( "GB_en" );
        theRock.setForename( "Dwayne" );
        theRock.setSurname( "Johnson" );
        theRock.setDisplayName( "The Rock" );
        theRock.setEmail( "the.rock@wrestling.com" );
        theRock.setRoles( Set.of( Role.BADOS_USER, Role.RESTRICTED_WORD ) );
        theRock.setCreated( LocalDateTime.now().minusDays( 4 ) );
        theRock.setUpdated( LocalDateTime.now().minusDays( 2 ) );

        final var harleyQuinn = new Users();
        harleyQuinn.setId( "333" );
        harleyQuinn.setLocale( "GB_en" );
        harleyQuinn.setForename( "Harleen" );
        harleyQuinn.setSurname( "Quinzel" );
        harleyQuinn.setDisplayName( "Harley Quinn" );
        harleyQuinn.setEmail( "harley.quinn@gotham.city" );
        harleyQuinn.setRoles( Set.of( Role.APPEALS_TEAM ) );
        harleyQuinn.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harleyQuinn.setUpdated( LocalDateTime.now().minusDays( 5 ) );

        usersRepository.insert( List.of( eminem, theRock, harleyQuinn ) );

        Mockito.doNothing().when(interceptorConfig).addInterceptors( any() );
    }

    @Test
    void searchUserDetailsWithoutQueryParamsReturnsBadRequest() throws Exception {
        mockMvc.perform( get( "/users/search" ).header("X-Request-Id", "theId") ).andExpect(status().isBadRequest());
    }

    @Test
    void searchUserDetailsWithMalformedEmailReturnsBadRequest() throws Exception {
        mockMvc.perform( get( "/users/search?user_email=null" ).header("X-Request-Id", "theId") ).andExpect(status().isBadRequest());
        mockMvc.perform( get( "/users/search?user_email=" ).header("X-Request-Id", "theId") ).andExpect(status().isBadRequest());
        mockMvc.perform( get( "/users/search?user_email=\"\"" ).header("X-Request-Id", "theId") ).andExpect(status().isBadRequest());
        mockMvc.perform( get( "/users/search?user_email=xyz" ).header("X-Request-Id", "theId") ).andExpect(status().isBadRequest());
    }

    @Test
    void searchUserDetailsWithNonexistentEmailsReturnsEmptyList() throws Exception {

        final var responseBody =
                mockMvc.perform( get( "/users/search?user_email=back.street.boys@the90s.city" ).header("X-Request-Id", "theId") )
                        .andExpect(status().isNoContent())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var users = objectMapper.readValue(responseBody, new TypeReference<List<User>>(){} );

        Assertions.assertEquals( List.of(), users );
    }

    @Test
    void searchUserDetailsWithOneEmailReturnsOneUser() throws Exception {

        final var responseBody =
        mockMvc.perform( get( "/users/search?user_email=harley.quinn@gotham.city" ).header("X-Request-Id", "theId") )
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
    void searchUserDetailsWithMultipleEmailsReturnsMultipleUsers() throws Exception {

        final var responseBody =
                mockMvc.perform( get( "/users/search?user_email=eminem@rap.com&user_email=the.rock@wrestling.com" ).header("X-Request-Id", "theId") )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var users = objectMapper.readValue(responseBody, new TypeReference<List<User>>(){} );

        Assertions.assertEquals( 2, users.size() );
        Assertions.assertTrue( users.stream().map( User::getDisplayName ).toList().containsAll( List.of( "Eminem", "The Rock" ) ) );
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }

}