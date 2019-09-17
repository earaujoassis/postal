package services;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
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

import utils.Environment;

@Singleton
public class StoreService {

    private final static Logger logger = LoggerFactory.getLogger(StoreService.class);
    protected Connection conn;

    @Inject
    public StoreService(AppConfig conf) {
        final String environment = Environment.currentEnvironment();
        final String hostname = conf.getValue("datastore.hostname");
        final String dbName = String.format("%s_%s", conf.getValue("datastore.name_prefix"), environment);
        final String url = String.format("jdbc:postgresql://%s/%s", hostname, dbName);

        this.conn = null;

        try {
            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(url);
            logger.info(String.format("Connected to data store at %s", url));
            this.syncMigrations();
        } catch (SQLException e) {
            logger.info(String.format("Connection to datastore at %s failed", url));
        } catch (ClassNotFoundException e) {
            logger.info("org.postgresql.Driver not found");
        }
    }

    protected void syncMigrations() {
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
        for (File file : files) {
            String filename = file.getName();
            ResultSet result;
            boolean alreadyMigrated = false;

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
                StringBuffer currentQuery = new StringBuffer();
                logger.info(String.format("Migrating: %s", filename));
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line = reader.readLine();
                    currentQuery = new StringBuffer();
                    while (line != null) {
                        if (line.trim().endsWith(";")) {
                            currentQuery.append(line.trim());
                            currentStatement = this.conn.createStatement();
                            currentStatement.execute(currentQuery.toString());
                            currentQuery = new StringBuffer();
                        } else if (line.trim().length() > 0) {
                            currentQuery.append(line.trim());
                        }
                        line = reader.readLine();
                    }
                    preparedStatement = this.conn.prepareStatement("INSERT INTO migrations(filename) VALUES(?);");
                    preparedStatement.setString(1, filename);
                    preparedStatement.execute();
                } catch (SQLException e) {
                    logger.info(String.format("Failed statement: %s", currentQuery.toString()));
                    e.printStackTrace();
                } catch (IOException e) {
                    logger.info(String.format("Something unexpected happened on file %s", file.getName()));
                    e.printStackTrace();
                }
            } else {
                logger.info(String.format("File already migrated: %s", filename));
            }
        }
    }

}
