package uk.gov.companieshouse.accounts.user.integration;

import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.List;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
@Tag("integration-test")
public class UserRolesControllerTest {

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

        final var supervisor = new RolesList();
        supervisor.add( Role.SUPERVISOR );

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

        final var badosUserAndRestrictedWord = new RolesList();
        badosUserAndRestrictedWord.addAll( List.of( Role.BADOS_USER, Role.RESTRICTED_WORD ) );

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

        final var appealsTeam = new RolesList();
        appealsTeam.add( Role.APPEALS_TEAM );

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

        Mockito.doNothing().when(interceptorConfig).addInterceptors( any() );
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
        final var roles = objectMapper.writeValueAsString( List.of("support-member") );

        mockMvc.perform( put( "/users/{user_id}/roles", "333" )
                         .header( "X-Request-Id", "theId123" )
                         .contentType( "application/json" )
                         .content( roles ) )
               .andExpect( status().isOk() );

        Assertions.assertEquals( List.of(Role.SUPPORT_MEMBER), usersRepository.findUsersById( "333" ).get().getRoles() );
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
        Assertions.assertTrue( actualRoles.containsAll( List.of(Role.SUPPORT_MEMBER, Role.CSI_SUPPORT) ) );
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

        Assertions.assertEquals( List.of(Role.SUPPORT_MEMBER ), usersRepository.findUsersById( "333" ).get().getRoles() );
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

        Assertions.assertEquals( List.of(Role.SUPPORT_MEMBER), usersRepository.findUsersById( "444" ).get().getRoles() );
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }

}
