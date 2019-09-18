package models;

import java.util.Map;

public class User {

    public final static String ENTITY_NAME = "user";

    public static class Attributes {
        public final static String EXTERNAL_ID = "external_id";
        public final static String FULL_NAME = "full_name";
        public final static String METADATA = "metadata";
    }

    public static class Metadata {
        public Metadata(Object json) {
        }

        public boolean hasRemoteStorage() {
            // TODO Implement it
            return false;
        }

        /**
         * It should return a hashmap with the following attributes:
         *      access_key: <Access key for the AWS S3 user>
         *      secret_access_key: <Secret key for the AWS S3 user>
         *      kms_key: <KMS key to decrypt S3 objects/messages>
         *      bucket_name: <the Bucket where the objects are stored>
         *      bucket_prefix: <the Bucket prefix (generally, the user defined by SES)>
         */
        public Map<String, String> getRemoteStorageSettings() {
            return null;
        }
    }

    public final String externalId;
    public final String fullName;
    public final Metadata metadata;

    public User(final String externalId,
                final String fullName) {
        this.externalId = externalId;
        this.fullName = fullName;
        this.metadata = new Metadata(null);
    }

    public User(Object hash) {
        Map<String, Object> user = (Map<String, Object>) hash;

        this.externalId = (String) user.get(Attributes.EXTERNAL_ID);
        this.fullName = (String) user.get(Attributes.FULL_NAME);
        this.metadata = new Metadata(user.get(Attributes.METADATA));
    }

}
