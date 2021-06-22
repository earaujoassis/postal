package relational;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractConnectionHandler {

    protected void teardownConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
