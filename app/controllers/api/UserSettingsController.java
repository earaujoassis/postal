package controllers.api;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import play.mvc.Http;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import actions.Authentication;
import actions.AuthenticationAttrs;
import repositories.UserRepository;
import models.user.User;
import models.user.UserMetadata;

@Authentication(enforce = true)
public class UserSettingsController extends Controller {

    @Inject UserRepository userRepository;

    public Result show() {
        User user = request().attrs().get(AuthenticationAttrs.USER);
        return ok(Json.toJson(streamSettings(user)));
    }

    public Result update() {
        UserMetadata metadata;
        JsonNode temporaryMetadata;
        JsonNode json = request().body().asJson();
        ObjectMapper objectMapper = new ObjectMapper();
        User user = request().attrs().get(AuthenticationAttrs.USER);

        if (json == null) {
            return badRequest("Expecting JSON data");
        } else {
            temporaryMetadata = json.findValue(User.ENTITY_NAME).findValue(User.Attributes.METADATA);
            if (temporaryMetadata == null) {
                return badRequest(String.format("Only `%s` is updatable", User.Attributes.METADATA));
            } else {
                metadata = (UserMetadata) objectMapper.convertValue(temporaryMetadata, UserMetadata.class);
                if (metadata.isValid()) {
                    this.userRepository.update(user.externalId, metadata);
                    return ok(Json.toJson(streamSettings(user, metadata)));
                } else {
                    return status(Http.Status.NOT_ACCEPTABLE, "Metada is not valid");
                }
            }
        }
    }

    private ObjectNode streamSettings(User user) {
        ObjectNode result = Json.newObject();
        result.put("settings", Json.toJson(user.metadata));
        result.put("profile", Json.toJson(user));
        return result;
    }

    private ObjectNode streamSettings(User user, UserMetadata metadata) {
        ObjectNode result = Json.newObject();
        result.put("settings", Json.toJson(metadata));
        result.put("profile", Json.toJson(user));
        return result;
    }

}
