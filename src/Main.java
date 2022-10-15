import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        String jbdcUrl = "database.db";
        try {
            Connection connection = DriverManager.getConnection(jbdcUrl);
            Statement statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("Error connecting to db");
            e.printStackTrace();
        }

    }
}