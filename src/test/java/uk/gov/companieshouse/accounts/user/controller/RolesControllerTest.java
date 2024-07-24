package uk.gov.companieshouse.accounts.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.service.RolesService;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.Roles;

@Tag("unit-test")
@WebMvcTest(RolesController.class)
class RolesControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    RolesService rolesService;

    @MockBean
    UsersService usersService;

    @MockBean
    InterceptorConfig interceptorConfig;

    UserRole supervisor = new UserRole();
    UserRole admin = new UserRole();

    @BeforeEach
    public void setup() {
        supervisor.setId("supervisor");
        supervisor.setPermissions(List.of("permission1", "permission2"));

        admin.setId("admin");
        admin.setPermissions(List.of("permission3", "permission4"));

        Mockito.doNothing().when(interceptorConfig).addInterceptors(any());
    }

    @Test
    void getUserRolesWithoutPathVariableReturnsNotFound() throws Exception {
        mockMvc.perform( get( "/admin/role" ).header( "X-Request-Id", "theId123" ) )
                .andExpect( status().isNotFound() );
    }

    @DisplayName("Get all roles using the controller")
    @Test
    void getAllRoles() throws Exception {

        Roles roles = new Roles();

        Role admin = new Role();
        admin.setId("admin");

        Role supervisor = new Role();
        supervisor.setId("supervisor");
        roles.add(supervisor);
        roles.add(admin);

        Mockito.doReturn(roles).when(rolesService).getRoles(); 

        String responseBody = mockMvc.perform( get( "/internal/admin/roles" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        final var objectMapper = new ObjectMapper();        
        final var returnedRoles = objectMapper.readValue(responseBody, new TypeReference<Set<UserRole>>(){} );

        Assertions.assertEquals( 2, returnedRoles.size() );
    }

    @DisplayName("Get all roles using the controller - empty database")
    @Test
    void getAllRolesEmptyDatabase() throws Exception {

        Mockito.doReturn(new Roles()).when(rolesService).getRoles(); 

        String responseBody = mockMvc.perform( get( "/internal/admin/roles" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

        final var objectMapper = new ObjectMapper();        
        final var roles = objectMapper.readValue(responseBody, new TypeReference<Set<UserRole>>(){} );

        Assertions.assertEquals( 0, roles.size() );
    }

    @DisplayName("Adding a new role to the database - No Permissions")
    @Test
    void addNewRoleToDatabaseNoPermissions() throws Exception {

        Role restrictedWord = new Role();
        restrictedWord.setId("restrictedWord");

        when(rolesService.addRole(any())).thenReturn(true);

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( restrictedWord );

         mockMvc.perform( post( "/internal/admin/roles/add" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( restrictedWordJson ) )
            .andExpect( status().isNoContent());
    }

    @DisplayName("Adding a new role to the database - No Role ID - Bad request thrown")
    @Test
    void addNewRoleToDatabaseNoRoleId() throws Exception {

        Role restrictedWord = new Role();
        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");
        restrictedWord.setPermissions(permissions);

        when(rolesService.addRole(any())).thenReturn(true);

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( restrictedWord );

         mockMvc.perform( post( "/internal/admin/roles/add" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( restrictedWordJson ) )
            .andExpect( status().isBadRequest());
    }

    @DisplayName("Adding a new role to the database")
    @Test
    void addNewRoleToDatabase() throws Exception {

        Role restrictedWord = new Role();
        restrictedWord.setId("restrictedWord");
        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");
        restrictedWord.setPermissions(permissions);

        when(rolesService.addRole(any())).thenReturn(true);

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( restrictedWord );

         mockMvc.perform( post( "/internal/admin/roles/add" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( restrictedWordJson ) )
            .andExpect( status().isCreated());
    }

    @DisplayName("Adding a new role to the database - empty fields")
    @Test
    void addNewRoleToDatabaseEmptyFields() throws Exception {

        when(rolesService.addRole(any())).thenReturn(true);

        mockMvc.perform( post( "/internal/admin/roles/add" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( new byte[0] ) )
            .andExpect( status().isBadRequest());
    }
    
    @DisplayName("Adding a new role to the database -  failed addition")
    @Test
    void addNewRoleToDatabaseFailedAddition() throws Exception {

        Role restrictedWord = new Role();
        restrictedWord.setId("restrictedWord");
        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");
        restrictedWord.setPermissions(permissions);

        when(rolesService.addRole(any())).thenReturn(false);

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( restrictedWord );

         mockMvc.perform( post( "/internal/admin/roles/add" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( restrictedWordJson ) )
            .andExpect(status().isBadRequest());
    }

    @DisplayName("Deleting a role from the database - malformed request")
    @Test
    void deletingARoleFromTheDatabaseMalformedRequest() throws Exception {

        when(rolesService.deleteRole(any())).thenReturn(true);

         mockMvc.perform( delete( "/internal/admin/roles/^/delete" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isBadRequest());
    }

    @DisplayName("Deleting a role from the database - failed deletion")
    @Test
    void deletingARoleFromTheDatabaseFailedDeletion() throws Exception {

        when(rolesService.deleteRole(any())).thenReturn(false);

         mockMvc.perform( delete( "/internal/admin/roles/supervisor/delete" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @DisplayName("Deleting a role from the database")
    @Test
    void deletingARoleFromTheDatabase() throws Exception {

        when(rolesService.deleteRole(any())).thenReturn(true);

         mockMvc.perform( delete( "/internal/admin/roles/supervisor/delete" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isNoContent())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @DisplayName("Modifying the permissions for a role - failed update")
    @Test
    void modifyThePermissionsForARoleFailedUpdate() throws Exception {

        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");

        final var objectMapper = new ObjectMapper();
        final var permissionsJson = objectMapper.writeValueAsString( permissions );        

        when(rolesService.editRole(any(),any())).thenReturn(false);

         mockMvc.perform( put( "/internal/admin/roles/admin/edit" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( permissionsJson ) )
            .andExpect( status().isBadRequest());
    }    

    @DisplayName("Modifying the permissions for a role - permissions not provided")
    @Test
    void modifyThePermissionsForARoleNoPermissions() throws Exception {

        PermissionsList permissions = new PermissionsList();

        final var objectMapper = new ObjectMapper();
        final var permissionJson = objectMapper.writeValueAsString( permissions );        

        when(rolesService.editRole(any(),any())).thenReturn(true);

         mockMvc.perform( put( "/internal/admin/roles/admin/edit" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content(permissionJson ) )
            .andExpect( status().isOk());
    } 

    @DisplayName("Modifying the permissions for a role")
    @Test
    void modifyThePermissionsForARole() throws Exception {

        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");

        final var objectMapper = new ObjectMapper();
        final var permissionsJson = objectMapper.writeValueAsString( permissions );        

        when(rolesService.editRole(any(),any())).thenReturn(true);

         mockMvc.perform( put( "/internal/admin/roles/admin/edit" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( permissionsJson ) )
            .andExpect( status().isNoContent());
    }  

    @DisplayName("Modifying the permissions for a role - malformed request")
    @Test
    void modifyThePermissionsForARoleMalformedREquest() throws Exception {

        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");

        final var objectMapper = new ObjectMapper();
        final var permissionsJson = objectMapper.writeValueAsString( permissions );        

        when(rolesService.editRole(any(),any())).thenReturn(false);

         mockMvc.perform( put( "/internal/admin/roles/^/edit" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content(permissionsJson) )
            .andExpect(status().isBadRequest());
    }      
}