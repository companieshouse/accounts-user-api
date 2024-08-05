package uk.gov.companieshouse.accounts.user.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Document(collection = "oauth2_authorisations")
public class Oauth2AuthorisationsDao {

    @Id
    @JsonProperty("_id")
    private String id;

    @Field("type")
    private String type;

    @Field("code")
    private String code;

    @Field("token_permissions")
    private Map<String, String> tokenPermissions;

    @Field("user_details")
    private UserDetailsDao userDetails;

    @Field("code_associated_data")
    private String codeAssociatedData;

    @Field("requested_scope")
    private String requestedScope;

    @Field("permissions")
    private Map<String, Integer> permissions;

    @Field("code_valid_until")
    private long codeValidUntil;

    @Field("client_id")
    private String clientID;

    @Field("identity_provider")
    private String identityProvider;

    @Field("refresh_token_id")
    private String refreshTokenID;

    @Field("refresh_token_password")
    private String refreshTokenPassword;

    @Field("token")
    private String token;

    @Field("token_valid_until")
    private long tokenValidUntil;

    @Field("authorisation_granted_on")
    private LocalDateTime authorisationGrantedOn;

    @Field("revoked")
    private Map<String, String> revoked;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getTokenPermissions() {
        return tokenPermissions;
    }

    public void setTokenPermissions(Map<String, String> tokenPermissions) {
        this.tokenPermissions = tokenPermissions;
    }

    public UserDetailsDao getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetailsDao userDetails) {
        this.userDetails = userDetails;
    }

    public String getCodeAssociatedData() {
        return codeAssociatedData;
    }

    public void setCodeAssociatedData(String codeAssociatedData) {
        this.codeAssociatedData = codeAssociatedData;
    }

    public String getRequestedScope() {
        return requestedScope;
    }

    public void setRequestedScope(String requestedScope) {
        this.requestedScope = requestedScope;
    }

    public Map<String, Integer> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Integer> permissions) {
        this.permissions = permissions;
    }

    public long getCodeValidUntil() {
        return codeValidUntil;
    }

    public void setCodeValidUntil(long codeValidUntil) {
        this.codeValidUntil = codeValidUntil;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(String identityProvider) {
        this.identityProvider = identityProvider;
    }

    public String getRefreshTokenID() {
        return refreshTokenID;
    }

    public void setRefreshTokenID(String refreshTokenID) {
        this.refreshTokenID = refreshTokenID;
    }

    public String getRefreshTokenPassword() {
        return refreshTokenPassword;
    }

    public void setRefreshTokenPassword(String refreshTokenPassword) {
        this.refreshTokenPassword = refreshTokenPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTokenValidUntil() {
        return tokenValidUntil;
    }

    public void setTokenValidUntil(long tokenValidUntil) {
        this.tokenValidUntil = tokenValidUntil;
    }

    public void setAuthorisationGrantedOn(LocalDateTime authorisationGrantedOn) {
        this.authorisationGrantedOn = authorisationGrantedOn;
    }

    public Map<String, String> getRevoked() {
        return revoked;
    }

    public void setRevoked(Map<String, String> revoked) {
        this.revoked = revoked;
    }

    @JsonInclude(Include.NON_NULL)
    public Long getAuthorisationGrantedOn() {
        return authorisationGrantedOn == null ? null : 
                authorisationGrantedOn.atZone(ZoneId.of("GMT")).toInstant().toEpochMilli();
    }
}
