package controllers.api;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.node.ObjectNode;

import actions.Authentication;
import actions.AuthenticationAttrs;
import repositories.UserRepository;
import models.user.User;

@Authentication(enforce = true)
public class UserSettingsController extends Controller {

    public Result show() {
        User user = request().attrs().get(AuthenticationAttrs.USER);
        return ok(Json.toJson(streamSettings(user)));
    }

    public Result update() {
        User user = request().attrs().get(AuthenticationAttrs.USER);
        return ok(Json.toJson(streamSettings(user)));
    }

    private ObjectNode streamSettings(User user) {
        ObjectNode result = Json.newObject();
        result.put("settings", Json.toJson(user.metadata));
        result.put("profile", Json.toJson(user));
        return result;
    }

}
