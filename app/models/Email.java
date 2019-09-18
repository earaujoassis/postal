package models;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.OffsetDateTime;
import java.util.regex.Pattern;
import javax.mail.Address;
import org.jsoup.Jsoup;

import utils.RandomStringGenerator;

public class Email {

    public final static String ENTITY_NAME = "email";

    public static class Attributes {
        public final static String PUBLIC_ID = "public_id";
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
            Pattern urlPattern = Pattern.compile("((https?|ftp|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)", Pattern.CASE_INSENSITIVE);
            Pattern garbagePattern = Pattern.compile("([-_=]{2,})", Pattern.CASE_INSENSITIVE);
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

    public static class Presentation {
        public final String publicId;
        public final OffsetDateTime sentAt;
        public final String subject;
        public final String from;
        public final String fromPersonal;
        public final List<String> to;
        public final List<String> bcc;
        public final List<String> cc;
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

        public final static String METADATA_READ = "read";
        public final static String METADATA_FOLDER = "folder";

        public final boolean read;
        public final String folder;

        public Metadata() {
            this.read = false;
            this.folder = null;
        }

        public Metadata(Object hash) {
            Map<String, Object> metadata = (Map<String, Object>) hash;

            this.read = ((Boolean) metadata.get(METADATA_READ)).booleanValue();
            this.folder = (String) metadata.get(METADATA_FOLDER);
        }

    }

    public final String publicId;
    public final String bucketKey;
    public final OffsetDateTime sentAt;
    public final String subject;
    public final String from;
    public final String fromPersonal;
    public final List<String> to;
    public final List<String> bcc;
    public final List<String> cc;
    public final String replyTo;
    public final String bodyPlain;
    public final String bodyHTML;
    public final Metadata metadata;

    public Email(final String bucketKey,
                 final OffsetDateTime sentAt,
                 final String subject,
                 final String from,
                 final String fromPersonal,
                 final List<String> to,
                 final List<String> bcc,
                 final List<String> cc,
                 final String replyTo,
                 final String bodyPlain,
                 final String bodyHTML) {
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

        this.publicId = (String) email.get(Attributes.PUBLIC_ID);
        this.bucketKey = (String) email.get(Attributes.BUCKET_KEY);
        this.sentAt = OffsetDateTime.parse((String) email.get(Attributes.SENT_AT));
        this.subject = (String) email.get(Attributes.SUBJECT);
        this.from = (String) email.get(Attributes.FROM);
        this.fromPersonal = (String) email.get(Attributes.FROM_PERSONAL);
        this.to = (List<String>) email.get(Attributes.TO);
        this.bcc = (List<String>) email.get(Attributes.BCC);
        this.cc = (List<String>) email.get(Attributes.CC);
        this.replyTo = (String) email.get(Attributes.REPLY_TO);
        this.bodyPlain = (String) email.get(Attributes.BODY_PLAIN);
        this.bodyHTML = (String) email.get(Attributes.BODY_HTML);
        this.metadata = new Metadata(email.get(Attributes.METADATA));
    }

    public Summary toSummary() {
        return new Summary(this);
    }

    public Presentation toPresentation() {
        return new Presentation(this);
    }

    public static List<String> parseAddressList(List<Address> addresses) {
        List<String> newList = new ArrayList<String>();
        for (Address address : addresses) {
            newList.add(address.toString());
        }
        return newList;
    }

}
