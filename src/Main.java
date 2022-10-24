import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        InitDB.makeTables();
        InitDB.addTestData();
        JFrame window = new JFrame();
        window.setSize(800, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel pane = new JPanel();
        window.add(pane);

        JMenuBar menuBar = new JMenuBar();
        JMenu opt1 = new JMenu("Main Menu");

        JMenu opt2 = new JMenu("Modify Decks");
        JMenu opt3 = new JMenu("Settings");

        menuBar.add(opt1);
        menuBar.add(opt2);
        menuBar.add(opt3);

        window.setJMenuBar(menuBar);
        window.setVisible(true);
        try {
            new BoardGUI(1, window);
        } catch (InterruptedException ie) {
            System.out.println(ie.getMessage());
        }
//        QueryDB.getDeckSummary();
//        new MainGUI(pane);

    }
}