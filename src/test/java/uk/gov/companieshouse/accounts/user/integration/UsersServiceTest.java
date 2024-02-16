package uk.gov.companieshouse.accounts.user.integration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

@SpringBootTest
@Testcontainers(parallel = true)
@Tag("integration-test")
public class UsersServiceTest {

    @Container
    @ServiceConnection
    static MongoDBContainer container = new MongoDBContainer("mongo:5");

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    UsersService usersService;

    @BeforeEach
    void setup(){

        final var supervisor = new RolesList();
        supervisor.add( Role.SUPERVISOR );

        final var eminem = new Users();
        eminem.setId( "111" );
        eminem.setLocale( "GB_en" );
        eminem.setForename( "Marshall" );
        eminem.setSurname( "Mathers" );
        eminem.setDisplayName( "Eminem" );
        eminem.setEmail( "eminem@rap.com" );
        eminem.setRoles( supervisor );
        eminem.setCreated( LocalDateTime.now().minusDays( 1 ) );
        eminem.setUpdated( LocalDateTime.now() );

        final var badosUserAndRestrictedWord = new RolesList();
        badosUserAndRestrictedWord.addAll( List.of( Role.BADOS_USER, Role.RESTRICTED_WORD ) );

        final var theRock = new Users();
        theRock.setId( "222" );
        theRock.setLocale( "GB_en" );
        theRock.setForename( "Dwayne" );
        theRock.setSurname( "Johnson" );
        theRock.setDisplayName( "The Rock" );
        theRock.setEmail( "the.rock@wrestling.com" );
        theRock.setRoles( badosUserAndRestrictedWord );
        theRock.setCreated( LocalDateTime.now().minusDays( 4 ) );
        theRock.setUpdated( LocalDateTime.now().minusDays( 2 ) );

        final var appealsTeam = new RolesList();
        appealsTeam.add( Role.APPEALS_TEAM );

        final var harleyQuinn = new Users();
        harleyQuinn.setId( "333" );
        harleyQuinn.setLocale( "GB_en" );
        harleyQuinn.setForename( "Harleen" );
        harleyQuinn.setSurname( "Quinzel" );
        harleyQuinn.setDisplayName( "Harley Quinn" );
        harleyQuinn.setEmail( "harley.quinn@gotham.city" );
        harleyQuinn.setRoles( appealsTeam );
        harleyQuinn.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harleyQuinn.setUpdated( LocalDateTime.now().minusDays( 5 ) );

        final var harryPotter = new Users();
        harryPotter.setId( "444" );
        harryPotter.setLocale( "GB_en" );
        harryPotter.setForename( "Daniel" );
        harryPotter.setSurname( "Radcliff" );
        harryPotter.setDisplayName( "Harry Potter" );
        harryPotter.setEmail( "harry.potter@under-the-stairs.com" );
        harryPotter.setCreated( LocalDateTime.now().minusDays( 10 ) );
        harryPotter.setUpdated( LocalDateTime.now().minusDays( 5 ) );

        usersRepository.insert( List.of( eminem, theRock, harleyQuinn, harryPotter ) );
    }

    @Test
    void fetchUsersWithNullInputThrowsUncategorizedMongoDbException(){
        Assertions.assertThrows( UncategorizedMongoDbException.class, () -> usersService.fetchUsers( null ) );
    }

    @Test
    void fetchUsersWithEmptyListOrListWithMalformedEmailsOrListWithNonexistentUserReturnsEmptyList(){
        final var listWithNull = new ArrayList<String>();
        listWithNull.add( null );

        Assertions.assertEquals( List.of(), usersService.fetchUsers( List.of() ) );
        Assertions.assertEquals( List.of(), usersService.fetchUsers( listWithNull ) );
        Assertions.assertEquals( List.of(), usersService.fetchUsers( List.of( "xxx" ) ) );
        Assertions.assertEquals( List.of(), usersService.fetchUsers( List.of( "rebecca.addlington@olympics.com" ) ) );
    }

    @Test
    void fetchUsersReturnsSpecifiedUsers(){
        final var oneUser = usersService.fetchUsers( List.of( "harley.quinn@gotham.city" ) );
        final var multipleUsers = usersService.fetchUsers( List.of( "eminem@rap.com", "the.rock@wrestling.com" ) );

        Assertions.assertEquals( 1, oneUser.size() );
        Assertions.assertEquals( "Harley Quinn", oneUser.get( 0 ).getDisplayName() );

        Assertions.assertEquals( 2, multipleUsers.size() );
        Assertions.assertTrue( multipleUsers.stream()
                                            .map( User::getDisplayName )
                                            .toList()
                                            .containsAll(List.of("The Rock", "Eminem") ) );
    }

    @Test
    void fetchUserWithMalformedInputOrNonexistentUserIdReturnsEmptyOptional(){
        Assertions.assertFalse( usersService.fetchUser( null ).isPresent() );
        Assertions.assertFalse( usersService.fetchUser( "" ).isPresent() );
        Assertions.assertFalse( usersService.fetchUser( "$" ).isPresent() );
        Assertions.assertFalse( usersService.fetchUser( "999" ).isPresent() );
    }

    @Test
    void fetchUserFetchesUser(){
        Assertions.assertEquals( "Harley Quinn", usersService.fetchUser( "333" ).get().getDisplayName() );
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection( Users.class );
    }

    @Test
    void setRolesWithNullOrMalformedOrNonexistentUserIdUserDoesNothing(){
        usersService.setRoles( null, List.of( Role.SUPPORT_MEMBER ) );
        usersService.setRoles( "", List.of( Role.SUPPORT_MEMBER ) );
        usersService.setRoles( "$", List.of( Role.SUPPORT_MEMBER ) );
        usersService.setRoles( "999", List.of( Role.SUPPORT_MEMBER ) );

        final var users = usersRepository.findAll();
        Assertions.assertEquals( users.size(), 4 );
        for ( Users user: users ){
            final var roles = user.getRoles();
            Assertions.assertTrue( Objects.isNull( roles ) || !roles.contains( Role.SUPPORT_MEMBER ) );
        }
    }

    @Test
    void setRolesInsertsRolesFieldIfNotPresent(){
        usersService.setRoles( "444", List.of( Role.SUPPORT_MEMBER ) );
        Assertions.assertEquals( List.of( Role.SUPPORT_MEMBER ), usersRepository.findUsersById( "444" ).get().getRoles() );
    }

    @Test
    void setRolesWithNullRolesThrowsNullPointerException(){
        Assertions.assertThrows( NullPointerException.class, () -> usersService.setRoles( "333", null ) );
    }

    @Test
    void setRolesUpdatesRoles(){

        usersService.setRoles( "333", List.of() );
        Assertions.assertEquals( List.of(), usersRepository.findUsersById("333").get().getRoles() );

        usersService.setRoles( "333", List.of( Role.SUPPORT_MEMBER ) );
        Assertions.assertEquals( List.of( Role.SUPPORT_MEMBER ), usersRepository.findUsersById("333").get().getRoles() );

        usersService.setRoles( "333", List.of( Role.SUPPORT_MEMBER, Role.CSI_SUPPORT ) );

        final var roles = usersRepository.findUsersById("333").get().getRoles();
        Assertions.assertEquals( 2, roles.size() );
        Assertions.assertTrue( roles.containsAll( List.of( Role.SUPPORT_MEMBER, Role.CSI_SUPPORT ) ) );
    }

    @Test
    void setRolesEliminatesDuplicates(){
        usersService.setRoles( "444", List.of( Role.SUPPORT_MEMBER, Role.SUPPORT_MEMBER ) );
        Assertions.assertEquals( List.of( Role.SUPPORT_MEMBER ) , usersService.fetchUser( "444" ).get().getRoles() );
    }

}
