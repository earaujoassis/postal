package relational;

import com.google.inject.AbstractModule;
import javax.inject.Singleton;

public class RelationalModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Migrator.class).asEagerSingleton();
        bind(RepositoryConnector.class).asEagerSingleton();
    }

}
