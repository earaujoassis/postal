package services;

import javax.inject.Singleton;
import javax.inject.Inject;
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
        logger.info("Checking if local configuration exists");
        File localConfigFile = new File("conf/config.local.json");

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
