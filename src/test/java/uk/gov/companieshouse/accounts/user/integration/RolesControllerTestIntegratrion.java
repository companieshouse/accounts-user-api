package uk.gov.companieshouse.accounts.user.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import uk.gov.companieshouse.api.accounts.user.model.Role;

@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Tag("integration-test")
public class RolesControllerTestIntegratrion extends BaseMongoIntegration {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    InterceptorConfig interceptorConfig;

    @BeforeEach
    public void setup() {

    
        UserRole supervisor = new UserRole();
        supervisor.setId("supervisor");
        supervisor.setPermissions(List.of("permission1", "permission2"));

        UserRole admin = new UserRole();
        admin.setId("admin");
        admin.setPermissions(List.of("permission3", "permission4"));

        rolesRepository.insert(List.of(admin, supervisor));
    }

    @DisplayName("Get all roles using the controller")
    @Test
    void getAllRoles() throws Exception {
        String responseBody = mockMvc.perform( get( "/internal/admin/roles" )
                .header("X-Request-Id", "theId123") 
                .header("ERIC-Identity", "123")                
                .header("ERIC-Identity-Type", "oauth2") 
                .header("ERIC-Authorised-Roles", "/admin/roles"))
            .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        final var objectMapper = new ObjectMapper();        
        final var roles = objectMapper.readValue(responseBody, new TypeReference<Set<UserRole>>(){} );

        Assertions.assertEquals( 2, roles.size() );
    }

    @DisplayName("Adding a new role to the database")
    @Test
    void addNewRoleToDatabase() throws Exception {

        Role restrictedWord = new Role();
        restrictedWord.setId("restrictedWord");
        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");

        restrictedWord.setPermissions(permissions);

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( restrictedWord );

         mockMvc.perform( post( "/internal/admin/roles/add" )
            .header("X-Request-Id", "theId123") 
            .header("ERIC-Identity", "123")                
            .header("ERIC-Identity-Type", "oauth2") 
            .header("ERIC-Authorised-Roles", "/admin/roles")            
            .contentType( "application/json" )
            .content(restrictedWordJson ))
            .andExpect(status().isCreated());

        Assertions.assertTrue(rolesRepository.existsById( restrictedWord.getId()));
    }

    @DisplayName("Deleting a role from the database")
    @Test
    void deletingARoleFromTheDatabase() throws Exception {

         mockMvc.perform( delete( "/internal/admin/roles/admin/delete" )
            .header("X-Request-Id", "theId123") 
            .header("ERIC-Identity", "123")                
            .header("ERIC-Identity-Type", "oauth2") 
            .header("ERIC-Authorised-Roles", "/admin/roles"))
            .andExpect(status().isNoContent())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Assertions.assertFalse(rolesRepository.existsById( "admin"));
    }

    @DisplayName("Modifying the permissions for a role")
    @Test
    void modifyThePermissionsForARole() throws Exception {

        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( permissions );        

         mockMvc.perform( put( "/internal//admin/roles/admin/edit" )
            .header("X-Request-Id", "theId123")
            .header("ERIC-Identity", "123")                
            .header("ERIC-Identity-Type", "oauth2") 
            .header("ERIC-Authorised-Roles", "/admin/roles")            
            .contentType( "application/json")
            .content(restrictedWordJson ))
            .andExpect(status().isNoContent());

        Assertions.assertTrue(rolesRepository.findById( "admin").get().getPermissions().contains("permission99"));
    }    

    @DisplayName("Modifying the permissions using the wrong authorised role")
    @Test
    void modifyThePermissionsForARoleNotAuthorised() throws Exception {

         mockMvc.perform( put( "/internal/admin/roles/admin/edit" )
            .header("X-Request-Id", "theId123")
            .header("ERIC-Identity", "123")                
            .header("ERIC-Identity-Type", "oauth2") 
            .header("ERIC-Authorised-Roles", "/admin/user/search")
            .contentType( "application/json"))
            .andExpect(status().isForbidden());
    } 

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( UserRole.class );
    }

}