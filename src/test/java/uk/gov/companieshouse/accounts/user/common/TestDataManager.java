package uk.gov.companieshouse.accounts.user.common;

import static uk.gov.companieshouse.GenerateEtagUtil.generateEtag;
import static uk.gov.companieshouse.accounts.user.common.ParsingUtils.localDateTimeToOffsetDateTime;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapperImpl;
import uk.gov.companieshouse.accounts.user.models.OneLoginDataDao;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.api.accounts.user.model.Roles;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

public class TestDataManager {

    private static TestDataManager instance = null;

    public static TestDataManager getInstance(){
        if ( Objects.isNull( instance ) ){
            instance = new TestDataManager();
        }
        return instance;
    }

    private final LocalDateTime now = LocalDateTime.now();

    private Map<String, Supplier<Users>> usersDaoSuppliers = new HashMap<>();

    public void instantiateUsersDaoSuppliers(){

        final Supplier<Users> elonMuskSupplier = () -> {
            final var oneloginDataDao = new OneLoginDataDao();
            oneloginDataDao.setOneLoginUserId( "CEOONELOGINUSER001" );

            final var usersDao = new Users();
            usersDao.setId( "CEOUSER001" );
            usersDao.setEmail( "admin.elon.musk@tesla.com" );
            usersDao.setForename( "Elon" );
            usersDao.setSurname( "Musk" );
            usersDao.setDisplayName( "Elon Musk" );
            usersDao.setLocale( "GB_en" );
            usersDao.setRoles( List.of( "admin-role-1" ) );
            usersDao.setOneLoginData( oneloginDataDao );

            return usersDao;
        };
        usersDaoSuppliers.put( "CEOUSER001", elonMuskSupplier );

        final Supplier<Users> markZuckerbergSupplier = () -> {
            final var usersDao = new Users();
            usersDao.setId( "CEOUSER002" );
            usersDao.setEmail( "admin.mark.zuckerberg@facebook.com" );
            usersDao.setForename( "Mark" );
            usersDao.setSurname( "Zuckerberg" );
            usersDao.setDisplayName( "Mark Zuckerberg" );
            usersDao.setLocale( "GB_en" );
            usersDao.setRoles( List.of( "admin-role-1" ) );
            usersDao.setOneLoginLinkRemovedBy( "CEOUSER001" );
            usersDao.setOneLoginLinkRemovedAt( LocalDateTime.now() );
            usersDao.setEtag( generateEtag() );

            return usersDao;
        };
        usersDaoSuppliers.put( "CEOUSER002", markZuckerbergSupplier );

        final Supplier<Users> susanWojcickiSupplier = () -> {
            final var oneloginDataDao = new OneLoginDataDao();
            oneloginDataDao.setOneLoginUserId( "CEOONELOGINUSER003" );

            final var usersDao = new Users();
            usersDao.setId( "CEOUSER003" );
            usersDao.setEmail( "susan.wojcicki@youtube.com" );
            usersDao.setForename( "Susan" );
            usersDao.setSurname( "Wojcicki" );
            usersDao.setDisplayName( "Susan Wojcicki" );
            usersDao.setLocale( "GB_en" );
            usersDao.setOneLoginData( oneloginDataDao );

            return usersDao;
        };
        usersDaoSuppliers.put( "CEOUSER003", susanWojcickiSupplier );
    }

    private TestDataManager(){
        instantiateUsersDaoSuppliers();
    }

    public List<Users> fetchUsersDaos( final String... ids ){
        return Arrays.stream( ids )
                .map( usersDaoSuppliers::get )
                .map( Supplier::get )
                .collect( Collectors.toList() );
    }

    public List<User> fetchUserDtos( final String... ids ){
        return fetchUsersDaos( ids )
                .stream()
                .map( userDao -> {
                    final var rolesList = new RolesList();
                    rolesList.addAll( Objects.isNull( userDao.getRoles() ) ? List.of() : userDao.getRoles() );

                    final var userDto = new User();
                    userDto.setCreated( localDateTimeToOffsetDateTime( userDao.getCreated() ) );
                    userDto.setForename( userDao.getForename() );
                    userDto.setSurname( userDao.getSurname() );
                    userDto.setEmail( userDao.getEmail() );
                    userDto.setUserId( userDao.getId() );
                    userDto.setDisplayName( userDao.getDisplayName() );
                    userDto.setRoles( rolesList );
                    userDto.setHasLinkedOneLogin( Objects.nonNull( userDao.getOneLoginData() ) );
                    userDto.setIsPrivateBetaUser( userDao.isPrivateBetaUser() );
                    return userDto;
                } )
                .collect( Collectors.toList() );
    }


}
