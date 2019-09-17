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
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AppConfig {

    private final static Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private final Map<String, String> secrets;

    @Inject
    public AppConfig(Config conf) throws VaultException, IOException {
        logger.info("Checking if local configuration exists");
        File localConfigFile = new File("conf/config.local.json");

        if (localConfigFile.exists() && !localConfigFile.isDirectory()) {
            logger.info("Local configuration available; attempting to load it");
            this.secrets = new ObjectMapper().readValue(localConfigFile, HashMap.class);
            logger.info("Configuration successfully loaded from local file");
            return;
        }

        logger.info("Local configuration unavailable; attempting connection to Vault");

        final VaultConfig vaultConfig = new VaultConfig()
                .address(conf.getString("postal.configuration_store.addr"))
                .token(conf.getString("postal.configuration_store.token"))
                .build();
        final Vault vault = new Vault(vaultConfig, 1);
        this.secrets = vault.logical()
            .read(conf.getString("postal.configuration_store.path"))
            .getData();

        logger.info("Connection successful; configuration loaded");
    }

    public String getValue(String key) {
        return this.secrets.get(key);
    }

}
