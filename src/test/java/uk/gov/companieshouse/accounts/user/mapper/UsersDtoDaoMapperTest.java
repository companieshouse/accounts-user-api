package uk.gov.companieshouse.accounts.user.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.User;

@SpringBootTest
@Tag("unit-test")
public class UsersDtoDaoMapperTest {

    @Autowired
    UsersDtoDaoMapper usersDtoDaoMapper;

    private final String EMINEM_ID = "111";
    private final String EMINEM_LOCALE = "GB_en";
    private final String EMINEM_FORENAME = "Marshall";
    private final String EMINEM_SURNAME = "Mathers";
    private final String EMINEM_DISPLAY_NAME = "Eminem";
    private final String EMINEM_EMAIL = "eminem@rap.com";
    private final LocalDateTime EMINEM_CREATED = LocalDateTime.now();
    private final List<Role> EMINEM_ROLES = List.of( Role.SUPERVISOR );

    @Test
    void usersDaoToDtoWithNullInputReturnsNull(){
        Assertions.assertNull( usersDtoDaoMapper.daoToDto( (Users) null ) );
    }

    @Test
    void usersDaoToDtoWithObjectWhereFieldAreNullReturnsObjectWhereAllFieldsAreNull(){
         final var user = usersDtoDaoMapper.daoToDto( new Users() );
         Assertions.assertNull( user.getForename() );
         Assertions.assertNull( user.getSurname() );
         Assertions.assertNull( user.getEmail() );
         Assertions.assertNull( user.getUserId() );
         Assertions.assertNull( user.getDisplayName() );
         Assertions.assertTrue( user.getRoles().isEmpty() );
    }

    @Test
    void usersDaoToDtoShouldMapObject(){
        final var eminemUsers = new Users(EMINEM_LOCALE, EMINEM_FORENAME, EMINEM_SURNAME, EMINEM_DISPLAY_NAME, EMINEM_EMAIL, EMINEM_CREATED, EMINEM_ROLES );
        eminemUsers.setId( EMINEM_ID );

        final var user = usersDtoDaoMapper.daoToDto( eminemUsers );

        Assertions.assertEquals( EMINEM_FORENAME, user.getForename() );
        Assertions.assertEquals( EMINEM_SURNAME, user.getSurname() );
        Assertions.assertEquals( EMINEM_EMAIL, user.getEmail() );
        Assertions.assertEquals( EMINEM_ID, user.getUserId() );
        Assertions.assertEquals( EMINEM_DISPLAY_NAME, user.getDisplayName() );
        Assertions.assertEquals( EMINEM_ROLES, user.getRoles() );
    }

    @Test
    void userDtoToDaoWithNullInputReturnsNull(){
        Assertions.assertNull( usersDtoDaoMapper.dtoToDao( (User) null ) );
    }

    @Test
    void usersDtoToDaoWithObjectWhereFieldAreNullReturnsObjectWhereAllFieldsAreNull(){
        final var users = usersDtoDaoMapper.dtoToDao( new User() );

        Assertions.assertNull( users.getForename() );
        Assertions.assertNull( users.getSurname() );
        Assertions.assertNull( users.getEmail() );
        Assertions.assertNull( users.getId() );
        Assertions.assertNull( users.getDisplayName() );
        Assertions.assertTrue( users.getRoles().isEmpty() );
    }

    @Test
    void usersDtoToDaoShouldMapObject(){
        final var eminemUser =
                new User().forename( EMINEM_FORENAME )
                          .surname( EMINEM_SURNAME )
                          .email( EMINEM_EMAIL )
                          .userId( EMINEM_ID )
                          .displayName( EMINEM_DISPLAY_NAME )
                          .roles( EMINEM_ROLES );

        final var user = usersDtoDaoMapper.dtoToDao( eminemUser );

        Assertions.assertEquals( EMINEM_FORENAME, user.getForename() );
        Assertions.assertEquals( EMINEM_SURNAME, user.getSurname() );
        Assertions.assertEquals( EMINEM_EMAIL, user.getEmail() );
        Assertions.assertEquals( EMINEM_ID, user.getId() );
        Assertions.assertEquals( EMINEM_DISPLAY_NAME, user.getDisplayName() );
        Assertions.assertEquals( EMINEM_ROLES, user.getRoles() );
    }

    @Test
    void roleDaoToDtoWithNullInputReturnsNull(){
        Assertions.assertNull( usersDtoDaoMapper.daoToDto( (Role) null ) );
    }

    @Test
    void roleDaoToDtoShouldMapObject(){
        Assertions.assertEquals( Role.SUPERVISOR, usersDtoDaoMapper.daoToDto( Role.SUPERVISOR ) );
    }

    @Test
    void roleDtoToDaoWithNullInputReturnsNull(){
        Assertions.assertNull( usersDtoDaoMapper.dtoToDao( (Role) null ) );
    }

    @Test
    void roleDtoToDaoShouldMapObject(){
        Assertions.assertEquals( Role.SUPERVISOR, usersDtoDaoMapper.dtoToDao( Role.SUPERVISOR ) );
    }

}
