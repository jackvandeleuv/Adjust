import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        InitDB.makeTables();
        InitDB.addTestData();
        try {
            new BoardGUI(1);

        } catch (InterruptedException ie) {
            System.out.println(ie.getMessage());
        }

    }
}