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

import uk.gov.companieshouse.accounts.user.models.UserRoles;
import uk.gov.companieshouse.accounts.user.repositories.UserRolesRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit-test")
public class RolesServiceTest {

    @Mock
    UserRolesRepository userRolesRepository;
    RolesService rolesService;

    private UserRoles admin = new UserRoles();
    private UserRoles supervisor = new UserRoles();
    private List<UserRoles> userRoles = new ArrayList<>();
    @BeforeEach
    void setup(){
        admin.setId("01");
        admin.setPermissions(List.of("permission1","permission2"));
        userRoles.add(admin);

        supervisor.setId("02");
        supervisor.setPermissions(List.of("permission3","permission4"));        
        userRoles.add(supervisor);

        rolesService = new RolesService(userRolesRepository);
    }

    @Test
    @DisplayName("Test all roles are returned")
    void getAllRoles(){
        when(userRolesRepository.findAll()).thenReturn(userRoles);
        RolesService rolesService = new RolesService(userRolesRepository);
        List<UserRoles> roles =rolesService.getRoles();
        assertEquals(2, roles.size());
    }

    @Test
    @DisplayName("Add a new role")
    void addRole(){
        when(userRolesRepository.existsById(admin.getId())).thenReturn(false);
        rolesService.addRole(admin);
        verify(userRolesRepository).insert(admin);
    }

    @Test
    @DisplayName("Trying to add an existing role")
    void addRoleThatExistsAlready(){
        when(userRolesRepository.existsById(admin.getId())).thenReturn(true);
        rolesService.addRole(admin);
        verify(userRolesRepository,times(0)).insert(admin);
    }

    @Test
    @DisplayName("Trying to add an existing role")
    void editRole(){
        admin.setPermissions(List.of("permission5","permission6"));
        rolesService.editRole(admin);
        verify(userRolesRepository).updateRole(any(),any());
    }    

    @Test
    @DisplayName("Add a new role")
    void deleteAnExistingRole(){
        when(userRolesRepository.existsById(admin.getId())).thenReturn(true);
        rolesService.deleteRole(admin);
        verify(userRolesRepository).deleteById(admin.getId());
    }

    @Test
    @DisplayName("Trying to add an existing role")
    void deleteRoleThatDoesntExist(){
        when(userRolesRepository.existsById(admin.getId())).thenReturn(false);
        rolesService.addRole(admin);
        verify(userRolesRepository,times(0)).deleteById(admin.getId());
    }    
}
