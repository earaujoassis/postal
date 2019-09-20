package tasks;

import javax.inject.Named;
import javax.inject.Inject;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;

import actors.SyncMessagesActorProtocol.Request;

public class SyncMessagesTask {

    private final ActorRef syncMessagesActor;
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    @Inject
    public SyncMessagesTask(@Named("sync-messages-actor") ActorRef syncMessagesActor,
                            ActorSystem actorSystem,
                            ExecutionContext executionContext) {
        this.syncMessagesActor = syncMessagesActor;
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialize();
    }

    private void initialize() {
        actorSystem.scheduler().schedule(
            Duration.create(1, TimeUnit.MINUTES),
            Duration.create(5, TimeUnit.MINUTES),
            syncMessagesActor,
            new Request("sync"),
            executionContext,
            ActorRef.noSender()
        );
    }

}
