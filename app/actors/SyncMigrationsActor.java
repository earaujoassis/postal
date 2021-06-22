package actors;

import akka.actor.*;
import akka.japi.*;
import javax.inject.Inject;
import play.api.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actors.SyncMigrationsActorProtocol.Request;
import services.AppConfig;
import relational.Migrator;

public class SyncMigrationsActor extends AbstractActor {

    private final static Logger logger = LoggerFactory.getLogger(SyncMigrationsActor.class);
    private AppConfig conf;
    private Migrator migrator;

    @Inject
    public SyncMigrationsActor(AppConfig conf, Migrator migrator) {
        this.conf = conf;
        this.migrator = migrator;
    }

    public static Props getProps() {
        return Props.create(SyncMigrationsActor.class);
    }

    @Override
    public Receive createReceive() {
      return receiveBuilder()
        .match(Request.class, request -> {
            String reply = "all-done";
            if (request.isSync()) {
                syncMigrations();
            }
            sender().tell(reply, self());
        })
        .build();
    }

    public void syncMigrations() {
        logger.info("Starting the sync migrations task");
        migrator.syncMigrations();
        logger.info("Finishing the sync migrations task");
    }

}
