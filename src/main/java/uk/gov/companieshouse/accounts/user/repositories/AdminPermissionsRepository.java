package uk.gov.companieshouse.accounts.user.repositories;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.accounts.user.models.AdminPermissions;

@Repository
public interface AdminPermissionsRepository extends MongoRepository<AdminPermissions, String> {

    @Query( "{ 'id': ?0 }" )
    int updateRole( String roleId, Update update );

    @Query( "{ 'entra_group_id': ?0 }" )
    AdminPermissions findByEntraGroupId( String entraGroupId );
}

