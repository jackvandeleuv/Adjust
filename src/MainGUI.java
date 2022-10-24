import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public class MainGUI {
    private JButton[] deckButtons;
    private List<String> nameList;
    private List<Integer> reviewCounts;

    public MainGUI(JPanel pane) {
        try {
            QueryDB queryDB = new QueryDB();
            QueryDB.DeckSummary decksSummary = queryDB.getDecksSummary();

            nameList = decksSummary.getNameList();
            reviewCounts = decksSummary.getReviewCounts();
            deckButtons = new JButton[nameList.size()];
            for (int i = 0; i < deckButtons.length; i++) {
                JButton button = new JButton();
                JLabel review = new JLabel("Review");
                button.add(review);
                deckButtons[i] = button;
            }

        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex.getMessage());
        }

        for (int i = 0; i < deckButtons.length; i++) {
            JLabel deckName = new JLabel(nameList.get(i));
            JLabel reviewCount = new JLabel(String.valueOf(reviewCounts.get(i)));
            pane.add(deckButtons[i]);
            pane.add(deckName);
            pane.add(reviewCount);
        }

        pane.repaint();
        pane.revalidate();
    }
}
