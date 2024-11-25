package uk.gov.companieshouse.accounts.user.integration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.companieshouse.accounts.user.mapper.RolesDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.accounts.user.service.RolesService;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.Roles;

@SpringBootTest
@Testcontainers(parallel = true)
@Tag("integration-test")
public class UserRolesServiceTest {

    @Container
    @ServiceConnection
    static MongoDBContainer container = new MongoDBContainer("mongo:6");

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    RolesDtoDaoMapper rolesDtoDaoMapper;

    @Autowired
    RolesRepository userRolesRepository;

    @Autowired
    RolesService rolesService;

    private UserRole admin = new UserRole();
    private UserRole supervisor = new UserRole();
    @BeforeEach
    void setup(){
        admin.setId("admin");
        admin.setPermissions(List.of("permission1","permission2"));

        supervisor.setId("supervisor");
        supervisor.setPermissions(List.of("permission3","permission4"));        

        rolesService = new RolesService(userRolesRepository, rolesDtoDaoMapper);

        userRolesRepository.insert(List.of( admin,supervisor));
    }

    @Test
    @DisplayName("Test all roles are returned")
    void getAllRoles(){
        Roles roles = rolesService.getRoles();
        assertEquals(2, roles.size());
    }

    @Test
    @DisplayName("Add a new role")
    void addNewRole(){
        Role fes = new Role();
        PermissionsList fesPermissions =  new PermissionsList();
        fes.setId("fes");
        fes.setPermissions(fesPermissions);
        rolesService.addRole(fes);
        assertEquals(3, rolesService.getRoles().size());
    }

    @Test
    @DisplayName("Trying to add an existing role")
    void addNewRoleThatExistsAlready(){
        Role admin = new Role();
        admin.setId("admin");
        PermissionsList adminPermissions =  new PermissionsList();
        adminPermissions.add("permission99");
        admin.setPermissions(adminPermissions);
        rolesService.addRole(admin);
        assertEquals(2, rolesService.getRoles().size());

        Role role = rolesService.getRoles().getFirst();
        assertEquals("admin", role.getId());
        assertTrue(rolesService.getRoles().getFirst().getPermissions().containsAll(List.of("permission1","permission2")));
    }

    @Test
    @DisplayName("Editing an existing role")
    void editingRole(){
        PermissionsList adminPermissions =  new PermissionsList();
        adminPermissions.add("permission99");
        rolesService.editRole("admin", adminPermissions);

        Role role = rolesService.getRoles().getFirst();
        assertEquals("admin", role.getId());
        assertTrue(rolesService.getRoles().getFirst().getPermissions().containsAll(List.of("permission99")));
    }    
 
    @Test
    @DisplayName("Deleting a role")
    void deleteRole(){
        rolesService.deleteRole("admin");
        Roles roles = rolesService.getRoles();
        assertFalse(roles.contains(admin));
    }
    
    @AfterEach
    public void after() {
        mongoTemplate.dropCollection(UserRole.class);
    }

}
