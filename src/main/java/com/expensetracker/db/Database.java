package com.expensetracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens SQLite connections and creates the schema on first run.
 *  To use MySQL instead: change URL, add the mysql-connector-j dependency,
 *  and replace "AUTOINCREMENT" with "AUTO_INCREMENT" in the schema. */
public class Database {
    private static final String URL = "jdbc:sqlite:expenses.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        String sql = "CREATE TABLE IF NOT EXISTS expenses ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "date TEXT NOT NULL,"
                + "category TEXT NOT NULL,"
                + "description TEXT,"
                + "amount REAL NOT NULL)";
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialise database", e);
        }
    }
}
