package uk.gov.companieshouse.accounts.user.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.accounts.user.models.Users;

@Repository
public interface UsersRepository extends MongoRepository<Users, String> {

}

