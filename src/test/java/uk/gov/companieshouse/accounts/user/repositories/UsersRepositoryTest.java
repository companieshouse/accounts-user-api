package uk.gov.companieshouse.accounts.user.repositories;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.accounts.user.models.OneLoginDataDao;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.models.Users;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootTest
@Testcontainers(parallel = true)
@Tag("integration-test")
public class UsersRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer container = new MongoDBContainer("mongo:5");

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    UsersRepository usersRepository;

    List<Users> users ;

    @BeforeEach
    void setup(){

        final var eminem = new Users();
        eminem.setId( "111" );
        eminem.setLocale( "GB_en" );
        eminem.setForename( "Marshall" );
        eminem.setSurname( "Mathers" );
        eminem.setDisplayName( "Eminem" );
        eminem.setEmail( "eminem@rap.com" );
        eminem.setRoles( List.of("supervisor", "restricted_word") );
        eminem.setCreated( LocalDateTime.now().minusDays( 1 ) );
        eminem.setUpdated( LocalDateTime.now() );

        final var theRock = new Users();
        theRock.setId( "222" );
        theRock.setLocale( "GB_en" );
        theRock.setForename( "Dwayne" );
        theRock.setSurname( "Johnson" );
        theRock.setDisplayName( "The Rock" );
        theRock.setEmail( "the.rock@wrestling.com" );
        theRock.setRoles( List.of( "bados_user", "restricted_word" ) );
        theRock.setCreated( LocalDateTime.now().minusDays( 4 ) );
        theRock.setUpdated( LocalDateTime.now().minusDays( 2 ) );

        final var harleyQuinn = new Users();
        harleyQuinn.setId( "333" );
        harleyQuinn.setLocale( "GB_en" );
        harleyQuinn.setForename( "Harleen" );
        harleyQuinn.setSurname( "Quinzel" );
        harleyQuinn.setDisplayName( "Harley Quinn" );
        harleyQuinn.setEmail( "harley.quinn@gotham.city" );
        harleyQuinn.setRoles( List.of( "appeals_team" ) );
        harleyQuinn.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harleyQuinn.setUpdated( LocalDateTime.now().minusDays( 5 ) );
        OneLoginDataDao oneLoginDataDao = new OneLoginDataDao();
        oneLoginDataDao.setOneLoginUserId("333");
        harleyQuinn.setOneLoginData(oneLoginDataDao);


        final var harryPotter = new Users();
        harryPotter.setId( "444" );
        harryPotter.setLocale( "GB_en" );
        harryPotter.setForename( "Daniel" );
        harryPotter.setSurname( "Radcliff" );
        harryPotter.setDisplayName( "Harry Potter" );
        harryPotter.setEmail( "harry.potter@under-the-stairs.com" );
        harryPotter.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harryPotter.setUpdated( LocalDateTime.now().minusDays( 5 ) );

        users = usersRepository.saveAll( List.of( eminem, theRock, harleyQuinn, harryPotter ) );
    }

    @Test
    void fetchUsersWithNullInputThrowsUncategorizedMongoDbException(){
        Assertions.assertThrows( UncategorizedMongoDbException.class, () -> usersRepository.fetchUsers( null ) );
    }

    @Test
    void fetchUsersWithEmptyListOrListWithMalformedEmailsOrListWithNonexistentUserReturnsEmptyList(){
        final var listWithNull = new ArrayList<String>();
        listWithNull.add( null );

        Assertions.assertEquals( List.of(), usersRepository.fetchUsers( List.of() ) );
        Assertions.assertEquals( List.of(), usersRepository.fetchUsers( listWithNull ) );
        Assertions.assertEquals( List.of(), usersRepository.fetchUsers( List.of( "xxx" ) ) );
        Assertions.assertEquals( List.of(), usersRepository.fetchUsers( List.of( "rebecca.addlington@olympics.com" ) ) );
    }

    @Test
    void fetchUsersReturnsSpecifiedUsers(){
        final var oneUser = usersRepository.fetchUsers( List.of( "harley.quinn@gotham.city" ) );
        final var multipleUsers = usersRepository.fetchUsers( List.of( "eminem@rap.com", "the.rock@wrestling.com" ) );

        Assertions.assertEquals( 1, oneUser.size() );
        Assertions.assertEquals( "Harley Quinn", oneUser.get( 0 ).getDisplayName() );

        Assertions.assertEquals( 2, multipleUsers.size() );
        Assertions.assertTrue( multipleUsers.stream()
                                            .map(Users::getDisplayName)
                                            .toList()
                                            .containsAll(List.of("The Rock", "Eminem") ) );
    }

    @Test
    void findUsersByIdWithMalformedInputOrNonexistentUserIdReturnsEmptyOptional(){
        Assertions.assertFalse( usersRepository.findUsersById( null ).isPresent() );
        Assertions.assertFalse( usersRepository.findUsersById( "" ).isPresent() );
        Assertions.assertFalse( usersRepository.findUsersById( "$" ).isPresent() );
        Assertions.assertFalse( usersRepository.findUsersById( "999" ).isPresent() );
    }

    @Test
    void findUsersByIdFetchesUser(){
        Assertions.assertEquals( "Harley Quinn", usersRepository.findUsersById( "333" ).get().getDisplayName() );
        Assertions.assertEquals("333",usersRepository.findUsersById( "333" ).get().getOneLoginData().getOneLoginUserId());
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }

    @Test
    void updateUserWithNullOrMalformedOrNonexistentUserIdUserDoesNothing(){
        final var update = new Update().set( "roles", List.of( "support_member") );
        usersRepository.updateUser( null, update );
        usersRepository.updateUser( "", update );
        usersRepository.updateUser( "$", update );
        usersRepository.updateUser( "999", update );

        final var users = usersRepository.findAll();
        Assertions.assertEquals( 4, users.size());
        for ( Users user: users ){
            final var roles = user.getRoles();
            Assertions.assertTrue( Objects.isNull( roles ) || !roles.contains( "support_member" ) );
        }
    }

    @Test
    void updateUserWithNullUpdateThrowsIllegalStateException(){
        Assertions.assertThrows( IllegalStateException.class, () -> usersRepository.updateUser( "444", null ) );
    }

    @Test
    void updateUserInsertSpecifiedFieldIfNotPresent(){
        final var update = new Update().set( "roles", List.of( "support_member") );
        usersRepository.updateUser( "444", update );
        Assertions.assertEquals( List.of( "support_member"), usersRepository.findUsersById( "444" ).get().getRoles() );
    }

    @Test
    void updateUserUpdatesSpecifiedField(){
        final var update = new Update().set( "roles", List.of( "support_member" ) );
        usersRepository.updateUser( "333", update );
        Assertions.assertEquals( List.of( "support_member" ), usersRepository.findUsersById( "333" ).get().getRoles() );
    }

    @Test
    void fetchUsersUsingPartialEmail(){
        final var oneUser = usersRepository.findUsersByEmailLike("city", Limit.of(50));
        final var multipleUsers = usersRepository.findUsersByEmailLike("ha",  Limit.of(50)); Limit.of(50);

        Assertions.assertEquals(1, oneUser.size());
        Assertions.assertEquals("Harley Quinn", oneUser.get(0).getDisplayName());

        Assertions.assertEquals(2, multipleUsers.size());
        
        Assertions.assertTrue(multipleUsers.stream()
                                            .map(Users::getDisplayName).allMatch(user -> (user.equals("Harry Potter")) || (user.equals("Harley Quinn"))));
    }

    @Test
    void findUsersWithRole(){
        List<UserRole> roles = usersRepository.findByRolesContaining("restricted_word");
        assertEquals(2, roles.size());
    }
    
}
