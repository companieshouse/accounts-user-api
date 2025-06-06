package uk.gov.companieshouse.accounts.user.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Tag("integration-test")
public class FindUserBasedOnEmailControllerTest {

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
        appealsTeam.add("appeals_team" );

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

        usersRepository.insert( List.of( eminem, theRock, harleyQuinn ) );

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

        final var responseBody =
        mockMvc.perform( get( "/users/search?user_email=harley.quinn@gotham.city" ).header("X-Request-Id", "theId123") )
               .andExpect(status().isOk())
               .andReturn()
               .getResponse()
               .getContentAsString();

        final var objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        final var users = objectMapper.readValue(responseBody, new TypeReference<List<User>>(){} );

        Assertions.assertEquals( 1, users.size() );
        Assertions.assertEquals( "Harley Quinn", users.get(0).getDisplayName() );
    }

    @Test
    void searchUserDetailsWithMultipleEmailsReturnsMultipleUsers() throws Exception {

        final var responseBody =
                mockMvc.perform( get( "/users/search?user_email=eminem@rap.com&user_email=the.rock@wrestling.com" ).header("X-Request-Id", "theId123") )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

        final var users = objectMapper.readValue(responseBody, new TypeReference<List<User>>(){} );

        Assertions.assertEquals( 2, users.size() );
        Assertions.assertTrue( users.stream().map( User::getDisplayName ).toList().containsAll( List.of( "Eminem", "The Rock" ) ) );
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }

}