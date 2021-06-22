package controllers.api;

import javax.inject.Inject;
import play.mvc.*;

import actions.Authentication;
import relational.RepositoryConnector;

@Authentication(enforce = false)
public class HealthCheckController extends Controller {

    @Inject RepositoryConnector repositoryConnector;

    public Result status() {
        if (repositoryConnector.isPoolHealthy()) {
            return ok("healthy");
        } else {
            return status(503, "unhealthy");
        }
    }

}
