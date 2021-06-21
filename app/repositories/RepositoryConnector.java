package repositories;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdbcdslog.ConnectionLoggingProxy;
import org.postgresql.util.PSQLException;
import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;

import utils.Environment;
import services.AppConfig;

@Singleton
public class RepositoryConnector {

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
        DriverAdapterCPDS cpds = new DriverAdapterCPDS();

        final String environment = Environment.currentEnvironment();
        final String hostname = this.conf.getValue("datastore.hostname");
        final String dbName = String.format("%s_%s", this.conf.getValue("datastore.namePrefix"), environment);
        final String url = String.format("jdbc:postgresql://%s/%s", hostname, dbName);

        try {
            cpds.setDriver("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.info("org.postgresql.Driver not found");
            e.printStackTrace();
        }

        cpds.setUrl(url);

        if (environment.equals("production")) {
            String[] credentialsParts = this.conf.getValue("datastore.credentials").split(":");
            cpds.setUser(credentialsParts[0]);
            cpds.setPassword(credentialsParts[1]);
        }

        SharedPoolDataSource pool = new SharedPoolDataSource();
        pool.setConnectionPoolDataSource(cpds);
        pool.setMaxActive(10);
        pool.setMaxIdle(10);
        pool.setMaxWait(50);

        dataSource = pool;
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

    public boolean isUnhealthy() {
        return !this.isHealthy();
    }

}
