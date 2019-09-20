package models;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.IOException;

@JsonPropertyOrder(alphabetic=false)
public class User {

    public final static String ENTITY_NAME = "user";

    public static class Metadata {

        /**
         * RemoteStorage represents a S3 storage connection:
         * @accessKey: Access key for the AWS S3 user
         * @secretAccessKey: Secret key for the AWS S3 user
         * @kmsKey: KMS key to decrypt S3 objects/messages
         * @bucketName: the Bucket where the objects are stored
         * @bucketPrefix: the Bucket prefix (generally, the user defined by SES)
         */
        public static class RemoteStorage {
            public final String accessKey;
            public final String secretAccessKey;
            public final String kmsKey;
            public final String bucketName;
            public final String bucketPrefix;

            public RemoteStorage() {
                this.accessKey = null;
                this.secretAccessKey = null;
                this.kmsKey = null;
                this.bucketName = null;
                this.bucketPrefix = null;
            }
        }

        public final RemoteStorage remoteStorage;

        public Metadata() {
            this.remoteStorage = null;
        }

        public boolean hasRemoteStorage() {
            return this.remoteStorage != null;
        }

    }

    public static class Attributes {
        public final static String ID = "id";
        public final static String EXTERNAL_ID = "external_id";
        public final static String FULL_NAME = "full_name";
        public final static String METADATA = "metadata";
    }

    @JsonIgnore
    @SqlField(name = Attributes.ID)
    public final Integer _id;

    @JsonProperty("id")
    @SqlField(name = Attributes.EXTERNAL_ID)
    public final String externalId;

    @SqlField(name = Attributes.FULL_NAME)
    public final String fullName;

    @JsonIgnore
    @SqlField(name = Attributes.METADATA)
    public Metadata metadata;

    public User(final String externalId,
                final String fullName) {
        this._id = null;
        this.externalId = externalId;
        this.fullName = fullName;
        this.metadata = new Metadata();
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
                this.metadata = (Metadata) objectMapper.readValue(metadata.toString(), Metadata.class);
            } catch (IOException e) {
                e.printStackTrace();
                this.metadata = new Metadata();
            }
        } else {
            this.metadata = new Metadata();
        }
    }

    public final String toString() {
        return String.format("User::%s", this.fullName);
    }

}
