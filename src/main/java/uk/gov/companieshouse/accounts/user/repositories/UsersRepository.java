package uk.gov.companieshouse.accounts.user.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.accounts.user.models.Users;

@Repository
public interface UsersRepository extends MongoRepository<Users, String> {

    @Query( "{ 'email': { $in: ?0 } }" )
    List<Users> fetchUsers( List<String> emails );

    Optional<Users> findUsersById( String userId );

    @Query("{email : { $regex : /?0/}}")
    Page<Users> fetchUsersUsingPartialEmail(String subStringOfEmail, Pageable pageable);

    @Query( "{ 'id': ?0 }" )
    int updateUser( String userId, Update update );

}

