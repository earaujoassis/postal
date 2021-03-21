package models.email;

public class EmailMetadata {

    public final String provider;
    public final boolean read;
    public final String folder;

    public EmailMetadata() {
        this.provider = null;
        this.read = false;
        this.folder = null;
    }

    public EmailMetadata(String provider, String folder) {
        this.provider = provider;
        this.read = false;
        this.folder = folder;
    }

}
