package relational;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdbcdslog.ConnectionLoggingProxy;
import org.postgresql.util.PSQLException;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import utils.Environment;
import services.AppConfig;

@Singleton
public class RepositoryConnector extends AbstractConnectionHandler {

    private final static int TIMEOUT_IN_SECONDS = 5;

    private final static Logger logger = LoggerFactory.getLogger(RepositoryConnector.class);

    private Connection connection;
    private AppConfig conf;
    private DataSource dataSource;

    @Inject
    public RepositoryConnector(AppConfig conf) {
        this.conf = conf;
        this.connection = null;
        this.setupPool();
    }

    private void setupPool() {
        PoolProperties properties = new PoolProperties();

        final String environment = Environment.currentEnvironment();
        final String hostname = this.conf.getValue("datastore.hostname");
        final String dbName = String.format("%s_%s", this.conf.getValue("datastore.namePrefix"), environment);
        final String url = String.format("jdbc:postgresql://%s/%s", hostname, dbName);

        properties.setDriverClassName("org.postgresql.Driver");
        properties.setUrl(url);
        properties.setMaxActive(100);
        properties.setMaxIdle(10);
        properties.setMaxWait(10000);
        properties.setRemoveAbandoned(true);
        properties.setRemoveAbandonedTimeout(60);
        properties.setMinEvictableIdleTimeMillis(5000);
        properties.setTimeBetweenEvictionRunsMillis(30000);
        properties.setJdbcInterceptors(
            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
            "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer"
        );

        if (environment.equals("production")) {
            String[] credentialsParts = this.conf.getValue("datastore.credentials").split(":");
            properties.setUsername(credentialsParts[0]);
            properties.setPassword(credentialsParts[1]);
        }

        DataSource pool = new DataSource();
        pool.setPoolProperties(properties);
        this.dataSource = pool;
    }

    public Connection getConnectionFromPool()
    throws SQLException {
        final String environment = Environment.currentEnvironment();

        if (environment.equals("production")) {
            return dataSource.getConnection();
        } else {
            return ConnectionLoggingProxy.wrap(dataSource.getConnection());
        }
    }

    public boolean isPoolHealthy() {
        try {
            Connection connection = this.getConnectionFromPool();
            return connection.isValid(TIMEOUT_IN_SECONDS);
        } catch (SQLException e) {
            return false;
        } finally {
            teardownConnection(connection);
        }
    }

    @Deprecated
    public Connection getConnection() {
        final String environment = Environment.currentEnvironment();
        final String hostname = this.conf.getValue("datastore.hostname");
        final String dbName = String.format("%s_%s", this.conf.getValue("datastore.namePrefix"), environment);
        final String url = String.format("jdbc:postgresql://%s/%s", hostname, dbName);

        if (this.isUnhealthy()) {
            try {
                Class.forName("org.postgresql.Driver");
                if (environment.equals("production")) {
                    logger.info("Production environment, providing credentials for datastore");
                    String[] credentialsParts = this.conf.getValue("datastore.credentials").split(":");
                    this.connection = DriverManager.getConnection(url,
                        credentialsParts[0], credentialsParts[1]);
                } else {
                    this.connection = ConnectionLoggingProxy.wrap(DriverManager.getConnection(url));
                }
                logger.info(String.format("Connected to data store at %s", url));
            } catch (PSQLException e) {
                e.printStackTrace();
                logger.info("Internal DBMS error");
            } catch (SQLException e) {
                e.printStackTrace();
                logger.info(String.format("Connection to datastore at %s failed", url));
            } catch (ClassNotFoundException e) {
                logger.info("org.postgresql.Driver not found");
            }
        }

        return this.connection;
    }

    @Deprecated
    public boolean isHealthy() {
        if (this.connection == null) {
            return false;
        }

        try {
            return this.connection.isValid(TIMEOUT_IN_SECONDS) ||
                !this.connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Deprecated
    public boolean isUnhealthy() {
        return !this.isHealthy();
    }

}
