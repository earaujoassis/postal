package controllers;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import java.util.List;
import java.util.ArrayList;

import repositories.UserRepository;
import models.User;

public class ApiUserController extends Controller {

    private UserRepository userRepository;

    @Inject
    public ApiUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Result show(String id) {
        return ok(Json.parse("null"));
    }

}
