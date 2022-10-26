import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class MainGUI implements ActionListener {
    private List<String> nameList;
    private List<Integer> reviewCounts;
    private final JPanel modPane;
    private final CardLayout controller;
    private final JPanel container;
    private final DefaultListModel<QueryDB.DeckSummary> decksModel;
    private final JList<QueryDB.DeckSummary> decksListComp;
    private final JPanel mainPane;
    private final JPanel cardsPane = new JPanel();
    private final JButton reviewBtn = new JButton("Review Deck");
    private final JButton modBtn = new JButton("Modify Decks");
    private final JPanel boardPane = new JPanel();
    private final ModDecksGUI modGUI;

    public MainGUI() throws InterruptedException, SQLException, ClassNotFoundException {
        JFrame window = new JFrame();
        window.setSize(800, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        container = new JPanel();
        controller = new CardLayout();
        container.setLayout(controller);
        window.add(container);

        decksModel = new DefaultListModel<>();
        decksListComp = new JList<>(decksModel);
        decksListComp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        decksListComp.setFixedCellWidth(600);
        decksListComp.setFixedCellHeight(30);
        JScrollPane scroller = new JScrollPane(decksListComp);
        scroller.setSize(300, 300);

        mainPane = new JPanel();
        mainPane.setSize(300, 300);
        mainPane.add(scroller);
        mainPane.add(reviewBtn);
        mainPane.add(modBtn);

        modBtn.addActionListener(this);
        reviewBtn.addActionListener(this);

        modPane = new JPanel();

        container.add(mainPane, "main");
        container.add(boardPane, "board");
        container.add(modPane, "mod");
        container.add(cardsPane, "cards");

        modGUI = new ModDecksGUI(modPane, controller, container, this, cardsPane);

        this.updateMainPane();

        window.setVisible(true);
    }

    public void updateMainPane() throws SQLException, ClassNotFoundException {
        decksModel.removeAllElements();
        QueryDB queryDB = new QueryDB();
        List<QueryDB.DeckSummary> deckSums = queryDB.getDeckSummaries();

        for (int i = 0; i < deckSums.size(); i++) {
            decksModel.add(i, deckSums.get(i));
        }

        mainPane.repaint();
        mainPane.revalidate();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == modBtn) {
            try {
                modGUI.updateDeckModel();
                controller.show(container, "mod");
            } catch (SQLException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (e.getSource() == reviewBtn) {
            int selIndex = decksListComp.getSelectedIndex();
            if (selIndex != -1) {
                QueryDB.DeckSummary selDeck = decksModel.get(selIndex);
                int selDeckPK = selDeck.getDeckPK();
                try {
                    new BoardGUI(selDeckPK, boardPane, container, controller, this);
                    controller.show(container, "board");
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

        }
    }
}
