package uk.gov.companieshouse.accounts.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.accounts.user.model.Role.SUPERVISOR;

@Tag("unit-test")
@WebMvcTest(FindRolesBasedOnUserIDController.class)
public class FindRolesBasedOnUserIDControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    UsersService usersService;

    @MockBean
    InterceptorConfig interceptorConfig;

    private User userTestOne;
    private User userTestTwo;
    private User userEminem;

    @BeforeEach
    void setup() {

        userEminem = new User();
        userEminem.userId("111")
                .forename("Marshall")
                .surname("Mathers")
                .displayName("Eminem")
                .email("eminem@rap.com")
                .roles(Set.of( Role.SUPERVISOR ));

        userTestOne = new User();
        userTestOne.userId("111")
        .forename( "Marshall" )
        .surname( "Mathers" )
        .displayName( "Eminem" )
        .email( "eminem@rap.com" )
        .roles( Set.of( Role.SUPERVISOR ) );


        userTestTwo = new User();
        userTestTwo.userId("222")
                .roles(Set.of(Role.SUPPORT_MEMBER, Role.RESTRICTED_WORD));

        Mockito.doNothing().when(interceptorConfig).addInterceptors(any());
    }

    @Test
    void getUserRolesWithMissingParamsReturnsNotFound() throws Exception {
        mockMvc.perform( get( "/users/{user_id}/roles" , "333").header("X-Request-Id", "theId") ).andExpect(status().isNotFound());
    }

    @Test
    void getUserRolesWithMalformedUserIDReturnsEmptyList() throws Exception {

        Mockito.doReturn(Set.of()).when(usersService).findRolesByUserId(any());

        final var responseBody =
                mockMvc.perform(get("/users/{user_id}/roles", "abc").header("X-Request-Id", "theId")).andExpect(status().isNotFound());
    }

    @Test
    void getUserRolesWithNonexistentUserIDReturnsNotFound() throws Exception {
        mockMvc.perform( get( "/users/{user_id}/roles", "111111" ).header("X-Request-Id", "theId") ).andExpect(status().isNotFound());
    }

    @Test
    void getUserRolesWithOneUserIdReturnsOneRole() throws Exception {

      //  Mockito.doReturn(List.of( userTestOne ) ).when( usersService ).findRolesByUserId(any());

        final var responseBody =
                mockMvc.perform( get( "/users/{user_id}/roles" , "111").header("X-Request-Id", "theId") )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.readValue(responseBody, new TypeReference<List<Role>>(){} );

        Assertions.assertEquals( 1, roles.size() );
        Assertions.assertTrue( roles.contains(SUPERVISOR) );
    }

    @Test
    void getUserRolesWithOneUserIdReturnsMultipleRoles() throws Exception {

        Mockito.doReturn( List.of( userTestTwo ) ).when( usersService ).fetchUsers( any() );

        final var responseBody =
                mockMvc.perform( get( "/users/{user_id}/roles" , "111").header("X-Request-Id", "theId") )
                        .andExpect(status().isOk());

        final var objectMapper = new ObjectMapper();
       // final var roles = objectMapper.readValue(responseBody, new TypeReference<Set<Role>>(){} );

       // Assertions.assertEquals( 2, roles.size() );
       // Assertions.assertTrue( roles.containsAll( Set.of( "SUPPORT_MEMBER", "RESTRICTED_WORD" ) ) );
    }
        //searchuserRolesWithBaduserIdReturnsBadRequest
    //searchuserRolesWithNonExistingUserIdReturnsEmtpyList
    //searchuserRolesWithValidUserIdReturnsValidListOfRoles
}
