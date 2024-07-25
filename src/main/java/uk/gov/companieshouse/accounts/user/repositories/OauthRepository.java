package uk.gov.companieshouse.accounts.user.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.accounts.user.models.Oauth2AuthorisationsDao;

import java.util.List;

@Repository
public interface OauthRepository extends MongoRepository<Oauth2AuthorisationsDao, String> {
    Oauth2AuthorisationsDao findByCode(String code);

    Oauth2AuthorisationsDao findByToken(String token);

    @Query(sort="{'token_valid_until':-1}")
    List<Oauth2AuthorisationsDao> findByClientIDAndUserDetailsUserID(String clientId, String userId);

    Oauth2AuthorisationsDao findByRefreshTokenID(String refreshTokenId);
}