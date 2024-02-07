package uk.gov.companieshouse.accounts.user.mapper;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-07T12:10:59+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Eclipse Adoptium)"
)
@Component
public class UsersDtoDaoMapperImpl implements UsersDtoDaoMapper {

    @Override
    public User daoToDto(Users users) {
        if ( users == null ) {
            return null;
        }

        User user = new User();

        user.setUserId( users.getId() );
        user.setForename( users.getForename() );
        user.setSurname( users.getSurname() );
        user.setEmail( users.getEmail() );
        user.setDisplayName( users.getDisplayName() );
        Set<Role> set = users.getRoles();
        if ( set != null ) {
            user.setRoles( new LinkedHashSet<Role>( set ) );
        }

        return user;
    }

    @Override
    public Users dtoToDao(User user) {
        if ( user == null ) {
            return null;
        }

        Users users = new Users();

        users.setId( user.getUserId() );
        users.setForename( user.getForename() );
        users.setSurname( user.getSurname() );
        users.setDisplayName( user.getDisplayName() );
        users.setEmail( user.getEmail() );
        Set<Role> set = user.getRoles();
        if ( set != null ) {
            users.setRoles( new LinkedHashSet<Role>( set ) );
        }

        return users;
    }

    @Override
    public Role daoToDto(Role role) {
        if ( role == null ) {
            return null;
        }

        Role role1;

        switch ( role ) {
            case SUPERVISOR: role1 = Role.SUPERVISOR;
            break;
            case SUPPORT_MEMBER: role1 = Role.SUPPORT_MEMBER;
            break;
            case CSI_SUPPORT: role1 = Role.CSI_SUPPORT;
            break;
            case COMPANY_REFRESH: role1 = Role.COMPANY_REFRESH;
            break;
            case EXTENSIONS_ADMIN: role1 = Role.EXTENSIONS_ADMIN;
            break;
            case FOI: role1 = Role.FOI;
            break;
            case RESTRICTED_WORD: role1 = Role.RESTRICTED_WORD;
            break;
            case APPEALS_TEAM: role1 = Role.APPEALS_TEAM;
            break;
            case STRIKE_OFF_OBJECTIONS_ADMIN: role1 = Role.STRIKE_OFF_OBJECTIONS_ADMIN;
            break;
            case BADOS_USER: role1 = Role.BADOS_USER;
            break;
            case FES_ADMIN_USER: role1 = Role.FES_ADMIN_USER;
            break;
            case BULK_REFUNDS: role1 = Role.BULK_REFUNDS;
            break;
            case CHS_ORDERS_INVESTIGATOR: role1 = Role.CHS_ORDERS_INVESTIGATOR;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + role );
        }

        return role1;
    }

    @Override
    public Role dtoToDao(Role role) {
        if ( role == null ) {
            return null;
        }

        Role role1;

        switch ( role ) {
            case SUPERVISOR: role1 = Role.SUPERVISOR;
            break;
            case SUPPORT_MEMBER: role1 = Role.SUPPORT_MEMBER;
            break;
            case CSI_SUPPORT: role1 = Role.CSI_SUPPORT;
            break;
            case COMPANY_REFRESH: role1 = Role.COMPANY_REFRESH;
            break;
            case EXTENSIONS_ADMIN: role1 = Role.EXTENSIONS_ADMIN;
            break;
            case FOI: role1 = Role.FOI;
            break;
            case RESTRICTED_WORD: role1 = Role.RESTRICTED_WORD;
            break;
            case APPEALS_TEAM: role1 = Role.APPEALS_TEAM;
            break;
            case STRIKE_OFF_OBJECTIONS_ADMIN: role1 = Role.STRIKE_OFF_OBJECTIONS_ADMIN;
            break;
            case BADOS_USER: role1 = Role.BADOS_USER;
            break;
            case FES_ADMIN_USER: role1 = Role.FES_ADMIN_USER;
            break;
            case BULK_REFUNDS: role1 = Role.BULK_REFUNDS;
            break;
            case CHS_ORDERS_INVESTIGATOR: role1 = Role.CHS_ORDERS_INVESTIGATOR;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + role );
        }

        return role1;
    }
}
