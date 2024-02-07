package uk.gov.companieshouse.accounts.user.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.controller.FindRolesBasedOnUserIDController;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
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
        final var userTestOne = new Users();
        userTestOne.setId("111");
        userTestOne.setLocale( "GB_en" );
        userTestOne.setForename( "Marshall" );
        userTestOne.setSurname( "Mathers" );
        userTestOne.setDisplayName( "Eminem" );
        userTestOne.setEmail( "eminem@rap.com" );
        userTestOne.setRoles( Set.of( Role.SUPERVISOR ) );
        userTestOne.setCreated( LocalDateTime.now().minusDays( 1 ) );
        userTestOne.setUpdated( LocalDateTime.now() );


        final var userTestTwo = new Users();
        userTestTwo.setId("222");
        userTestTwo.setRoles(Set.of(Role.SUPPORT_MEMBER, Role.RESTRICTED_WORD));


       usersRepository.insert( List.of( userTestOne, userTestTwo ) );

        Mockito.doNothing().when(interceptorConfig).addInterceptors(any());
    }

    @Test
    void getUserRolesWithMissingParamsReturnsNotFound() throws Exception {
        mockMvc.perform( get( "/users/{user_id}/roles" , "333").header("X-Request-Id", "theId") ).andExpect(status().isNotFound());
    }

    @Test
    void getUserRolesWithMalformedUserIDReturnsEmptyList() throws Exception {

        Mockito.doReturn(Set.of()).when(usersRepository).findRolesByUserId(any());

        final var responseBody =
                mockMvc.perform(get("/users/{user_id}/roles", "abc").header("X-Request-Id", "theId")).andExpect(status().isNotFound());
    }

    @Test
    void getUserRolesWithNonexistentUserIDReturnsNotFound() throws Exception {
        mockMvc.perform( get( "/associations/companies/{company_number}/users/{user_email}/{status}", "111111", "krishna.patel@dahai.art", "Removed" ).header("X-Request-Id", "theId") ).andExpect(status().isNotFound());
    }

    @Test
    void searchUserRolesWithEmptyUserIDReturnsEmptyList() throws Exception {

        final var responseBody =
                mockMvc.perform( get( "/users/{444}/roles" ).header("X-Request-Id", "theId") )
                        .andExpect(status().isNoContent())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.readValue(responseBody, new TypeReference<Set<Role>>(){} );

        Assertions.assertEquals( Set.of(), roles );
    }
    @Test
    void getUserRolesWithOneUserIdReturnsOneRole() throws Exception {
usersRepository.findAll().stream().forEach(System.out::println);
        final var responseBody =
                mockMvc.perform( get( "/users/{user_id}/roles" , "111" ).header("X-Request-Id", "theId") )
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
                mockMvc.perform( get( "/users/{222}/roles" ).header("X-Request-Id", "theId") )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var roles = objectMapper.readValue(responseBody, new TypeReference<Set<Role>>(){} );

        Assertions.assertEquals( 2, roles.size() );
        //Assertions.assertTrue( roles.stream().map( User::getDisplayName ).toList().containsAll( Set.of( "SUPPORT_MEMBER", "RESTRICTED_WORD" ) ) );
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }
        //searchuserRolesWithBaduserIdReturnsBadRequest
    //searchuserRolesWithNonExistingUserIdReturnsEmtpyList
    //searchuserRolesWithValidUserIdReturnsValidListOfRoles
}
