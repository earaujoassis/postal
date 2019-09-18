package controllers;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        User doc = this.userRepository.getOne(id);

        if (doc == null) {
            return ok(Json.parse("null"));
        }

        return ok(Json.toJson(doc));
    }

}
