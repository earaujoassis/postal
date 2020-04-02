package services;

import com.google.inject.AbstractModule;
import javax.inject.Singleton;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AppConfig.class).asEagerSingleton();
        bind(RemoteStorageService.class).asEagerSingleton();
        bind(OAuthService.class).asEagerSingleton();
    }

}
