package actors;

import akka.actor.*;
import akka.japi.*;
import com.typesafe.config.Config;
import play.api.Configuration;
import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Comparator;
import java.util.Properties;
import com.mongodb.client.model.Filters;
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
import org.apache.commons.mail.util.MimeMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actors.SyncMessagesActorProtocol.Request;
import services.DocumentStoreService;
import models.Email;

import static com.mongodb.client.model.Filters.eq;

public class SyncMessagesActor extends AbstractActor {

    private final static Logger logger = LoggerFactory.getLogger(SyncMessagesActor.class);
    private Config configuration;
    private DocumentStoreService documentStore;

    @Inject
    public SyncMessagesActor(Config configuration, DocumentStoreService documentStore) {
        this.configuration = configuration;
        this.documentStore = documentStore;
    }

    public static Props getProps() {
        return Props.create(SyncMessagesActor.class);
    }

    @Override
    public Receive createReceive() {
      return receiveBuilder()
        .match(Request.class, request -> {
            String reply = "all-done";
            if (request.isSync()) {
                syncMessages();
            }
            sender().tell(reply, self());
        })
        .build();
    }

    public void syncMessages() throws MessagingException {
        logger.info("Starting the sync messages task");
        logger.info("Attempting a connection with the ASW S3 Bucket");
        AWSCredentials credentials = new BasicAWSCredentials(configuration.getString("postal.aws.access_key"),
            configuration.getString("postal.aws.secret_access_key"));
        AmazonS3Encryption s3Encryption = AmazonS3EncryptionClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_EAST_1)
            .withCryptoConfiguration(new CryptoConfiguration(CryptoMode.EncryptionOnly).withAwsKmsRegion(Region.getRegion(Regions.US_EAST_1)))
            .withEncryptionMaterials(new KMSEncryptionMaterialsProvider(configuration.getString("postal.aws.kms_key")))
            .build();
        logger.info("Connection established; obtaining list of objects");
        ListObjectsV2Result bucket = s3Encryption.listObjectsV2(configuration.getString("postal.aws.bucket_name"),
            configuration.getString("postal.aws.bucket_prefix"));
        List<S3ObjectSummary> objectSummaries = bucket.getObjectSummaries();
        objectSummaries.sort(new Comparator<S3ObjectSummary>() {
            @Override
            public int compare(S3ObjectSummary obj1, S3ObjectSummary obj2) {
                // reverse sorting
                return obj2.getLastModified().compareTo(obj1.getLastModified());
            }
        });
        logger.info("Listing objects and checking for updates... (may take a while)");
        for (S3ObjectSummary objSummary : objectSummaries) {
            String messageKey = objSummary.getKey();
            logger.info(String.format("Checking for the following key: %s", messageKey));
            if (this.documentStore
                    .getCollection(DocumentStoreService.Collections.EMAILS)
                    .find(eq(Email.Attributes.BUCKET_KEY, messageKey))
                    .first() == null) {
                logger.info(String.format("Adding a new object with key %s", messageKey));
                String rawMessage = s3Encryption.getObjectAsString(configuration.getString("postal.aws.bucket_name"), messageKey);
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
                        rawMessage,
                        message.getSentDate(),
                        enrichedMessage.getSubject(),
                        enrichedMessage.getFrom(),
                        ((InternetAddress) message.getFrom()[0]).getPersonal(),
                        Email.parseAddressList(enrichedMessage.getTo()),
                        Email.parseAddressList(enrichedMessage.getBcc()),
                        Email.parseAddressList(enrichedMessage.getCc()),
                        enrichedMessage.getReplyTo(),
                        enrichedMessage.getPlainContent(),
                        enrichedMessage.getHtmlContent());
                    this.documentStore.getCollection("emails").insertOne(emailDocument.toDocument());
                } catch (Exception exc) {
                    logger.error(exc.toString());
                    exc.printStackTrace();
                    continue;
                }
            }
        }
        logger.info("Objects synced with the datastore");
        logger.info("Finishing the sync messages task");
    }

}
