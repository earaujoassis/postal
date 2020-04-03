package controllers;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import java.util.List;
import java.util.ArrayList;

import actions.Authentication;
import actions.AuthenticationAttrs;
import repositories.UserRepository;
import models.User;

@Authentication(enforce = true)
public class ApiUserController extends Controller {

    public Result show() {
        User user = request().attrs().get(AuthenticationAttrs.USER);
        return ok(Json.toJson(user));
    }

}
