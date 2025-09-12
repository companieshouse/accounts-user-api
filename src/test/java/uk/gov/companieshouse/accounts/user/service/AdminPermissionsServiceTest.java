package uk.gov.companieshouse.accounts.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.accounts.user.mapper.AdminPermissionsDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.AdminPermissions;
import uk.gov.companieshouse.accounts.user.repositories.AdminPermissionsRepository;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroup;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroups;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit-test")
public class AdminPermissionsServiceTest {

    @Mock
    AdminPermissionsRepository adminPermissionsRepository;

    @Mock
    AdminPermissionsDtoDaoMapper adminPermissionsDtoDaoMapper;

    @InjectMocks
    AdminPermissionsService adminPermissionsService;

    private AdminPermissions admin = new AdminPermissions();
    private AdminPermissions supervisor = new AdminPermissions();
    private List<AdminPermissions> adminPermissionsList;

    @BeforeEach
    void setup(){
        admin.setId("admin");
        admin.setEntraGroupId("adminEntraId");
        admin.setGroupName("adminGroupName");
        admin.setPermissions(List.of("permission1","permission2"));

        supervisor.setId("supervisor");
        supervisor.setEntraGroupId("supervisorEntraId");
        supervisor.setGroupName("supervisorGroupName");
        supervisor.setPermissions(List.of("permission3","permission4"));
        adminPermissionsList = List.of(admin, supervisor);


    }

    @Test
    @DisplayName("Test all Admin permissions are returned")
    void getAllAdminPermissions(){

        when(adminPermissionsRepository.findAll()).thenReturn(adminPermissionsList);

        AdminPermissionsGroup adminPermissionsGroup = new AdminPermissionsGroup();
        adminPermissionsGroup.setId("admin");
        PermissionsList adminPermissions =  new PermissionsList();
        adminPermissions.add("permission1");
        adminPermissions.add("permission2");
        adminPermissionsGroup.setPermissions(adminPermissions);

        AdminPermissionsGroup supervisorRole = new AdminPermissionsGroup();
        supervisorRole.setId("supervisor");
        PermissionsList supervisorPermissions =  new PermissionsList();
        supervisorPermissions.add("permission1");
        supervisorPermissions.add("permission2");
        supervisorRole.setPermissions(supervisorPermissions);

        when(adminPermissionsDtoDaoMapper.daoToDto(admin)).thenReturn(adminPermissionsGroup);
        when(adminPermissionsDtoDaoMapper.daoToDto(supervisor)).thenReturn(supervisorRole);


        AdminPermissionsGroups adminPermissionsGroups = adminPermissionsService.getAdminGroup();

        assertEquals(2, adminPermissionsGroups.size());
    }

    @Test
    @DisplayName("Add a new Admin Permissions")
    void addAdminPermissions(){
        AdminPermissionsGroup adminPermissionsGroup = new AdminPermissionsGroup();
        adminPermissionsGroup.setId("admin");
        adminPermissionsGroup.setEntraGroupId("adminEntraId");
        PermissionsList permissionsList =  new PermissionsList();
        permissionsList.add("permission99");
        adminPermissionsGroup.setEntraGroupId("permission99EntraId");

        adminPermissionsGroup.setPermissions(permissionsList);

        when(adminPermissionsDtoDaoMapper.dtoToDao(adminPermissionsGroup)).thenReturn(admin);
        when(adminPermissionsRepository.findByEntraGroupId(admin.getEntraGroupId())).thenReturn(null);
        when(adminPermissionsRepository.insert((AdminPermissions) any())).thenReturn(admin);

        adminPermissionsService.addAdminPermissions(adminPermissionsGroup);
        verify(adminPermissionsRepository).insert(admin);
    }

    @Test
    @DisplayName("Trying to add an existing Admin Permissions")
    void addAdminPermissionsThatExistsAlready(){
        AdminPermissionsGroup adminPermissionsGroup = new AdminPermissionsGroup();
        adminPermissionsGroup.setId("admin");
        adminPermissionsGroup.setEntraGroupId("adminEntraId");
        PermissionsList permissionsList =  new PermissionsList();
        permissionsList.add("permission99");
        adminPermissionsGroup.setEntraGroupId("permission99EntraId");

        adminPermissionsGroup.setPermissions(permissionsList);

        when(adminPermissionsDtoDaoMapper.dtoToDao(adminPermissionsGroup)).thenReturn(admin);
        when(adminPermissionsRepository.findByEntraGroupId(admin.getEntraGroupId())).thenReturn(admin);

        var response = adminPermissionsService.addAdminPermissions(adminPermissionsGroup);
        assertNull(response);
        verify(adminPermissionsRepository,times(0)).insert(admin);
    }

    @Test
    @DisplayName("Editing the permissions for a Admin Permissions that doesn't exist")
    void editingAdminPermissionsThatDoesntExist(){
        admin.setPermissions(List.of("permission5","permission6"));
        PermissionsList permissions =  new PermissionsList();
        permissions.add("permission88");
        adminPermissionsService.editAdminPermissions("blank",permissions );
        verify(adminPermissionsRepository, times(0)).updateRole(any(),any());
    }

    @Test
    @DisplayName("Editing the permissions for a Admin Permissions")
    void editAdminPermissions(){
        admin.setPermissions(List.of("permission5","permission6"));
        PermissionsList permissions =  new PermissionsList();
        permissions.add("permission88");

        when(adminPermissionsRepository.existsById(any())).thenReturn(true);

        adminPermissionsService.editAdminPermissions(admin.getId(),permissions );

        verify(adminPermissionsRepository).updateRole(any(),any());
    }

    @Test
    @DisplayName("Deleting a Admin Permissions")
    void deleteAnExistingAdminPermissions(){
        when(adminPermissionsRepository.existsById(admin.getId())).thenReturn(true);
        adminPermissionsService.deleteAdminPermissions(admin.getId());
        verify(adminPermissionsRepository).deleteById(admin.getId());
    }

    @Test
    @DisplayName("Trying to delete a non-existant Admin Permissions")
    void deleteRoleThatDoesntExist(){
        when(adminPermissionsRepository.existsById(admin.getId())).thenReturn(false);
        adminPermissionsService.deleteAdminPermissions(admin.getId());
        verify(adminPermissionsRepository,times(0)).deleteById(admin.getId());
    }
}
