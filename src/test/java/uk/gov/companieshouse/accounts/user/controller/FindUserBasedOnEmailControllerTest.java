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
import uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit-test")
@WebMvcTest(FindUserBasedOnEmailController.class)
class FindUserBasedOnEmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    @MockBean
    private InterceptorConfig interceptorConfig;

    @MockBean
    private StaticPropertyUtil staticPropertyUtil;

    private User userEminem;
    private User userTheRock;
    private User userHarleyQuinn;

    @BeforeEach
    void setup() {

        final var supervisor = new RolesList();
        supervisor.add( "supervisor" );

        userEminem = new User();
        userEminem.userId("111")
                .forename("Marshall")
                .surname("Mathers")
                .displayName("Eminem")
                .email("eminem@rap.com")
                .roles( supervisor );

        final var badosUserAndRestrictedWord = new RolesList();
        badosUserAndRestrictedWord.addAll( List.of( "bados_user", "restricted_word" ) );

        userTheRock = new User();
        userTheRock.userId("222")
                .forename("Dwayne")
                .surname("Johnson")
                .displayName("The Rock")
                .email("the.rock@wrestling.com")
                .roles( badosUserAndRestrictedWord );

        final var appealsTeam = new RolesList();
        appealsTeam.add( "appeals_team" );

        userHarleyQuinn = new User();
        userHarleyQuinn.userId("333")
                .forename("Harleen")
                .surname("Quinzel")
                .displayName("Harley Quinn")
                .email("harley.quinn@gotham.city")
                .roles( appealsTeam );

        Mockito.doNothing().when(interceptorConfig).addInterceptors( any() );
    }

    @Test
    void searchUserDetailsWithoutQueryParamsReturnsBadRequest() throws Exception {
        mockMvc.perform( get( "/users/search" ).header("X-Request-Id", "theId123") ).andExpect(status().isBadRequest());
    }

    @Test
    void searchUserDetailsWithMalformedEmailReturnsBadRequest() throws Exception {
        mockMvc.perform( get( "/users/search?user_email=null" ).header("X-Request-Id", "theId123") ).andExpect(status().isBadRequest());
        mockMvc.perform( get( "/users/search?user_email=" ).header("X-Request-Id", "theId123") ).andExpect(status().isBadRequest());
        mockMvc.perform( get( "/users/search?user_email=\"\"" ).header("X-Request-Id", "theId123") ).andExpect(status().isBadRequest());
        mockMvc.perform( get( "/users/search?user_email=xyz" ).header("X-Request-Id", "theId123") ).andExpect(status().isBadRequest());
    }

    @Test
    void searchUserDetailsWithNonexistentEmailsReturnsEmptyList() throws Exception {

        Mockito.doReturn( List.of() ).when( usersService ).fetchUsers( any() );

        final var responseBody =
                mockMvc.perform( get( "/users/search?user_email=back.street.boys@the90s.city" ).header("X-Request-Id", "theId123") )
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

        Mockito.doReturn( List.of( userHarleyQuinn ) ).when( usersService ).fetchUsers( any() );

        final var responseBody =
                mockMvc.perform( get( "/users/search?user_email=harley.quinn@gotham.city" ).header("X-Request-Id", "theId123") )
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

        Mockito.doReturn( List.of( userEminem, userTheRock ) ).when( usersService ).fetchUsers( any() );

        final var responseBody =
                mockMvc.perform( get( "/users/search?user_email=eminem@rap.com&user_email=the.rock@wrestling.com" ).header("X-Request-Id", "theId123") )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var users = objectMapper.readValue(responseBody, new TypeReference<List<User>>(){} );

        Assertions.assertEquals( 2, users.size() );
        Assertions.assertTrue( users.stream().map( User::getDisplayName ).toList().containsAll( List.of( "Eminem", "The Rock" ) ) );
    }

}
