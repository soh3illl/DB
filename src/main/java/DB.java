import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DB {
    private final String USERNAME = "root";
    private final String PASSWORD = "password";
    private final String DB_URL = "jdbc:mysql://localhost:3306/BankSystem";

    private Connection connection;
    private Statement statement;

    private String query = "[QUERY] [TABLE]";

    public String getQuery() {
        return query;
    }

    public DB() {
        try {
            this.connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            this.statement = connection.createStatement();
        } catch (
                SQLException e) {
            e.printStackTrace();
        }
    }

    public DB table(String table) {
        if (table.isEmpty()) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        this.query = query.replace("[TABLE]", table);

        return this;
    }

    public DB insert(List<String> keys, List<String> values) {
        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("Keys doesn't match values");
        }

        query = query.replace("[QUERY]", "INSERT INTO");

        // add keys to the query
        query += "(";
        query += keys.stream().collect(Collectors.joining(", "));
        query += ")";

        // add values
        query += " VALUES(";
        query += values.stream().collect(Collectors.joining(", "));
        query += ")";

        return this;
    }

    public DB select() {
        return this.select("*");
    }

    public DB select(String... fields) {
        String selectQuery = "SELECT [FIELDS] FROM";
        String fields_str = Arrays.asList(fields).stream().collect(Collectors.joining(", "));
        selectQuery = selectQuery.replace("[FIELDS]", fields_str);

        this.query = query.replace("[QUERY]", selectQuery);

        return this;
    }

    public DB update(List<String> keys, List<String> values) {
        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("Keys doesn't match values");
        }

        this.query = query.replace("[QUERY]", "UPDATE");
        this.query += " SET ";

        for (int i = 0; i < keys.size(); i++) {
            query += String.format("%s = %s", keys.get(i), values.get(i));

            if (i != keys.size() - 1) {
                query += ", ";
            }
        }

        return this;
    }

    public DB where(String column, String value) {
        return where(column, "=", value);
    }

    public DB where(String column, String operator, String value) {
        this.query += String.format(" WHERE %s %s %s", column, operator, value);

        return this;
    }

    public DB delete() {
        this.query = this.query.replace("[QUERY]", "DELETE FROM");

        return this;
    }

    public ResultSet get() {
        try {
            ResultSet result = this.statement.executeQuery(this.getQuery());
            return result;
        } catch (
                SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void execute() {
        try {
            this.statement.executeUpdate(this.getQuery());
        } catch (
                SQLException e) {
            e.printStackTrace();
        }
    }
}
