import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This GUI class defines the behavior of the submenu that allows you to add and remove cards from your review decks.
 * @author Jack Vandeleuv
 */
public class AddCardsGUI implements ActionListener {
    // Primary key identifying the deck we are currently modifying.
    private final int deckID;

    // The JPanel on which this GUI is painted. We receive this panel as a parameter in the constructor because the
    // CardLayout needs to have a reference to it.
    private final JPanel cardsMenu;

    // The container panel that holds all the different menus using CardLayout.
    private final JPanel container;

    // The main menu panel, which we get a reference to through the constructor. This is necessary to allow us to
    // repaint the main menu with update deck information before we return to it using the back button.
    private final MainMenuGUI mainMenu;

    // CardLayout that allows us to switch between panels based on user input.
    private final CardLayout controller;

    // This model contains a list of lineListItem objects, each one of which is a set of data about a particular line
    // that gets displayed on the GUI as an option that the user can select.
    private final DefaultListModel<lineListItem> linesModel = new DefaultListModel<>();

    // This JList displays the contents of the linesModel.
    private final JList<lineListItem> linesListComp = new JList<>(linesModel);

    // This model contains a list of cardListItem objects, each of which is a user-created card with one associated
    // move in the MOVES table.
    private final DefaultListModel<cardListItem> cardsModel = new DefaultListModel<>();

    // JList that displays the cardsModel.
    private final JList<cardListItem> cardsListComp = new JList<>(cardsModel);

    // JButton that creates a set of new cards based on the selected opening line(s).
    private final JButton makeCardsBtn = new JButton("Make Card(s)");

    // JButton that deletes selected cards.
    private final JButton deleteBtn = new JButton("Delete Card(s)");

    // Returns user to the main menu.
    private final JButton backBtn = new JButton("Back");

    // Allows the user to select black or white. Each line contains alternating moves by black and white. By toggling
    // this option, the user will add cards to their deck corresponding to the color they pick. E.g. if the user has
    // "WHITE" toggled, and they hit the makeCardsBtn, they will only make cards for white's moves in the selected line.
    private final JComboBox<String> clrSel = new JComboBox<>(new String[]{"White", "Black"});

    /**
     * This class is the GUI for modifying the cards contained in a given deck.
     * @param newCardsMenu The JPanel on which this GUI is painted.
     * @param deckPK Primary key that identifies the deck currently being reviewed.
     * @param outerContainer JPanel holding the CardLayout.
     * @param outerController CardLayout that allows us to switch between panels.
     * @param mainGUIObj The main menu GUI, which we repaint before returning the user to it.
     * @throws ClassNotFoundException If the methods interacting with the database cannot successfully execute.
     * @throws SQLException If a query fails.
     */
    public AddCardsGUI(JPanel newCardsMenu, int deckPK, JPanel outerContainer,
                       CardLayout outerController, MainMenuGUI mainGUIObj) throws ClassNotFoundException, SQLException {

        // Set the matching instance variables equal to each of the constructor's parameters.
        mainMenu = mainGUIObj;
        controller = outerController;
        container = outerContainer;
        cardsMenu = newCardsMenu;
        deckID = deckPK;

        // Allow the user to select multiple items on these JLists simultaneously.
        linesListComp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        cardsListComp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Set size for items in JLists.
        linesListComp.setFixedCellWidth(600);
        linesListComp.setFixedCellHeight(30);
        cardsListComp.setFixedCellWidth(600);
        cardsListComp.setFixedCellHeight(30);

        // Wrap list components in scrollers to create list that the user can scroll through..
        JScrollPane totalScroller = new JScrollPane(linesListComp);
        JScrollPane cardsScroller = new JScrollPane(cardsListComp);
        totalScroller.setPreferredSize(new Dimension(900, 230));
        cardsScroller.setPreferredSize(new Dimension(900, 230));

        // Register action listeners.
        makeCardsBtn.addActionListener(this);
        clrSel.addActionListener(this);
        deleteBtn.addActionListener(this);
        backBtn.addActionListener(this);

        // Wrap the different buttons and the ComboBox together in one panel.
        JPanel btnBox = new JPanel();
        btnBox.add(backBtn);
        btnBox.add(deleteBtn);
        btnBox.add(makeCardsBtn);
        btnBox.add(clrSel);

        // Add both scrollers and the JPanel containing buttons and the ComboBox.
        cardsMenu.add(totalScroller);
        cardsMenu.add(btnBox);
        cardsMenu.add(cardsScroller);

        // Set this GUI panel's layout to a vertical box layout.
        cardsMenu.setLayout(new BoxLayout(this.cardsMenu, BoxLayout.Y_AXIS));

        // Adding padding for the entire panel.
        cardsMenu.setBorder(new EmptyBorder(0, 15, 0, 15));

        // Update JList with all the available lines found in the database.
        this.queryTotalLines("", "");

        // Query database for an updated list of all review cards created by the user so far.
        this.queryCards();

        // Validate and paint the GUI panel.
        cardsMenu.revalidate();
        cardsMenu.repaint();
    }

    /**
     * This method pulls all the different opening lines stored in the database, filtered by two search parameters.
     * @param ecoSearch User defined search parameter that filters our lines by ECO code. Not implemented yet.
     * @param searchTerm User defined search parameter that filters our lines by variation name. Not implemented yet.
     * @throws SQLException If the query cannot be executed, throw an error.
     */
    private void queryTotalLines(String ecoSearch, String searchTerm) throws SQLException {
        // Validate input for null strings.
        if (ecoSearch == null || searchTerm == null) {
            throw new IllegalArgumentException("Can't pass null string to queryLines!");
        }

        // % indicates "any number of characters" in SQLite. ECO codes are well-formatted, so we require an exact match
        // for the beginning of the code. For the name search, we allow any number of characters before or after the
        // matching substring.
        ecoSearch = ecoSearch + "%";
        searchTerm = "%" + searchTerm + "%";

        // This query returns all items in the LINES table, filtered by ECO and NAME if search terms are provided.
        StringBuilder query = new StringBuilder();
        query.append("SELECT ID, NAME, LINE, ECO ");
        query.append("FROM LINES ");
        query.append("WHERE ECO LIKE ? ");
        query.append("OR NAME LIKE ? ");

        // Create a parameterized query with the two search terms as parameters.
        PreparedStatement lineQ = Main.conn.prepareStatement(query.toString());
        lineQ.setString(1, ecoSearch);
        lineQ.setString(2, searchTerm);

        // Execute query.
        ResultSet rs = lineQ.executeQuery();

        // Remove all lineListItem objects from linesModel.
        linesModel.clear();

        // Iterate over the query result, pulling out the Primary Key, Name, Line, and ECO code.
        int index = 0;
        while (rs.next()) {
            int pk = rs.getInt(1);

            // Instantiate a new lineListItem using the primary key from the LINES tuple. This object represents an
            // opening line from our database.
            lineListItem line = new lineListItem(pk);
            line.setName(rs.getString(2));
            line.setLine(rs.getString(3));
            line.setEco(rs.getString(4));

            // Store each object in our model, which is displayed by the associated JList.
            linesModel.add(index, line);
            index = index + 1;
        }

        // Revalidate and paint the GUI panel.
        cardsMenu.revalidate();
        cardsMenu.repaint();
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

        cardsModel.clear();
        int index = 0;
        while (rs.next()) {
            int cardPK = rs.getInt(1);
            cardListItem card = new cardListItem(cardPK);
            card.setOrder(rs.getInt(2));
            card.setEco(rs.getString(3));
            card.setName(rs.getString(4));
            card.setLastReview(rs.getLong(5));
            cardsModel.add(index, card);
            index = index + 1;
        }

        cardsMenu.revalidate();
        cardsMenu.repaint();
        Main.conn.commit();
    }

    private void makeCards(String clr) throws SQLException {
        List<lineListItem> choices = linesListComp.getSelectedValuesList();
        int[] linePkList = new int[choices.size()];
        for (int i = 0; i < linePkList.length; i++) {
            linePkList[i] = choices.get(i).getPk();
        }

        String colorChoice = "";
        if (clr.equals("White")) {
            colorChoice = " AND MOVES.ORDER_IN_LINE % 2 != 0 ";
        }

        if (clr.equals("Black")) {
            colorChoice = " AND MOVES.ORDER_IN_LINE % 2 == 0 ";
        }

        List<Integer> movePkList = new ArrayList<>();
        // Get all the line ids identified by the user that are not already in a deck.
        PreparedStatement movesStmt = Main.conn.prepareStatement("SELECT MOVES.ID FROM MOVES " +
                "WHERE LINES_ID = ? AND LINES_ID NOT IN (" +
                "SELECT MOVES.LINES_ID FROM CARDS " +
                "JOIN CARDS_TO_MOVES ON CARDS_TO_MOVES.CARDS_ID = CARDS.ID " +
                "JOIN MOVES ON CARDS_TO_MOVES.MOVES_ID = MOVES.ID " +
                "WHERE DECKS_ID = ?)" + colorChoice + " AND BEFORE_FEN NOT IN (" +
                "SELECT MOVES.BEFORE_FEN FROM MOVES " +
                "JOIN CARDS_TO_MOVES ON MOVES.ID = CARDS_TO_MOVES.MOVES_ID " +
                "JOIN CARDS ON CARDS_TO_MOVES.CARDS_ID = CARDS.ID " +
                "WHERE CARDS.DECKS_ID = ?)");
        ResultSet rs = movesStmt.getResultSet();
        for (int linePk : linePkList) {
            movesStmt.setInt(1, linePk);
            movesStmt.setInt(2, deckID);
            movesStmt.setInt(3, deckID);
            movesStmt.executeQuery();
            while (rs.next()) {
                movePkList.add(rs.getInt(1));
            }
        }

        PreparedStatement maxID = Main.conn.prepareStatement("SELECT MAX(ID) FROM CARDS");
        ResultSet getKey = maxID.executeQuery();
        int lastCardPK;
        if (getKey.next()) {
            lastCardPK = getKey.getInt(1);
        } else {
            lastCardPK = 0;
        }

        PreparedStatement addCards = Main.conn.prepareStatement("INSERT INTO CARDS(ID, DECKS_ID, REP_NUMBER, " +
                "EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES (NULL, ?, 0, 2.5, 0, ?)");
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < movePkList.size(); i++) {
            System.out.println("+++++++++++++++");
            System.out.println("Inserting into cards (deckId, currentTime):");
            System.out.println(deckID);
            System.out.println(currentTime);
            System.out.println("+++++++++++++++");

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
            System.out.println("+++++++++++++++");
            System.out.println("Inserting into CARDS_TO_MOVES, cards_id, moves_id:");
            System.out.println(lastCardPK + 1 + i);
            System.out.println(movePkList.get(i));
            System.out.println("+++++++++++++++");

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
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (e.getSource() == deleteBtn) {
            try {
                this.deleteCards();
                this.queryCards();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (e.getSource() == backBtn) {
            try {
                mainMenu.updateDeckModel();
                controller.show(container, "main");
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
