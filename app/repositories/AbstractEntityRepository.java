package repositories;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import models.SqlField;

public abstract class AbstractEntityRepository {

    protected RepositoryConnector store;
    protected List<String> listOfFields;
    protected String allFields;

    protected List<Map<String, Object>> fromResultSetToListOfHashes(ResultSet rs)
    throws SQLException {
        List<Map<String, Object>> target = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> hash = new HashMap<String, Object>();
            for (String attribute : this.listOfFields) {
                hash.put(attribute, rs.getObject(attribute));
            }
            target.add(hash);
        }

        return target;
    }

    protected String questionMarksForFields() {
        List<String> questionMarks = new ArrayList<>();
        int size = this.listOfFields.size();

        for (int i = 0; i < size; i++) {
            questionMarks.add("?");
        }

        return String.join(",", questionMarks);
    }

    protected String questionMarksForFields(int size) {
        List<String> questionMarks = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            questionMarks.add("?");
        }

        return String.join(",", questionMarks);
    }

    protected String getSqlFieldKey(Field field) {
        String value = field.getAnnotation(SqlField.class).name();
        return value.isEmpty() ? field.getName() : value;
    }

    protected List<Field> getFields(Class modelClass) {
        List<Field> fieldsList = new ArrayList<>();
        Field[] fields = modelClass.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers()) && field.isAnnotationPresent(SqlField.class)) {
                fieldsList.add(field);
            }
        }
        return fieldsList;
    }

    protected List<String> getFieldsNames(Class modelClass) {
        List<String> fieldsNames = new ArrayList<>();
        Field[] fields = modelClass.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers()) && field.isAnnotationPresent(SqlField.class)) {
                fieldsNames.add(this.getSqlFieldKey(field));
            }
        }
        return fieldsNames;
    }

    protected List<Field> getSettableFields(Class modelClass) {
        List<Field> fieldsList = new ArrayList<>();
        Field[] fields = modelClass.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers()) &&
                field.isAnnotationPresent(SqlField.class) &&
                field.getAnnotation(SqlField.class).settable()) {
                fieldsList.add(field);
            }
        }
        return fieldsList;
    }

    protected List<String> getSettableFieldsNames(Class modelClass) {
        List<String> fieldsNames = new ArrayList<>();
        Field[] fields = modelClass.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers()) &&
                field.isAnnotationPresent(SqlField.class) &&
                field.getAnnotation(SqlField.class).settable()) {
                fieldsNames.add(this.getSqlFieldKey(field));
            }
        }
        return fieldsNames;
    }

}
