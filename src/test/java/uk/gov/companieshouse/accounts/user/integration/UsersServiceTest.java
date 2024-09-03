package uk.gov.companieshouse.accounts.user.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootTest
@Testcontainers(parallel = true)
@DisabledInAotMode
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

    @Autowired
    RolesRepository userRolesRepository;

    @BeforeEach
    void setup(){

        final var supervisor = new ArrayList<String>();
        supervisor.add( "supervisor" );

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

        final var badosUserAndRestrictedWord = new ArrayList<String>();
        badosUserAndRestrictedWord.addAll( List.of( "BADOS_USER", "RESTRICTED_WORD" ) );

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

        final var appealsTeam = new ArrayList<String>();
        appealsTeam.add( "APPEALS_TEAM" );

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

        UserRole supervisor1 = new UserRole();
        supervisor1.setId("supervisor");

        UserRole badosUser = new UserRole();
        badosUser.setId("bados_user");


        UserRole restrictedWord = new UserRole();
        restrictedWord.setId("restricted_word");


        UserRole supportMember = new UserRole();
        supportMember.setId("support_member");

        UserRole csiSupport = new UserRole();
        csiSupport.setId("csi_support");

        userRolesRepository.insert(List.of(supervisor1,badosUser,restrictedWord,supportMember,csiSupport));

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


    @Test
    void setRolesWithNullOrMalformedOrNonexistentUserIdUserDoesNothing(){
        var rolesList = new RolesList();
        rolesList.add("support_member");
        usersService.setRoles( null, rolesList);
        usersService.setRoles( "", rolesList );
        usersService.setRoles( "$", rolesList );
        usersService.setRoles( "999", rolesList );

        final var users = usersRepository.findAll();
        Assertions.assertEquals( 4, users.size() );
        for ( Users user: users ){
            final var roles = user.getRoles();
            Assertions.assertTrue( Objects.isNull( roles ) || !roles.contains( "support_member") );
        }
    }

    @Test
    void setRolesInsertsRolesFieldIfNotPresent(){
        var rolesList = new RolesList();
        rolesList.add("support_member");
        usersService.setRoles( "444", rolesList);
        Assertions.assertEquals( List.of( "support_member"), usersRepository.findUsersById( "444" ).get().getRoles() );
    }

    @Test
    void setRolesWithNullRolesThrowsNullPointerException(){
        Assertions.assertThrows( NullPointerException.class, () -> usersService.setRoles( "333", null ) );
    }

    @Test
    void setRolesUpdatesRoles(){
        var rolesList = new RolesList();

        usersService.setRoles( "333", rolesList );
        Assertions.assertEquals( List.of(), usersRepository.findUsersById("333").get().getRoles() );

        rolesList.add("support_member");

        usersService.setRoles( "333", rolesList );
        Assertions.assertEquals( "support_member", usersRepository.findUsersById("333").get().getRoles().get(0) );

        rolesList.add("csi_support");
        usersService.setRoles( "333", rolesList);

        final var roles = usersRepository.findUsersById("333").get().getRoles();
        Assertions.assertEquals( 2, roles.size() );
        Assertions.assertTrue( roles.containsAll( List.of( "support_member", "csi_support" ) ) );
    }

    @Test
    void setRolesUpdatesRolesShouldThrowErrorWhenDummyRoleAdded(){
        var rolesList = new RolesList();
        rolesList.add("dummy");
        Assertions.assertThrows( BadRequestRuntimeException.class, () ->  usersService.setRoles( "333", rolesList ) );
    }

    @AfterEach
    public void after() {
        mongoTemplate.dropCollection(UserRole.class);
        mongoTemplate.dropCollection( Users.class );
    }

}
