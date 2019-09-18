package utils;

import java.util.List;
import java.util.ArrayList;

public class StoreUtils {

    public static String questionMarksForFields(List<String> listOfFields) {
        List<String> questionMarks = new ArrayList<>();
        int size = listOfFields.size();

        for (int i = 0; i < size; i++) {
            questionMarks.add("?");
        }

        return String.join(",", questionMarks);
    }

}
