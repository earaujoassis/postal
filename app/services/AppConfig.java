package services;

import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.Map;
import play.api.Configuration;
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
    public AppConfig(Config conf) throws VaultException {
        logger.info("Attempting connection to Vault");

        final VaultConfig vaultConfig = new VaultConfig()
                .address(conf.getString("postal.configuration_store.addr"))
                .token(conf.getString("postal.configuration_store.token"))
                .build();
        final Vault vault = new Vault(vaultConfig, 1);
        this.secrets = vault.logical()
            .read(conf.getString("postal.configuration_store.path"))
            .getData();

        logger.info("Connection successful");
    }

    public String getValue(String key) {
        return this.secrets.get(key);
    }

}
