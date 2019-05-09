package controllers;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;

import services.EmailService;
import models.Email;

public class ApiEmailController extends Controller {

    private EmailService emailService;

    @Inject
    public ApiEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    public Result status() {
        return ok(Json.toJson(this.emailService.status()));
    }

    public Result list(String folder) {
        List<Email.Summary> emails = new ArrayList<Email.Summary>();
        Iterable<Email> docs = docs = this.emailService.getAll(folder);

        for (Email doc : docs) {
            emails.add(new Email.Summary(doc));
        }

        return ok(Json.toJson(emails));
    }

    public Result show(String id) {
        Email doc = this.emailService.getOne(id);

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
                this.emailService.update(id, metadata);
                return noContent();
            }
        }
    }

}
