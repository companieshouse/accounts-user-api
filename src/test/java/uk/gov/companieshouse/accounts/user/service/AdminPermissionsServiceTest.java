package uk.gov.companieshouse.accounts.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.accounts.user.mapper.AdminPermissionsDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.AdminPermissions;
import uk.gov.companieshouse.accounts.user.repositories.AdminPermissionsRepository;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroup;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroups;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit-test")
public class AdminPermissionsServiceTest {

    @Mock
    AdminPermissionsRepository adminPermissionsRepository;

    @Mock
    AdminPermissionsDtoDaoMapper adminPermissionsDtoDaoMapper;

    AdminPermissionsService adminPermissionsService;

    private AdminPermissions admin = new AdminPermissions();
    private AdminPermissions supervisor = new AdminPermissions();
    private List<AdminPermissions> adminPermissions = List.of(new AdminPermissions());

    @BeforeEach
    void setup(){
        admin.setId("admin");
        admin.setPermissions(List.of("permission1","permission2"));
        adminPermissions.add(admin);

        supervisor.setId("supervisor");
        supervisor.setPermissions(List.of("permission3","permission4"));        
        adminPermissions.add(supervisor);

        adminPermissionsService = new AdminPermissionsService(adminPermissionsRepository, adminPermissionsDtoDaoMapper);
    }

//    @Test
//    @DisplayName("Test all roles are returned")
//    void getAllRoles(){
//        when(adminPermissionsRepository.findAll()).thenReturn(adminPermissions);
//
//        AdminPermissionsGroup adminRole = new AdminPermissionsGroup();
//        adminRole.setId("admin");
//        PermissionsList adminPermissions =  new PermissionsList();
//        adminPermissions.add("permission1");
//        adminPermissions.add("permission2");
//        adminRole.setPermissions(adminPermissions);
//
//        AdminPermissionsGroup supervisorRole = new AdminPermissionsGroup();
//        supervisorRole.setId("supervisor");
//        PermissionsList supervisorPermissions =  new PermissionsList();
//        supervisorPermissions.add("permission1");
//        supervisorPermissions.add("permission2");
//        supervisorRole.setPermissions(supervisorPermissions);
//
//        when(adminPermissionsDtoDaoMapper.daoToDto(admin)).thenReturn(adminRole);
//        when(adminPermissionsDtoDaoMapper.daoToDto(supervisor)).thenReturn(supervisorRole);
//
//
//        AdminPermissionsGroups adminPermissionsGroups = adminPermissionsService.getAdminGroup();
//
//        assertEquals(2, adminPermissionsGroups.size());
//    }

//    @Test
//    @DisplayName("Add a new role")
//    void addRole(){
//        Role adminRole = new Role();
//        adminRole.setId("admin");
//        PermissionsList adminPermissions =  new PermissionsList();
//        adminPermissions.add("permission99");
//        adminRole.setPermissions(adminPermissions);
//
//        when(adminPermissionsDtoDaoMapper.dtoToDao(adminRole)).thenReturn(admin);
//        when(userRolesRepository.existsById(admin.getId())).thenReturn(false);
//
//        adminPermissionsService.addRole(adminRole);
//        verify(userRolesRepository).insert(admin);
//    }
//
//    @Test
//    @DisplayName("Trying to add an existing role")
//    void addRoleThatExistsAlready(){
//        Role adminRole = new Role();
//        adminRole.setId("admin");
//        PermissionsList adminPermissions =  new PermissionsList();
//        adminPermissions.add("permission99");
//        adminRole.setPermissions(adminPermissions);
//
//        when(userRolesRepository.existsById(admin.getId())).thenReturn(true);
//        when(adminPermissionsDtoDaoMapper.dtoToDao(adminRole)).thenReturn(admin);
//
//        adminPermissionsService.addRole(adminRole);
//        verify(userRolesRepository,times(0)).insert(admin);
//    }
//
//    @Test
//    @DisplayName("Editing the permissions for a role that doesn't exist")
//    void edittingARoleThatDoesntExist(){
//        admin.setPermissions(List.of("permission5","permission6"));
//        PermissionsList permissions =  new PermissionsList();
//        permissions.add("permission88");
//        adminPermissionsService.editRole("blank",permissions );
//        verify(userRolesRepository, times(0)).updateRole(any(),any());
//    }
//
//    @Test
//    @DisplayName("Editing the permissions for a role")
//    void editRole(){
//        admin.setPermissions(List.of("permission5","permission6"));
//        PermissionsList permissions =  new PermissionsList();
//        permissions.add("permission88");
//
//        when(userRolesRepository.existsById(any())).thenReturn(true);
//
//        adminPermissionsService.editRole(admin.getId(),permissions );
//
//        verify(userRolesRepository).updateRole(any(),any());
//    }
//
//    @Test
//    @DisplayName("Deleting a role")
//    void deleteAnExistingRole(){
//        when(userRolesRepository.existsById(admin.getId())).thenReturn(true);
//        adminPermissionsService.deleteRole(admin.getId());
//        verify(userRolesRepository).deleteById(admin.getId());
//    }
//
//    @Test
//    @DisplayName("Trying to delete a non-existant role")
//    void deleteRoleThatDoesntExist(){
//        when(userRolesRepository.existsById(admin.getId())).thenReturn(false);
//        adminPermissionsService.deleteRole(admin.getId());
//        verify(userRolesRepository,times(0)).deleteById(admin.getId());
//    }
}
