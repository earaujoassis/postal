package repositories;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.postgresql.util.PSQLException;
import java.lang.reflect.Field;

import models.user.UserSession;

@Singleton
public class UserSessionRepository extends AbstractEntityRepository {

    private final static String tableName = UserSession.ENTITY_NAME + "s";

    @Inject
    public UserSessionRepository(RepositoryConnector store) {
        List<String> fieldsNames = this.getFieldsNames(UserSession.class);
        this.listOfFields = fieldsNames;
        this.allFields = String.join(",", fieldsNames);
        this.store = store;
    }

    public UserSession getById(Integer id) {
        final String SQL = String.format("SELECT %s FROM %s WHERE %s = ?",
            this.allFields, this.tableName, UserSession.Attributes.ID);
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setInt(1, id.intValue());
            rs = pStmt.executeQuery();
            results = this.fromResultSetToListOfHashes(rs);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (results.size() > 0) {
            return new UserSession(results.get(0));
        }

        return null;
    }

    public UserSession getActiveById(Integer id) {
        final String SQL = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = false",
            this.allFields, this.tableName, UserSession.Attributes.ID, UserSession.Attributes.INVALIDATED);
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setInt(1, id.intValue());
            rs = pStmt.executeQuery();
            results = this.fromResultSetToListOfHashes(rs);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (results.size() > 0) {
            return new UserSession(results.get(0));
        }

        return null;
    }

    protected UserSession getByAccessToken(String token) {
        final String SQL = String.format("SELECT %s FROM %s WHERE %s = ?",
            this.allFields, this.tableName, UserSession.Attributes.ACCESS_TOKEN);
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setString(1, token);
            rs = pStmt.executeQuery();
            results = this.fromResultSetToListOfHashes(rs);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (results.size() > 0) {
            return new UserSession(results.get(0));
        }

        return null;
    }

    public boolean insert(UserSession session) {
        final List<String> fieldsNames = this.getSettableFieldsNames(UserSession.class);
        final String allFields = String.join(",", fieldsNames);
        final String SQL = String.format("INSERT INTO %s(%s) VALUES(%s)",
            this.tableName, allFields, this.questionMarksForFields(fieldsNames.size()));
        List<Field> fields = this.getSettableFields(UserSession.class);
        int fieldsSize = fields.size();
        PreparedStatement pStmt;
        UserSession inserted;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            for (int i = 1; i < fieldsSize + 1; i++) {
                Field field = fields.get(i - 1);
                Object entry = null;

                try {
                    entry = field.get(session);
                    pStmt.setObject(i, entry);
                } catch (IllegalAccessException e) {
                    pStmt.setObject(i, null);
                    continue;
                } catch (PSQLException e) {
                    pStmt.setObject(i, null);
                }
            }
            pStmt.execute();
            pStmt.close();
            inserted = this.getByAccessToken(session.accessToken);
            session._id = inserted._id;
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean invalidate(Integer id) {
        final String SQL = String.format("UPDATE %s SET %s = ? WHERE %s = ?",
            this.tableName, UserSession.Attributes.INVALIDATED, UserSession.Attributes.ID);
        PreparedStatement pStmt;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setBoolean(1, true);
            pStmt.setInt(2, id);
            pStmt.execute();
            pStmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
