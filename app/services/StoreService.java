package services;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.rethinkdb.RethinkDB.r;

import utils.Environment;

@Singleton
public class StoreService {

    private final static Logger logger = LoggerFactory.getLogger(StoreService.class);
    protected final String dbName;
    protected final Connection conn;

    @Inject
    public StoreService(AppConfig conf) {
        final String environment = Environment.currentEnvironment();
        final String hostname = conf.getValue("datastore.hostname");

        this.dbName = String.format("%s_%s", conf.getValue("datastore.name_prefix"), environment);
        this.conn = r.connection()
            .hostname(hostname)
            .port(28015)
            .db(this.dbName)
            .connect();

        logger.info(
            String.format("Connected to data store at rethinkdb://%s:28015/%s",
            hostname,
            this.dbName));
    }

}
