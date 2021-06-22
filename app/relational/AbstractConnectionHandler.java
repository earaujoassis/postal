package relational;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public abstract class AbstractConnectionHandler {

    protected void teardownStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

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
