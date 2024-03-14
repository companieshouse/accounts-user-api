package uk.gov.companieshouse.accounts.user.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.accounts.user.models.AdminRole;

@Repository
public interface AdminRoleRepository extends MongoRepository<AdminRole, String> {

    Optional<AdminRole> findRoleById( String roleId );

    @Query( "{ 'id': ?0 }" )
    int updateRole( String roleId, Update update );

}

