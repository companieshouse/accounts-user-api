package uk.gov.companieshouse.accounts.user.models;

import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.io.Serializable;

public class OneLoginDataDao implements Serializable {

    @Serial
    private static final long serialVersionUID = 1234567L;
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