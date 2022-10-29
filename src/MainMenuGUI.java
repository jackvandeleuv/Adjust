import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.sql.Statement;

/**
 * This class defines the behavior of the main menu JPanel. It is instantiated when the user launches the application.
 * @author Jack Vandeleuv
 */
public final class MainMenuGUI implements ActionListener {
    // JButtons that allow the user to customize the decks in their application.
    private final JButton createBtn = new JButton("CREATE");
    private final JButton renameBtn = new JButton("RENAME");
    private final JButton deleteBtn = new JButton("DELETE");

    // JButtons that allow the user to navigate to the Modify Deck and Review Deck submenus.
    private final JButton modifyBtn = new JButton("MODIFY");
    private final JButton reviewBtn = new JButton("REVIEW");

    // This JPanel and CardLayout contains the other menu panels and allow the user to switch between them.
    private final JPanel container = new JPanel();
    private final CardLayout controller = new CardLayout();

    // Main menu pane.
    private final JPanel mainPane = new JPanel();

    // Modify Deck menu that lets the user add/remove cards.
    private final JPanel cardsPane = new JPanel();

    // Review Deck menu that allows the user to review cards on a chess board.
    private final JPanel boardPane = new JPanel();

    // List Model that contains a list of DeckListItems, each representing one user-created deck.
    private final DefaultListModel<DeckListItem> decksModel = new DefaultListModel<>();

    // JList that displays the decksModel.
    private final JList<DeckListItem> decksListComp = new JList<>(decksModel);

    /**
     * Class defining the behavior of the Main Menu GUI.
     * @throws SQLException If a database operation cannot be performed, throw an exception.
     * @throws ClassNotFoundException If the JDBC Class cannot be found, throw an exception.
     */
    public MainMenuGUI() throws SQLException, ClassNotFoundException {
        // Create a JFrame to hold all the JPanels used by the application.
        JFrame window = new JFrame();
        window.setSize(1000, 600);

        // Passing null centers the JFrame in the middle of the screen.
        window.setLocationRelativeTo(null);

        // Close the program when (X) button is clicked on the JFrame.
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Attach CardLayout to container panel and add panels for each menu.
        container.setLayout(controller);
        container.add(mainPane, "main");
        container.add(boardPane, "board");
        container.add(cardsPane, "cards");
        window.add(container);

        // Add BoxLayout to mainPane to align components vertically.
        BoxLayout boxLayout = new BoxLayout(mainPane, BoxLayout.Y_AXIS);
        mainPane.setLayout(boxLayout);

        // Add padding to the left and right of all the elements in mainPane.
        mainPane.setBorder(new EmptyBorder(0, 15, 0, 15));

        // Add a JLabel with the name of the application. Wrap it in a JPanel to help align it with other components.
        JPanel titlePane = new JPanel();
        titlePane.setPreferredSize(new Dimension(800, 120));
        JLabel title = new JLabel("ADJUST");
        title.setFont(new Font("Sans", Font.BOLD, 64));
        titlePane.add(title);

        // Register action listeners for each button.
        createBtn.addActionListener(this);
        modifyBtn.addActionListener(this);
        renameBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        reviewBtn.addActionListener(this);

        // Modify button sizes.
        createBtn.setPreferredSize(new Dimension(120, 45));
        modifyBtn.setPreferredSize(new Dimension(120, 45));
        renameBtn.setPreferredSize(new Dimension(120, 45));
        deleteBtn.setPreferredSize(new Dimension(120, 45));
        reviewBtn.setPreferredSize(new Dimension(120, 45));

        // Create panel to hold all the buttons in mainPane.
        JPanel btnPane = new JPanel();

        // Center the components in the panel by applying GridBagLayout.
        btnPane.setLayout(new GridBagLayout());

        // Set preferred size for btnPane.
        btnPane.setPreferredSize(new Dimension(800, 100));

        // Add each button to the btnPane JPanel to keep them aligned.
        btnPane.add(createBtn);
        btnPane.add(reviewBtn);
        btnPane.add(modifyBtn);
        btnPane.add(renameBtn);
        btnPane.add(deleteBtn);

        // Make it so that only one element in the decksListComp JList can be selected at once.
        decksListComp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Adjust size of JList elements.
        decksListComp.setFixedCellWidth(800);
        decksListComp.setFixedCellHeight(70);

        // Wrap the decksListComp JList in a scroller to make it change to a scrollable window when the contents
        // overflow.
        JScrollPane scroller = new JScrollPane(decksListComp);
        scroller.setPreferredSize(new Dimension(800, 350));

        // Add each wrapper to the mainPane JPanel.
        mainPane.add(titlePane);
        mainPane.add(scroller);
        mainPane.add(btnPane);

        // Update the list of DeckListItem objects.
        this.updateDeckModel();

        // Display the window.
        window.setVisible(true);
    }

    /**
     * This class gets an updated list of decks (including a summary of the cards inside each) and displays it.
     * @throws SQLException If a database operation cannot be performed, throw an exception.
     */
    public void updateDeckModel() throws SQLException {
        // This query selects two elements: DECKS.ID, which is the primary key for each deck in the database, and
        // a count of CARDS.ID contained in each deck. This count represents the total number of cards associated with
        // the deck in the same tuple. COALESCE is necessary in the case where no cards are associated with the deck.
        // We perform a left join to avoid excluding decks with no associated cards.
        String cardTotalsQ = "SELECT DECKS.ID, COALESCE(COUNT(CARDS.ID), 0) " +
                            "FROM DECKS LEFT JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID " +
                            "GROUP BY DECKS.ID " +
                            "ORDER BY DECKS.NAME DESC ";

        // This query selects two elements: DECKS.NAME, which is the user-defined name for each deck, and a count of
        // CARDS.ID, grouped by deck, where the card is due to be reviewed. We perform a left join to avoid excluding
        // decks with no associated cards.
        String toReviewQ = "SELECT DECKS.NAME, COALESCE(COUNT(CARDS.ID), 0) " +
                            "FROM DECKS LEFT JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID " +
                            "WHERE (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) " +
                            "OR CARDS.ID IS NULL " +
                            "GROUP BY DECKS.ID " +
                            "ORDER BY DECKS.NAME DESC ";

        // Get the current UNIX timestamp for this machine.
        long currentTime = System.currentTimeMillis();

        // Create a statement to execute the first query.
        Statement cardTotalsStmt = Main.conn.createStatement();

        // Create a parameterized statement for the second query.
        PreparedStatement toReviewStmt = Main.conn.prepareStatement(toReviewQ);
        toReviewStmt.setLong(1, currentTime);

        // Execute both queries.
        ResultSet rs1 = cardTotalsStmt.executeQuery(cardTotalsQ);
        ResultSet rs2 = toReviewStmt.executeQuery();

        // Clear any DeckListItems currently in the decksModel.
        decksModel.clear();

        // Iterate through all the results from the first query, instantiating a new DeckListItem for each.
        int index1 = 0;
        while (rs1.next()) {
            DeckListItem deck = new DeckListItem(rs1.getInt(1));

            // Get the column representing the total number of cards associated with the deck and add it to the object.
            deck.setCardTotal(rs1.getInt(2));
            decksModel.add(index1, deck);
            index1 = index1 + 1;
        }

        // Iterate through the results from the second query and store them in the corresponding DeckListItem. Because
        // the two queries are ordered based on the same criteria we perform this iteration without checking the
        // identity of each deck.
        int index2 = 0;
        while (rs2.next()) {
            DeckListItem deck = decksModel.get(index2);
            deck.setName(rs2.getString(1));

            // Get the review count from the query and add it to the object. This count is an int representing the
            // number of cards due to be reviewed.
            deck.setReviewCount(rs2.getInt(2));
            index2 = index2 + 1;
        }
    }

    /**
     * Deletes one deck (and other associated data in other tables) based on DECKS.ID primary key.
     * @param deckPK The primary key of the deck to be deleted.
     * @throws ClassNotFoundException If the JDBC Class cannot be found, throw an exception.
     * @throws SQLException If a database operation cannot be performed, throw an exception.
     */
    private void deleteDeck(int deckPK) throws ClassNotFoundException, SQLException {
        // Delete deck using a parameterized query.
        PreparedStatement deleteDeck = Main.conn.prepareStatement("DELETE FROM DECKS WHERE ID = ?");
        deleteDeck.setInt(1, deckPK);
        deleteDeck.executeUpdate();

        // Commit the transaction.
        Main.conn.commit();

        // Delete all tuples from the CARDS_TO_MOVES intermediate table where the CARDS_ID is associated with a deck
        // that is being deleted.
        String delRelQuery = "DELETE FROM CARDS_TO_MOVES WHERE CARDS_ID IN ( " +
                                "SELECT ID FROM CARDS WHERE DECKS_ID = ?) ";

        // Execute a parameterized query to perform the deletion.
        PreparedStatement delRels = Main.conn.prepareStatement(delRelQuery);
        delRels.setInt(1, deckPK);
        delRels.executeUpdate();

        // Commit the transaction.
        Main.conn.commit();

        // This parameterized query deletes cards that are associated with the deck being deleted.
        PreparedStatement deleteCards = Main.conn.prepareStatement("DELETE FROM CARDS WHERE DECKS_ID = ?");
        deleteCards.setInt(1, deckPK);
        deleteCards.executeUpdate();

        // Commit the transaction.
        Main.conn.commit();

        // Update and repaint the list of decks displayed to the user.
        this.updateDeckModel();
        mainPane.revalidate();
        mainPane.repaint();
    }

    /**
     * Creates a new, empty deck with a user-defined name.
     * @param name The user-defined name for the deck.
     * @throws ClassNotFoundException If the JDBC Class cannot be found, throw an exception.
     * @throws SQLException If a database operation cannot be performed, throw an exception.
     */
    private void createDeck(String name) throws ClassNotFoundException, SQLException {
        // Create a new deck using a parameterized query. Pass NULL so that SQLite auto-increments the INTEGER PRIMARY
        // KEY using the value of ROWID.
        PreparedStatement createStmt = Main.conn.prepareStatement("INSERT INTO DECKS(ID, NAME) VALUES(NULL, ?)");
        createStmt.setString(1, name.strip());
        createStmt.executeUpdate();

        // Commit the transaction.
        Main.conn.commit();

        // Get an updated list of decks and display them to the user. Repaint the main menu to reflect the updated data.
        this.updateDeckModel();
        mainPane.revalidate();
        mainPane.repaint();
    }

    /**
     * Rename the deck currently selected by the user.
     * @param pk Primary key of the deck to be renamed.
     * @param name New name for the deck.
     * @throws ClassNotFoundException If the JDBC Class cannot be found, throw an exception.
     * @throws SQLException If a database operation cannot be performed, throw an exception.
     */
    private void renameDeck(int pk, String name) throws ClassNotFoundException, SQLException {
        // Update the relevant DECKS tuple using a parameterized query.
        PreparedStatement renameStmt = Main.conn.prepareStatement("UPDATE DECKS SET NAME = ? WHERE ID = ?");
        renameStmt.setString(1, name.strip());
        renameStmt.setInt(2, pk);
        renameStmt.executeUpdate();

        // Commit the transaction.
        Main.conn.commit();

        // Get an updated list of information about the decks in the database and repaint the GUI.
        this.updateDeckModel();
        mainPane.revalidate();
        mainPane.repaint();
    }

    /**
     * Action Listener for this GUI.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteBtn) {

            // If the user clicks delete, get the index of the item that was selected when they clicked delete.
            int index = decksListComp.getSelectedIndex();

            // -1 is returned by getSelectedIndex if a row was not selected when the user clicked delete. If this is the
            // case, do nothing.
            if (index != -1) {
                DeckListItem deck = decksModel.getElementAt(index);

                // Get the primary key of the DeckListItem object selected by the user.
                int pk = deck.getDeckPK();
                try {

                    // Delete the selected deck and update the GUI.
                    this.deleteDeck(pk);
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (e.getSource() == createBtn) {

            // Prompt the user with an input dialog pop-up.
            String deckName = JOptionPane.showInputDialog("Enter the name for your new deck:");

            // If the user entered anything, use the input string to name the deck.
            if (deckName != null && !deckName.isBlank()) {
                try {

                    // Create a deck with the given name and repaint the GUI.
                    this.createDeck(deckName);
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (e.getSource() == renameBtn) {

            // Get index of the DeckListItem object representing the deck selected by the user.
            int index = decksListComp.getSelectedIndex();

            // -1 is returned if nothing was selected. If this is the case, do nothing.
            if (index != -1) {
                DeckListItem deck = decksModel.getElementAt(index);
                int pk = deck.getDeckPK();

                // Prompt the user to enter a new name with an input dialog pop-up.
                String newName = JOptionPane.showInputDialog("Enter the updated name:");

                // If the input string is not empty, update the deck name. If it is empty, do nothing.
                if (newName != null && !newName.isBlank()) {
                    try {

                        // Rename the deck and repaint the GUI.
                        this.renameDeck(pk, newName);
                    } catch (SQLException | ClassNotFoundException ex){
                        throw new RuntimeException(ex);
                    }
                }
            }
        }

        if (e.getSource() == modifyBtn) {

            // Get the index of the selected deck.
            int index = decksListComp.getSelectedIndex();

            // -1 is returned if no rows were selected when this action event occurred.
            if (index != -1) {

                // Get the deck item at the given index and extract the primary key.
                DeckListItem deck = decksModel.getElementAt(index);
                int deckPK = deck.getDeckPK();
                try {
                    // Clear any elements currently on the cardsPane GUI JPanel.
                    cardsPane.removeAll();

                    // Instantiate a new AddCardsGUI object to define the behavior of the GUI. Pass the cardsPane JPanel
                    // to paint its elements on and a deck primary key, which lets the object know which deck to modify.
                    // Pass container and controller to allow the AddCardsGUI to navigate back to the Main Menu. Also
                    // pass a reference to the current instance to allow AddCardsGUI to update this MainMenuGUI before
                    // switching the user back to this panel.
                    new AddCardsGUI(cardsPane, deckPK, container, controller, this);

                    // Switch to the AddCardsGUI using the CardLayout.
                    controller.show(container, "cards");
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (e.getSource() == reviewBtn) {

            // Get the index of the selected deck.
            int selIndex = decksListComp.getSelectedIndex();

            // -1 is returned if no rows were selected when this action event occurred.
            if (selIndex != -1) {

                // Get the deck item at the given index and extract the primary key.
                DeckListItem selDeck = decksModel.get(selIndex);

                // Get the primary key of the selected deck.
                int selDeckPK = selDeck.getDeckPK();
                try {
                    // Clear any elements currently on the cardsPane GUI JPanel.
                    boardPane.removeAll();

                    // Instantiate a new BoardGUI object to define the behavior of the GUI. Pass the boardPane JPanel
                    // to paint its elements on and a deck primary key, which lets the object know which deck to modify.
                    // Pass container and controller to allow the BoardGUI to navigate back to the Main Menu. Also
                    // pass a reference to the current instance to allow BoardGUI to update this MainMenuGUI before
                    // switching the user back to this panel.
                    new BoardGUI(selDeckPK, boardPane, container, controller, this);

                    // Switch to the boardGUI using CardLayout.
                    controller.show(container, "board");
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

        }
    }

    /**
     * This non-static nested class wraps together different pieces of information, which together summarize a single
     * deck for the user.
     */
    public class DeckListItem {

        // Primary key of the deck.
        private final int deckPK;

        // User-defined name of the deck.
        private String name;

        // Total cards associated with the deck.
        private int cardTotal;

        // Number of cards currently due to be reviewed by the user.
        private int reviewCount;

        // Constructor that takes the primary key as an argument.
        public DeckListItem(int newDeckPK) {
            deckPK = newDeckPK;
        }

        // Getter for primary key.
        public int getDeckPK() {
            return deckPK;
        }

        // Setters for the different instance variables.
        public void setReviewCount(int newReviewCount) { reviewCount = newReviewCount; }
        public void setCardTotal(int newCardTotal) { cardTotal = newCardTotal; }
        public void setName(String newName) { name = newName; }

        /**
         * Allows the JList to display the deck summary to the user.
         * @return A string representation of the object.
         */
        @Override
        public String toString() {
            return name + " DUE: " + reviewCount + "/" + cardTotal;
        }
    }
}
