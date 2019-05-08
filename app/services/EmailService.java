package services;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.rethinkdb.net.Cursor;

import static com.rethinkdb.RethinkDB.r;

import models.Email;

@Singleton
public class EmailService {

    public final static String EMAILS_TABLE = "emails";
    private final StoreService store;

    @Inject
    public EmailService(StoreService store) {
        this.store = store;

        List<String> tables = r.db(this.store.dbName).tableList().run(this.store.conn);
        if (!tables.contains(EMAILS_TABLE)) {
            r.db(this.store.dbName).tableCreate(EMAILS_TABLE).run(this.store.conn);
            r.db(this.store.dbName).table(EMAILS_TABLE).indexCreate(Email.Attributes.PUBLIC_ID).run(this.store.conn);
            r.db(this.store.dbName).table(EMAILS_TABLE).indexCreate(Email.Attributes.BUCKET_KEY).run(this.store.conn);
            r.db(this.store.dbName).table(EMAILS_TABLE).indexCreate(Email.Attributes.SENT_AT).run(this.store.conn);
        }
    }

    public Map<String, Long> status() {
        Map<String, Long> status = new HashMap<String, Long>();
        status.put("total", (Long) r.db(this.store.dbName).table(EMAILS_TABLE)
            .count()
            .run(this.store.conn));
        status.put("unread", (Long) r.db(this.store.dbName).table(EMAILS_TABLE)
            .filter(row -> row.g(Email.Attributes.METADATA).g(Email.Metadata.METADATA_READ).eq(false))
            .count()
            .run(this.store.conn));

        return status;
    }

    public boolean isEmailAvailable(String key) {
        return ((Long) r.db(this.store.dbName)
            .table(EMAILS_TABLE)
            .filter(row -> row.g(Email.Attributes.BUCKET_KEY).eq(key))
            .count()
            .run(this.store.conn)).intValue() > 0;
    }

    public boolean insert(Email email) {
        Map<String, Object> result = r.db(this.store.dbName).table(EMAILS_TABLE)
            .insert(
                r.hashMap(Email.Attributes.PUBLIC_ID, email.publicId)
                    .with(Email.Attributes.BUCKET_KEY, email.bucketKey)
                    .with(Email.Attributes.BUCKET_OBJECT, email.bucketObject)
                    .with(Email.Attributes.SENT_AT, email.sentAt.toString())
                    .with(Email.Attributes.SUBJECT, email.subject)
                    .with(Email.Attributes.FROM, email.from)
                    .with(Email.Attributes.FROM_PERSONAL, email.fromPersonal)
                    .with(Email.Attributes.TO, email.to)
                    .with(Email.Attributes.BCC, email.bcc)
                    .with(Email.Attributes.CC, email.cc)
                    .with(Email.Attributes.REPLY_TO, email.replyTo)
                    .with(Email.Attributes.BODY_PLAIN, email.bodyPlain)
                    .with(Email.Attributes.BODY_HTML, email.bodyHTML)
                    .with(Email.Attributes.METADATA, r.hashMap(Email.Metadata.METADATA_READ, email.metadata.read)
                        .with(Email.Metadata.METADATA_FOLDER, email.metadata.folder)
               )
            )
            .run(this.store.conn);

        return ((Long) result.get("inserted")).intValue() > 0;
    }

    public boolean update(String id, Email.Metadata metadata) {
        Map<String, Object> result = r.db(this.store.dbName)
            .table(EMAILS_TABLE)
            .filter(row -> row.g(Email.Attributes.PUBLIC_ID).eq(id))
            .update(r.hashMap(Email.Attributes.METADATA, r.hashMap(Email.Metadata.METADATA_READ, metadata.read)
                .with(Email.Metadata.METADATA_FOLDER, metadata.folder)))
            .run(this.store.conn);

            return ((Long) result.get("replaced")).intValue() > 0;
    }

    public Iterable<Email> getAll(String folder) {
        List<Email> target = new ArrayList<>();
        Iterable<Object> entries = r.db(this.store.dbName)
            .table(EMAILS_TABLE)
            .orderBy().optArg("index", r.desc(Email.Attributes.SENT_AT))
            .limit(10)
            .run(this.store.conn);

        for (Object hash : entries) {
            target.add(new Email(hash));
        }

        return target;
    }

    public Email getOne(String id) {
        Object entry = ((Cursor) r.db(this.store.dbName)
            .table(EMAILS_TABLE)
            .filter(row -> row.g(Email.Attributes.PUBLIC_ID).eq(id))
            .run(this.store.conn)).toList().get(0);

        return new Email(entry);
    }

}
