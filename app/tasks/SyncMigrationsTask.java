package tasks;

import javax.inject.Named;
import javax.inject.Inject;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;

import actors.SyncMigrationsActorProtocol.Request;

public class SyncMigrationsTask {

    private final ActorRef syncMigrationsActor;
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    @Inject
    public SyncMigrationsTask(@Named("sync-migrations-actor") ActorRef syncMigrationsActor,
                            ActorSystem actorSystem,
                            ExecutionContext executionContext) {
        this.syncMigrationsActor = syncMigrationsActor;
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialize();
    }

    private void initialize() {
        actorSystem.scheduler().scheduleOnce(
            Duration.create(5, TimeUnit.SECONDS),
            syncMigrationsActor,
            new Request("sync"),
            executionContext,
            ActorRef.noSender()
        );
    }

}
