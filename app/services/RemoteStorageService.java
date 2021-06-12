package services;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

import repositories.EmailRepository;
import models.user.User;
import models.user.UserMetadata;
import models.email.Email;
import models.email.EmailFolder;
import models.email.EmailMetadata;
import models.providers.RemoteStorage;
import models.providers.Provider;

public class RemoteStorageService implements IMailer {

    private EmailRepository emailRepository;

    @Inject
    public RemoteStorageService(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    public void retrieveNewEmailMessagesForUser(User user, int batchSize) {
        AWSCredentials credentials;
        AmazonS3Encryption s3Encryption;
        ListObjectsV2Result bucket;
        List<S3ObjectSummary> objectSummaries;
        int batchItemsCounter = 0;

        if (user == null || !user.metadata.hasRemoteStorage()) {
            return;
        }

        RemoteStorage remoteSettings = user.metadata.remoteStorage;

        credentials = new BasicAWSCredentials(remoteSettings.accessKey, remoteSettings.secretAccessKey);
        s3Encryption = AmazonS3EncryptionClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_EAST_1)
            .withCryptoConfiguration(new CryptoConfiguration(CryptoMode.EncryptionOnly).withAwsKmsRegion(Region.getRegion(Regions.US_EAST_1)))
            .withEncryptionMaterials(new KMSEncryptionMaterialsProvider(remoteSettings.kmsKey))
            .build();
        bucket = s3Encryption.listObjectsV2(remoteSettings.bucketName, remoteSettings.bucketPrefix);
        objectSummaries = bucket.getObjectSummaries();
        objectSummaries.sort(new Comparator<S3ObjectSummary>() {
            @Override
            public int compare(S3ObjectSummary obj1, S3ObjectSummary obj2) {
                // descending ordering
                return obj1.getLastModified().compareTo(obj2.getLastModified());
            }
        });
        Collections.reverse(Arrays.asList(objectSummaries));
        for (S3ObjectSummary objSummary : objectSummaries) {
            if (batchItemsCounter >= batchSize) {
                break;
            }

            String messageKey = objSummary.getKey();
            if (!this.emailRepository.isEmailAvailable(user._id, messageKey)) {
                batchItemsCounter++;
                String rawMessage = s3Encryption.getObjectAsString(remoteSettings.bucketName, messageKey);
                Session session = Session.getInstance(new Properties());
                InputStream inputStream = new ByteArrayInputStream(rawMessage.getBytes());
                EmailMetadata metadata = new EmailMetadata(Provider.REMOTE_STORAGE, EmailFolder.INBOX);
                Email emailDocument;
                MimeMessage message;
                MimeMessageParser enrichedMessage;

                try {
                    message = new MimeMessage(session, inputStream);
                    enrichedMessage = new MimeMessageParser(message);
                    enrichedMessage.parse();
                    emailDocument = new Email(
                        user._id,
                        messageKey,
                        message.getSentDate().toInstant().atOffset(ZoneOffset.UTC),
                        enrichedMessage.getSubject(),
                        enrichedMessage.getFrom(),
                        ((InternetAddress) message.getFrom()[0]).getPersonal(),
                        Email.parseAddressList(enrichedMessage.getTo()),
                        Email.parseAddressList(enrichedMessage.getBcc()),
                        Email.parseAddressList(enrichedMessage.getCc()),
                        enrichedMessage.getReplyTo(),
                        enrichedMessage.getPlainContent(),
                        enrichedMessage.getHtmlContent(),
                        metadata);
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
