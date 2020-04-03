package repositories;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PGobject;
import java.lang.reflect.Field;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.User;

@Singleton
public class UserRepository extends AbstractEntityRepository {

    private final static String tableName = User.ENTITY_NAME + "s";

    @Inject
    public UserRepository(RepositoryConnector store) {
        List<String> fieldsNames = this.getFieldsNames(User.class);
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

    public User getById(Integer id) {
        final String SQL = String.format("SELECT %s FROM %s WHERE %s = ?",
            this.allFields, this.tableName, User.Attributes.ID);
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
            return new User(results.get(0));
        }

        return null;
    }

    public User getByExternalId(String id) {
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

    public boolean insert(User user) {
        final List<String> fieldsNames = this.getSettableFieldsNames(User.class);
        final String allFields = String.join(",", fieldsNames);
        final String SQL = String.format("INSERT INTO %s(%s) VALUES(%s)",
            this.tableName, allFields, this.questionMarksForFields(fieldsNames.size()));
        List<Field> fields = this.getSettableFields(User.class);
        int fieldsSize = fields.size();
        PreparedStatement pStmt;
        User inserted;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            for (int i = 1; i < fieldsSize + 1; i++) {
                Field field = fields.get(i - 1);
                Object entry = null;

                try {
                    entry = field.get(user);
                } catch (IllegalAccessException e) {
                    pStmt.setObject(i, null);
                    continue;
                }

                try {
                    pStmt.setObject(i, entry);
                } catch (PSQLException e) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    PGobject jsonObject = new PGobject();
                    String metadataJSONString;

                    try {
                        metadataJSONString = objectMapper.writeValueAsString(entry);
                    } catch (JsonProcessingException exc) {
                        metadataJSONString = "null";
                    }

                    jsonObject.setType("json");
                    jsonObject.setValue(metadataJSONString);
                    pStmt.setObject(i, jsonObject);
                }
            }
            pStmt.execute();
            pStmt.close();
            inserted = this.getByExternalId(user.externalId);
            user._id = inserted._id;
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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

}
