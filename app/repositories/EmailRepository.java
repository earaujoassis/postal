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
import org.postgresql.util.PSQLException;
import org.postgresql.util.PGobject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.email.Email;
import models.email.EmailMetadata;

@Singleton
public class EmailRepository extends AbstractEntityRepository {

    private final static String tableName = Email.ENTITY_NAME + "s";

    @Inject
    public EmailRepository(RepositoryConnector store) {
        List<String> fieldsNames = this.getFieldsNames(Email.class);
        this.listOfFields = fieldsNames;
        this.allFields = String.join(",", fieldsNames);
        this.store = store;
    }

    public Iterable<Email> getAll(Integer userId, String folder) {
        final String SQL = String.format("SELECT %s FROM %s WHERE %s = ? AND %s->>'%s' = ? ORDER BY %s DESC LIMIT(10)",
            this.allFields, this.tableName, Email.Attributes.USER_ID, Email.Attributes.METADATA, "folder", Email.Attributes.SENT_AT);
        List<Email> target = new ArrayList<>();
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setInt(1, userId);
            pStmt.setString(2, folder);
            rs = pStmt.executeQuery();
            results = this.fromResultSetToListOfHashes(rs);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return target;
        }

        for (Map<String, Object> hash : results) {
            target.add(new Email(hash));
        }

        return target;
    }

    public Iterable<Email> getAll(Integer userId) {
        final String SQL = String.format("SELECT %s FROM %s WHERE %s = ? ORDER BY %s DESC LIMIT(10)",
            this.allFields, this.tableName, Email.Attributes.USER_ID, Email.Attributes.SENT_AT);
        List<Email> target = new ArrayList<>();
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setInt(1, userId);
            rs = pStmt.executeQuery();
            results = this.fromResultSetToListOfHashes(rs);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return target;
        }

        for (Map<String, Object> hash : results) {
            target.add(new Email(hash));
        }

        return target;
    }

    public Email getByPublicId(Integer userId, String id) {
        final String SQL = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?",
            this.allFields, this.tableName, Email.Attributes.USER_ID, Email.Attributes.PUBLIC_ID);
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setInt(1, userId);
            pStmt.setString(2, id);
            rs = pStmt.executeQuery();
            results = this.fromResultSetToListOfHashes(rs);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (results.size() > 0) {
            return new Email(results.get(0));
        }

        return null;
    }

    public boolean insert(Email email) {
        final List<String> fieldsNames = this.getSettableFieldsNames(Email.class);
        final String allFields = String.join(",", fieldsNames);
        final String SQL = String.format("INSERT INTO %s(%s) VALUES(%s)",
            this.tableName, allFields, this.questionMarksForFields(fieldsNames.size()));
        List<Field> fields = this.getSettableFields(Email.class);
        int fieldsSize = fields.size();
        PreparedStatement pStmt;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            for (int i = 1; i < fieldsSize + 1; i++) {
                Field field = fields.get(i - 1);
                Object entry = null;

                try {
                    entry = field.get(email);
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
                        e.printStackTrace();
                        metadataJSONString = "null";
                    }

                    jsonObject.setType("json");
                    jsonObject.setValue(metadataJSONString);
                    pStmt.setObject(i, jsonObject);
                }
            }
            pStmt.execute();
            pStmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Integer userId, String id, EmailMetadata metadata) {
        ObjectMapper objectMapper = new ObjectMapper();
        final String SQL = String.format("UPDATE %s SET %s = (?)::json WHERE %s = ? AND %s = ?",
            this.tableName, Email.Attributes.METADATA, Email.Attributes.USER_ID, Email.Attributes.PUBLIC_ID);
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
            pStmt.setInt(2, userId);
            pStmt.setString(3, id);
            pStmt.execute();
            pStmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Long> status(Integer userId) {
        Map<String, Long> status = new HashMap<String, Long>();
        final String SQLTotal = String.format("SELECT count(*) FROM %s WHERE %s = ?",
            this.tableName, Email.Attributes.USER_ID);
        final String SQLRead = String.format("SELECT count(*) FROM %s WHERE %s = ? AND (%s->>'%s')::boolean is false",
            this.tableName, Email.Attributes.USER_ID, Email.Attributes.METADATA, "read");
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQLTotal);
            pStmt.setInt(1, userId);
            rs = pStmt.executeQuery();
            rs.next();
            status.put("total", Long.valueOf(rs.getInt(1)));
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            status.put("total", Long.valueOf(0));
        }

        try {
            pStmt = this.store.conn.prepareStatement(SQLRead);
            pStmt.setInt(1, userId);
            rs = pStmt.executeQuery();
            rs.next();
            status.put("unread", Long.valueOf(rs.getInt(1)));
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            status.put("unread", Long.valueOf(0));
        }

        return status;
    }

    public boolean isEmailAvailable(Integer userId, String key) {
        final String SQL = String.format("SELECT count(*) FROM %s WHERE %s = ? AND %s = ?",
            this.tableName, Email.Attributes.USER_ID, Email.Attributes.BUCKET_KEY);
        PreparedStatement pStmt;
        ResultSet rs;
        boolean emailAvailable = false;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setInt(1, userId);
            pStmt.setString(2, key);
            rs = pStmt.executeQuery();
            rs.next();
            emailAvailable = rs.getInt(1) > 0;
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emailAvailable;
    }

}
