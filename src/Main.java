import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
//        try {
//            new BoardGUI();
//
//        } catch (InterruptedException ie) {
//            System.out.println(ie.getMessage());
//        }
        InitDB.makeTables();
        InitDB.addTestData();
        ReviewEngine.getNextCard(1);
    }
}