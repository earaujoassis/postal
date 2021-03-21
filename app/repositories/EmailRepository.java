package repositories;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.email.Email;
import models.email.EmailMetadata;
import relational.QueryBuilder;

@Singleton
public class EmailRepository extends AbstractEntityRepository {

    @Inject
    public EmailRepository(RepositoryConnector store) {
        this.store = store;
    }

    public Iterable<Email> getAll(Integer userId, String folder) {
        List<Email> target = new ArrayList<>();

        List<Map<String, Object>> results = QueryBuilder.forModel(Email.class).bind(this.store)
            .select().from()
            .where("user_id = ?", userId)
            .and("metadata->>'folder' = ?", folder)
            .orderByDesc("sent_at")
            .limit(10)
            .getAll();

        for (Map<String, Object> hash : results) {
            target.add(new Email(hash));
        }

        return target;
    }

    public Iterable<Email> getAll(Integer userId) {
        List<Email> target = new ArrayList<>();
        List<Map<String, Object>> results = QueryBuilder.forModel(Email.class).bind(this.store)
            .select().from()
            .where("user_id = ?", userId)
            .and("(metadata->>'folder')::text not like 'trash'")
            .or("(metadata->'folder')::text like 'null'")
            .orderByDesc("sent_at")
            .limit(10)
            .getAll();

        for (Map<String, Object> hash : results) {
            target.add(new Email(hash));
        }

        return target;
    }

    public int getCount(Integer userId, String folder) {
        return QueryBuilder.forModel(Email.class).bind(this.store)
            .select("count(*)").from()
            .where("user_id = ?", userId)
            .and("metadata->>'folder' = ?", folder)
            .getCount();
    }

    public int getCount(Integer userId) {
        return QueryBuilder.forModel(Email.class).bind(this.store)
            .select("count(*)").from()
            .where("user_id = ?", userId)
            .and("(metadata->>'folder')::text not like 'trash'")
            .or("(metadata->'folder')::text like 'null'")
            .getCount();
    }

    public Email getByPublicId(Integer userId, String id) {
        Map<String, Object> result = QueryBuilder.forModel(Email.class).bind(this.store)
            .select().from()
            .where("user_id = ?", userId)
            .and("public_id = ?", id)
            .limit(1)
            .getOne();

        if (result != null) {
            return new Email(result);
        }

        return null;
    }

    public boolean insert(Email email) {
        return QueryBuilder.forModel(Email.class).bind(this.store)
            .insert()
            .instance(email);
    }

    public boolean update(Integer userId, String id, EmailMetadata metadata) {
        return QueryBuilder.forModel(Email.class).bind(this.store)
            .update()
            .set("metadata = (?)::json", metadata)
            .where("user_id = ?", userId)
            .and("public_id = ?", id)
            .commit();
    }

    public Map<String, Integer> status(Integer userId) {
        Map<String, Integer> status = new HashMap<String, Integer>();
        int unreadResult;

        unreadResult = QueryBuilder.forModel(Email.class).bind(this.store)
            .select("count(*)")
            .from()
            .where("user_id = ?", userId)
            .and("(metadata->>'read')::boolean is false")
            .getCount();
        status.put("unread", Integer.valueOf(unreadResult));

        return status;
    }

    public boolean isEmailAvailable(Integer userId, String key) {
        return ((QueryBuilder.forModel(Email.class).bind(this.store)
            .select("count(*)")
            .from()
            .where("user_id = ?", userId)
            .and("bucket_key = ?", key)
            .getCount()) > 0);
    }

}
