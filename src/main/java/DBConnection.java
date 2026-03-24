import java.sql.*;

public class DBConnection {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = "jdbc:mysql://localhost:3306/insurance_db";
            connection = DriverManager.getConnection(url, "root", "123456");
        }
        return connection;
    }
}