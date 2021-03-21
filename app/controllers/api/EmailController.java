package controllers.api;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.ArrayList;

import actions.Authentication;
import actions.AuthenticationAttrs;
import repositories.EmailRepository;
import models.email.Email;
import models.email.EmailMetadata;
import models.email.EmailPresentation;
import models.email.EmailSummary;
import models.user.User;

@Authentication(enforce = true)
public class EmailController extends Controller {

    @Inject EmailRepository emailRepository;

    public Result status() {
        User user = request().attrs().get(AuthenticationAttrs.USER);
        return ok(Json.toJson(this.emailRepository.status(user._id)));
    }

    public Result list(String folder) {
        User user = request().attrs().get(AuthenticationAttrs.USER);
        List<EmailSummary> emails = new ArrayList<EmailSummary>();
        Iterable<Email> docs;
        int totalCount;

        if (folder == null) {
            docs = this.emailRepository.getAll(user._id);
            totalCount = this.emailRepository.getCount(user._id);
        } else {
            docs = this.emailRepository.getAll(user._id, folder);
            totalCount = this.emailRepository.getCount(user._id, folder);
        }

        for (Email doc : docs) {
            emails.add(new EmailSummary(doc));
        }

        ObjectNode result = Json.newObject();
        result.put("emails", Json.toJson(emails));
        result.put("total", Json.toJson(totalCount));

        return ok(Json.toJson(result));
    }

    public Result show(String id) {
        User user = request().attrs().get(AuthenticationAttrs.USER);
        Email doc = this.emailRepository.getByPublicId(user._id, id);

        if (doc == null) {
            return ok(Json.parse("null"));
        }

        return ok(Json.toJson(new EmailPresentation(doc)));
    }

    public Result update(String id) {
        EmailMetadata localMetadata, alienMetadata, merged;
        JsonNode temporaryMetadata;
        JsonNode json = request().body().asJson();
        ObjectMapper objectMapper = new ObjectMapper();
        User user = request().attrs().get(AuthenticationAttrs.USER);

        if (json == null) {
            return badRequest("Expecting JSON data");
        } else {
            temporaryMetadata = json.findValue(Email.ENTITY_NAME).findValue(Email.Attributes.METADATA);
            if (temporaryMetadata == null) {
                return badRequest(String.format("Only `%s` is updatable", Email.Attributes.METADATA));
            } else {
                alienMetadata = (EmailMetadata) objectMapper.convertValue(temporaryMetadata, EmailMetadata.class);
                localMetadata = this.emailRepository.getByPublicId(user._id, id).metadata;
                merged = EmailMetadata.merge(localMetadata, alienMetadata);
                this.emailRepository.update(user._id, id, merged);
                return noContent();
            }
        }
    }

}
