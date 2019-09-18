package services;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Properties;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.mail.util.MimeMessageParser;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.time.ZoneOffset;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Encryption;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.CryptoMode;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import services.AppConfig;
import repositories.EmailRepository;
import models.User;
import models.Email;

public class RemoteStorageService implements IMailer {

    private AppConfig conf;
    private EmailRepository emailRepository;

    @Inject
    public RemoteStorageService(AppConfig conf, EmailRepository emailRepository) {
        this.conf = conf;
        this.emailRepository = emailRepository;
    }

    public void retrieveNewEmailMessagesForUser(User user) {
        AWSCredentials credentials;
        AmazonS3Encryption s3Encryption;
        ListObjectsV2Result bucket;
        List<S3ObjectSummary> objectSummaries;

        if (user == null || !user.metadata.hasRemoteStorage()) {
            return;
        }

        Map<String, String> remoteSettings = user.metadata.getRemoteStorageSettings();

        credentials = new BasicAWSCredentials(remoteSettings.get("access_key"), remoteSettings.get("secret_access_key"));
        s3Encryption = AmazonS3EncryptionClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_EAST_1)
            .withCryptoConfiguration(new CryptoConfiguration(CryptoMode.EncryptionOnly).withAwsKmsRegion(Region.getRegion(Regions.US_EAST_1)))
            .withEncryptionMaterials(new KMSEncryptionMaterialsProvider(remoteSettings.get("kms_key")))
            .build();
        bucket = s3Encryption.listObjectsV2(remoteSettings.get("bucket_name"), remoteSettings.get("bucket_prefix"));
        objectSummaries = bucket.getObjectSummaries();
        objectSummaries.sort(new Comparator<S3ObjectSummary>() {
            @Override
            public int compare(S3ObjectSummary obj1, S3ObjectSummary obj2) {
                // reverse sorting
                return obj2.getLastModified().compareTo(obj1.getLastModified());
            }
        });
        for (S3ObjectSummary objSummary : objectSummaries) {
            String messageKey = objSummary.getKey();
            if (!this.emailRepository.isEmailAvailable(messageKey)) {
                String rawMessage = s3Encryption.getObjectAsString(conf.getValue("aws.bucket_name"), messageKey);
                Session session = Session.getInstance(new Properties());
                InputStream inputStream = new ByteArrayInputStream(rawMessage.getBytes());
                MimeMessage message;
                MimeMessageParser enrichedMessage;
                Email emailDocument;

                try {
                    message = new MimeMessage(session, inputStream);
                    enrichedMessage = new MimeMessageParser(message);
                    enrichedMessage.parse();
                    emailDocument = new Email(messageKey,
                        message.getSentDate().toInstant().atOffset(ZoneOffset.UTC),
                        enrichedMessage.getSubject(),
                        enrichedMessage.getFrom(),
                        ((InternetAddress) message.getFrom()[0]).getPersonal(),
                        Email.parseAddressList(enrichedMessage.getTo()),
                        Email.parseAddressList(enrichedMessage.getBcc()),
                        Email.parseAddressList(enrichedMessage.getCc()),
                        enrichedMessage.getReplyTo(),
                        enrichedMessage.getPlainContent(),
                        enrichedMessage.getHtmlContent());
                    this.emailRepository.insert(emailDocument);
                } catch (Exception exc) {
                    // Item is not created in the datastore; it can be retrieved another time
                    exc.printStackTrace();
                    continue;
                }
            } else {
                // It reached a document that was already imported; break loop
                break;
            }
        }
    }

}
