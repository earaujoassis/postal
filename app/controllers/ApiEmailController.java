package controllers;

import javax.inject.*;
import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;
import org.bson.Document;
import org.bson.conversions.Bson;

import services.DocumentStoreService;
import models.Email;

import static com.mongodb.client.model.Projections.fields​;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.orderBy;
import static com.mongodb.client.model.Sorts.descending;

public class ApiEmailController extends Controller {

    private DocumentStoreService documentStore;

    @Inject
    public ApiEmailController(DocumentStoreService documentStore) {
        this.documentStore = documentStore;
    }

    public Result list(String folder) {
        Bson projection = fields(include(Email.Attributes.PUBLIC_ID,
            Email.Attributes.SENT_AT,
            Email.Attributes.SUBJECT,
            Email.Attributes.FROM,
            Email.Attributes.FROM_PERSONAL,
            Email.Attributes.BODY_PLAIN,
            Email.Attributes.BODY_HTML,
            Email.Attributes.METADATA), excludeId());
        Bson sorting = orderBy(descending(Email.Attributes.SENT_AT));
        List<Email.Summary> emails = new ArrayList<Email.Summary>();
        Iterable<Document> docs;

        if (folder == null) {
            docs = this.documentStore
                .getCollection(DocumentStoreService.Collections.EMAILS)
                .find()
                .projection(projection)
                .sort(sorting);
        } else {
            docs = this.documentStore
                .getCollection(DocumentStoreService.Collections.EMAILS)
                .find(new Document(Email.Attributes.METADATA, new Document(Email.Metadata.METADATA_FOLDER, folder)))
                .projection(projection)
                .sort(sorting);
        }

        for (Document doc : docs) {
            emails.add(new Email.Summary(doc));
        }

        return ok(Json.toJson(emails));
    }

    public Result show(String id) {
        Document doc = this.documentStore
            .getCollection(DocumentStoreService.Collections.EMAILS)
            .find(eq(Email.Attributes.PUBLIC_ID, id))
            .projection(fields(exclude(Email.Attributes.BUCKET_KEY, Email.Attributes.BUCKET_OBJECT), excludeId()))
            .first();

        if (doc == null) {
            return ok(Json.toJson(null));
        }

        return ok(Json.toJson(new Email.Presentation(doc)));
    }

    public Result update(String id) {
        Email.Metadata metadata;
        JsonNode temporaryMetadata;
        JsonNode json = request().body().asJson();
        ObjectMapper objectMapper = new ObjectMapper();

        if (json == null) {
            return badRequest("Expecting JSON data");
        } else {
            temporaryMetadata = json.findValue(Email.ENTITY_NAME).findValue(Email.Attributes.METADATA);
            if (temporaryMetadata == null) {
                return badRequest(String.format("Only `%s` is updatable", Email.Attributes.METADATA));
            } else {
                metadata = (Email.Metadata) objectMapper.convertValue(temporaryMetadata, Email.Metadata.class);
                this.documentStore
                    .getCollection(DocumentStoreService.Collections.EMAILS)
                    .updateOne(eq(Email.Attributes.PUBLIC_ID, id),
                        new Document("$set", new Document(Email.Attributes.METADATA, metadata.toDocument())));
                return noContent();
            }
        }
    }

}
