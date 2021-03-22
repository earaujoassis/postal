package controllers.api;

import javax.inject.Inject;
import play.mvc.*;

import actions.Authentication;
import repositories.RepositoryConnector;

@Authentication(enforce = false)
public class HealthCheckController extends Controller {

    @Inject RepositoryConnector repositoryConnector;

    public Result status() {
        if (repositoryConnector.isHealthy()) {
            return ok("healthy");
        } else {
            return internalServerError("unhealthy");
        }
    }

}