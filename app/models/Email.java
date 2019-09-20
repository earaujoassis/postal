package models;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import javax.mail.Address;
import org.jsoup.Jsoup;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.IOException;

import utils.RandomStringGenerator;

public class Email {

    public final static String ENTITY_NAME = "email";

    @JsonPropertyOrder(alphabetic=false)
    public static class Summary {

        public final String publicId;
        public final OffsetDateTime sentAt;
        public final String subject;
        public final String from;
        public final String fromPersonal;
        public final Metadata metadata;

        private final String bodyPlain;
        private final String bodyHTML;

        public Summary(Email source) {
            this.publicId = source.publicId;
            this.sentAt = source.sentAt;
            this.subject = source.subject;
            this.from = source.from;
            this.fromPersonal = source.fromPersonal;
            this.bodyPlain = source.bodyPlain;
            this.bodyHTML = source.bodyHTML;
            this.metadata = source.metadata;
        }

        public String getExcerpt() {
            Pattern urlPattern = Pattern.compile(
                "((https?|ftp|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)",
                Pattern.CASE_INSENSITIVE);
            Pattern garbagePattern = Pattern.compile(
                "([-_=]{2,})",
                Pattern.CASE_INSENSITIVE);
            String excerpt;

            if (this.bodyHTML != null) {
                excerpt = Jsoup.parse(this.bodyHTML).text();
                return garbagePattern.matcher(excerpt).replaceAll("");
            } else if (this.bodyPlain != null) {
                excerpt = this.bodyPlain;
                return garbagePattern.matcher(urlPattern.matcher(excerpt).replaceAll("")).replaceAll("");
            }

            return "not available";
        }

    }

    @JsonPropertyOrder(alphabetic=false)
    public static class Presentation {

        public final String publicId;
        public final OffsetDateTime sentAt;
        public final String subject;
        public final String from;
        public final String fromPersonal;
        public final String to;
        public final String bcc;
        public final String cc;
        public final String replyTo;
        public final String bodyPlain;
        public final String bodyHTML;
        public final Metadata metadata;

        public Presentation(Email source) {
            this.publicId = source.publicId;
            this.sentAt = source.sentAt;
            this.subject = source.subject;
            this.from = source.from;
            this.fromPersonal = source.fromPersonal;
            this.to = source.to;
            this.bcc = source.bcc;
            this.cc = source.cc;
            this.replyTo = source.replyTo;
            this.bodyPlain = source.bodyPlain;
            this.bodyHTML = source.bodyHTML;
            this.metadata = source.metadata;
        }

    }

    public static class Metadata {

        public final boolean read;
        public final String folder;

        public Metadata() {
            this.read = false;
            this.folder = null;
        }

    }

    public static class Attributes {
        public final static String PUBLIC_ID = "public_id";
        public final static String USER_ID = "user_id";
        public final static String BUCKET_KEY = "bucket_key";
        public final static String SENT_AT = "sent_at";
        public final static String SUBJECT = "subject";
        public final static String FROM = "from_email";
        public final static String FROM_PERSONAL = "from_personal";
        public final static String TO = "to_email";
        public final static String BCC = "bcc_email";
        public final static String CC = "cc_email";
        public final static String REPLY_TO = "reply_to";
        public final static String BODY_PLAIN = "body_plain";
        public final static String BODY_HTML = "body_html";
        public final static String METADATA = "metadata";
    }

    @SqlField(name = Attributes.PUBLIC_ID)
    public final String publicId;

    @JsonIgnore
    @SqlField(name = Attributes.USER_ID)
    public final Integer userId;

    @SqlField(name = Attributes.BUCKET_KEY)
    public final String bucketKey;

    @SqlField(name = Attributes.SENT_AT)
    public final OffsetDateTime sentAt;

    @SqlField(name = Attributes.SUBJECT)
    public final String subject;

    @SqlField(name = Attributes.FROM)
    public final String from;

    @SqlField(name = Attributes.FROM_PERSONAL)
    public final String fromPersonal;

    @SqlField(name = Attributes.TO)
    public final String to;

    @SqlField(name = Attributes.BCC)
    public final String bcc;

    @SqlField(name = Attributes.CC)
    public final String cc;

    @SqlField(name = Attributes.REPLY_TO)
    public final String replyTo;

    @SqlField(name = Attributes.BODY_PLAIN)
    public final String bodyPlain;

    @SqlField(name = Attributes.BODY_HTML)
    public final String bodyHTML;

    @SqlField(name = Attributes.METADATA)
    public Metadata metadata;

    public Email(final Integer userId,
                 final String bucketKey,
                 final OffsetDateTime sentAt,
                 final String subject,
                 final String from,
                 final String fromPersonal,
                 final String to,
                 final String bcc,
                 final String cc,
                 final String replyTo,
                 final String bodyPlain,
                 final String bodyHTML) {
        this.userId = userId;
        this.bucketKey = bucketKey;
        this.sentAt = sentAt;
        this.subject = subject;
        this.from = from;
        this.fromPersonal = fromPersonal;
        this.to = to;
        this.bcc = bcc;
        this.cc = cc;
        this.replyTo = replyTo;
        this.bodyPlain = bodyPlain;
        this.bodyHTML = bodyHTML;

        this.publicId = RandomStringGenerator.generate(64);
        this.metadata = new Metadata();
    }

    public Email(Object hash) {
        Map<String, Object> email = (Map<String, Object>) hash;
        Object metadata;

        this.publicId = (String) email.get(Attributes.PUBLIC_ID);
        this.userId = Integer.valueOf(email.get(Attributes.USER_ID).toString());
        this.bucketKey = (String) email.get(Attributes.BUCKET_KEY);
        this.sentAt = OffsetDateTime.parse(
            email.get(Attributes.SENT_AT).toString(),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"));
        this.subject = (String) email.get(Attributes.SUBJECT);
        this.from = (String) email.get(Attributes.FROM);
        this.fromPersonal = (String) email.get(Attributes.FROM_PERSONAL);
        this.to = (String) email.get(Attributes.TO);
        this.bcc = (String) email.get(Attributes.BCC);
        this.cc = (String) email.get(Attributes.CC);
        this.replyTo = (String) email.get(Attributes.REPLY_TO);
        this.bodyPlain = (String) email.get(Attributes.BODY_PLAIN);
        this.bodyHTML = (String) email.get(Attributes.BODY_HTML);

        this.metadata = new Metadata();
        metadata = email.get(Attributes.METADATA);
        if (metadata != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                this.metadata = (Metadata) objectMapper.readValue(metadata.toString(), Metadata.class);
            } catch (IOException e) {
                e.printStackTrace();
                this.metadata = new Metadata();
            }
        }
    }

    public Summary toSummary() {
        return new Summary(this);
    }

    public Presentation toPresentation() {
        return new Presentation(this);
    }

    public static String parseAddressList(List<Address> addresses) {
        List<String> newList = new ArrayList<String>();
        for (Address address : addresses) {
            newList.add(address.toString());
        }
        return String.join(", ", newList);
    }

}
