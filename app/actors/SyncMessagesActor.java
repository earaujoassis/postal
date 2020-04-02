package actors;

import akka.actor.*;
import akka.japi.*;
import javax.inject.Inject;
import play.api.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actors.SyncMessagesActorProtocol.Request;
import services.AppConfig;
import services.RemoteStorageService;
import repositories.UserRepository;
import models.User;

public class SyncMessagesActor extends AbstractActor {

    private final static Logger logger = LoggerFactory.getLogger(SyncMessagesActor.class);
    private AppConfig conf;
    private UserRepository userRepository;
    private Injector injector;

    @Inject
    public SyncMessagesActor(AppConfig conf, UserRepository userRepository, Injector injector) {
        this.conf = conf;
        this.userRepository = userRepository;
        this.injector = injector;
    }

    public static Props getProps() {
        return Props.create(SyncMessagesActor.class);
    }

    @Override
    public Receive createReceive() {
      return receiveBuilder()
        .match(Request.class, request -> {
            String reply = "all-done";
            if (request.isSync()) {
                syncMessages();
            }
            sender().tell(reply, self());
        })
        .build();
    }

    public void syncMessages() {
        Iterable<User> users = userRepository.getAll();
        RemoteStorageService remoteStorageService = this.injector.instanceOf(RemoteStorageService.class);

        logger.info("Starting the sync messages task");

        for (User user : users) {
            logger.info(String.format("Retrieving new messages for %s", user.toString()));
            remoteStorageService.retrieveNewEmailMessagesForUser(user);
        }

        logger.info("Finishing the sync messages task");
    }

}
