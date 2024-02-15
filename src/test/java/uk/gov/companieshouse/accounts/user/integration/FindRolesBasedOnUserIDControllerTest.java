package uk.gov.companieshouse.accounts.user.integration;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.accounts.user.model.Role.SUPERVISOR;

@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
@Tag("integration-test")
public class FindRolesBasedOnUserIDControllerTest {

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
        final var eminem = new Users();
        eminem.setId( "111" );
        eminem.setLocale( "GB_en" );
        eminem.setForename( "Marshall" );
        eminem.setSurname( "Mathers" );
        eminem.setDisplayName( "Eminem" );
        eminem.setEmail( "eminem@rap.com" );
        eminem.setRoles( List.of( Role.SUPERVISOR ) );
        eminem.setCreated( LocalDateTime.now().minusDays( 1 ) );
        eminem.setUpdated( LocalDateTime.now() );

        final var theRock = new Users();
        theRock.setId( "222" );
        theRock.setLocale( "GB_en" );
        theRock.setForename( "Dwayne" );
        theRock.setSurname( "Johnson" );
        theRock.setDisplayName( "The Rock" );
        theRock.setEmail( "the.rock@wrestling.com" );
        theRock.setRoles( List.of( Role.BADOS_USER, Role.RESTRICTED_WORD ) );
        theRock.setCreated( LocalDateTime.now().minusDays( 4 ) );
        theRock.setUpdated( LocalDateTime.now().minusDays( 2 ) );

        final var harleyQuinn = new Users();
        harleyQuinn.setId( "333" );
        harleyQuinn.setLocale( "GB_en" );
        harleyQuinn.setForename( "Harleen" );
        harleyQuinn.setSurname( "Quinzel" );
        harleyQuinn.setDisplayName( "Harley Quinn" );
        harleyQuinn.setEmail( "harley.quinn@gotham.city" );
        harleyQuinn.setRoles( List.of( Role.APPEALS_TEAM ) );
        harleyQuinn.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harleyQuinn.setUpdated( LocalDateTime.now().minusDays( 5 ) );

        usersRepository.insert( List.of( eminem, theRock, harleyQuinn ) );

        Mockito.doNothing().when(interceptorConfig).addInterceptors( any() );
    }
    @Test
    void getUserRolesWithMalformedUserIDReturnsBadRequest() throws Exception {

        mockMvc.perform(get("/users/{user_id}/roles", "$%").header("X-Request-Id", "theId123")).andExpect(status().isBadRequest());
    }

    @Test
    void getUserRolesWithNonexistentUserIDReturnsNotFound() throws Exception {
        mockMvc.perform( get( "/associations/companies/{company_number}/users/{user_email}/{status}", "111111", "krishna.patel@dahai.art", "Removed" ).header("X-Request-Id", "theId123") ).andExpect(status().isNotFound());
    }

    @Test
    void getUserRolesWithOneUserIdReturnsOneRole() throws Exception {

        final var responseBody =
                mockMvc.perform( get( "/users/{user_id}/roles", "111" ).header("X-Request-Id", "theId123") )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.readValue(responseBody, new TypeReference<Set<Role>>(){} );

        Assertions.assertEquals( 1, roles.size() );
        Assertions.assertTrue( roles.contains(SUPERVISOR) );
    }
    @Test
    void searchUserRolesWithOneUserIdReturnsMultipleRoles() throws Exception {

        final var responseBody =
                mockMvc.perform( get( "/users/{user_id}/roles" , "222" ).header("X-Request-Id", "theId123") )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.readValue(responseBody, new TypeReference<Set<Role>>(){} );

        Assertions.assertEquals( 2, roles.size() );
        Assertions.assertTrue( roles.containsAll( Set.of(Role.BADOS_USER, Role.RESTRICTED_WORD )));
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }

}
