package repositories;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.user.User;
import models.user.UserMetadata;
import relational.QueryBuilder;

@Singleton
public class UserRepository extends AbstractEntityRepository {

    @Inject
    public UserRepository(RepositoryConnector store) {
        this.store = store;
    }

    public Iterable<User> getAll() {
        List<User> target = new ArrayList<>();

        List<Map<String, Object>> results = QueryBuilder.forModel(User.class).bind(this.store)
            .select().from()
            .getAll();

        for (Map<String, Object> hash : results) {
            target.add(new User(hash));
        }

        return target;
    }

    public User getById(Integer id) {
        Map<String, Object> result = QueryBuilder.forModel(User.class).bind(this.store)
            .select().from()
            .where("id = ?", id)
            .limit(1)
            .getOne();

        if (result != null) {
            return new User(result);
        }

        return null;
    }

    public User getByExternalId(String id) {
        Map<String, Object> result = QueryBuilder.forModel(User.class).bind(this.store)
            .select().from()
            .where("external_id = ?", id)
            .limit(1)
            .getOne();

        if (result != null) {
            return new User(result);
        }

        return null;
    }

    public boolean insert(User user) {
        return QueryBuilder.forModel(User.class).bind(this.store)
            .insert()
            .instance(user);
    }

    public boolean update(String id, UserMetadata metadata) {
        return QueryBuilder.forModel(User.class).bind(this.store)
            .update()
            .set("metadata = (?)::json", metadata)
            .where("external_id = ?", id)
            .commit();
    }

}
