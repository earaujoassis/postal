package controllers;

import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;

import services.AppConfig;
import services.OAuthService;
import services.OAuthServiceException;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private OAuthService authService;

    @Inject
    public HomeController(OAuthService authService) {
        this.authService = authService;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.landing.render());
    }

    public Result signin() {
        return redirect(authService.getAuthorizeUrl());
    }

    public Result signup() {
        return redirect(authService.getBaseUrl());
    }

    public Result callback(String code, String scope, String state) {
        String jsonResponse;

        try {
            jsonResponse = authService.retrieveAccessToken(code);
        } catch (OAuthServiceException err) {
            jsonResponse = err.jsonMessage();
        }

        return ok(Json.parse(jsonResponse));
    }

}
