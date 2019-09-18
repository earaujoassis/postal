package repositories;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractEntityRepository {

    protected RepositoryConnector store;
    protected List<String> listOfFields;
    protected String allFields;

    protected List<Map<String, Object>> fromResultSetToListOfHashes(ResultSet rs) throws SQLException {
        List<Map<String, Object>> target = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> hash = new HashMap<String, Object>();
            for (String attribute : this.listOfFields) {
                hash.put(attribute, rs.getObject(attribute));
            }
        }

        return target;
    }

}
