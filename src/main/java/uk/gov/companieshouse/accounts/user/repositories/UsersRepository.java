package uk.gov.companieshouse.accounts.user.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.accounts.user.models.Users;

@Repository
public interface UsersRepository extends MongoRepository<Users, String> {

    @Query( "{ 'email': { $in: ?0 } }" )
    List<Users> fetchUsers( List<String> emails );
    @Query( value = "{ 'user_id': ?0 }")
    Optional<Users> findRolesByUserId(String userId);

    Optional<Users> findUsersById( String userId );

}

