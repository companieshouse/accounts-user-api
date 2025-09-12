package uk.gov.companieshouse.accounts.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.service.AdminPermissionsService;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroup;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroups;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPermissionsControllerTest {

    @InjectMocks
    private AdminPermissionsController controller;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminPermissionsService adminPermissionsService;

    @Mock
    private InterceptorConfig interceptorConfig;



    private UserRole supervisor = new UserRole();
    private UserRole admin = new UserRole();

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
        mockMvc.perform( get( "/admin/permissions" ).header( "X-Request-Id", "theId123" ) )
                .andExpect( status().isNotFound() );
    }

    @DisplayName("Get all permissions using the controller")
    @Test
    void getAllAdminPermissions() throws Exception {

        AdminPermissionsGroups adminPermissionsGroups = new AdminPermissionsGroups();

        AdminPermissionsGroup admin = new AdminPermissionsGroup();
        admin.setId("admin");
        admin.setEntraGroupId("admin");

        AdminPermissionsGroup supervisor = new AdminPermissionsGroup();
        supervisor.setId("supervisor");
        adminPermissionsGroups.add(supervisor);
        adminPermissionsGroups.add(admin);

        when(adminPermissionsService.getAdminGroup()).thenReturn(adminPermissionsGroups);

        String responseBody = mockMvc.perform( get( "/internal/admin/permissions" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        final var objectMapper = new ObjectMapper();        
        final var readValue = objectMapper.readValue(responseBody, new TypeReference<Set<AdminPermissionsGroup>>(){} );

        Assertions.assertEquals( 2, readValue.size() );
    }

    @DisplayName("Get all permissions using the controller - empty database")
    @Test
    void getAllAdminPermissionsEmptyDatabase() throws Exception {

        Mockito.doReturn(new AdminPermissionsGroups()).when(adminPermissionsService).getAdminGroup();

        String responseBody = mockMvc.perform( get( "/internal/admin/permissions" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

        final var objectMapper = new ObjectMapper();
        final var readValue = objectMapper.readValue(responseBody, new TypeReference<Set<AdminPermissionsGroup>>(){} );

        Assertions.assertEquals( 0, readValue.size() );
    }

    @DisplayName("Adding a new AdminPermissions to the database - No Permissions")
    @Test
    void addNewRoleToDatabaseNoPermissions() throws Exception {

        AdminPermissionsGroup restrictedWord = new AdminPermissionsGroup();
        restrictedWord.setId("restrictedWord");
        restrictedWord.setEntraGroupId("restrictedWord");

        when(adminPermissionsService.addAdminPermissions(any())).thenReturn(restrictedWord);

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( restrictedWord );

        mockMvc.perform( post( "/internal/admin/permissions/add" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( restrictedWordJson ) )
            .andExpect( status().isCreated());
    }

    @DisplayName("Adding a new AdminPermissions to the database - No AdminPermissions ID - Bad request thrown")
    @Test
    void addNewAdminPermissionsToDatabaseNoRoleId() throws Exception {

        AdminPermissionsGroup restrictedWord = new AdminPermissionsGroup();
        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");
        restrictedWord.setPermissions(permissions);

        when(adminPermissionsService.addAdminPermissions(any())).thenReturn(null);

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( restrictedWord );

         mockMvc.perform( post( "/internal/admin/permissions/add" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( restrictedWordJson ) )
            .andExpect( status().isBadRequest());
    }

    @DisplayName("Adding a new Admin Permissions to the database")
    @Test
    void addNewAdminPermissionsToDatabase() throws Exception {

        AdminPermissionsGroup restrictedWord = new AdminPermissionsGroup();
        restrictedWord.setId("restrictedWordId");
        restrictedWord.setEntraGroupId("restrictedWordGroup");
        restrictedWord.setGroupName("restrictedWordName");
        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");
        restrictedWord.setPermissions(permissions);

        when(adminPermissionsService.addAdminPermissions(any())).thenReturn(restrictedWord);

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( restrictedWord );

        var responseBody = mockMvc.perform( post( "/internal/admin/permissions/add" )
                 .header("X-Request-Id", "theId123")
                 .contentType( "application/json" )
                 .content( restrictedWordJson ) )
                 .andExpect( status().isCreated())
                 .andReturn()
                 .getResponse()
                 .getContentAsString();

        Assertions.assertTrue(responseBody.contains("restrictedWordId"));
        Assertions.assertTrue(responseBody.contains("restrictedWordGroup"));
        Assertions.assertTrue(responseBody.contains("restrictedWordName"));
    }

    @DisplayName("Adding a new Admin Permissions to the database  -  failed addition")
    @Test
    void addNewAdminPermissionsToDatabaseEmptyFields() throws Exception {

        AdminPermissionsGroup restrictedWord = new AdminPermissionsGroup();
        restrictedWord.setId("restrictedWordId");
        restrictedWord.setEntraGroupId("restrictedWordGroup");
        restrictedWord.setGroupName("restrictedWordName");
        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");
        restrictedWord.setPermissions(permissions);

        when(adminPermissionsService.addAdminPermissions(any())).thenReturn(null);

        final var objectMapper = new ObjectMapper();
        final var restrictedWordJson = objectMapper.writeValueAsString( restrictedWord );

        mockMvc.perform( post( "/internal/admin/permissions/add" )
                        .header("X-Request-Id", "theId123")
                        .contentType( "application/json" )
                        .content( restrictedWordJson ) )
                .andExpect( status().isBadRequest());
    }


    @DisplayName("Deleting a Admin Permissions from the database - malformed request")
    @Test
    void deletingAdminPermissionFromTheDatabaseMalformedRequest() throws Exception {

        when(adminPermissionsService.deleteAdminPermissions(any())).thenReturn(true);

         mockMvc.perform( delete( "/internal/admin/permissions/^/delete" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isBadRequest());
    }

    @DisplayName("Deleting a Admin Permissions from the database - failed deletion")
    @Test
    void deletingAnAdminPermissionFromTheDatabaseFailedDeletion() throws Exception {

        when(adminPermissionsService.deleteAdminPermissions(any())).thenReturn(false);

         mockMvc.perform( delete( "/internal/admin/permissions/supervisor/delete" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @DisplayName("Deleting a Admin Permissions from the database")
    @Test
    void deletingAnAdminPermissionFromTheDatabase() throws Exception {

        when(adminPermissionsService.deleteAdminPermissions(any())).thenReturn(true);

         mockMvc.perform( delete( "/internal/admin/permissions/supervisor/delete" )
            .header("X-Request-Id", "theId123") )
            .andExpect(status().isNoContent())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @DisplayName("Modifying the permissions list - failed update")
    @Test
    void modifyThePermissionsFailedUpdate() throws Exception {

        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");

        final var objectMapper = new ObjectMapper();
        final var permissionsJson = objectMapper.writeValueAsString( permissions );

        when(adminPermissionsService.editAdminPermissions(any(),any())).thenReturn(false);

         mockMvc.perform( put( "/internal/admin/permissions/admin/edit" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( permissionsJson ) )
            .andExpect( status().isBadRequest());
    }

    @DisplayName("Modifying the permissions list  - permissions not provided")
    @Test
    void modifyThePermissionsNoPermissions() throws Exception {

        PermissionsList permissions = new PermissionsList();

        final var objectMapper = new ObjectMapper();
        final var permissionJson = objectMapper.writeValueAsString( permissions );

        when(adminPermissionsService.editAdminPermissions(any(),any())).thenReturn(true);

         mockMvc.perform( put( "/internal/admin/permissions/admin/edit" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content(permissionJson ) )
            .andExpect( status().isNoContent());
    }

    @DisplayName("Modifying the permissions List")
    @Test
    void modifyThePermissions() throws Exception {

        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");

        final var objectMapper = new ObjectMapper();
        final var permissionsJson = objectMapper.writeValueAsString( permissions );

        when(adminPermissionsService.editAdminPermissions(any(),any())).thenReturn(true);

         mockMvc.perform( put( "/internal/admin/permissions/admin/edit" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content( permissionsJson ) )
            .andExpect( status().isNoContent());
    }

    @DisplayName("Modifying the permissions list  - malformed request")
    @Test
    void modifyThePermissionsForARoleMalformedREquest() throws Exception {

        PermissionsList permissions = new PermissionsList();
        permissions.add("permission99");

        final var objectMapper = new ObjectMapper();
        final var permissionsJson = objectMapper.writeValueAsString( permissions );

        when(adminPermissionsService.editAdminPermissions(any(),any())).thenReturn(false);

         mockMvc.perform( put( "/internal/admin/permissions/^/edit" )
            .header("X-Request-Id", "theId123")
            .contentType( "application/json" )
            .content(permissionsJson) )
            .andExpect(status().isBadRequest());
    }
}