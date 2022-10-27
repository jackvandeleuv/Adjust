import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static Connection conn;

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        try {
            Class.forName("org.sqlite.JDBC");
            String jbdcUrl = "jdbc:sqlite:database.db";
            conn = DriverManager.getConnection(jbdcUrl);
            conn.setAutoCommit(false);
            new MainMenuGUI();
        } catch (ClassNotFoundException | SQLException  ex) {
            System.out.println("Could not successfully start program");
            System.out.println(ex.getMessage());
        }
        //InitDB.queryDB();
    }
}