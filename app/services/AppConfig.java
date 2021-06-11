package services;

import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import play.api.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AppConfig {

    private final static Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private final Map<String, Object> secrets;

    @Inject
    public AppConfig(Config conf)
    throws IOException {
        String fileLocation;
        File localConfigFile;

        logger.info("Loading local configuration file; checking if it exists");
        fileLocation = Optional.ofNullable(conf.getString("postal.configuration.file"))
            .orElseGet(() -> System.getProperty("postal.configuration.file"));
        logger.info(String.format("Loading local configuration file at: %s", fileLocation));

        if (fileLocation == null) {
            throw new AppConfigException("Cannot open configuration file: `null`; aborting");
        }

        localConfigFile = new File(fileLocation);

        if (localConfigFile.exists() && !localConfigFile.isDirectory()) {
            logger.info("Local configuration available; attempting to load it");
            this.secrets = new ObjectMapper().readValue(localConfigFile, HashMap.class);
            logger.info("Configuration successfully loaded from local file");
            return;
        }

        throw new AppConfigException("Cannot open configuration file; aborting");
    }

    public String getValueAsString(String key) {
        return (String) this.secrets.get(key);
    }

    public int getValueAsInteger(String key) {
        return ((Integer) this.secrets.get(key)).intValue();
    }

    @Deprecated(forRemoval = true)
    public String getValue(String key) {
        return this.getValueAsString(key);
    }

}
