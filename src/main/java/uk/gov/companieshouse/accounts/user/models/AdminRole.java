package uk.gov.companieshouse.accounts.user.models;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "adminRole")
public class AdminRole implements Serializable {

    private static final long serialVersionUID = 9010542196922507828L;

    @Id
    private String id;

    @Field("permissions")
    private List<String> permissions ;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(final List<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "Role {" +
            "id='" + id + "'," +
             "permissions='" + permissions + 
             "'}";

    } 
}