package uk.gov.companieshouse.accounts.user.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.aot.DisabledInAotMode;

import uk.gov.companieshouse.accounts.user.models.OneLoginDataDao;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Tag("unit-test")
@DisabledInAotMode
public class UsersDtoDaoMapperTest {

    @Autowired
    UsersDtoDaoMapper usersDtoDaoMapper;

    private final String EMINEM_ID = "111";
    private final String EMINEM_FORENAME = "Marshall";
    private final String EMINEM_SURNAME = "Mathers";
    private final String EMINEM_DISPLAY_NAME = "Eminem";
    private final String EMINEM_EMAIL = "eminem@rap.com";
    private final LocalDateTime EMINEM_CREATED = LocalDateTime.now();
    private final List<String> EMINEM_ROLES;

    public UsersDtoDaoMapperTest(){
        EMINEM_ROLES = new ArrayList<>();
        EMINEM_ROLES.add( "supervisor" );
    }

    @Test
    void usersDaoToDtoWithNullInputReturnsNull(){
        Assertions.assertNull( usersDtoDaoMapper.daoToDto(  null ) );
    }

    @Test
    void usersDaoToDtoWithObjectWhereFieldAreNullReturnsObjectWhereAllFieldsAreNull(){
         final var user = usersDtoDaoMapper.daoToDto( new Users() );
         Assertions.assertNull( user.getForename() );
         Assertions.assertNull( user.getSurname() );
         Assertions.assertNull( user.getEmail() );
         Assertions.assertNull( user.getUserId() );
         Assertions.assertNull( user.getDisplayName() );
         Assertions.assertNull( user.getRoles() );
    }

    @Test
    void usersDaoToDtoShouldMapObject(){
        final var eminemUsers = new Users();
        eminemUsers.setId( EMINEM_ID );
        eminemUsers.setEmail(EMINEM_EMAIL);
        eminemUsers.setForename(EMINEM_FORENAME);
        eminemUsers.setSurname(EMINEM_SURNAME);
        String EMINEM_LOCALE = "GB_en";
        eminemUsers.setLocale(EMINEM_LOCALE);
        eminemUsers.setDisplayName(EMINEM_DISPLAY_NAME);
        eminemUsers.setCreated(EMINEM_CREATED);
        eminemUsers.setRoles(EMINEM_ROLES);
        eminemUsers.setPrivateBetaUser(true);
        eminemUsers.setOneLoginData(new OneLoginDataDao());


        final var user = usersDtoDaoMapper.daoToDto( eminemUsers );

        Assertions.assertEquals( EMINEM_FORENAME, user.getForename() );
        Assertions.assertEquals( EMINEM_SURNAME, user.getSurname() );
        Assertions.assertEquals( EMINEM_EMAIL, user.getEmail() );
        Assertions.assertEquals( EMINEM_ID, user.getUserId() );
        Assertions.assertEquals( EMINEM_DISPLAY_NAME, user.getDisplayName() );
        Assertions.assertEquals( EMINEM_ROLES.get(0), user.getRoles().getFirst() );
        Assertions.assertTrue(user.getIsPrivateBetaUser());
        Assertions.assertTrue(user.getHasLinkedOneLogin());
    }

    @Test
    void userDtoToDaoWithNullInputReturnsNull(){
        Assertions.assertNull( usersDtoDaoMapper.dtoToDao( null ) );
    }

    @Test
    void usersDtoToDaoWithObjectWhereFieldAreNullReturnsObjectWhereAllFieldsAreNull(){
        final var users = usersDtoDaoMapper.dtoToDao( new User() );

        Assertions.assertNull( users.getForename() );
        Assertions.assertNull( users.getSurname() );
        Assertions.assertNull( users.getEmail() );
        Assertions.assertNull( users.getId() );
        Assertions.assertNull( users.getDisplayName() );
        Assertions.assertNull( users.getRoles() );
    }

    @Test
    void usersDtoToDaoShouldMapObject(){
        var supervisorRole = new RolesList();
        supervisorRole.add("supervisor");
        final var eminemUser =
                new User().forename( EMINEM_FORENAME )
                          .surname( EMINEM_SURNAME )
                          .email( EMINEM_EMAIL )
                          .userId( EMINEM_ID )
                          .displayName( EMINEM_DISPLAY_NAME )
                          .roles( supervisorRole );

        final var user = usersDtoDaoMapper.dtoToDao( eminemUser );

        Assertions.assertEquals( EMINEM_FORENAME, user.getForename() );
        Assertions.assertEquals( EMINEM_SURNAME, user.getSurname() );
        Assertions.assertEquals( EMINEM_EMAIL, user.getEmail() );
        Assertions.assertEquals( EMINEM_ID, user.getId() );
        Assertions.assertEquals( EMINEM_DISPLAY_NAME, user.getDisplayName() );
        Assertions.assertEquals( List.of( "supervisor"), user.getRoles() );
    }

}
