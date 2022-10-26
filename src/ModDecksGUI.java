import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ModDecksGUI implements ActionListener {
    List<DeckListItem> deckList = new ArrayList<>();
    private final JPanel deckPane = new JPanel();
    private final JButton createBtn;
    private final JButton modifyBtn;
    private final JButton renameBtn;
    private final JButton deleteBtn;
    private final JButton backBtn;
    private final JPanel pane;
    private final Connection conn;
    private final CardLayout controller;
    private final JPanel container;
    private final MainGUI mainMenu;
    private final DefaultListModel<DeckListItem> decksModel;
    private final JList<DeckListItem> decksListComp;

    public ModDecksGUI(JPanel modPane, CardLayout outerController, JPanel outerContainer, MainGUI mainMenuGUI) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        conn = DriverManager.getConnection(jbdcUrl);
        conn.setAutoCommit(false);
        container = outerContainer;
        controller = outerController;
        pane = modPane;
        mainMenu = mainMenuGUI;

        JLabel title = new JLabel("MODIFY DECKS");
        createBtn = new JButton("CREATE");
        createBtn.addActionListener(this);
        modifyBtn = new JButton("MODIFY");
        modifyBtn.addActionListener(this);
        renameBtn = new JButton("RENAME");
        renameBtn.addActionListener(this);
        deleteBtn = new JButton("DELETE");
        deleteBtn.addActionListener(this);
        backBtn = new JButton("BACK");
        backBtn.addActionListener(this);

        JPanel header = new JPanel();
        header.add(title);
        JPanel leftBar = new JPanel();
        leftBar.add(createBtn);
        leftBar.add(modifyBtn);
        leftBar.add(renameBtn);
        leftBar.add(deleteBtn);
        leftBar.add(backBtn);

        decksModel = new DefaultListModel<>();
        decksListComp = new JList<>(decksModel);
        decksListComp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        decksListComp.setFixedCellWidth(600);
        decksListComp.setFixedCellHeight(30);
        JScrollPane scroller = new JScrollPane(decksListComp);
        scroller.setSize(300, 300);

        this.updateDeckModel();

        pane.add(header);
        pane.add(leftBar);
        pane.add(deckPane);
        pane.revalidate();
        pane.repaint();
    }

    private synchronized void getUpdatedSummaries() throws SQLException {
        StringBuilder cardTotalsQ = new StringBuilder();
        cardTotalsQ.append("SELECT DECKS.ID, COALESCE(COUNT(CARDS.ID), 0) ");
        cardTotalsQ.append("FROM DECKS LEFT JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        cardTotalsQ.append("GROUP BY DECKS.ID ");
        cardTotalsQ.append("ORDER BY DECKS.NAME DESC ");

        StringBuilder toReviewQ = new StringBuilder();
        toReviewQ.append("SELECT DECKS.NAME, COALESCE(COUNT(CARDS.ID), 0) ");
        toReviewQ.append("FROM DECKS LEFT JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        toReviewQ.append("WHERE (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) ");
        toReviewQ.append("OR CARDS.ID IS NULL ");
        toReviewQ.append("GROUP BY DECKS.ID ");
        toReviewQ.append("ORDER BY DECKS.NAME DESC ");


        long currentTime = System.currentTimeMillis();
        PreparedStatement toReviewStmt = conn.prepareStatement(toReviewQ.toString());
        PreparedStatement cardTotalsStmt = conn.prepareStatement(cardTotalsQ.toString());
        toReviewStmt.setLong(1, currentTime);

        ResultSet rs1 = cardTotalsStmt.executeQuery();
        ResultSet rs2 = toReviewStmt.executeQuery();
        while (rs1.next()) {
            DeckListItem deck = new DeckListItem(rs1.getInt(1));
            deck.setCardTotal(rs1.getInt(2));
            deckList.add(deck);
        }

        int index = 0;
        while (rs2.next()) {
            DeckListItem deck = deckList.get(index);
            deck.setName(rs2.getString(1));
            deck.setReviewCount(rs2.getInt(2));
            index = index + 1;
        }

        conn.commit();
    }

    public synchronized void updateDeckModel() throws SQLException, ClassNotFoundException {
        this.getUpdatedSummaries();

        decksModel.removeAllElements();
        for (int i = 0; i < deckList.size(); i++) {
            decksModel.add(i, deckList.get(i));
        }
    }

    private synchronized void deleteDeck(int deckPK) throws ClassNotFoundException, SQLException {
        PreparedStatement deleteDeck = conn.prepareStatement("DELETE FROM DECKS WHERE ID = ?");
        deleteDeck.setInt(1, deckPK);
        deleteDeck.executeUpdate();
        conn.commit();

        StringBuilder delRelQuery = new StringBuilder();
        delRelQuery.append("DELETE FROM CARDS_TO_MOVES WHERE CARDS_ID IN ( ");
        delRelQuery.append("SELECT ID FROM CARDS WHERE DECKS_ID = ?) ");
        PreparedStatement delRels = conn.prepareStatement(delRelQuery.toString());
        delRels.setInt(1, deckPK);
        delRels.executeUpdate();
        conn.commit();

        PreparedStatement deleteCards = conn.prepareStatement("DELETE FROM CARDS WHERE DECKS_ID = ?");
        deleteCards.setInt(1, deckPK);
        deleteCards.executeUpdate();
        conn.commit();

        this.updateDeckModel();
        pane.revalidate();
        pane.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteBtn) {
            // If the user clicks delete, get the index of the item that was selected when they clicked delete.
            int index = decksListComp.getSelectedIndex();
            // -1 is returned by getSelectedIndex if not row was selected when the user clicked delete.

            // !!!!! Currently a bug if you add decks and delete them. Possibly decksPK is not being updated correctly
            if (index != -1) {
                DeckListItem deck = decksModel.getElementAt(index);
                int pk = deck.getDeckPK();
                try {
                    this.deleteDeck(pk);
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (e.getSource() == createBtn) {
            CreateDeckThread cdt = new CreateDeckThread();
            cdt.start();
        }

        if (e.getSource() == backBtn) {
            try {
                mainMenu.updateMainPane();
                controller.show(container, "main");
            } catch (SQLException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (e.getSource() == modifyBtn) {

        }
    }

    public class DeckListItem {
        private final int deckPK;
        private String name;
        private int cardTotal;
        private int reviewCount;

        public DeckListItem(int newDeckPK) {
            deckPK = newDeckPK;
        }

        public int getDeckPK() {
            return deckPK;
        }

        public void setReviewCount(int newReviewCount) {reviewCount = newReviewCount;}
        public void setCardTotal(int newCardTotal) {cardTotal = newCardTotal;}
        public void setName(String newName) {name = newName;}

        @Override
        public String toString() {
            return name + " DUE: " + reviewCount + "/" + cardTotal;
        }
    }

    public class CreateDeckThread extends Thread {
        @Override
        public void run() {
            new CreateDeckGUI();
        }
    }

    public final class CreateDeckGUI implements ActionListener {
        private JButton createButton;
        private JTextField enterName;
        private JFrame popup;

        public CreateDeckGUI() {
            popup = new JFrame();
            popup.setSize(400, 300);
            popup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JPanel createPane = new JPanel();
            popup.add(createPane);

            JLabel headLabel = new JLabel("CREATE A NEW DECK");
            enterName = new JTextField(15);
            createButton = new JButton("CREATE");
            createButton.addActionListener(this);
            createPane.add(headLabel);
            createPane.add(enterName);
            createPane.add(createButton);
            popup.setVisible(true);
        }

        private synchronized void createDeck(String name) throws ClassNotFoundException, SQLException {
            // Pane, conn, and data modified by makeDeckList are mutable and between threads, so the thread
            // needs to be synchronized.
            PreparedStatement createStmt = conn.prepareStatement("INSERT INTO DECKS(ID, NAME) VALUES(NULL, ?)");
            createStmt.setString(1, name.strip());
            createStmt.executeUpdate();
            conn.commit();

            ModDecksGUI.this.updateDeckModel();
            pane.revalidate();
            pane.repaint();

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == createButton)   {
                if (!enterName.getText().isEmpty()) {
                    String name = enterName.getText();
                    try {
                        this.createDeck(name);
                    } catch (ClassNotFoundException | SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                    popup.dispose();
                }
            }
        }
    }
}
