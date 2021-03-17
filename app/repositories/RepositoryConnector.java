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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdbcdslog.ConnectionLoggingProxy;

import utils.Environment;
import services.AppConfig;

@Singleton
public class RepositoryConnector {

    private final static Logger logger = LoggerFactory.getLogger(RepositoryConnector.class);
    protected Connection conn;

    @Inject
    public RepositoryConnector(AppConfig conf) {
        final String environment = Environment.currentEnvironment();
        final String hostname = conf.getValue("datastore.hostname");
        final String dbName = String.format("%s_%s", conf.getValue("datastore.name_prefix"), environment);
        final String url = String.format("jdbc:postgresql://%s/%s", hostname, dbName);

        this.conn = null;

        try {
            Class.forName("org.postgresql.Driver");
            if (environment.equals("production")) {
                logger.info("Production environment, providing credentials for datastore");
                String[] credentialsParts = conf.getValue("datastore.credentials").split(":");
                this.conn = ConnectionLoggingProxy.wrap(DriverManager.getConnection(url,
                    credentialsParts[0], credentialsParts[1]));
            } else {
                this.conn = ConnectionLoggingProxy.wrap(DriverManager.getConnection(url));
            }
            logger.info(String.format("Connected to data store at %s", url));
            // LEGACY The call below was used to syncMigrations. There's now an actor for that (async)
            // this.syncMigrations();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.info(String.format("Connection to datastore at %s failed", url));
        } catch (ClassNotFoundException e) {
            logger.info("org.postgresql.Driver not found");
        }
    }

    public void syncMigrations() {
        Statement currentStatement = null;
        PreparedStatement preparedStatement = null;
        final String migrationTable = ("CREATE TABLE IF NOT EXISTS migrations (\n" +
            "filename            VARCHAR PRIMARY KEY,\n" +
            "created_at          TIMESTAMPTZ NOT NULL DEFAULT now()\n" +
        ");");

        try {
            currentStatement = this.conn.createStatement();
            currentStatement.execute(migrationTable);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.info("Failed to create migration table");
        } finally {
            if (currentStatement != null) {
                try {
                    currentStatement.close();
                } catch (SQLException e) {
                    // Do nothing here
                }
                currentStatement = null;
            }
        }

        File[] files = new File("db/migrations").listFiles();
        Arrays.sort(files);
        for (File file : files) {
            boolean alreadyMigrated = false;
            String filename = file.getName();
            ResultSet result;

            logger.info(String.format("Checking migration file: %s", filename));
            try {
                currentStatement = this.conn.createStatement();
                result = currentStatement.executeQuery(String.format(
                    "SELECT count(*) FROM migrations WHERE filename = '%s';",
                    filename
                ));
                result.next();
                alreadyMigrated = result.getInt(1) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                continue;
            }

            if (!alreadyMigrated) {
                logger.info(String.format("Migrating: %s", filename));
                try {
                    this.conn.setAutoCommit(false);
                    currentStatement = this.conn.createStatement();
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line = reader.readLine();
                    StringBuffer currentQuery = new StringBuffer();
                    while (line != null) {
                        if (line.trim().endsWith(";")) {
                            currentQuery.append(line.trim());
                            currentStatement.addBatch(currentQuery.toString());
                            currentQuery = new StringBuffer();
                        } else if (line.trim().length() > 0) {
                            currentQuery.append(line.trim());
                        }
                        line = reader.readLine();
                    }
                    currentStatement.executeBatch();
                    preparedStatement = this.conn.prepareStatement("INSERT INTO migrations(filename) VALUES(?);");
                    preparedStatement.setString(1, filename);
                    preparedStatement.execute();
                    this.conn.commit();
                    this.conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.info(String.format("Something unexpected happened on file %s", file.getName()));
                    e.printStackTrace();
                    try {
                        this.conn.rollback();
                    } catch (SQLException er) {
                        logger.info("Error while reverting statement; aborting anyway");
                        e.printStackTrace();
                    }
                    break;
                } catch (IOException e) {
                    logger.info(String.format("Something unexpected happened on file %s", file.getName()));
                    e.printStackTrace();
                    try {
                        this.conn.rollback();
                    } catch (SQLException er) {
                        logger.info("Error while reverting statement; aborting anyway");
                        e.printStackTrace();
                    }
                    break;
                } catch (RuntimeException e) {
                    logger.info(String.format("Something unexpected happened on file %s", file.getName()));
                    e.printStackTrace();
                    try {
                        this.conn.rollback();
                    } catch (SQLException er) {
                        logger.info("Error while reverting statement; aborting anyway");
                        e.printStackTrace();
                    }
                    break;
                } finally {
                    if (currentStatement != null) {
                        try {
                            currentStatement.close();
                        } catch (SQLException e) {
                            // Do nothing here
                        }
                        currentStatement = null;
                    }
                    if (preparedStatement != null) {
                        try {
                            preparedStatement.close();
                        } catch (SQLException e) {
                            // Do nothing here
                        }
                        preparedStatement = null;
                    }
                }
            } else {
                logger.info(String.format("File already migrated: %s", filename));
            }
        }
    }

}
