import javax.swing.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MainGUI {
    private JButton[] deckSummaries;

    public MainGUI() {
        try {
            Map<String, Integer> decksSummary = QueryDB.getDecksSummary();
            deckSummaries = new JButton[decksSummary.size()];
            decksSummary.forEach();
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }
}
