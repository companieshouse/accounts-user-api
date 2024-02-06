package uk.gov.companieshouse.accounts.user.repositories;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.accounts.user.models.Users;

@Repository
public interface UsersRepository extends MongoRepository<Users, String> {

    @Query( "{ 'email': { $in: ?0 } }" )
    List<Users> fetchUsers( List<String> emails );

}

