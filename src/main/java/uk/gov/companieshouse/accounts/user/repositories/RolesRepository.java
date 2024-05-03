package uk.gov.companieshouse.accounts.user.repositories;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.accounts.user.models.UserRole;

@Repository
public interface RolesRepository extends MongoRepository<UserRole, String> {

    @Query( "{ 'id': ?0 }" )
    int updateRole( String roleId, Update update );
}

