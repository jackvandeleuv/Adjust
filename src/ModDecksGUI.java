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
    private final CardLayout controller;
    private final JPanel container;
    private final MainGUI mainMenu;
    private final DefaultListModel<DeckListItem> decksModel;
    private final JList<DeckListItem> decksListComp;
    private final JPanel cardsPane;

    public ModDecksGUI(JPanel modPane, CardLayout outerController, JPanel outerContainer, MainGUI mainMenuGUI, JPanel outerCardsPane) throws SQLException, ClassNotFoundException {
        container = outerContainer;
        controller = outerController;
        pane = modPane;
        cardsPane = outerCardsPane;
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
        decksListComp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        decksListComp.setFixedCellWidth(600);
        decksListComp.setFixedCellHeight(30);
        JScrollPane scroller = new JScrollPane(decksListComp);
        scroller.setSize(300, 300);

        this.updateDeckModel();

        pane.add(header);
        pane.add(leftBar);
        deckPane.add(decksListComp);
        pane.add(deckPane);
        pane.revalidate();
        pane.repaint();
    }

    private synchronized void updateDeckList() throws SQLException {
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
        PreparedStatement toReviewStmt = Main.conn.prepareStatement(toReviewQ.toString());
        PreparedStatement cardTotalsStmt = Main.conn.prepareStatement(cardTotalsQ.toString());
        toReviewStmt.setLong(1, currentTime);

        ResultSet rs1 = cardTotalsStmt.executeQuery();
        ResultSet rs2 = toReviewStmt.executeQuery();

        deckList.clear();
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
        Main.conn.commit();
    }

    public synchronized void updateDeckModel() throws SQLException, ClassNotFoundException {
        this.updateDeckList();

        decksModel.removeAllElements();
        for (int i = 0; i < deckList.size(); i++) {
            decksModel.add(i, deckList.get(i));
        }
        pane.revalidate();
        pane.repaint();
    }

    private synchronized void deleteDeck(int deckPK) throws ClassNotFoundException, SQLException {
        System.out.println("ModDecksGUI -> deleteDeck using sql");
        PreparedStatement deleteDeck = Main.conn.prepareStatement("DELETE FROM DECKS WHERE ID = ?");
        deleteDeck.setInt(1, deckPK);
        deleteDeck.executeUpdate();
        Main.conn.commit();

        StringBuilder delRelQuery = new StringBuilder();
        delRelQuery.append("DELETE FROM CARDS_TO_MOVES WHERE CARDS_ID IN ( ");
        delRelQuery.append("SELECT ID FROM CARDS WHERE DECKS_ID = ?) ");
        PreparedStatement delRels = Main.conn.prepareStatement(delRelQuery.toString());
        delRels.setInt(1, deckPK);
        delRels.executeUpdate();
        Main.conn.commit();

        PreparedStatement deleteCards = Main.conn.prepareStatement("DELETE FROM CARDS WHERE DECKS_ID = ?");
        deleteCards.setInt(1, deckPK);
        deleteCards.executeUpdate();
        Main.conn.commit();

        this.updateDeckModel();
        pane.revalidate();
        pane.repaint();
    }

    private void createDeck(String name) throws ClassNotFoundException, SQLException {
        // Pane, conn, and data modified by makeDeckList are mutable and between threads, so the thread
        // needs to be synchronized.
        PreparedStatement createStmt = Main.conn.prepareStatement("INSERT INTO DECKS(ID, NAME) VALUES(NULL, ?)");
        createStmt.setString(1, name.strip());
        createStmt.executeUpdate();
        Main.conn.commit();

        this.updateDeckModel();
        pane.revalidate();
        pane.repaint();
    }

    private void renameDeck(int pk, String name) throws ClassNotFoundException, SQLException {
        // Pane, conn, and data modified by makeDeckList are mutable and between threads, so the thread
        // needs to be synchronized.
        PreparedStatement renameStmt = Main.conn.prepareStatement("UPDATE DECKS SET NAME = ? WHERE ID = ?");
        renameStmt.setString(1, name.strip());
        renameStmt.setInt(2, pk);
        renameStmt.executeUpdate();
        Main.conn.commit();

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
            String deckName = JOptionPane.showInputDialog("Enter the name for your new deck:");
            if (deckName != null && !deckName.isBlank()) {
                try {
                    this.createDeck(deckName);
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (e.getSource() == renameBtn) {
            int index = decksListComp.getSelectedIndex();
            if (index != -1) {
                DeckListItem deck = decksModel.getElementAt(index);
                int pk = deck.getDeckPK();
                String newName = JOptionPane.showInputDialog("Enter the updated name:");
                if (newName != null && !newName.isBlank()) {
                    try {
                        this.renameDeck(pk, newName);
                    } catch (SQLException | ClassNotFoundException ex){
                        throw new RuntimeException(ex);
                    }
                }
            }
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
            int index = decksListComp.getSelectedIndex();
            DeckListItem deck = decksModel.getElementAt(index);
            int deckPK = deck.getDeckPK();
            try {
                cardsPane.removeAll();
                new AddCardsGUI(cardsPane, deckPK, container, controller, this);
                controller.show(container, "cards");
            } catch (ClassNotFoundException | SQLException ex) {
                throw new RuntimeException(ex);
            }
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
}


