package controllers;

import javax.inject.*;
import play.mvc.*;
import play.libs.Json;
import java.util.List;
import java.util.ArrayList;
import org.bson.Document;

import services.DocumentStoreService;
import models.Email;

import static com.mongodb.client.model.Projections.fields​;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Filters.eq;

public class ApiEmailController extends Controller {

    private DocumentStoreService documentStore;

    @Inject
    public ApiEmailController(DocumentStoreService documentStore) {
        this.documentStore = documentStore;
    }

    public Result list() {
        Iterable<Document> docs = this.documentStore
            .getCollection(DocumentStoreService.Collections.EMAILS)
            .find()
            .projection(fields(include(Email.Attributes.PUBLIC_ID,
                Email.Attributes.SENT_AT,
                Email.Attributes.SUBJECT,
                Email.Attributes.FROM,
                Email.Attributes.FROM_PERSONAL,
                Email.Attributes.BODY_PLAIN,
                Email.Attributes.BODY_HTML), excludeId()));
        List<Email.Summary> emails = new ArrayList<Email.Summary>();

        for (Document doc : docs) {
            emails.add(new Email.Summary(doc));
        }

        return ok(Json.toJson(emails));
    }

    public Result show(String id) {
        Document doc = this.documentStore
            .getCollection(DocumentStoreService.Collections.EMAILS)
            .find(eq(Email.Attributes.PUBLIC_ID, id))
            .projection(fields(exclude(Email.Attributes.BUCKET_KEY,
                Email.Attributes.BUCKET_OBJECT), excludeId()))
            .first();

        if (doc == null) {
            return ok(Json.toJson(null));
        }

        return ok(Json.toJson(new Email(doc)));
    }

}
