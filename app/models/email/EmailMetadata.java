package models.email;

import java.util.Optional;

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

    public EmailMetadata(String provider, boolean read, String folder) {
        this.provider = provider;
        this.read = read;
        this.folder = folder;
    }

    public static EmailMetadata merge(EmailMetadata local, EmailMetadata alien) {
        String provider = Optional.ofNullable(alien.provider).orElse(local.provider);
        boolean read = alien.read || local.read;
        String folder = Optional.ofNullable(alien.folder).orElse(local.folder);

        return new EmailMetadata(provider, read, folder);
    }

}
