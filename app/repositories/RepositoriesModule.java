package repositories;

import com.google.inject.AbstractModule;
import javax.inject.Singleton;

public class RepositoriesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserRepository.class).asEagerSingleton();
        bind(UserSessionRepository.class).asEagerSingleton();
        bind(EmailRepository.class).asEagerSingleton();
    }

}
