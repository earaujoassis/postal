package repositories;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

import models.user.UserSession;
import relational.QueryBuilder;

@Singleton
public class UserSessionRepository extends AbstractEntityRepository {

    @Inject
    public UserSessionRepository(RepositoryConnector store) {
        this.store = store;
    }

    public UserSession getById(Integer id) {
        Map<String, Object> result = QueryBuilder.forModel(UserSession.class).bind(this.store)
            .select().from()
            .where("id = ?", id)
            .limit(1)
            .getOne();

        if (result != null) {
            return new UserSession(result);
        }

        return null;
    }

    public UserSession getActiveById(Integer id) {
        Map<String, Object> result = QueryBuilder.forModel(UserSession.class).bind(this.store)
            .select().from()
            .where("id = ?", id)
            .and("invalidated = false")
            .limit(1)
            .getOne();

        if (result != null) {
            return new UserSession(result);
        }

        return null;
    }

    public UserSession getByAccessToken(String token) {
        Map<String, Object> result = QueryBuilder.forModel(UserSession.class).bind(this.store)
            .select().from()
            .where("access_token = ?", token)
            .limit(1)
            .getOne();

        if (result != null) {
            return new UserSession(result);
        }

        return null;
    }

    public boolean insert(UserSession session) {
        return QueryBuilder.forModel(UserSession.class).bind(this.store)
            .insert()
            .instance(session);
    }

    public boolean invalidate(Integer id) {
        return QueryBuilder.forModel(UserSession.class).bind(this.store)
            .update()
            .set("invalidated = ?", Boolean.TRUE)
            .where("id = ?", id)
            .commit();
    }

}
