package uk.gov.companieshouse.accounts.user.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.companieshouse.accounts.user.models.AdminRole;
import uk.gov.companieshouse.accounts.user.repositories.AdminRoleRepository;

@Transactional
@Service
public class AdminRoleService {

    private final AdminRoleRepository adminRoleRepository;

    public AdminRoleService(final AdminRoleRepository adminRoleRepository) {
        this.adminRoleRepository = adminRoleRepository;
    }

    public List<AdminRole> fetchAdminRoles() {
        return adminRoleRepository.findAll();
    }

    public Optional<AdminRole> fetchAdminRole(final String adminRole) {
        return adminRoleRepository.findById(adminRole);
    }

    public int updateRole(final String roleId, final List<String> permissions){
        final var permissionsSet = new HashSet<>( permissions );
        final var update = new Update().set( "permissions", permissionsSet );
        return adminRoleRepository.updateRole( roleId, update );
    }

    public AdminRole addAdminRole(final AdminRole adminRole) throws DuplicateKeyException{
        return adminRoleRepository.insert(adminRole);
    }

    public void deleteAdminRole(final String adminRoleId){
        adminRoleRepository.deleteById(adminRoleId);
    }
}
