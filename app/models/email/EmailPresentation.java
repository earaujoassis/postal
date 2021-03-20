package models.email;

import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic=false)
public class EmailPresentation {

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
    public final EmailMetadata metadata;

    public EmailPresentation(Email source) {
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
