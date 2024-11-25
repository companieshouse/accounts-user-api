package uk.gov.companieshouse.accounts.user.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
public class Users implements Serializable {

    @Serial
    private static final long serialVersionUID = 1234567L;

    @Id
    private String id;

    @Field("locale")
    private String locale;

    @Field("forename")
    private String forename;

    @Field("surname")
    private String surname;

    @Field("display_name")
    private String displayName;

    @Field("email")
    private String email;

    @Field("roles")
    private List<String> roles;

    @Field("created")
    private LocalDateTime created;

    @Field("updated")
    @LastModifiedDate
    private LocalDateTime updated;

    @Field("one_login_data")
    private OneLoginDataDao oneLoginData;

    @Field("private_beta_user")
    private boolean privateBetaUser;

    @Field("one_login_link_removed_by")
    private String oneLoginLinkRemovedBy;

    @Field("one_login_link_removed_at")
    private LocalDateTime oneLoginLinkRemovedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }


    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setOneLoginData(OneLoginDataDao oneLoginData) {
        this.oneLoginData = oneLoginData;
    }

    public OneLoginDataDao getOneLoginData() {
        return oneLoginData;
    }

    public boolean isPrivateBetaUser() {
        return privateBetaUser;
    }

    public void setPrivateBetaUser(boolean privateBetaUser) {
        this.privateBetaUser = privateBetaUser;
    }

    public String getOneLoginLinkRemovedBy() {
        return oneLoginLinkRemovedBy;
    }

    public void setOneLoginLinkRemovedBy(String oneLoginLinkRemovedBy) {
        this.oneLoginLinkRemovedBy = oneLoginLinkRemovedBy;
    }

    public LocalDateTime getOneLoginLinkRemovedAt() {
        return oneLoginLinkRemovedAt;
    }

    public void setOneLoginLinkRemovedAt(LocalDateTime oneLoginLinkRemovedAt) {
        this.oneLoginLinkRemovedAt = oneLoginLinkRemovedAt;
    }

    @Override
    public String toString() {
        return "Users{" +
                "id='" + id + '\'' +
                ", locale='" + locale + '\'' +
                ", forename='" + forename + '\'' +
                ", surname='" + surname + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", created=" + created +
                ", updated=" + updated +
                ", oneLoginData=" + oneLoginData +
                ", privateBetaUser=" + privateBetaUser +
                ", oneLoginLinkRemovedBy=" + oneLoginLinkRemovedBy +
                ", oneLoginLinkRemovedAt=" + oneLoginLinkRemovedAt +
                '}';
    }
}