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

import uk.gov.companieshouse.accounts.user.models.UserRoles;
import uk.gov.companieshouse.accounts.user.repositories.UserRolesRepository;
import uk.gov.companieshouse.accounts.user.service.RolesService;

@SpringBootTest
@Testcontainers(parallel = true)
@Tag("integration-test")
public class UserRolesServiceTest {

    @Container
    @ServiceConnection
    static MongoDBContainer container = new MongoDBContainer("mongo:5");

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    UserRolesRepository userRolesRepository;

    @Autowired
    RolesService rolesService;

    private UserRoles admin = new UserRoles();
    private UserRoles supervisor = new UserRoles();
    @BeforeEach
    void setup(){
        admin.setId("admin");
        admin.setPermissions(List.of("permission1","permission2"));

        supervisor.setId("supervisor");
        supervisor.setPermissions(List.of("permission3","permission4"));        

        rolesService = new RolesService(userRolesRepository);

        userRolesRepository.insert(List.of( admin,supervisor));
    }

    @Test
    @DisplayName("Test all roles are returned")
    void getAllRoles(){
        RolesService rolesService = new RolesService(userRolesRepository);
        List<UserRoles> roles =rolesService.getRoles();
        assertEquals(2, roles.size());
    }

    @Test
    @DisplayName("Add a new role")
    void addNewRole(){
        UserRoles fes = new UserRoles();       
        fes.setId("fes");
        fes.setPermissions(List.of("permission1","permission2"));
        rolesService.addRole(fes);
        assertEquals(3, rolesService.getRoles().size());
    }

    @Test
    @DisplayName("Trying to add an existing role")
    void addNewRoleThatExistsAlready(){
        admin.setPermissions(List.of("permission1","permission4"));
        rolesService.addRole(admin);
        assertEquals(2, rolesService.getRoles().size());

        UserRoles role = rolesService.getRoles().get(0);
        assertEquals("admin", role.getId());
        assertTrue(rolesService.getRoles().get(0).getPermissions().containsAll(List.of("permission1","permission2")));
    }

    @Test
    @DisplayName("Editing an existing role")
    void editingRole(){
        admin.setPermissions(List.of("permission5","permission6"));
        rolesService.editRole(admin);

        UserRoles role = rolesService.getRoles().get(0);
        assertEquals("admin", role.getId());
        assertTrue(rolesService.getRoles().get(0).getPermissions().containsAll(List.of("permission5","permission6")));
    }    

    @Test
    @DisplayName("Deleting a role")
    void deleteRole(){
        rolesService.deleteRole(admin);
        List<UserRoles> roles = rolesService.getRoles();
        assertFalse(roles.contains(admin));
    }
    
    @AfterEach
    public void after() {
        mongoTemplate.dropCollection(UserRoles.class);
    }

}
