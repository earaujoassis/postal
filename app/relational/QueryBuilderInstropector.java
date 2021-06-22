package relational;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import relational.SqlTable;
import relational.SqlField;

public abstract class QueryBuilderInstropector extends AbstractConnectionHandler {

    protected String questionMarksForFields(int size) {
        List<String> questionMarks = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            questionMarks.add("?");
        }

        return String.join(",", questionMarks);
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

    protected String getSqlFieldKey(Field field) {
        String value = field.getAnnotation(SqlField.class).name();
        return value.isEmpty() ? field.getName() : value;
    }

    protected String tableName(Class modelClass) {
        if (modelClass.isAnnotationPresent(SqlTable.class)) {
            return this.getSqlTableName(modelClass);
        } else {
            throw new QueryBuilderException("Class %s doesn't have SqlTable annotation");
        }
    }

    protected String getSqlTableName(Class modelClass) {
        SqlTable tableDef = ((SqlTable) modelClass.getAnnotation(SqlTable.class));
        if (tableDef == null) {
            throw new QueryBuilderException(
                String.format("Error: Class %s isn't annotated with SqlTable",
                    modelClass.getSimpleName()));
        }
        String value = tableDef.name();
        return value.isEmpty() ? String.format("%ss", modelClass.getSimpleName().toLowerCase()) : value;
    }

    protected List<Map<String, Object>> fromResultSetToListOfHashes(ResultSet rs, Class modelClass)
    throws SQLException {
        List<Map<String, Object>> target = new ArrayList<>();
        List<String> listOfFields = getFieldsNames(modelClass);

        while (rs.next()) {
            Map<String, Object> hash = new HashMap<String, Object>();
            for (String attribute : listOfFields) {
                hash.put(attribute, rs.getObject(attribute));
            }
            target.add(hash);
        }

        return target;
    }

    protected List<Map<String, Object>> emptyList() {
        return (new ArrayList<>());
    }

}
