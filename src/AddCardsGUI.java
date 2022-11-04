import javax.swing.JPanel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

    // The main menu panel, which we get a reference to through the constructor. This is necessary to allow us to
    // repaint the main menu with update deck information before we return to it using the back button.
    private final MainMenuGUI mainMenu;

    // This model contains a list of LineListItem objects, each one of which is a set of data about a particular line
    // that gets displayed on the GUI as an option that the user can select.
    private final DefaultListModel<LineListItem> linesModel = new DefaultListModel<>();

    // This JList displays the contents of the linesModel.
    private final JList<LineListItem> linesList = new JList<>(linesModel);

    // This model contains a list of CardListItem objects, each of which is a user-created card with one associated
    // move in the MOVES table.
    private final DefaultListModel<CardListItem> cardsModel = new DefaultListModel<>();

    // JList that displays the cardsModel.
    private final JList<CardListItem> cardsList = new JList<>(cardsModel);

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
     * @param mainGUIObj The main menu GUI, which we repaint before returning the user to it.
     * @throws ClassNotFoundException If the methods interacting with the database cannot successfully execute.
     * @throws SQLException If a query fails.
     */
    public AddCardsGUI(JPanel newCardsMenu, int deckPK, MainMenuGUI mainGUIObj)
            throws ClassNotFoundException, SQLException {

        // Set the matching instance variables equal to each of the constructor's parameters.
        mainMenu = mainGUIObj;
        cardsMenu = newCardsMenu;
        deckID = deckPK;

        // Allow the user to select multiple items on these JLists simultaneously.
        linesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        cardsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Set size for items in JLists.
        linesList.setFixedCellWidth(600);
        linesList.setFixedCellHeight(30);
        cardsList.setFixedCellWidth(600);
        cardsList.setFixedCellHeight(30);

        // Wrap list components in scrollers to create list that the user can scroll through.
        JScrollPane totalScroller = new JScrollPane(linesList);
        JScrollPane cardsScroller = new JScrollPane(cardsList);
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
        String query = "SELECT ID, NAME, LINE, ECO " +
                        "FROM LINES " +
                        "WHERE ECO LIKE ? " +
                        "OR NAME LIKE ? ";

        // Create a parameterized query with the two search terms as parameters.
        PreparedStatement lineQ = Main.conn.prepareStatement(query);
        lineQ.setString(1, ecoSearch);
        lineQ.setString(2, searchTerm);

        // Execute query.
        ResultSet rs = lineQ.executeQuery();

        // Remove all LineListItem objects from linesModel.
        linesModel.clear();

        // Iterate over the query result, pulling out the Primary Key, Name, Line, and ECO code.
        int index = 0;
        while (rs.next()) {
            int pk = rs.getInt(1);

            // Instantiate a new LineListItem using the primary key from the LINES tuple. This object represents an
            // opening line from our database.
            LineListItem line = new LineListItem(pk);
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

    /**
     * Get all cards associated with the current deck from the database.
     * @throws SQLException If we cannot access the database, throw an exception.
     */
    private void queryCards() throws SQLException {
        // This query returns the relevant information about each card in the database. Because we also want to know
        // the information about the line and the move associated with the card, we join both of those tables, which
        // also requires the intermediate CARDS_TO_MOVES table.
        String query = "SELECT CARDS.ID, MOVES.ORDER_IN_LINE, LINES.ECO, LINES.NAME, CARDS.LAST_REVIEW " +
                        "FROM DECKS JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID " +
                        "JOIN CARDS_TO_MOVES ON CARDS.ID = CARDS_TO_MOVES.CARDS_ID " +
                        "JOIN MOVES ON CARDS_TO_MOVES.MOVES_ID = MOVES.ID " +
                        "JOIN LINES ON MOVES.LINES_ID = LINES.ID " +
                        "WHERE DECKS.ID = ? ";

        // Create a parameterized query, with the primary key for DECKS as a parameter, as we are only interested in
        // cards associated with the current deck.
        PreparedStatement preStmt = Main.conn.prepareStatement(query);
        preStmt.setInt(1, deckID);

        // Execute the query.
        ResultSet rs = preStmt.executeQuery();

        // Clear any cardListItems currently stored in the cardsModel.
        cardsModel.clear();

        // Iterate through the results returned by the query, storing information from each tuple in a corresponding
        // CardListItem object.
        int index = 0;
        while (rs.next()) {
            int cardPK = rs.getInt(1);

            // Create a CardListItem using the primary key. This object represents a user-created review card.
            CardListItem card = new CardListItem(cardPK);

            // Use setters to store the query results in the object.
            card.setOrder(rs.getInt(2));
            card.setEco(rs.getString(3));
            card.setName(rs.getString(4));
            card.setLastReview(rs.getLong(5));

            // Add each object to the cardsModel.
            cardsModel.add(index, card);
            index = index + 1;
        }

        // Revalidate and paint the GUI panel.
        cardsMenu.revalidate();
        cardsMenu.repaint();
    }

    /**
     * This method makes new cards based on whichever lines and whatever color is selected when the user clicks the
     * create button.
     * @param clr The color (white or black) selected by the user in the JComboBox.
     * @throws SQLException If the transactions in this method cannot be executed, throw an exception.
     */
    private void makeCards(String clr) throws SQLException {
        // Get the lines selected by the user.
        List<LineListItem> choices = linesList.getSelectedValuesList();

        // Create an array with the primary keys of the lines selected by the user.
        int[] linePkList = new int[choices.size()];

        // Iterate through the int array and the ArrayList of lineListItems, storing the primary keys of the latter
        // in the former.
        for (int i = 0; i < linePkList.length; i++) { linePkList[i] = choices.get(i).getPk(); }

        // Initialize color choice with an empty string.
        String colorChoice = "";

        // If the JComboBox has WHITE selected, use this SQL sub-clause.
        if (clr.equals("White")) { colorChoice = " AND MOVES.ORDER_IN_LINE % 2 != 0 "; }

        // If the JComboBox has BLACK selected, use this SQL sub-clause.
        if (clr.equals("Black")) { colorChoice = " AND MOVES.ORDER_IN_LINE % 2 == 0 "; }

        // This query gets all the move primary keys associated with the lines identified by the user, filtered by the
        // user-selected color (white or black). It excludes moves that are already in the deck we are modifying.
        PreparedStatement movesStmt = Main.conn.prepareStatement(
                "SELECT MOVES.ID, MOVES.BEFORE_FEN FROM MOVES " +
                "WHERE LINES_ID = ? AND LINES_ID NOT IN (" +
                "SELECT MOVES.LINES_ID FROM CARDS " +
                "JOIN CARDS_TO_MOVES ON CARDS_TO_MOVES.CARDS_ID = CARDS.ID " +
                "JOIN MOVES ON CARDS_TO_MOVES.MOVES_ID = MOVES.ID " +
                "WHERE DECKS_ID = ?)" + colorChoice + " AND MOVES.BEFORE_FEN NOT IN (" +
                "SELECT MOVES.BEFORE_FEN FROM MOVES " +
                "JOIN CARDS_TO_MOVES ON MOVES.ID = CARDS_TO_MOVES.MOVES_ID " +
                "JOIN CARDS ON CARDS_TO_MOVES.CARDS_ID = CARDS.ID " +
                "WHERE CARDS.DECKS_ID = ?)"
        );

        // Create an ArrayList to hold the different primary keys.
        List<Integer> movePkList = new ArrayList<>();

        // Create a ResultSet to get results from the query.
        ResultSet rs = movesStmt.getResultSet();

        // Iterate through the list of LINES.ID primary keys, executing a new query each time.
        for (int linePk : linePkList) {
            movesStmt.setInt(1, linePk);
            movesStmt.setInt(2, deckID);
            movesStmt.setInt(3, deckID);
            movesStmt.executeQuery();

            // Get the primary keys from the query and add each to the ArrayList.
            while (rs.next()) {
                movePkList.add(rs.getInt(1));
                System.out.println(rs.getString(2));
            }
        }

        // In order to make new tuples in the CARDS_TO_MOVES intermediate table, we need to know which CARDS.ID ints are
        // going to be created. Because CARDS auto-increments using INTEGER PRIMARY KEY, the solution is to find the
        // highest ID in CARDS, and use that value to infer the key which will be created.
        PreparedStatement maxID = Main.conn.prepareStatement("SELECT MAX(ID) FROM CARDS");
        ResultSet getKey = maxID.executeQuery();

        int lastCardPK;
        if (getKey.next()) {
            lastCardPK = getKey.getInt(1);
        } else {
            // If no results are found by the query, the table is empty and the next primary key that will be assigned
            // by SQLite is 0.
            lastCardPK = 0;
        }

        // Create a query to insert new rows in the CARDS table. We pass NULL to ID so that SQLite knows to
        // auto-increment that field. REP_NUMBER, EASY_FACTOR, and LAST_REVIEW are parameters in the SuperMemo2
        // algorithm and start at the default values of 0, 2.5, and 0.
        PreparedStatement addCards = Main.conn.prepareStatement("INSERT INTO CARDS(ID, DECKS_ID, REP_NUMBER, " +
                "EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES (NULL, ?, 0, 2.5, 0, ?)");

        // Get the system's current UNIX time-stamp.
        long currentTime = System.currentTimeMillis();

        // Iterate n times, with n being defined by the number of MOVES.ID primary keys associated with the lines
        // selected earlier by the user.
        for (int i = 0; i < movePkList.size(); i++) {
            addCards.setInt(1, deckID);
            addCards.setLong(2, currentTime);
            addCards.addBatch();
        }

        // Execute the batch of insertion operations.
        addCards.executeBatch();

        // Commit the transaction.
        Main.conn.commit();

        // Insert rows into the CARDS_TO_MOVES intermediate table, each of which represents a relationship between a
        // card and a chess move. Because the makeCards method creates one card for each move identified by the user,
        // we can simply iterate through movePkList, and pair each MOVES_ID primary key with a CARDS_ID primary key
        // that is derived by adding i to the highest primary key currently in CARDS (+ 1 to avoid a duplicate key
        // at the beginning).
        PreparedStatement cardsRel = Main.conn.prepareStatement("INSERT INTO CARDS_TO_MOVES(CARDS_ID, MOVES_ID) " +
                "VALUES (?, ?)");

        // Last key is the last INTEGER PRIMARY KEY SQLite inserted into CARDS.
        for (int i = 0; i < movePkList.size(); i++) {
            cardsRel.setInt(1, lastCardPK + 1 + i);
            cardsRel.setInt(2, movePkList.get(i));
            cardsRel.addBatch();
        }

        // Execute the batch of insertion operations.
        cardsRel.executeBatch();

        // Commit the transaction.
        Main.conn.commit();
    }

    /**
     * This method deletes whichever cards are currently selected by the user when the delete button is clicked.
     * @throws SQLException If the database transaction cannot be processed, throw an exception.
     */
    private void deleteCards() throws SQLException {
        // Get the cardListItems currently selected in the cardsListComp JList.
        List<CardListItem> choices = cardsList.getSelectedValuesList();

        // Delete all the CARDS_TO_MOVES tuples matching the CARDS.ID primary keys selected by the user. Each tuple
        // in this table represents a relationship between CARDS and MOVES.
        PreparedStatement relDel = Main.conn.prepareStatement("DELETE FROM CARDS_TO_MOVES WHERE CARDS_ID = ?");

        // Iterate through the different cardListItems chosen by the user, getting the primary key from each and
        // inserting it into the parameterized query.
        for (CardListItem choice : choices) {
            int cardPK = choice.getPk();
            relDel.setInt(1, cardPK);
            relDel.addBatch();
        }

        // Execute the batch of DELETE operations as one transaction.
        relDel.executeBatch();

        // Commit transaction.
        Main.conn.commit();

        // Delete all cards matching the primary keys of the cardListItems selected by the user.
        PreparedStatement cardDel = Main.conn.prepareStatement("DELETE FROM CARDS WHERE ID = ?");

        // Iterate through the different cardListItems, getting the primary key from each and inserting it into the
        // parameterized query.
        for (CardListItem choice : choices) {
            int cardPK = choice.getPk();
            cardDel.setInt(1, cardPK);
            cardDel.addBatch();
        }

        // Execute the batch of DELETE operations as one transaction.
        cardDel.executeBatch();

        // Commit the transaction.
        Main.conn.commit();
    }

    /**
     * Action listener for this GUI JPanel.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Activated if the user clicks the makeCards button.
        if (e.getSource() == makeCardsBtn) {
            try {
                // Get the currently-selected color (black or white) from the JComboBox.
                String clr = (String) clrSel.getSelectedItem();

                // Make one or more cards based on the JList items currently selected by the user.
                this.makeCards(clr);

                // Update the GUI to reflect the newly created cards in the database.
                this.queryCards();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        // This conditional is activated if the user clicks the delete button.
        if (e.getSource() == deleteBtn) {
            try {
                // This method deletes all cards currently selected by the user.
                this.deleteCards();

                // Update the GUI to reflect any deleted cards.
                this.queryCards();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        // This conditional is activated if the user clicks the back button.
        if (e.getSource() == backBtn) {
            // Switch back to the main menu and update and repaint, so it reflects any changes made in this AddCardsGUI.
            mainMenu.mainReturn();
        }
    }

    /**
     * Static nested class. Each instance of this class represents a user-created cards, which is paralleled by the
     * items in the CARDS table in database.db.
     */
    private static class CardListItem {
        // The primary key for this card, which can be used to retrieve information from the CARDS table in the
        // database.
        private final int pk;

        // The order in the opening line. Each card has a one-to-one relationship with a chess move in the MOVES
        // database. Those moves are each associated with an opening line. This int represents the moves position in
        // the sequence of moves that make up the line. The sequence begins with one.
        private int orderInLine;

        // The ECO code under which the line associated with this card is classified.
        private String eco;

        // The name of the line associated with this card.
        private String name;

        // A UNIX timestamp representing the last time the user reviewed this card.
        private long lastReview;

        // Constructor takes primary key as an argument.
        public CardListItem(int newPk)  { pk = newPk;  }

        // Getter for primary key.
        public int getPk() {return pk;}

        // Setters for the other four instance variables. These variables are immutable and are only used by the
        // toString method.
        public void setOrder(int newOrder) {orderInLine = newOrder;}
        public void setEco(String newEco) {eco = newEco;}
        public void setName(String newName) {name = newName;}
        public void setLastReview(long newLastReview) {lastReview = newLastReview;}

        /**
         * This toString method is used to display the information about the card to the user.
         * @return A string representation of this object.
         */
        @Override
        public String toString() {
            return orderInLine + " " + eco + " " + name + " " + new Date(lastReview);
        }
    }

    /**
     * Static nested class. Each instance of this class represents a chess opening line, which is paralleled by the
     * items in the LINES table in database.db.
     */
    private static class LineListItem {
        // The primary key for this card, which can be used to retrieve information from the LINES table in the
        // database.
        private final int pk;

        // The name of the line.
        private String name;

        // A String representing the list of moves in the line. The moves are represented in standard algebraic
        // notation.
        private String line;

        // The ECO code under which this line is classified.
        private String eco;

        // Constructor takes primary key as an argument.
        public LineListItem(int newPk)  { pk = newPk; }

        // Getter for primary key.
        public int getPk() {return pk;}

        // Setters for ECO, Name, and Line, all of which are immutable. These instance variables are only used by the
        // toString method, which is itself used to display information about this line to the user.
        public void setEco(String newEco) {eco = newEco;}
        public void setName(String newName) {name = newName;}
        public void setLine(String newLine) {line = newLine;}

        /**
         * This toString method is used to display information about the associated line to the user.
         * @return Returns a string representation of this object.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(eco);
            sb.append(": ");
            sb.append(name);
            sb.append(", Line: ");
            sb.append(line);
            return sb.toString();
        }
    }
}
