package models.user;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic=false)
public class UserMetadata {

    /**
     * RemoteStorage represents a S3 storage connection:
     * @accessKey: Access key for the AWS S3 user
     * @secretAccessKey: Secret key for the AWS S3 user
     * @kmsKey: KMS key to decrypt S3 objects/messages
     * @bucketName: the Bucket where the objects are stored
     * @bucketPrefix: the Bucket prefix (generally, the user defined by SES)
     */
    public class RemoteStorage {
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

    public UserMetadata() {
        this.remoteStorage = null;
    }

    public boolean hasRemoteStorage() {
        return this.remoteStorage != null;
    }

}
