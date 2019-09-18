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

import models.Email;
import utils.StoreUtils;

@Singleton
public class EmailRepository extends AbstractEntityRepository {

    private final static String tableName = Email.ENTITY_NAME + "s";

    @Inject
    public EmailRepository(RepositoryConnector store) {
        List<String> fieldsNames = new ArrayList<>();
        Field[] fields = Email.Attributes.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers())) {
                try {
                    fieldsNames.add((String) field.get(new Email.Attributes()));
                } catch (IllegalAccessException e) {
                    // Do nothing
                }
            }
        }

        this.listOfFields = fieldsNames;
        this.allFields = String.join(",", fieldsNames);
        this.store = store;
    }

    public Map<String, Long> status() {
        Map<String, Long> status = new HashMap<String, Long>();
        final String SQLTotal = String.format("SELECT count(*) FROM %s", this.tableName);
        final String SQLRead = String.format("SELECT count(*) FROM %s WHERE (%s->>'%s')::boolean is false",
            this.tableName, Email.Attributes.METADATA, Email.Metadata.METADATA_READ);
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQLTotal);
            rs = pStmt.executeQuery();
            pStmt.close();
            rs.next();
            status.put("total", Long.valueOf(rs.getInt(1)));
        } catch (SQLException e) {
            status.put("total", Long.valueOf(0));
        }

        try {
            pStmt = this.store.conn.prepareStatement(SQLRead);
            rs = pStmt.executeQuery();
            pStmt.close();
            rs.next();
            status.put("unread", Long.valueOf(rs.getInt(1)));
        } catch (SQLException e) {
            status.put("unread", Long.valueOf(0));
        }

        return status;
    }

    public boolean isEmailAvailable(String key) {
        final String SQL = String.format("SELECT count(*) FROM %s WHERE %s = ?",
            this.tableName, Email.Attributes.BUCKET_KEY);
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setString(1, key);
            rs = pStmt.executeQuery();
            pStmt.close();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean insert(Email email) {
        final String SQL = String.format("INSERT INTO %s(%s) VALUES(%s)",
            this.tableName, this.allFields, StoreUtils.questionMarksForFields(this.listOfFields));
        Field[] fields = Email.class.getDeclaredFields();
        int size = fields.length;
        PreparedStatement pStmt;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            for (int i = 0; i < size; i++) {
                Field field = fields[i];
                if (Modifier.isPublic(field.getModifiers())) {
                    try {
                        pStmt.setObject(i + 1, field.get(email));
                    } catch (IllegalAccessException e) {
                        pStmt.setObject(i + 1, null);
                    }
                }
            }
            pStmt.executeQuery();
            pStmt.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean update(String id, Email.Metadata metadata) {
        ObjectMapper objectMapper = new ObjectMapper();
        final String SQL = String.format("UPDATE %s SET %s = ? WHERE %s = ?",
            this.tableName, Email.Attributes.METADATA, Email.Attributes.PUBLIC_ID);
        String metadataJSONString;
        PreparedStatement pStmt;

        try {
            metadataJSONString = objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
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
            return false;
        }
    }

    public Iterable<Email> getAll(String folder) {
        final String SQL = String.format("SELECT %s FROM %s LIMIT 10 ORDER BY %s DESC",
            this.allFields, this.tableName, Email.Attributes.SENT_AT);
        List<Email> target = new ArrayList<>();
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            rs = pStmt.executeQuery();
            pStmt.close();
            results = this.fromResultSetToListOfHashes(rs);
        } catch (SQLException e) {
            return target;
        }

        for (Map<String, Object> hash : results) {
            target.add(new Email(hash));
        }

        return target;
    }

    public Email getOne(String id) {
        final String SQL = String.format("SELECT %s FROM %s WHERE %s = ?",
            this.allFields, this.tableName, Email.Attributes.PUBLIC_ID);
        List<Map<String, Object>> results;
        PreparedStatement pStmt;
        ResultSet rs;

        try {
            pStmt = this.store.conn.prepareStatement(SQL);
            pStmt.setString(1, id);
            rs = pStmt.executeQuery();
            pStmt.close();
            results = this.fromResultSetToListOfHashes(rs);
        } catch (SQLException e) {
            return null;
        }

        if (results.size() > 0) {
            return new Email(results.get(0));
        }

        return null;
    }

}
