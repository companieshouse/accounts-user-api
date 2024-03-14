package uk.gov.companieshouse.accounts.user.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.companieshouse.accounts.user.models.AdminRole;

@SpringBootTest
@Testcontainers(parallel = true)
@Tag("integration-test")
public class AdminRolesRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer container = new MongoDBContainer("mongo:5");

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    AdminRoleRepository adminRoleRepository;
    AdminRole supervisor;
    
    @BeforeEach
    void setup(){

            AdminRole chsOrdersInvestigator = new AdminRole();
            chsOrdersInvestigator.setId("chsOrdersInvestigator");
            chsOrdersInvestigator.setPermissions(List.of("/admin/chs-orders-investigation"));

            AdminRole downloadExtensionsDocuments = new AdminRole();
            downloadExtensionsDocuments.setId("downloadExtensionsDocuments");
            downloadExtensionsDocuments.setPermissions(List.of("/admin/extensions-download"));

            AdminRole  viewExtensionsResources = new AdminRole();
            viewExtensionsResources.setId("viewExtensionsResources");
            viewExtensionsResources.setPermissions(List.of("/admin/extensions-view"));

            AdminRole bulkRefunds = new AdminRole();
            bulkRefunds.setId("bulkRefunds");
            bulkRefunds.setPermissions(List.of("/admin/payments-bulk-refunds"));
            
            AdminRole badosUser = new AdminRole();
            badosUser.setId("badosUser");
            badosUser.setPermissions(List.of("/admin/officer-search/scottish-bankrupt-officer", "/admin/transaction-search"));

            AdminRole restrictedWord = new AdminRole();
            restrictedWord.setId("restrictedWord");
            restrictedWord.setPermissions(List.of("/admin/restricted-word"));

            supervisor = new AdminRole();
            supervisor.setId("supervisor");
            supervisor.setPermissions(List.of("/admin/view_json",
            "/admin/user/search",
            "/admin/monitor",
            "/admin/roles",
            "/admin/transaction-reprocess",
            "/admin/user/filings",
            "/admin/filing/resend",
            "/admin/search",
            "/admin/images",
            "/admin/user/roles",
            "/admin/filing/resubmit",
            "/admin/queues",
            "/admin/company",
            "/admin/transaction-lookup",
            "/admin/penalty-lookup"));
    
       adminRoleRepository.insert( List.of( 
            chsOrdersInvestigator,
            downloadExtensionsDocuments,
            viewExtensionsResources,
            bulkRefunds,
            badosUser,
            restrictedWord,
            supervisor) );
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( AdminRole.class );
    }

    @Test
    void findAllRoles(){
        List<AdminRole> allRoles  = adminRoleRepository.findAll();
        assertEquals(7,allRoles.size()); 
    }

    @Test
    void findRole(){
        Optional<AdminRole> returnedSupervisor  = adminRoleRepository.findById("supervisor");
        assertTrue(returnedSupervisor.isPresent());
        assertTrue(new ReflectionEquals(supervisor).matches(returnedSupervisor.get()));    
    }    

    @Test
    void updatePermissionsFailure(){
        final var update = new Update().set( "permissions", List.of("blank") );
        assertEquals(0, adminRoleRepository.updateRole("doesntExist", update));
    }    

    @Test
    void updatePermissions(){
        final var update = new Update().set( "permissions", List.of("blank") );
        assertEquals(1, adminRoleRepository.updateRole("bulkRefunds", update));
        Optional<AdminRole> returnedSupervisor  = adminRoleRepository.findById("bulkRefunds");
        assertTrue(returnedSupervisor.isPresent());
        assertTrue(returnedSupervisor.get().getPermissions().contains("blank"));
        assertEquals(1, returnedSupervisor.get().getPermissions().size());
    }

    @Test
    void addNewRole(){
        AdminRole fes = new AdminRole();
        fes.setId("fes");
        fes.setPermissions(List.of("/admin/fes"));
        adminRoleRepository.insert(fes);
        List<AdminRole> allRoles  = adminRoleRepository.findAll();
        assertEquals(8,allRoles.size()); 
    }

    @Test
    void addDuplaicateRole(){
        AdminRole fes = new AdminRole();
        fes.setId("fes");
        fes.setPermissions(List.of("/admin/fes"));
        adminRoleRepository.insert(fes);
        assertThrows( DuplicateKeyException.class, () -> adminRoleRepository.insert(fes) );
    }
}
