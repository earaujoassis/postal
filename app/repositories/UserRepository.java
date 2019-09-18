package repositories;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.User;
import models.SqlField;

@Singleton
public class UserRepository extends AbstractEntityRepository {

    private final static String tableName = User.ENTITY_NAME + "s";

    @Inject
    public UserRepository(RepositoryConnector store) {
        List<String> fieldsNames = new ArrayList<>();
        Field[] fields = User.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers()) && field.isAnnotationPresent(SqlField.class)) {
                fieldsNames.add(this.getSqlFieldKey(field));
            }
        }

        this.listOfFields = fieldsNames;
        this.allFields = String.join(",", fieldsNames);
        this.store = store;
    }

    public Iterable<User> getAll() {
        final String SQL = String.format("SELECT %s FROM %s",
            this.allFields, this.tableName);
        List<User> target = new ArrayList<>();
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            rs = pStmt.executeQuery();
            results = this.fromResultSetToListOfHashes(rs);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return target;
        }

        for (Map<String, Object> hash : results) {
            target.add(new User(hash));
        }

        return target;
    }

    public boolean update(String id, User.Metadata metadata) {
        ObjectMapper objectMapper = new ObjectMapper();
        final String SQL = String.format("UPDATE %s SET %s = (?)::json WHERE %s = ?",
            this.tableName, User.Attributes.METADATA, User.Attributes.EXTERNAL_ID);
        String metadataJSONString;
        PreparedStatement pStmt;

        try {
            metadataJSONString = objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            metadataJSONString = "";
        }

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setString(1, metadataJSONString);
            pStmt.setString(2, id);
            pStmt.executeQuery();
            pStmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getOne(String id) {
        final String SQL = String.format("SELECT %s FROM %s WHERE %s = ?",
            this.allFields, this.tableName, User.Attributes.EXTERNAL_ID);
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setString(1, id);
            rs = pStmt.executeQuery();
            results = this.fromResultSetToListOfHashes(rs);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (results.size() > 0) {
            return new User(results.get(0));
        }

        return null;
    }

}
