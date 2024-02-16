package uk.gov.companieshouse.accounts.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
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
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Tag("unit-test")
@WebMvcTest(GetUserRecordController.class)
public class GetUserRecordControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    UsersService usersService;

    @MockBean
    InterceptorConfig interceptorConfig;

    private User userEminem;
    private User userTheRock;
    private User userHarleyQuinn;

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
        Mockito.doReturn( Optional.empty() ).when( usersService ).fetchUser( any() );

        mockMvc.perform( get( "/users/{user_id}", "999" ).header( "X-Request-Id", "theId123" ) ).andExpect( status().isNotFound() );
    }

    @Test
    void getUserDetailsFetchesUserDetails() throws Exception {
        Mockito.doReturn( Optional.of( userHarleyQuinn ) ).when( usersService ).fetchUser( any() );

        final var responseBody =
                mockMvc.perform( get( "/users/{user_id}", "333" ).header( "X-Request-Id", "theId123" ) )
                        .andExpect( status().isOk() )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var user = objectMapper.readValue( responseBody, User.class );

        Assertions.assertEquals( "Harley Quinn", user.getDisplayName() );
    }

}
