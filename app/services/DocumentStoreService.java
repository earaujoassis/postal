package services;

import javax.inject.*;
import com.typesafe.config.Config;
import play.api.Configuration;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

@Singleton
public class DocumentStoreService {

    public static class Collections {
        public final static String EMAILS = "emails";
    }

    private final Config configuration;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    @Inject
    public DocumentStoreService(Config configuration) {
        this.configuration = configuration;
        this.mongoClient = MongoClients.create(configuration.getString("postal.datastore.client_url"));
        this.database = mongoClient.getDatabase(configuration.getString("postal.datastore.database"));
    }

    public MongoCollection<Document> getCollection(String name) {
        return this.database.getCollection(name);
    }

}
