package uk.gov.companieshouse.accounts.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.User;

@ExtendWith(MockitoExtension.class)
@Tag("unit-test")
public class UsersServiceTest {

    @Mock
    UsersDtoDaoMapper usersDtoDaoMapper;

    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    UsersService usersService;

    private Users usersEminem;
    private User userEminem;
    private Users usersTheRock;
    private User userTheRock;
    private Users usersHarleyQuinn;
    private User userHarleyQuinn;

    @BeforeEach
    void setup(){

        usersEminem = new Users();
        usersEminem.setId( "111" );
        usersEminem.setLocale( "GB_en" );
        usersEminem.setForename( "Marshall" );
        usersEminem.setSurname( "Mathers" );
        usersEminem.setDisplayName( "Eminem" );
        usersEminem.setEmail( "eminem@rap.com" );
        usersEminem.setRoles( Set.of( Role.SUPERVISOR ) );
        usersEminem.setCreated( LocalDateTime.now().minusDays( 1 ) );
        usersEminem.setUpdated( LocalDateTime.now() );

        userEminem = new User();
        userEminem.userId("111")
                  .forename("Marshall")
                  .surname("Mathers")
                  .displayName("Eminem")
                  .email("eminem@rap.com")
                  .roles(Set.of( Role.SUPERVISOR ));

        usersTheRock = new Users();
        usersTheRock.setId( "222" );
        usersTheRock.setLocale( "GB_en" );
        usersTheRock.setForename( "Dwayne" );
        usersTheRock.setSurname( "Johnson" );
        usersTheRock.setDisplayName( "The Rock" );
        usersTheRock.setEmail( "the.rock@wrestling.com" );
        usersTheRock.setRoles( Set.of( Role.BADOS_USER, Role.RESTRICTED_WORD ) );
        usersTheRock.setCreated( LocalDateTime.now().minusDays( 4 ) );
        usersTheRock.setUpdated( LocalDateTime.now().minusDays( 2 ) );

        userTheRock = new User();
        userTheRock.userId("222")
                   .forename("Dwayne")
                   .surname("Johnson")
                   .displayName("The Rock")
                   .email("the.rock@wrestling.com")
                   .roles(Set.of( Role.BADOS_USER, Role.RESTRICTED_WORD ));

        usersHarleyQuinn = new Users();
        usersHarleyQuinn.setId( "333" );
        usersHarleyQuinn.setLocale( "GB_en" );
        usersHarleyQuinn.setForename( "Harleen" );
        usersHarleyQuinn.setSurname( "Quinzel" );
        usersHarleyQuinn.setDisplayName( "Harley Quinn" );
        usersHarleyQuinn.setEmail( "harley.quinn@gotham.city" );
        usersHarleyQuinn.setRoles( Set.of( Role.APPEALS_TEAM ) );
        usersHarleyQuinn.setCreated( LocalDateTime.now().minusDays( 10 ) );
        usersHarleyQuinn.setUpdated( LocalDateTime.now().minusDays( 5 ) );

        userHarleyQuinn = new User();
        userHarleyQuinn.userId("333")
                       .forename("Harleen")
                       .surname("Quinzel")
                       .displayName("Harley Quinn")
                       .email("harley.quinn@gotham.city")
                       .roles(Set.of( Role.APPEALS_TEAM ));
    }

    @Test
    void fetchUsersWithNullInputThrowsUncategorizedMongoDbException(){
        Mockito.doThrow( new UncategorizedMongoDbException( "msg", new Exception() ) ).when( usersRepository ).fetchUsers( any() );
        Assertions.assertThrows( UncategorizedMongoDbException.class, () -> usersService.fetchUsers( null ) );
    }

    @Test
    void fetchUsersWithEmptyListOrListWithMalformedEmailsOrListWithNonexistentUserReturnsEmptyList(){
        final var listWithNull = new ArrayList<String>();
        listWithNull.add( null );

        Mockito.doReturn( List.of() ).when( usersRepository ).fetchUsers( any() );

        Assertions.assertEquals( List.of(), usersService.fetchUsers( List.of() ) );
        Assertions.assertEquals( List.of(), usersService.fetchUsers( listWithNull ) );
        Assertions.assertEquals( List.of(), usersService.fetchUsers( List.of( "xxx" ) ) );
        Assertions.assertEquals( List.of(), usersService.fetchUsers( List.of( "rebecca.addlington@olympics.com" ) ) );
    }

    @Test
    void fetchUsersReturnsSpecifiedUsers(){

        Mockito.doReturn( List.of( usersHarleyQuinn ) ).when( usersRepository ).fetchUsers( eq( List.of( "harley.quinn@gotham.city" ) ) );
        Mockito.doReturn( userHarleyQuinn ).when( usersDtoDaoMapper ).daoToDto( eq( usersHarleyQuinn ) );
        Mockito.doReturn( List.of( usersEminem, usersTheRock ) ).when( usersRepository ).fetchUsers( eq( List.of( "eminem@rap.com", "the.rock@wrestling.com" ) ) );
        Mockito.doReturn( userEminem ).when( usersDtoDaoMapper ).daoToDto( eq( usersEminem ) );
        Mockito.doReturn( userTheRock ).when( usersDtoDaoMapper ).daoToDto( eq( usersTheRock ) );

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
        Mockito.doReturn( Optional.empty() ).when( usersRepository ).findUsersById( any() );

        Assertions.assertFalse( usersService.fetchUser( null ).isPresent() );
        Assertions.assertFalse( usersService.fetchUser( "" ).isPresent() );
        Assertions.assertFalse( usersService.fetchUser( "$" ).isPresent() );
        Assertions.assertFalse( usersService.fetchUser( "999" ).isPresent() );
    }

    @Test
    void fetchUserFetchesUser(){
        Mockito.doReturn( Optional.of( usersHarleyQuinn ) ).when( usersRepository ).findUsersById( any() );
        Mockito.doReturn( userHarleyQuinn ).when( usersDtoDaoMapper ).daoToDto( eq( usersHarleyQuinn ) );

        Assertions.assertEquals( "Harley Quinn", usersService.fetchUser( "333" ).get().getDisplayName() );
    }


}
