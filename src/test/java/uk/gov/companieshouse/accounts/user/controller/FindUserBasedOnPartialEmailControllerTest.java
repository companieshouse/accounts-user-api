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
import uk.gov.companieshouse.accounts.user.repositories.OauthRepository;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit-test")
@WebMvcTest(FindUserBasedOnPartialEmailController.class)
public class FindUserBasedOnPartialEmailControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    UsersService usersService;

    @MockBean
    InterceptorConfig interceptorConfig;

    @MockBean
    OauthRepository oauthRepository;

    private User eminem;
    private User theRock;
    private User harleyQuinn;
    private User harryPotter;
    @BeforeEach
    void setup() {

        final var supervisor = new RolesList();
        supervisor.add("supervisor");

        eminem = new User();
        eminem.userId("111")
                .forename("Marshall")
                .surname("Mathers")
                .displayName("Eminem")
                .email("eminem@rap.com")
                .roles(supervisor);

        final var badosUserAndRestrictedWord = new RolesList();
        badosUserAndRestrictedWord.addAll(List.of("bados_user", "restricted_word"));

        theRock = new User();
        theRock.userId("222")
                .forename("Dwayne")
                .surname("Johnson")
                .displayName("The Rock")
                .email("the.rock@wrestling.com")
                .roles(badosUserAndRestrictedWord);

        final var appealsTeam = new RolesList();
        appealsTeam.add("appeals_team");

        harleyQuinn = new User();
        harleyQuinn.userId("333")
                .forename("Harleen")
                .surname("Quinzel")
                .displayName("Harley Quinn")
                .email("harley.quinn@gotham.city")
                .roles(appealsTeam);

        harryPotter = new User().userId("444")
        .forename("Daniel")
        .surname("Radcliff")
        .displayName("Harry Potter")
        .email("harry.potter@under-the-stairs.com");
        

        Mockito.doNothing().when(interceptorConfig).addInterceptors(any());
    }

    @Test
    void searchUsersDetailsPartialEmailSingle() throws Exception {
        
        Mockito.doReturn(List.of(theRock)).when(usersService).fetchUsersUsingPartialEmail(any());

        final var responseBody =
                mockMvc.perform(get("/internal/users/search?partial_email=rock").header("X-Request-Id", "theId123"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var users = objectMapper.readValue(responseBody, new TypeReference<List<User>>(){});

        Assertions.assertEquals(1, users.size());
        Assertions.assertTrue(users.stream().map(User::getDisplayName).toList().containsAll(List.of("The Rock")));
    }    
    
    @Test
    void searchUsersDetailsPartialEmailMultiple() throws Exception {
        
        Mockito.doReturn(List.of(harleyQuinn,harryPotter)).when(usersService).fetchUsersUsingPartialEmail(any());

        final var responseBody =
                mockMvc.perform(get("/internal/users/search?partial_email=ha").header("X-Request-Id", "theId123"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var users = objectMapper.readValue(responseBody, new TypeReference<List<User>>(){});

        Assertions.assertEquals(2, users.size());
        
        Assertions.assertTrue(users.stream().map(User::getDisplayName).allMatch(user -> (user.equals("Harry Potter")) || (user.equals("Harley Quinn"))));  
  }

   @Test
   void searchUsersDetailsPartialBadRequestNull() throws Exception {
        mockMvc.perform(get("/internal/users/search").header("X-Request-Id", "theId123"))
                .andExpect(status().isBadRequest());
        } 
        
        @Test
        void searchUsersDetailsPartialBadRequestEmpty() throws Exception {
             mockMvc.perform(get("/internal/users/search?partial_email").header("X-Request-Id", "theId123"))
                     .andExpect(status().isBadRequest());
             }         

}
