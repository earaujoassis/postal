package actors;

import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

public class ActorsModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {
        bindActor(SyncMessagesActor.class, "sync-messages-actor");
        bindActor(SyncMigrationsActor.class, "sync-migrations-actor");
    }

}
