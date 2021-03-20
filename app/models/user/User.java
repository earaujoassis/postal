package models.user;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.IOException;

import models.SqlField;

@JsonPropertyOrder(alphabetic=false)
public class User {

    public final static String ENTITY_NAME = "user";

    public static class Attributes {
        public final static String ID = "id";
        public final static String EXTERNAL_ID = "external_id";
        public final static String FULL_NAME = "full_name";
        public final static String METADATA = "metadata";
    }

    @JsonIgnore
    @SqlField(name = Attributes.ID, settable = false)
    public Integer _id;

    @JsonProperty("id")
    @SqlField(name = Attributes.EXTERNAL_ID)
    public final String externalId;

    @SqlField(name = Attributes.FULL_NAME)
    public final String fullName;

    @JsonIgnore
    @SqlField(name = Attributes.METADATA)
    public UserMetadata metadata;

    public User(final String externalId,
                final String fullName) {
        this._id = null;
        this.externalId = externalId;
        this.fullName = fullName;
        this.metadata = new UserMetadata();
    }

    public User(Object hash) {
        Map<String, Object> user = (Map<String, Object>) hash;
        Object metadata;

        this._id = Integer.valueOf(user.get(Attributes.ID).toString());
        this.externalId = (String) user.get(Attributes.EXTERNAL_ID);
        this.fullName = (String) user.get(Attributes.FULL_NAME);

        metadata = user.get(Attributes.METADATA);
        if (metadata != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                this.metadata = (UserMetadata) objectMapper.readValue(metadata.toString(), UserMetadata.class);
            } catch (IOException e) {
                e.printStackTrace();
                this.metadata = new UserMetadata();
            }
        } else {
            this.metadata = new UserMetadata();
        }
    }

    public final String toString() {
        return String.format("User::%s", this.fullName);
    }

}
