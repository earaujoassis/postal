package controllers.api;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import java.util.List;
import java.util.ArrayList;

import actions.Authentication;
import actions.AuthenticationAttrs;
import repositories.UserRepository;
import models.user.User;

@Authentication(enforce = true)
public class UserController extends Controller {

    public Result show() {
        User user = request().attrs().get(AuthenticationAttrs.USER);
        return ok(Json.toJson(user));
    }

}
