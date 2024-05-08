package uk.gov.companieshouse.accounts.user.service;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.AtMost;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit-test")
public class UsersServiceTest {

    @Mock
    UsersDtoDaoMapper usersDtoDaoMapper;

    @Mock
    UsersRepository usersRepository;

    @Mock
    RolesRepository userRolesRepository;

    @InjectMocks
    UsersService usersService;

    private Users usersEminem;
    private User userEminem;
    private Users usersTheRock;
    private User userTheRock;
    private Users usersHarleyQuinn;
    private User userHarleyQuinn;
    private Users usersHarryPotter;
    private User userHarryPotter;

    @BeforeEach
    void setup(){
        usersEminem = new Users();
        usersEminem.setId( "111" );
        usersEminem.setLocale( "GB_en" );
        usersEminem.setForename( "Marshall" );
        usersEminem.setSurname( "Mathers" );
        usersEminem.setDisplayName( "Eminem" );
        usersEminem.setEmail( "eminem@rap.com" );
        usersEminem.setRoles( List.of( "support_member") );
        usersEminem.setCreated( LocalDateTime.now().minusDays( 1 ) );
        usersEminem.setUpdated( LocalDateTime.now() );

        final var supervisor = new RolesList();
        supervisor.add( "support_member" );

        userEminem = new User();
        userEminem.userId("111")
                  .forename("Marshall")
                  .surname("Mathers")
                  .displayName("Eminem")
                  .email("eminem@rap.com")
                  .roles( supervisor );

        final var badosUserAndRestrictedWord = new ArrayList<String>();
        badosUserAndRestrictedWord.addAll( List.of( "bados_user", "restricted_word"  ) );

        usersTheRock = new Users();
        usersTheRock.setId( "222" );
        usersTheRock.setLocale( "GB_en" );
        usersTheRock.setForename( "Dwayne" );
        usersTheRock.setSurname( "Johnson" );
        usersTheRock.setDisplayName( "The Rock" );
        usersTheRock.setEmail( "the.rock@wrestling.com" );
        usersTheRock.setRoles( badosUserAndRestrictedWord );
        usersTheRock.setCreated( LocalDateTime.now().minusDays( 4 ) );
        usersTheRock.setUpdated( LocalDateTime.now().minusDays( 2 ) );

        final var badosUserAndRestrictedWordRoles = new RolesList();
        badosUserAndRestrictedWordRoles.addAll( List.of( "bados_user", "restricted_word"  ) );

        userTheRock = new User();
        userTheRock.userId("222")
                   .forename("Dwayne")
                   .surname("Johnson")
                   .displayName("The Rock")
                   .email("the.rock@wrestling.com")
                   .roles( badosUserAndRestrictedWordRoles );

        usersHarleyQuinn = new Users();
        usersHarleyQuinn.setId( "333" );
        usersHarleyQuinn.setLocale( "GB_en" );
        usersHarleyQuinn.setForename( "Harleen" );
        usersHarleyQuinn.setSurname( "Quinzel" );
        usersHarleyQuinn.setDisplayName( "Harley Quinn" );
        usersHarleyQuinn.setEmail( "harley.quinn@gotham.city" );
        usersHarleyQuinn.setRoles( List.of( "appeals_team" ) );
        usersHarleyQuinn.setCreated( LocalDateTime.now().minusDays( 10 ) );
        usersHarleyQuinn.setUpdated( LocalDateTime.now().minusDays( 5 ) );

        final var appealsTeam = new RolesList();
        appealsTeam.add( "appeals_team");

        userHarleyQuinn = new User();
        userHarleyQuinn.userId("333")
                       .forename("Harleen")
                       .surname("Quinzel")
                       .displayName("Harley Quinn")
                       .email("harley.quinn@gotham.city")
                       .roles( appealsTeam );

        usersHarryPotter = new Users();
        usersHarryPotter.setId("444");
        usersHarryPotter.setLocale("GB_en");
        usersHarryPotter.setForename("Daniel");
        usersHarryPotter.setSurname("Radcliff");
        usersHarryPotter.setDisplayName("Harry Potter");
        usersHarryPotter.setEmail("harry.potter@under-the-stairs.com");
        usersHarryPotter.setCreated(LocalDateTime.now().minusDays(10));
        usersHarryPotter.setUpdated(LocalDateTime.now().minusDays(5));

        userHarryPotter = new User();
        userHarryPotter.userId("444");
        userHarryPotter.setForename("Daniel");
        userHarryPotter.setSurname("Radcliff");
        userHarryPotter.setDisplayName("Harry Potter");
        userHarryPotter.setEmail("harry.potter@under-the-stairs.com");
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

        Mockito.doReturn( List.of( usersHarleyQuinn ) ).when( usersRepository ).fetchUsers( List.of( "harley.quinn@gotham.city" ));
        Mockito.doReturn( userHarleyQuinn ).when( usersDtoDaoMapper ).daoToDto( usersHarleyQuinn );
        Mockito.doReturn( List.of( usersEminem, usersTheRock ) ).when( usersRepository ).fetchUsers( List.of( "eminem@rap.com", "the.rock@wrestling.com" ));
        Mockito.doReturn( userEminem ).when( usersDtoDaoMapper ).daoToDto( usersEminem );
        Mockito.doReturn( userTheRock ).when( usersDtoDaoMapper ).daoToDto( usersTheRock );

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
        Mockito.doReturn( userHarleyQuinn ).when( usersDtoDaoMapper ).daoToDto( usersHarleyQuinn );

        Assertions.assertEquals( "Harley Quinn", usersService.fetchUser( "333" ).get().getDisplayName() );
    }

    private ArgumentMatcher<Update> setRolesUpdateParameterMatches( Set<String> expectedRoles ) {
        return update -> {
            final var document = update.getUpdateObject().get("$set", Document.class);
            final var roles = document.getOrDefault("roles", null );
            return roles.equals( expectedRoles );
        };
    }

    @Test
    void setRolesWithNullOrMalformedOrNonexistentUserIdUserRunsQuery(){
        final var support = new RolesList();
        support.add( "support_member" );
        Mockito.doReturn( 0 ).when( usersRepository ).updateUser( any(), any() );
        Mockito.doReturn( true ).when( userRolesRepository ).existsById( "support_member" );

        usersService.setRoles( null, support);
        Mockito.verify( usersRepository ).updateUser( isNull(), argThat(setRolesUpdateParameterMatches( Set.of( "support_member" ) ) ) );

        usersService.setRoles( "", support);
        Mockito.verify( usersRepository ).updateUser( eq(""), argThat(setRolesUpdateParameterMatches( Set.of( "support_member" ) ) ) );

        usersService.setRoles( "$", support );
        Mockito.verify( usersRepository ).updateUser( eq("$"), argThat(setRolesUpdateParameterMatches( Set.of( "support_member") ) ) );

        usersService.setRoles( "999", support );
        Mockito.verify( usersRepository ).updateUser( eq("999"), argThat(setRolesUpdateParameterMatches( Set.of( "support_member" ) ) ) );
    }

    @Test
    void setRolesInsertsRolesFieldIfNotPresentRunsQuery(){
        final var support = new RolesList();
        support.add( "support-member" );
        Mockito.doReturn( 1 ).when( usersRepository ).updateUser( any(), any() );
        Mockito.doReturn( true ).when( userRolesRepository ).existsById( "support-member" );

        usersService.setRoles( "444", support );
        Mockito.verify( usersRepository, new AtMost(1)).updateUser( eq("444"), argThat(setRolesUpdateParameterMatches( Set.of( "support_member" ) ) ) );
    }

    @Test
    void setRolesWithNullRolesThrowsNullPointerException(){
        Assertions.assertThrows( NullPointerException.class, () -> usersService.setRoles( "333", null ) );
    }

    @Test
    void setRolesUpdatesRolesRunsQuery(){
        Mockito.doReturn( 1 ).when( usersRepository ).updateUser( any(), any() );
        Mockito.doReturn( true ).when( userRolesRepository ).existsById( "support_member" );
        Mockito.doReturn( true ).when( userRolesRepository ).existsById( "csi_support" );

        usersService.setRoles( "333", new RolesList() );
        Mockito.verify( usersRepository ).updateUser( eq("333"), argThat(setRolesUpdateParameterMatches( Set.of(  ) ) ) );
        final var support = new RolesList();
        support.add( "support_member" );
        usersService.setRoles( "333", support);
        Mockito.verify( usersRepository, new AtMost(1) ).updateUser( eq("333"), argThat(setRolesUpdateParameterMatches( Set.of( "support_member") ) ) );

        support.add( "csi_support" );

        usersService.setRoles( "333", support);
        Mockito.verify( usersRepository, new AtMost(1) ).updateUser( eq("333"), argThat(setRolesUpdateParameterMatches( Set.of( "support_member", "csi_support") ) ) );
    }

    @Test
    void setRolesWithDummyRolesThrowsBadRequestException(){
        var rolesList = new RolesList();
        rolesList.add("dummy");
        when(userRolesRepository.existsById("dummy")).thenReturn(false);
        Assertions.assertThrows( BadRequestRuntimeException.class, () -> usersService.setRoles( "333", rolesList) );
    }

    @Test
    void fetchUsersUsingPartialEmail(){
        ReflectionTestUtils.setField(usersService, "limit", 50);
        Mockito.doReturn(List.of(usersHarleyQuinn, usersHarryPotter)).when(usersRepository).findUsersByEmailLike("har", Limit.of(50));
        Mockito.doReturn(userHarleyQuinn).when(usersDtoDaoMapper).daoToDto(usersHarleyQuinn);
        Mockito.doReturn(userHarryPotter).when(usersDtoDaoMapper).daoToDto(usersHarryPotter);

        final var multipleUsers = usersService.fetchUsersUsingPartialEmail("har");

        Assertions.assertEquals(2, multipleUsers.size());
        Assertions.assertTrue(multipleUsers.stream()
                .map(User::getDisplayName).allMatch(user -> (user.equals("Harry Potter")) || (user.equals("Harley Quinn"))));
    }

}
