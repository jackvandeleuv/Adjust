import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        InitDB.makeTables();
        InitDB.addTestData();
        JFrame window = new JFrame();
        window.setSize(800, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            new BoardGUI(1, window);

        } catch (InterruptedException ie) {
            System.out.println(ie.getMessage());
        }
//        QueryDB.getDeckSummary();

    }
}