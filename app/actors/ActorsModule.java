package actors;

import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

import actors.SyncMessagesActor;

public class ActorsModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {
        bindActor(SyncMessagesActor.class, "sync-messages-actor");
    }

}