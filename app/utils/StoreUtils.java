package utils;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;

import models.SqlField;

public class StoreUtils {

    public static String questionMarksForFields(List<String> listOfFields) {
        List<String> questionMarks = new ArrayList<>();
        int size = listOfFields.size();

        for (int i = 0; i < size; i++) {
            questionMarks.add("?");
        }

        return String.join(",", questionMarks);
    }

    public static String getSqlFieldKey(Field field) {
        String value = field.getAnnotation(SqlField.class).name();
        return value.isEmpty() ? field.getName() : value;
    }

}
