package relational;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.user.UserSession;

@Singleton
public class Migrator {

    private final static Logger logger = LoggerFactory.getLogger(Migrator.class);

    private static RepositoryConnector store;
    private static Connection connection;

    @Inject
    public Migrator(RepositoryConnector store) {
        this.store = store;
    }

    public void syncMigrations() {
        Statement currentStatement = null;
        PreparedStatement preparedStatement = null;
        final String migrationTable = ("CREATE TABLE IF NOT EXISTS migrations (\n" +
            "filename            VARCHAR PRIMARY KEY,\n" +
            "created_at          TIMESTAMPTZ NOT NULL DEFAULT now()\n" +
        ");");

        try {
            this.connection = this.store.getConnectionFromPool();
            currentStatement = this.connection.createStatement();
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
                currentStatement = this.connection.createStatement();
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
                    this.connection.setAutoCommit(false);
                    currentStatement = this.connection.createStatement();
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
                    preparedStatement = this.connection.prepareStatement("INSERT INTO migrations(filename) VALUES(?);");
                    preparedStatement.setString(1, filename);
                    preparedStatement.execute();
                    this.connection.commit();
                    this.connection.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.info(String.format("Something unexpected happened on file %s", file.getName()));
                    e.printStackTrace();
                    try {
                        this.connection.rollback();
                    } catch (SQLException er) {
                        logger.info("Error while reverting statement; aborting anyway");
                        e.printStackTrace();
                    }
                    break;
                } catch (IOException e) {
                    logger.info(String.format("Something unexpected happened on file %s", file.getName()));
                    e.printStackTrace();
                    try {
                        this.connection.rollback();
                    } catch (SQLException er) {
                        logger.info("Error while reverting statement; aborting anyway");
                        e.printStackTrace();
                    }
                    break;
                } catch (RuntimeException e) {
                    logger.info(String.format("Something unexpected happened on file %s", file.getName()));
                    e.printStackTrace();
                    try {
                        this.connection.rollback();
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

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
