import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddCardsGUI implements ActionListener {
    private List<lineListItem> lines;
    private List<cardListItem> cards;
    private final DefaultListModel<lineListItem> linesModel;
    private final DefaultListModel<cardListItem> cardsModel;
    private final JList<cardListItem> cardsListComp;
    private final JList<lineListItem> linesListComp;
    private final JPanel pane;
    private final int deckID;
    private final JButton makeCardsBtn;
    private final JButton deleteBtn;
    private int lastCardPK;
    private final JComboBox<String> clrSel;
    private final ModDecksGUI modGUI;
    private final CardLayout controller;
    private final JPanel container;
    private final JButton backBtn = new JButton("Back");

    public AddCardsGUI(JPanel cardsPane, int deckPK, JPanel outerContainer, CardLayout outerController, ModDecksGUI modGUIObj) throws ClassNotFoundException, SQLException {
        modGUI = modGUIObj;
        controller = outerController;
        container = outerContainer;
        pane = cardsPane;
        deckID = deckPK;

        linesModel = new DefaultListModel<>();
        linesListComp = new JList<>(linesModel);
        linesListComp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        linesListComp.setFixedCellWidth(600);
        linesListComp.setFixedCellHeight(30);
        JScrollPane totalScroller = new JScrollPane(linesListComp);
        totalScroller.setSize(600, 400);
        this.queryTotalLines("", "");
        this.updateLineModel();

        makeCardsBtn = new JButton("Make Card(s)");
        makeCardsBtn.addActionListener(this);
        String[] clrOpts = {"White", "Black"};
        clrSel = new JComboBox<>(clrOpts);
        clrSel.addActionListener(this);
        deleteBtn = new JButton("Delete Card(s)");
        deleteBtn.addActionListener(this);

        cardsModel = new DefaultListModel<>();
        cardsListComp = new JList<>(cardsModel);
        cardsListComp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        cardsListComp.setFixedCellWidth(600);
        cardsListComp.setFixedCellHeight(30);
        JScrollPane cardsScroller = new JScrollPane(cardsListComp);
        cardsScroller.setSize(600, 250);
        this.queryCards();
        this.updateCardsModel();

        backBtn.addActionListener(this);

        pane.add(totalScroller);
        pane.add(makeCardsBtn);
        pane.add(clrSel);
        pane.add(deleteBtn);
        pane.add(cardsScroller);
        pane.add(backBtn);
        pane.revalidate();
        pane.repaint();
    }

    private void updateLineModel() {
        linesModel.removeAllElements();
        for (int i = 0; i < lines.size(); i++) {
            linesModel.add(i, lines.get(i));
        }
        lines.clear();
        pane.revalidate();
        pane.repaint();
    }

    private void queryTotalLines(String ecoSearch, String searchTerm) throws SQLException {
        if (ecoSearch == null || searchTerm == null) {
            throw new IllegalArgumentException("Can't pass null string to queryLines!");
        }

        ecoSearch = ecoSearch + "%";
        searchTerm = "%" + searchTerm + "%";
        StringBuilder query = new StringBuilder();
        query.append("SELECT ID, NAME, LINE, ECO ");
        query.append("FROM LINES ");
        query.append("WHERE ECO LIKE ? ");
        query.append("OR NAME LIKE ? ");
        PreparedStatement lineQ = Main.conn.prepareStatement(query.toString());
        lineQ.setString(1, ecoSearch);
        lineQ.setString(2, searchTerm);

        ResultSet rs = lineQ.executeQuery();
        lines = new ArrayList<>();
        while (rs.next()) {
            int pk = rs.getInt(1);
            lineListItem line = new lineListItem(pk);
            line.setName(rs.getString(2));
            line.setLine(rs.getString(3));
            line.setEco(rs.getString(4));
            lines.add(line);
        }
        Main.conn.commit();
    }

    private void queryCards() throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT CARDS.ID, MOVES.ORDER_IN_LINE, LINES.ECO, LINES.NAME, CARDS.LAST_REVIEW ");
        query.append("FROM DECKS JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        query.append("JOIN CARDS_TO_MOVES ON CARDS.ID = CARDS_TO_MOVES.CARDS_ID ");
        query.append("JOIN MOVES ON CARDS_TO_MOVES.MOVES_ID = MOVES.ID ");
        query.append("JOIN LINES ON MOVES.LINES_ID = LINES.ID ");
        query.append("WHERE DECKS.ID = ? ");
        PreparedStatement preStmt = Main.conn.prepareStatement(query.toString());
        preStmt.setInt(1, deckID);
        ResultSet rs = preStmt.executeQuery();

        cards = new ArrayList<>();
        while (rs.next()) {
            int cardPK = rs.getInt(1);
            cardListItem card = new cardListItem(cardPK);
            card.setOrder(rs.getInt(2));
            card.setEco(rs.getString(3));
            card.setName(rs.getString(4));
            card.setLastReview(rs.getLong(5));
            cards.add(card);
        }
        Main.conn.commit();
    }

    private void updateCardsModel() {
        cardsModel.removeAllElements();
        for (int i = 0; i < cards.size(); i++) {
            cardsModel.add(i, cards.get(i));
        }
        cards.clear();
        pane.revalidate();
        pane.repaint();
    }

    private void makeCards(String clr) throws SQLException {
        List<lineListItem> choices = linesListComp.getSelectedValuesList();
        int[] linePkList = new int[choices.size()];
        for (int i = 0; i < linePkList.length; i++) {
            linePkList[i] = choices.get(i).getPk();
        }

        String colorChoice = "";
        if (clr.equals("White")) {
            colorChoice = " AND MOVES.ORDER_IN_LINE % 2 != 0";
        }

        if (clr.equals("Black")) {
            colorChoice = " AND MOVES.ORDER_IN_LINE % 2 == 0";
        }

        List<Integer> movePkList = new ArrayList<>();
        // Get all the line ids identified by the user that are not already in a deck.
        PreparedStatement movesStmt = Main.conn.prepareStatement("SELECT ID FROM MOVES " +
                "WHERE LINES_ID = ? AND LINES_ID NOT IN (" +
                "SELECT MOVES.LINES_ID FROM CARDS_TO_MOVES " +
                "JOIN MOVES ON CARDS_TO_MOVES.MOVES_ID = MOVES.ID)" + colorChoice);
        ResultSet rs = movesStmt.getResultSet();
        for (int linePk : linePkList) {
            movesStmt.setInt(1, linePk);
            movesStmt.executeQuery();
            while (rs.next()) {
                movePkList.add(rs.getInt(1));
            }
        }

        PreparedStatement maxID = Main.conn.prepareStatement("SELECT MAX(ID) FROM CARDS");
        ResultSet getKey = maxID.executeQuery();
        if (getKey.next()) {
            lastCardPK = getKey.getInt(1);
        } else {
            lastCardPK = 0;
        }

        PreparedStatement addCards = Main.conn.prepareStatement("INSERT INTO CARDS(ID, DECKS_ID, REP_NUMBER, " +
                "EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES (NULL, ?, 0, 2.5, 0, ?)");
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < movePkList.size(); i++) {
            addCards.setInt(1, deckID);
            addCards.setLong(2, currentTime);
            addCards.addBatch();
        }
        addCards.executeBatch();
        Main.conn.commit();

        PreparedStatement cardsRel = Main.conn.prepareStatement("INSERT INTO CARDS_TO_MOVES(CARDS_ID, MOVES_ID) " +
                "VALUES (?, ?)");
        // Last key is the last INTEGER PRIMARY KEY SQLite inserted into CARDS. We are adding one card for each move
        // in each line identified by the user, so movePkList.size() gives the correct number of new cards.
        for (int i = 0; i < movePkList.size(); i++) {
            cardsRel.setInt(1, lastCardPK + 1 + i);
            cardsRel.setInt(2, movePkList.get(i));
            cardsRel.addBatch();
        }
        cardsRel.executeBatch();

        Main.conn.commit();
    }

    public void deleteCards() throws SQLException {
        List<cardListItem> choices = cardsListComp.getSelectedValuesList();
        PreparedStatement relDel = Main.conn.prepareStatement("DELETE FROM CARDS_TO_MOVES WHERE CARDS_ID = ?");
        PreparedStatement cardDel = Main.conn.prepareStatement("DELETE FROM CARDS WHERE ID = ?");
        for (cardListItem choice : choices) {
            int cardPK = choice.getPk();
            relDel.setInt(1, cardPK);
            cardDel.setInt(1, cardPK);
            relDel.addBatch();
            cardDel.addBatch();
        }
        relDel.executeBatch();
        cardDel.executeBatch();
        Main.conn.commit();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == makeCardsBtn) {
            try {
                String clr = (String) clrSel.getSelectedItem();
                this.makeCards(clr);
                this.queryCards();
                this.updateCardsModel();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (e.getSource() == deleteBtn) {
            try {
                this.deleteCards();
                this.queryCards();
                this.updateCardsModel();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (e.getSource() == backBtn) {
            try {
                modGUI.updateDeckModel();
                controller.show(container, "mod");
            } catch (SQLException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public class cardListItem {
        private final int pk;
        private int orderInLine;
        private String eco;
        private String name;
        private long lastReview;
        public cardListItem(int newPk)  {
            pk = newPk;
        }
        public int getPk() {return pk;}
        public void setOrder(int newOrder) {orderInLine = newOrder;}
        public void setEco(String newEco) {eco = newEco;}
        public void setName(String newName) {name = newName;}
        public void setLastReview(long newLastReview) {lastReview = newLastReview;}
        @Override
        public String toString() {
            return orderInLine + " " + eco + " " + name + " " + new Date(lastReview);
        }
    }

    public class lineListItem {
        private final int pk;
        private String name;
        private String line;
        private String eco;
        public lineListItem(int newPk)  {
            pk = newPk;
        }
        public int getPk() {return pk;}
        public void setEco(String newEco) {eco = newEco;}
        public void setName(String newName) {name = newName;}
        public void setLine(String newLine) {line = newLine;}
        @Override
        public String toString() {
            return eco + " " + name + " " + line;
        }
    }
}
