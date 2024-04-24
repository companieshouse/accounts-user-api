package uk.gov.companieshouse.accounts.user.service;

import static org.junit.Assert.assertEquals;
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

    private UserRoles admin = new UserRoles();
    private UserRoles supervisor = new UserRoles();
    private List<UserRoles> userRoles = new ArrayList<>();
    @BeforeEach
    void setup(){
        admin.setId("01");
        admin.setPermissions(List.of("permission1","permission2"));
        userRoles.add(admin);

        supervisor.setId("01");
        supervisor.setPermissions(List.of("permission3","permission4"));        
        userRoles.add(supervisor);
        
        
    }

    @Test
    @DisplayName("Test all roles are returned")
    void getAllRoles(){
        when(userRolesRepository.findAll()).thenReturn(userRoles);
        RolesService rolesService = new RolesService(userRolesRepository);
        List<UserRoles> h =rolesService.getRoles();

        assertEquals(2, h.size());
    }
}
