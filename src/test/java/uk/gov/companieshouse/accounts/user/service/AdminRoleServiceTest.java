package uk.gov.companieshouse.accounts.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import uk.gov.companieshouse.accounts.user.models.AdminRole;
import uk.gov.companieshouse.accounts.user.repositories.AdminRoleRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit-test")
public class AdminRoleServiceTest {

    @Mock
    AdminRoleRepository adminRoleRepository;

    @InjectMocks
    AdminRoleService adminRoleService;

    private AdminRole bulkRefunds;
    private AdminRole badosUser;


    @BeforeEach
    void setup(){
            bulkRefunds = new AdminRole();
            bulkRefunds.setId("bulkRefunds");
            bulkRefunds.setPermissions(List.of("/admin/payments-bulk-refunds"));
            
            badosUser = new AdminRole();
            badosUser.setId("badosUser");
            badosUser.setPermissions(List.of(
                "admin/officer-search/scottish-bankrupt-officer", 
                "/admin/transaction-search"));
    }
    
    @Test
    void duplicateKeyExceptionTrown(){
        when(adminRoleService.addAdminRole(badosUser)).thenThrow(DuplicateKeyException.class);
        Assertions.assertThrows( DuplicateKeyException.class, () -> adminRoleService.addAdminRole( badosUser ) );
    }

    @Test
    void findAllRoles(){
        when(adminRoleRepository.findAll()).thenReturn(List.of(badosUser,bulkRefunds));        
        List<AdminRole> allRoles  = adminRoleService.fetchAdminRoles();
        assertEquals(2,allRoles.size()); 
    }

    @Test
    void findRole(){
        when(adminRoleRepository.findById("badosUser")).thenReturn(Optional.of(badosUser));
        Optional<AdminRole> returnedSupervisor  = adminRoleService.fetchAdminRole("badosUser");
        assertTrue(returnedSupervisor.isPresent());
        assertTrue(new ReflectionEquals(badosUser).matches(returnedSupervisor.get()));    
    }    

    @Test
    void updatePermissionsSuccess(){
        when(adminRoleRepository.updateRole(anyString(), any())).thenReturn(1);       
        assertEquals(1, adminRoleService.updateRole("bulkRefunds", List.of("blank")));
    }

    @Test
    void updatePermissionsFailure(){
        when(adminRoleRepository.updateRole(anyString(), any())).thenReturn(0);       
        assertEquals(0, adminRoleService.updateRole("bulkRefunds", List.of("blank")));
    }

    @Test
    void addNewRole(){
        AdminRole fes = new AdminRole();
        fes.setId("fes");
        fes.setPermissions(List.of("/admin/fes"));

        when(adminRoleRepository.insert(fes)).thenReturn(fes);       
              
        assertEquals(fes, adminRoleService.addAdminRole(fes)); 
    }

    @Test
    void addDuplaicateRole(){
        AdminRole fes = new AdminRole();
        fes.setId("fes");
        fes.setPermissions(List.of("/admin/fes"));
        when(adminRoleRepository.insert(fes)).thenThrow(DuplicateKeyException.class);  
        assertThrows(DuplicateKeyException.class, () -> adminRoleService.addAdminRole(fes)); 
    }

    
}