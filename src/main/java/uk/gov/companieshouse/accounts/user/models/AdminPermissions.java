package uk.gov.companieshouse.accounts.user.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "admin_permissions")
public class AdminPermissions {
    @Id
    @Field("_id")
    private String id;

    @Field("entra_group_id")
    private String entraGroupId;

    @Field("group_name")
    private String groupName;

    @Field("permissions")
    private List<String> permissions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntraGroupId() {
        return entraGroupId;
    }

    public void setEntraGroupId(String entraGroupId) {
        this.entraGroupId = entraGroupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}


