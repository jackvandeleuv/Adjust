import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException {
        JFrame window = new JFrame();
        window.setSize(800, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel container = new JPanel();
        CardLayout controller = new CardLayout();
        container.setLayout(controller);
        window.add(container);

        JMenuBar menuBar = new JMenuBar();
        JMenu opt1 = new JMenu("Main Menu");

        JMenu opt2 = new JMenu("Modify Decks");
        JMenu opt3 = new JMenu("Settings");

        menuBar.add(opt1);
        menuBar.add(opt2);
        menuBar.add(opt3);

        window.setJMenuBar(menuBar);
        window.setVisible(true);

//        InitDB.queryDB();
//        new ModDecksGUI(pane);
//        new AddLinesGUI(pane, 2);
//        try {
//            new BoardGUI(2, pane);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

    }
}