package models.providers;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @JsonIgnore
    public boolean isValid() {
        String[] fields = new String[]{this.accessKey, this.secretAccessKey, this.kmsKey, this.bucketName, this.bucketPrefix};
        Stream<String> fieldsStream = Arrays.stream(fields);
        Pattern pattern = Pattern.compile("([a-zA-Z0-9+-/])*");

        return fieldsStream.map(s -> Optional.ofNullable(s).orElseGet(() -> ""))
            .map(s -> pattern.matcher(s).matches())
            .allMatch(v -> v == true);
    }

}
