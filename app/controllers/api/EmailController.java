package controllers.api;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;

import actions.Authentication;
import repositories.EmailRepository;
import models.email.Email;
import models.email.EmailMetadata;
import models.email.EmailPresentation;
import models.email.EmailSummary;

@Authentication(enforce = true)
public class EmailController extends Controller {

    @Inject EmailRepository emailRepository;

    public Result status() {
        return ok(Json.toJson(this.emailRepository.status()));
    }

    public Result list(String folder) {
        List<EmailSummary> emails = new ArrayList<EmailSummary>();
        Iterable<Email> docs = docs = this.emailRepository.getAll(folder);

        for (Email doc : docs) {
            emails.add(new EmailSummary(doc));
        }

        return ok(Json.toJson(emails));
    }

    public Result show(String id) {
        Email doc = this.emailRepository.getByPublicId(id);

        if (doc == null) {
            return ok(Json.parse("null"));
        }

        return ok(Json.toJson(new EmailPresentation(doc)));
    }

    public Result update(String id) {
        EmailMetadata metadata;
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
                metadata = (EmailMetadata) objectMapper.convertValue(temporaryMetadata, EmailMetadata.class);
                this.emailRepository.update(id, metadata);
                return noContent();
            }
        }
    }

}