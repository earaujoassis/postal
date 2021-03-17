package controllers;

import java.util.Optional;
import javax.inject.Inject;
import play.api.inject.Injector;
import play.mvc.*;
import play.libs.Json;
import play.mvc.Http.Cookie;
import com.fasterxml.jackson.databind.JsonNode;

import actions.Authentication;
import services.AppConfig;
import services.OAuthService;
import services.OAuthServiceException;
import services.JWTService;
import repositories.UserRepository;
import repositories.UserSessionRepository;
import models.UserSession;
import models.User;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    @Inject OAuthService authService;
    @Inject Injector injector;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    @Authentication(json = false)
    public Result index() {
        JWTService jwtService = this.injector.instanceOf(JWTService.class);
        UserSessionRepository userSessionRepository = this.injector.instanceOf(UserSessionRepository.class);
        UserSession session = Optional.ofNullable(request().cookie("postal.session"))
            .map(cookie -> cookie.value())
            .map(sessionStr -> userSessionRepository.getActiveById(Integer.valueOf(jwtService.getSessionId(sessionStr))))
            .orElseGet(() -> null);

        if (session != null && session.stillValid()) {
            return ok(views.html.index.render());
        }

        return ok(views.html.landing.render());
    }

    public Result signin() {
        return redirect(authService.getAuthorizeUrl());
    }

    public Result signup() {
        return redirect(authService.getBaseUrl());
    }

    @Authentication(json = false)
    public Result signout() {
        JWTService jwtService = this.injector.instanceOf(JWTService.class);
        UserSessionRepository userSessionRepository = this.injector.instanceOf(UserSessionRepository.class);
        UserSession session = Optional.ofNullable(request().cookie("postal.session"))
            .map(cookie -> cookie.value())
            .map(sessionStr -> userSessionRepository.getById(Integer.valueOf(jwtService.getSessionId(sessionStr))))
            .orElseGet(() -> null);

        if (session != null && session.stillValid()) {
            userSessionRepository.invalidate(session._id);
        }

        return redirect("/").discardingCookie("postal.session");
    }

    public Result callback(String code, String scope, String state) {
        UserRepository userRepository = this.injector.instanceOf(UserRepository.class);
        UserSessionRepository userSessionRepository = this.injector.instanceOf(UserSessionRepository.class);
        JWTService jwtService = this.injector.instanceOf(JWTService.class);
        String sessionStr;

        try {
            String sessionResponse = authService.retrieveAccessToken(code);
            JsonNode jsonSession = Json.parse(sessionResponse);

            String userDataResponse = authService.getUserData(
                jsonSession.get("access_token").asText(), jsonSession.get("user_id").asText());
            JsonNode jsonUserData = Json.parse(userDataResponse);
            User user = userRepository.getByExternalId(jsonSession.get("user_id").asText());
            if (user == null) {
                user = new User(
                    jsonSession.get("user_id").asText(),
                    String.format("%s %s",
                        jsonUserData.get("user").get("first_name").asText(),
                        jsonUserData.get("user").get("last_name").asText())
                );
                if (!userRepository.insert(user)) {
                    return redirect("/?error=user_creation");
                }
            }
            UserSession session = new UserSession(
                user._id,
                jsonSession.get("access_token").asText(),
                new Integer(jsonSession.get("expires_in").asInt()),
                jsonSession.get("refresh_token").asText(),
                jsonSession.get("scope").asText(),
                jsonSession.get("token_type").asText());
            if (!userSessionRepository.insert(session)) {
                return redirect("/?error=session_creation");
            }
            sessionStr = jwtService.signToken(session._id.toString());
        } catch (OAuthServiceException err) {
            err.printStackTrace();
            return redirect("/?error=authorization_circuit");
        }

        return redirect("/").withCookies(Cookie.builder("postal.session", sessionStr).build());
    }

}
