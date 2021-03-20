package models.email;

import java.util.regex.Pattern;
import java.time.OffsetDateTime;
import org.jsoup.Jsoup;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic=false)
public class EmailSummary {

    public final String publicId;
    public final OffsetDateTime sentAt;
    public final String subject;
    public final String from;
    public final String fromPersonal;
    public final EmailMetadata metadata;

    private final String bodyPlain;
    private final String bodyHTML;

    public EmailSummary(Email source) {
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
