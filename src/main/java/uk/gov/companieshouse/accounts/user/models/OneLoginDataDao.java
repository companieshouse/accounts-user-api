package uk.gov.companieshouse.accounts.user.models;

import org.springframework.data.mongodb.core.mapping.Field;

public class OneLoginDataDao {
    @Field("user_id")
    private String oneLoginUserId;

    public String getOneLoginUserId() {
        return oneLoginUserId;
    }

    public void setOneLoginUserId(String oneLoginUserId) {
        this.oneLoginUserId = oneLoginUserId;
    }

    @Override
    public String toString() {
        return "OneLoginDataDao{" +
                "oneLoginUserId='" + oneLoginUserId + '\'' +
                '}';
    }
}