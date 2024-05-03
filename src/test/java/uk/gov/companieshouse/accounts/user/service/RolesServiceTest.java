package uk.gov.companieshouse.accounts.user.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.accounts.user.mapper.RolesDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.Roles;

@ExtendWith(MockitoExtension.class)
@Tag("unit-test")
public class RolesServiceTest {

    @Mock
    RolesRepository userRolesRepository;

    @Mock
    RolesDtoDaoMapper rolesDtoDaoMapper;

    RolesService rolesService;

    private UserRole admin = new UserRole();
    private UserRole supervisor = new UserRole();
    private List<UserRole> userRoles = new ArrayList<>();
    @BeforeEach
    void setup(){
        admin.setId("admin");
        admin.setPermissions(List.of("permission1","permission2"));
        userRoles.add(admin);

        supervisor.setId("supervisor");
        supervisor.setPermissions(List.of("permission3","permission4"));        
        userRoles.add(supervisor);

        rolesService = new RolesService(userRolesRepository, rolesDtoDaoMapper);
    }

    @Test
    @DisplayName("Test all roles are returned")
    void getAllRoles(){
        when(userRolesRepository.findAll()).thenReturn(userRoles);

        Role adminRole = new Role();
        adminRole.setId("admin");
        PermissionsList adminPermissions =  new PermissionsList();
        adminPermissions.add("permission1");
        adminPermissions.add("permission2");
        adminRole.setPermissions(adminPermissions);        

        Role supervisorRole = new Role();
        supervisorRole.setId("supervisor");        
        PermissionsList supervisorPermissions =  new PermissionsList();
        supervisorPermissions.add("permission1");
        supervisorPermissions.add("permission2");
        supervisorRole.setPermissions(supervisorPermissions);

        when(rolesDtoDaoMapper.daoToDto(admin)).thenReturn(adminRole);
        when(rolesDtoDaoMapper.daoToDto(supervisor)).thenReturn(supervisorRole);

        RolesService rolesService = new RolesService(userRolesRepository, rolesDtoDaoMapper);

        Roles roles = rolesService.getRoles();

        assertEquals(2, roles.size());
    }

    @Test
    @DisplayName("Add a new role")
    void addRole(){
        Role adminRole = new Role();
        adminRole.setId("admin");
        PermissionsList adminPermissions =  new PermissionsList();
        adminPermissions.add("permission99");
        adminRole.setPermissions(adminPermissions);

        when(rolesDtoDaoMapper.dtoToDao(adminRole)).thenReturn(admin);
        when(userRolesRepository.existsById(admin.getId())).thenReturn(false);

        rolesService.addRole(adminRole);
        verify(userRolesRepository).insert(admin);
    }

    @Test
    @DisplayName("Trying to add an existing role")
    void addRoleThatExistsAlready(){
        Role adminRole = new Role();
        adminRole.setId("admin");
        PermissionsList adminPermissions =  new PermissionsList();
        adminPermissions.add("permission99");
        adminRole.setPermissions(adminPermissions);

        when(userRolesRepository.existsById(admin.getId())).thenReturn(true);
        when(rolesDtoDaoMapper.dtoToDao(adminRole)).thenReturn(admin);

        rolesService.addRole(adminRole);
        verify(userRolesRepository,times(0)).insert(admin);
    }

    @Test
    @DisplayName("Editing the permissions for a role that doesn't exist")
    void edittingARoleThatDoesntExist(){
        admin.setPermissions(List.of("permission5","permission6"));
        PermissionsList permissions =  new PermissionsList();
        permissions.add("permission88");
        rolesService.editRole("blank",permissions );
        verify(userRolesRepository, times(0)).updateRole(any(),any());
    } 

    @Test
    @DisplayName("Editing the permissions for a role")
    void editRole(){
        admin.setPermissions(List.of("permission5","permission6"));
        PermissionsList permissions =  new PermissionsList();
        permissions.add("permission88");
        
        when(userRolesRepository.existsById(any())).thenReturn(true);
        
        rolesService.editRole(admin.getId(),permissions );

        verify(userRolesRepository).updateRole(any(),any());
    }    

    @Test
    @DisplayName("Deleting a role")
    void deleteAnExistingRole(){
        when(userRolesRepository.existsById(admin.getId())).thenReturn(true);
        rolesService.deleteRole(admin.getId());
        verify(userRolesRepository).deleteById(admin.getId());
    }

    @Test
    @DisplayName("Trying to delete a non-existant role")
    void deleteRoleThatDoesntExist(){
        when(userRolesRepository.existsById(admin.getId())).thenReturn(false);
        rolesService.deleteRole(admin.getId());
        verify(userRolesRepository,times(0)).deleteById(admin.getId());
    }    
}
