package actions;

import javax.inject.Inject;
import play.api.inject.Injector;
import play.mvc.Action;
import play.mvc.*;
import play.libs.Json;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

import services.JWTService;
import repositories.UserSessionRepository;
import repositories.UserRepository;
import models.user.UserSession;
import models.user.User;

public class AuthenticationAction extends Action<Authentication> {

    @Inject Injector injector;

    public CompletionStage<Result> call(Http.Request request) {
        JWTService jwtService = this.injector.instanceOf(JWTService.class);
        UserSessionRepository userSessionRepository = this.injector.instanceOf(UserSessionRepository.class);
        UserRepository userRepository = this.injector.instanceOf(UserRepository.class);
        boolean shouldEnforce = configuration.enforce();
        UserSession session = null;
        User user = null;
        String sessionStr = null;

        try {
            sessionStr = request.cookie("postal.session").value();
            session = userSessionRepository.getById(Integer.valueOf(jwtService.getSessionId(sessionStr)));
            user = userRepository.getById(session.userId);
            if (user == null && shouldEnforce) {
                return promiseResult(configuration.json());
            }
        } catch (NullPointerException e) {
            if (shouldEnforce) {
                return promiseResult(configuration.json());
            }
        }

        return delegate.call(request.addAttr(AuthenticationAttrs.USER, user));
    }

    private CompletionStage<Result> promiseResult(boolean jsonResult) {
        CompletionStage<Result> promiseOfResult;

        if (jsonResult) {
            promiseOfResult =
                CompletableFuture.supplyAsync(() -> unauthorized(Json.parse("{\"error\": \"unauthenticated user\"}")));
        } else {
            promiseOfResult =
                CompletableFuture.supplyAsync(() -> unauthorized(views.html.unauthenticated.render()));
        }

        return promiseOfResult;
    }

}
