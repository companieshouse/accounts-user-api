package uk.gov.companieshouse.accounts.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Tag("unit-test")
@WebMvcTest(UserRolesController.class)
public class UserRolesControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    UsersService usersService;

    @MockBean
    InterceptorConfig interceptorConfig;

    private User userEminem;
    private User userTheRock;
    private User userHarleyQuinn;
    private Users harryPotter;

    @BeforeEach
    void setup() {

        final var supervisor = new RolesList();
        supervisor.add( Role.SUPERVISOR );

        userEminem = new User();
        userEminem.userId("111")
                .forename("Marshall")
                .surname("Mathers")
                .displayName("Eminem")
                .email("eminem@rap.com")
                .roles( supervisor );

        final var badosUserAndRestrictedWord = new RolesList();
        badosUserAndRestrictedWord.addAll( List.of( Role.BADOS_USER, Role.RESTRICTED_WORD ) );

        userTheRock = new User();
        userTheRock.userId("222")
                .forename("Dwayne")
                .surname("Johnson")
                .displayName("The Rock")
                .email("the.rock@wrestling.com")
                .roles( badosUserAndRestrictedWord );

        final var appealsTeam = new RolesList();
        appealsTeam.add( Role.APPEALS_TEAM );

        userHarleyQuinn = new User();
        userHarleyQuinn.userId("333")
                .forename("Harleen")
                .surname("Quinzel")
                .displayName("Harley Quinn")
                .email("harley.quinn@gotham.city")
                .roles( appealsTeam );

        harryPotter = new Users();
        harryPotter.setId( "444" );
        harryPotter.setLocale( "GB_en" );
        harryPotter.setForename( "Daniel" );
        harryPotter.setSurname( "Radcliff" );
        harryPotter.setDisplayName( "Harry Potter" );
        harryPotter.setEmail( "harry.potter@under-the-stairs.com" );
        harryPotter.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harryPotter.setUpdated( LocalDateTime.now().minusDays( 5 ) );

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
        Mockito.doReturn(Optional.empty()).when(usersService).fetchUser(any());

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
        Mockito.doReturn( Optional.of( userHarleyQuinn ) ).when(usersService ).fetchUser( any() );

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("support-member") );

        mockMvc.perform( put( "/users/{user_id}/roles", "333" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isCreated() );

        Mockito.verify( usersService ).setRoles( eq( "333" ), eq( List.of( Role.SUPPORT_MEMBER ) ) );
    }

    @Test
    void setUserRolesWithMultipleRolesSetsTheUsersRoles() throws Exception {
        Mockito.doReturn( Optional.of( userHarleyQuinn ) ).when(usersService ).fetchUser( any() );

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("support-member", "csi_support" ) );

        mockMvc.perform( put( "/users/{user_id}/roles", "333" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isCreated() );

        Mockito.verify( usersService ).setRoles( eq( "333" ), eq( List.of( Role.SUPPORT_MEMBER, Role.CSI_SUPPORT ) ) );
    }

    @Test
    void setUserRolesWithDuplicatesRolesSetsTheRoleOnce() throws Exception {
        Mockito.doReturn( Optional.of( userHarleyQuinn ) ).when(usersService ).fetchUser( any() );

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("support-member", "support-member" ) );

        mockMvc.perform( put( "/users/{user_id}/roles", "333" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isCreated() );

        Mockito.verify( usersService ).setRoles( eq( "333" ), eq( List.of( Role.SUPPORT_MEMBER, Role.SUPPORT_MEMBER ) ) );
    }

    @Test
    void setUserRolesCreatesNewRoleFieldWhenNotPresent() throws Exception {
        Mockito.doReturn( Optional.of( harryPotter ) ).when(usersService ).fetchUser( any() );

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("support-member") );

        mockMvc.perform( put( "/users/{user_id}/roles", "444" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isCreated() );

        Mockito.verify( usersService ).setRoles( eq( "444" ), eq( List.of( Role.SUPPORT_MEMBER ) ) );
    }


    @Test
    void setUserRolesReturnsInternalServerErrorWhenDatabaseFailsToUpdate() throws Exception {
        Mockito.doReturn( Optional.of( harryPotter ) ).when(usersService ).fetchUser( any() );
        Mockito.doThrow( RuntimeException.class ).when( usersService ).setRoles( any(), any() );

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.writeValueAsString( List.of("support-member") );

        mockMvc.perform( put( "/users/{user_id}/roles", "444" )
                        .header( "X-Request-Id", "theId123" )
                        .contentType( "application/json" )
                        .content( roles ) )
                .andExpect( status().isInternalServerError() );

        Mockito.verify( usersService ).setRoles( eq( "444" ), eq( List.of( Role.SUPPORT_MEMBER ) ) );
    }

}
