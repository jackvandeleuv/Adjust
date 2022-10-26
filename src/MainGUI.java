import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MainGUI {
    private JButton[] deckButtons;
    private List<String> nameList;
    private List<Integer> reviewCounts;

    public MainGUI() {

        JFrame window = new JFrame();
        window.setSize(800, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel container = new JPanel();
        CardLayout controller = new CardLayout();
        container.setLayout(controller);
        window.add(container);

        JPanel mainPane = new JPanel();
        JPanel boardPane = new JPanel();
        JPanel modPane = new JPanel();
        JPanel cardsPane = new JPanel();

        new BoardGUI(boardPane);

        container.add(mainPane, 1);
        container.add(boardPane, 2);
        container.add(modPane, 3);
        container.add(cardsPane, 4);

        JMenuBar menuBar = new JMenuBar();
        JMenu opt1 = new JMenu("Main Menu");
        JMenu opt2 = new JMenu("Modify Decks");
        menuBar.add(opt1);
        menuBar.add(opt2);

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
            mainPane.add(deckButtons[i]);
            mainPane.add(deckName);
            mainPane.add(reviewCount);
        }

        mainPane.repaint();
        mainPane.revalidate();
    }
}
