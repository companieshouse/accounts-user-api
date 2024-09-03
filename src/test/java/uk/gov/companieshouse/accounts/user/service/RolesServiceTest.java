package uk.gov.companieshouse.accounts.user.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.test.context.aot.DisabledInAotMode;

import uk.gov.companieshouse.accounts.user.mapper.RolesDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import uk.gov.companieshouse.api.accounts.user.model.Role;

@ExtendWith(MockitoExtension.class)
@DisabledInAotMode
@Tag("unit-test")
class RolesServiceTest {

    MockRolesRepository userRolesRepository = new MockRolesRepository();

    MockRolesDtoDaoMapper rolesDtoDaoMapper;

    RolesService rolesService;

    private UserRole admin = new UserRole();
    private UserRole supervisor = new UserRole();
    private List<UserRole> userRoles = new ArrayList<>();

    Role adminRole = new Role();
    Role supervisorRole = new Role();

    @BeforeEach
    void setup(){

        adminRole = new Role();
        adminRole.setId("admin");
        PermissionsList adminPermissions =  new PermissionsList();
        adminPermissions.add("permission99");
        adminRole.setPermissions(adminPermissions);

        supervisorRole = new Role();
        supervisorRole.setId("supervisor");
        PermissionsList supervisorPermissions =  new PermissionsList();
        supervisorPermissions.add("permission99");
        supervisorRole.setPermissions(adminPermissions);

        admin.setId("admin");
        admin.setPermissions(List.of("permission1","permission2"));
        userRoles.add(admin);

        supervisor.setId("supervisor");
        supervisor.setPermissions(List.of("permission3","permission4"));        
        userRoles.add(supervisor);

        rolesDtoDaoMapper = new MockRolesDtoDaoMapper(List.of(adminRole, supervisorRole),List.of(admin,supervisor));

    }

    @Test
    @DisplayName("Test all roles are returned")
    void getAllRoles(){
        rolesService = new RolesService(userRolesRepository, rolesDtoDaoMapper);
        rolesService.addRole(adminRole);
        rolesService.addRole(supervisorRole);
        assertEquals(2,rolesService.getRoles().size());
    }

    @Test
    @DisplayName("Add a new role")
    void addRole(){
        rolesService = new RolesService(userRolesRepository, rolesDtoDaoMapper);
        rolesService.addRole(adminRole);
        assertTrue(userRolesRepository.existsById(adminRole.getId()));
    }

    @Test
    @DisplayName("Trying to add an existing role")
    void addRoleThatExistsAlready(){
        Role adminRole = new Role();
        adminRole.setId("admin");
        PermissionsList adminPermissions =  new PermissionsList();
        adminPermissions.add("permission99");
        adminRole.setPermissions(adminPermissions);;

        rolesService = new RolesService(userRolesRepository, rolesDtoDaoMapper);

        assertTrue(rolesService.addRole(adminRole));
        assertTrue(userRolesRepository.existsById(adminRole.getId()));
        assertFalse(rolesService.addRole(adminRole));
    }

/**  TODO: re-instate    @Test
    @DisplayName("Editing the permissions for a role that doesn't exist")
    void editingANonExistentRole(){
        admin.setPermissions(List.of("permission5","permission6"));
        PermissionsList permissions =  new PermissionsList();
        permissions.add("permission88");

        rolesService = new RolesService(userRolesRepository, rolesDtoDaoMapper);
        rolesService.editRole("blank",permissions );
        verify(userRolesRepository, times(0)).updateRole(any(),any());
    } 

    @Test
    @DisplayName("Editing the permissions for a role")
    void editRole(){
        PermissionsList permissions =  new PermissionsList();
        permissions.add("permission88");
        
        rolesService = new RolesService(userRolesRepository, rolesDtoDaoMapper);
        rolesService.addRole(adminRole);
        Optional<Role> adminRole = rolesService.getRoles().stream().filter(t ->  t.getId() == "admin").findFirst();
        Role admin = adminRole.get();
        assertEquals("admin", admin.getId());
        PermissionsList adminPermissions = admin.getPermissions();
        assertTrue(rolesService.editRole(admin.getId(),permissions ));
        Optional<Role> updatedAdminRole = rolesService.getRoles().stream().filter(t ->  t.getId() == "admin").findFirst();
        assertNotEquals(adminPermissions, updatedAdminRole.get().getPermissions());
        rolesService.editRole(admin.getId(),permissions );
        verify(userRolesRepository).updateRole(any(),any());
    }    
**/
    @Test
    @DisplayName("Deleting a role")
    void deleteAnExistingRole(){
        Role adminRole = new Role();
        adminRole.setId("admin");
        PermissionsList adminPermissions =  new PermissionsList();
        adminPermissions.add("permission99");
        adminRole.setPermissions(adminPermissions);

        rolesService = new RolesService(userRolesRepository, rolesDtoDaoMapper);
        assertTrue(rolesService.addRole(adminRole));
        assertTrue(userRolesRepository.existsById(adminRole.getId()));        
        assertTrue(rolesService.deleteRole(admin.getId()));
        assertFalse(userRolesRepository.existsById(adminRole.getId()));
    }
/**  TODO: re-instate 
    @Test
    @DisplayName("Trying to delete a non-existant role")
    void deleteRoleThatDoesntExist(){
        when(userRolesRepository.existsById(admin.getId())).thenReturn(false);
        rolesService.deleteRole(admin.getId());
        verify(userRolesRepository,times(0)).deleteById(admin.getId());
    }    
*/
}

class MockRolesRepository implements RolesRepository{

    private Map<String, UserRole> roles = new HashMap<>();

    @Override
    public <S extends UserRole> S insert(S entity) {
            roles.put(entity.getId(), entity);
            return entity;
        }

    @Override
    public <S extends UserRole> List<S> insert(Iterable<S> entities) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insert'");
    }

  

    @Override
    public List<UserRole> findAll() {
        return new ArrayList<UserRole>(this.roles.values());
    }

    @Override
    public List<UserRole> findAllById(Iterable<String> ids) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllById'");
    }

    @Override
    public <S extends UserRole> List<S> saveAll(Iterable<S> entities) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveAll'");
    }

    @Override
    public long count() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }

    @Override
    public void delete(UserRole entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void deleteAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
    }

    @Override
    public void deleteAll(Iterable<? extends UserRole> entities) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllById'");
    }

    @Override
    public void deleteById(String id) {
        this.roles.remove(id);
    }

    @Override
    public boolean existsById(String id) {
        return this.roles.containsKey(id);
    }

    @Override
    public Optional<UserRole> findById(String id) {
        return Optional.of(this.roles.get(id));
    }

    @Override
    public <S extends UserRole> S save(S entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public <S extends UserRole> List<S> findAll(Example<S> example) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public <S extends UserRole> List<S> findAll(Example<S> example, Sort sort) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public List<UserRole> findAll(Sort sort) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public Page<UserRole> findAll(Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public <S extends UserRole> long count(Example<S> example) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }

    @Override
    public <S extends UserRole> boolean exists(Example<S> example) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exists'");
    }

    @Override
    public <S extends UserRole> Page<S> findAll(Example<S> example, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public <S extends UserRole, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findBy'");
    }

    @Override
    public <S extends UserRole> Optional<S> findOne(Example<S> example) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findOne'");
    }

    private void getList(String string, Class<HashMap> class1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getList'");
    }

    @Override
    public int updateRole(String roleId, Update update) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateRole'");
    }
}

class MockRolesDtoDaoMapper implements RolesDtoDaoMapper{

    Map<String, Role> rolesMap = new HashMap<>();
    Map<String, UserRole> userRolesMap = new HashMap<>();

    public MockRolesDtoDaoMapper(List <Role> roles, List<UserRole> userRoles) {
        userRoles.forEach(userRole -> {
            userRolesMap.put(userRole.getId(), userRole);
        });

        roles.forEach(role -> {
            rolesMap.put(role.getId(), role);
        });        
    }

    @Override
    public Role daoToDto(UserRole userRole) {
        return this.rolesMap.get(userRole.getId());
    }

    @Override
    public UserRole dtoToDao(Role role) {
        return this.userRolesMap.get(role.getId());
    }
    
}