import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ModDecksGUI implements ActionListener {
    private JList<String> deckList;
    private final JPanel deckPane;
    private final JButton createBtn;
    private final JButton modifyBtn;
    private final JButton renameBtn;
    private final JButton deleteBtn;

    private List<String> nameList;
    private List<Integer> reviewCount;
    private List<Integer> cardTotals;
    private List<Integer> deckPKs;
    private final JPanel pane;

    private final Connection conn;

    public ModDecksGUI(JPanel newPane) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        conn = DriverManager.getConnection(jbdcUrl);
        conn.setAutoCommit(false);
        pane = newPane;

        JLabel title = new JLabel("MODIFY DECKS");
        createBtn = new JButton("CREATE");
        createBtn.addActionListener(this);
        modifyBtn = new JButton("MODIFY");
        modifyBtn.addActionListener(this);
        renameBtn = new JButton("RENAME");
        renameBtn.addActionListener(this);
        deleteBtn = new JButton("DELETE");
        deleteBtn.addActionListener(this);

        JPanel header = new JPanel();
        header.add(title);
        JPanel leftBar = new JPanel();
        leftBar.add(createBtn);
        leftBar.add(modifyBtn);
        leftBar.add(renameBtn);
        leftBar.add(deleteBtn);
        deckPane = new JPanel();

        this.makeDeckList();

        pane.removeAll();
        pane.add(header);
        pane.add(leftBar);
        pane.add(deckPane);
        pane.revalidate();
        pane.repaint();
    }

    private synchronized void getUpdatedSummaries() throws SQLException {
        StringBuilder toReviewQ = new StringBuilder();
        toReviewQ.append("SELECT DECKS.NAME, COALESCE(COUNT(CARDS.ID), 0) ");
        toReviewQ.append("FROM DECKS LEFT JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        toReviewQ.append("WHERE (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) ");
        toReviewQ.append("OR CARDS.ID IS NULL ");
        toReviewQ.append("GROUP BY DECKS.ID ");
        toReviewQ.append("ORDER BY DECKS.NAME DESC ");

        StringBuilder cardTotalsQ = new StringBuilder();
        cardTotalsQ.append("SELECT COALESCE(COUNT(CARDS.ID), 0), DECKS.ID ");
        cardTotalsQ.append("FROM DECKS LEFT JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        cardTotalsQ.append("GROUP BY DECKS.ID ");
        cardTotalsQ.append("ORDER BY DECKS.NAME DESC ");

        nameList = new ArrayList<String>();
        reviewCount = new ArrayList<Integer>();
        cardTotals = new ArrayList<Integer>();
        deckPKs = new ArrayList<Integer>();

        long currentTime = System.currentTimeMillis();
        PreparedStatement toReviewStmt = conn.prepareStatement(toReviewQ.toString());
        PreparedStatement cardTotalsStmt = conn.prepareStatement(cardTotalsQ.toString());
        toReviewStmt.setLong(1, currentTime);

        ResultSet rs1 = toReviewStmt.executeQuery();
        ResultSet rs2 = cardTotalsStmt.executeQuery();
        while (rs1.next()) {
            nameList.add(rs1.getString(1));
            reviewCount.add(rs1.getInt(2));
        }

        while (rs2.next()) {
            cardTotals.add(rs2.getInt(1));
            deckPKs.add(rs2.getInt(2));
        }
        conn.commit();
    }

    public synchronized void makeDeckList() throws SQLException, ClassNotFoundException {
        this.getUpdatedSummaries();
        String[] deckLabels = new String[nameList.size()];

        if (deckLabels.length == 0) {
            deckPane.removeAll();
            deckPane.add(new JLabel("To create a deck, click CREATE!"));
        }

        if (deckLabels.length != 0) {
            deckPane.removeAll();
            for (int i = 0; i < deckLabels.length; i++) {
                StringBuilder label = new StringBuilder();
                label.append(nameList.get(i));
                label.append("  DUE: ");
                label.append(reviewCount.get(i));
                label.append("  TOTAL: ");
                label.append(cardTotals.get(i));
                deckLabels[i] = label.toString();
            }

            deckList = new JList<>(deckLabels);
            deckPane.add(deckList);
        }
    }


    private synchronized void deleteDeck(int deckPK) throws ClassNotFoundException, SQLException {
        PreparedStatement deleteDeck = conn.prepareStatement("DELETE FROM DECKS WHERE ID = ?");
        deleteDeck.setInt(1, deckPK);
        deleteDeck.executeUpdate();
        StringBuilder delRelQuery = new StringBuilder();
        delRelQuery.append("DELETE FROM CARDS_TO_MOVES JOIN CARDS ON CARDS_TO_MOVES.CARDS_ID = CARDS.ID ");
        delRelQuery.append("WHERE DECKS_ID = ? ");
        PreparedStatement delRels = conn.prepareStatement(delRelQuery.toString());
        delRels.setInt(1, deckPK);
        delRels.executeUpdate();
        PreparedStatement deleteCards = conn.prepareStatement("DELETE FROM CARDS WHERE DECKS_ID = ?");
        deleteCards.setInt(1, deckPK);
        deleteCards.executeUpdate();
        conn.commit();

        this.makeDeckList();
        pane.revalidate();
        pane.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteBtn && deckList != null) {
            // If the user clicks delete, get the index of the item that was selected when they clicked delete.
            int index = deckList.getSelectedIndex();
            // -1 is returned by getSelectedIndex if not row was selected when the user clicked delete.

            // !!!!! Currently a bug if you add decks and delete them. Possibly decksPK is not being updated correctly
            if (index != -1) {
                int pk = deckPKs.get(index);
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
            System.out.println("Called createDeck");
            // Pane, conn, and data modified by makeDeckList are mutable and between threads, so the thread
            // needs to be synchronized.
            PreparedStatement createStmt = conn.prepareStatement("INSERT INTO DECKS(ID, NAME) VALUES(NULL, ?)");
            createStmt.setString(1, name.strip());
            createStmt.executeUpdate();
            conn.commit();

            ModDecksGUI.this.makeDeckList();
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
