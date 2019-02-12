package services;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import java.util.List;
import javax.mail.internet.MimeMessage;

public class MailerService {
    private AmazonS3 s3;
    private Bucket bucket;

    public MailerService(String bucketName) {
        this.s3 = AmazonS3ClientBuilder.defaultClient();
        this.bucket = null;

        if (this.s3.doesBucketExist(bucketName)) {
            System.out.format("Bucket %s already exists.\n", bucketName);
            this.bucket = getBucket(this.s3, bucketName);
        } else {
            try {
                this.bucket = s3.createBucket(bucketName);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
    }

    private static Bucket getBucket(AmazonS3 s3, String bucketName) {
        List<Bucket> buckets = s3.listBuckets();
        Bucket namedBucket = null;
        for (Bucket b : buckets) {
            if (b.getName().equals(bucketName)) {
                namedBucket = b;
            }
        }
        return namedBucket;
    }
}
