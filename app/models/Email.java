package models;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import javax.mail.Address;
import org.bson.Document;

import utils.RandomStringGenerator;

public class Email {

    public static class Attributes {
        public final static String PUBLIC_ID = "publicId";
        public final static String BUCKET_KEY = "bucketKey";
        public final static String BUCKET_OBJECT = "bucketObject";
        public final static String SENT_AT = "sentAt";
        public final static String SUBJECT = "subject";
        public final static String FROM = "from";
        public final static String FROM_PERSONAL = "fromPersonal";
        public final static String TO = "to";
        public final static String BCC = "bcc";
        public final static String CC = "cc";
        public final static String REPLY_TO = "replyTo";
        public final static String BODY_PLAIN = "bodyPlain";
        public final static String BODY_HTML = "bodyHTML";
        public final static String METADATA = "metadata";
    }

    public static class Summary {
        public final String publicId;
        public final Date sentAt;
        public final String subject;
        public final String from;
        public final String fromPersonal;
        public final String bodyPlain;
        public final String bodyHTML;

        public Summary(final String publicId,
                       final Date sentAt,
                       final String subject,
                       final String from,
                       final String fromPersonal,
                       final String bodyPlain,
                       final String bodyHTML) {
            this.publicId = publicId;
            this.sentAt = sentAt;
            this.subject = subject;
            this.from = from;
            this.fromPersonal = fromPersonal;
            this.bodyPlain = bodyPlain;
            this.bodyHTML = bodyHTML;
        }

        public Summary(Document doc) {
            Email email = new Email(doc);

            this.publicId = email.publicId;
            this.sentAt = email.sentAt;
            this.subject = email.subject;
            this.from = email.from;
            this.fromPersonal = email.fromPersonal;
            this.bodyPlain = email.bodyPlain;
            this.bodyHTML = email.bodyHTML;
        }
    }

    public final String publicId;
    public final String bucketKey;
    public final String bucketObject;
    public final Date sentAt;
    public final String subject;
    public final String from;
    public final String fromPersonal;
    public final List<String> to;
    public final List<String> bcc;
    public final List<String> cc;
    public final String replyTo;
    public final String bodyPlain;
    public final String bodyHTML;
    public final Map<String, Object> metadata;

    public Email(final String bucketKey,
                 final String bucketObject,
                 final Date sentAt,
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
        this.bucketObject = bucketObject;
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

        this.publicId = RandomStringGenerator.generate(32);
        this.metadata = new HashMap<String, Object>();
        this.metadata.put("read", Boolean.FALSE);
    }

    public Email(Document doc) {
        this.publicId = doc.get(Attributes.PUBLIC_ID, String.class);
        this.bucketKey = doc.get(Attributes.BUCKET_KEY, String.class);
        this.bucketObject = doc.get(Attributes.BUCKET_OBJECT, String.class);
        this.sentAt = doc.get(Attributes.SENT_AT, Date.class);
        this.subject = doc.get(Attributes.SUBJECT, String.class);
        this.from = doc.get(Attributes.FROM, String.class);
        this.fromPersonal = doc.get(Attributes.FROM_PERSONAL, String.class);
        this.to = doc.get(Attributes.TO, ArrayList.class);
        this.bcc = doc.get(Attributes.BCC, ArrayList.class);
        this.cc = doc.get(Attributes.CC, ArrayList.class);
        this.replyTo = doc.get(Attributes.REPLY_TO, String.class);
        this.bodyPlain = doc.get(Attributes.BODY_PLAIN, String.class);
        this.bodyHTML = doc.get(Attributes.BODY_HTML, String.class);
        this.metadata = (Document) doc.get(Attributes.METADATA);
    }

    public Document toDocument() {
        return new Document(Attributes.PUBLIC_ID, this.publicId)
            .append(Attributes.BUCKET_KEY, this.bucketKey)
            .append(Attributes.BUCKET_OBJECT, this.bucketObject)
            .append(Attributes.SENT_AT, this.sentAt)
            .append(Attributes.SUBJECT, this.subject)
            .append(Attributes.FROM, this.from)
            .append(Attributes.FROM_PERSONAL, this.fromPersonal)
            .append(Attributes.TO, this.to)
            .append(Attributes.BCC, this.bcc)
            .append(Attributes.CC, this.cc)
            .append(Attributes.REPLY_TO, this.replyTo)
            .append(Attributes.BODY_PLAIN, this.bodyPlain)
            .append(Attributes.BODY_HTML, this.bodyHTML)
            .append(Attributes.METADATA, this.metadata);
    }

    public Summary toSummary() {
        return new Summary(this.publicId,
            this.sentAt,
            this.subject,
            this.from,
            this.fromPersonal,
            this.bodyPlain,
            this.bodyHTML);
    }

    public static List<String> parseAddressList(List<Address> addresses) {
        List<String> newList = new ArrayList<String>();
        for (Address address : addresses) {
            newList.add(address.toString());
        }
        return newList;
    }

}
