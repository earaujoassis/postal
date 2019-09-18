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

@Singleton
public class UserRepository extends AbstractEntityRepository {

    private final static String tableName = User.ENTITY_NAME + "s";

    @Inject
    public UserRepository(RepositoryConnector store) {
        List<String> fieldsNames = new ArrayList<>();
        Field[] fields = User.Attributes.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers())) {
                try {
                    fieldsNames.add((String) field.get(new User.Attributes()));
                } catch (IllegalAccessException e) {
                    // Do nothing
                }
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
            pStmt.close();
            results = this.fromResultSetToListOfHashes(rs);
        } catch (SQLException e) {
            return target;
        }

        for (Map<String, Object> hash : results) {
            target.add(new User(hash));
        }

        return target;
    }

}
