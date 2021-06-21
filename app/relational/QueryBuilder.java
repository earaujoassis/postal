package relational;

import com.google.inject.Guice;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PGobject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import repositories.RepositoryConnector;

public class QueryBuilder extends QueryBuilderInstropector {

    private enum Mode {
        SELECT,
        INSERT,
        UPDATE
    }

    private Class modelClass;
    private Mode mode;

    private RepositoryConnector connector = null;

    private String select = null;
    private String from = null;
    private String where = null;
    private String optionals = null;
    private List<Object> arguments = null;

    private String insert = null;

    private String update = null;
    private String setters = null;

    public QueryBuilder(Class modelClass) {
        this.modelClass = modelClass;
    }

    public static QueryBuilder forModel(Class modelClass) {
        return (new QueryBuilder(modelClass));
    }

    public QueryBuilder bind(RepositoryConnector connector) {
        this.connector = connector;
        return this;
    }

    public QueryBuilder select() {
        this.mode = Mode.SELECT;
        this.select = String.format("SELECT %s",
            String.join(",", this.getFieldsNames(this.modelClass))).trim();
        return this;
    }

    public QueryBuilder select(String selectOperator) {
        this.mode = Mode.SELECT;
        this.select = String.format("SELECT %s",
            selectOperator).trim();
        return this;
    }

    public QueryBuilder from() {
        this.from = String.format("FROM %s",
            this.getSqlTableName(modelClass)).trim();
        return this;
    }

    public QueryBuilder where(String where, Object queryArgmnt) {
        this.where = String.format("WHERE %s", where).trim();
        this.addArgument(queryArgmnt);
        return this;
    }

    public <U> QueryBuilder and(String whereAnd, Object queryArgmnt) {
        this.where = String.format("%s AND %s",
            this.where, whereAnd).trim();
        this.addArgument(queryArgmnt);
        return this;
    }

    public <U> QueryBuilder and(String whereAnd) {
        this.where = String.format("%s AND %s",
            this.where, whereAnd).trim();
        return this;
    }

    public QueryBuilder or(String whereOr, Object queryArgmnt) {
        this.where = String.format("%s OR %s",
            this.where, whereOr).trim();
        this.addArgument(queryArgmnt);
        return this;
    }

    public QueryBuilder or(String whereOr) {
        this.where = String.format("%s OR %s",
            this.where, whereOr).trim();
        return this;
    }

    public QueryBuilder orderByDesc(String field) {
        this.optionals = String.format("%s ORDER BY %s DESC",
            this.getSafeOptionals(), field).trim();
        return this;
    }

    public QueryBuilder orderByAsc(String field) {
        this.optionals = String.format("%s ORDER BY %s ASC",
            this.getSafeOptionals(), field).trim();
        return this;
    }

    public QueryBuilder limit(int number) {
        this.optionals = String.format("%s LIMIT(%d)",
            this.getSafeOptionals(), number).trim();
        return this;
    }

    public List<Map<String, Object>> getAll() {
        if (this.mode != Mode.SELECT) {
            throw new QueryBuilderException("Error: trying to commit SELECT query; mode mismatch");
        }

        String sql = String.format("%s %s %s %s",
            this.select, this.from, this.getSafeWhere(), this.getSafeOptionals()).trim();
        List<Map<String, Object>> results = this.emptyList();
        Connection connection = null;
        PreparedStatement pStmt;

        try {
            connection = this.connector.getConnectionFromPool();
            pStmt = connection.prepareStatement(sql);
            int index = 1;
            if (this.arguments != null) {
                for (Object argument : this.arguments) {
                    pStmt.setObject(index++, argument);
                }
            }
            results = this.fromResultSetToListOfHashes(pStmt.executeQuery(), modelClass);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            teardownConnection(connection);
        }

        return results;
    }

    public Map<String, Object> getOne() {
        List<Map<String, Object>> results = this.getAll();

        if (results.size() > 0) {
            return results.get(0);
        }

        return null;
    }

    public int getCount() {
        if (this.mode != Mode.SELECT) {
            throw new QueryBuilderException("Error: trying to commit SELECT query; mode mismatch");
        }

        String sql = String.format("%s %s %s %s",
            this.select, this.from, this.getSafeWhere(), this.getSafeOptionals()).trim();
        int result = 0;
        Connection connection = null;
        PreparedStatement pStmt;
        ResultSet resultSet;

        try {
            connection = this.connector.getConnectionFromPool();
            pStmt = connection.prepareStatement(sql);
            int index = 1;
            if (this.arguments != null) {
                for (Object argument : this.arguments) {
                    pStmt.setObject(index++, argument);
                }
            }
            resultSet = pStmt.executeQuery();
            resultSet.next();
            result = resultSet.getInt(1);
            pStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            teardownConnection(connection);
        }

        return result;
    }

    public QueryBuilder insert() {
        List<String> fieldsNames = this.getSettableFieldsNames(this.modelClass);
        this.mode = Mode.INSERT;
        this.insert = String.format("INSERT INTO %s(%s) VALUES(%s)",
            this.getSqlTableName(this.modelClass),
            String.join(",", fieldsNames),
            this.questionMarksForFields(fieldsNames.size())).trim();

        return this;
    }

    public <T> boolean instance(T obj) {
        if (this.mode != Mode.INSERT) {
            throw new QueryBuilderException("Error: trying to commit INSERT query; mode mismatch");
        }

        List<Field> fields = this.getSettableFields(this.modelClass);
        int fieldsSize = fields.size();
        Connection connection = null;
        PreparedStatement pStmt;

        try {
            connection = this.connector.getConnectionFromPool();
            pStmt = connection.prepareStatement(this.insert);
            for (int i = 1; i < fieldsSize + 1; i++) {
                Field field = fields.get(i - 1);
                Object entry = null;

                try {
                    entry = field.get(obj);
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
        } finally {
            teardownConnection(connection);
        }
    }

    public QueryBuilder update() {
        this.mode = Mode.UPDATE;
        this.update = String.format("UPDATE %s",
            this.getSqlTableName(this.modelClass)).trim();
        return this;
    }

    public QueryBuilder set(String setter, Object queryArgmnt) {
        this.addArgument(queryArgmnt);
        if (this.setters == null) {
            this.setters = String.format("SET %s", setter).trim();
            return this;
        }

        this.setters = String.format("%s, %s",
            this.setters, setter).trim();
        return this;
    }

    public boolean commit() {
        if (this.mode != Mode.UPDATE) {
            throw new QueryBuilderException("Error: trying to commit UPDATE query; mode mismatch");
        }

        String sql = String.format("%s %s %s %s",
            this.update, this.setters, this.getSafeWhere(), this.getSafeOptionals()).trim();
        Connection connection = null;
        PreparedStatement pStmt;

        try {
            connection = this.connector.getConnectionFromPool();
            pStmt = connection.prepareStatement(sql);
            int index = 1;
            for (Object argument : arguments) {
                try {
                    pStmt.setObject(index, argument);
                } catch (PSQLException e) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    PGobject jsonObject = new PGobject();
                    String metadataJSONString;

                    try {
                        metadataJSONString = objectMapper.writeValueAsString(argument);
                    } catch (JsonProcessingException exc) {
                        e.printStackTrace();
                        metadataJSONString = "null";
                    }

                    jsonObject.setType("json");
                    jsonObject.setValue(metadataJSONString);
                    pStmt.setObject(index, jsonObject);
                }
                index++;
            }
            pStmt.execute();
            pStmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new QueryBuilderException("No arguments; cannot UPDATE");
        } finally {
            teardownConnection(connection);
        }
    }

    private String getSafeWhere() {
        if (this.where == null) {
            return "";
        }

        return this.where;
    }

    private String getSafeOptionals() {
        if (this.optionals == null) {
            return "";
        }

        return this.optionals;
    }

    private void addArgument(Object argument) {
        if (this.arguments == null) {
            this.arguments = new ArrayList<>();
        }

        this.arguments.add(argument);
    }

    private void teardownConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
