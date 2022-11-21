import java.sql.SQLException;
import javax.swing.*;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class defines the behavior of the review board GUI, which allows the user to review different chess positions.
 * @author Jack Vandeleuv
 */
public final class BoardGUI implements ActionListener {
    // JPanel on which this GUI is painted.
    private final JPanel pane;

    // Reference to the MainMenuGUI object, which allows us to refresh the mainMenu before returning.
    private final MainMenuGUI mainMenu;

    // ReviewEngine object that provides methods to pull new cards that are due to be reviewed and update cards once
    // they have been reviewed.
    private final ReviewEngine revEng;

    // JPanel representing the chess board.
    private final JPanel board = new JPanel();

    // Array of Square objects. Each Square holds information about one square on the chess board.
    private Square[] squares;

    // Name of the line currently being reviewed.
    private String lineName;

    // Positions of the board before and after the move currently being reviewed is made, represented in standard FEN
    // chess notation.
    private String beforeFEN;
    private String afterFEN;
    private static final String defaultFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // Primary key of the card currently being reviewed.
    private int currentCardId;

    // Sequence of the move being reviewed in the associated line.
    private int orderInLine;

    // JTextArea that displays information about the position currently being reviewed.
    private final JTextArea infoPanel = new JTextArea();

    // Button array that allows the user to rate how easy it was to recall the information.
    private final JButton[] selfRating = new JButton[6];

    // Button that reveals the correct move in the given position.
    private final JButton showAnswer = new JButton("Show Answer");

    // Allows the user to toggle between the before and after position for the move currently being reviewed.
    private final JButton leftArrow = new JButton("<");
    private final JButton rightArrow = new JButton(">");

    // Returns the user to the Main Menu.
    private final JButton backBtn = new JButton("Back");

    /**
     * Constructor for the review board GUI.
     * @param newCurrentDeckId Primary key for the deck currently being reviewed.
     * @param boardPane JPanel on which this menu is painted.
     * @param mainGUI Reference to the main menu GUI, which allows us to refresh it before returning.
     */
    public BoardGUI(int newCurrentDeckId, JPanel boardPane, MainMenuGUI mainGUI) {
        // Store each of the parameters in an instance variable.
        mainMenu = mainGUI;
        pane = boardPane;

        try {
            // Instantiate a ReviewEngine object, which provides methods for getting cards for review and updating them
            // after they have been reviewed. Pass the primary key for the deck currently being reviewed as an argument.
            revEng = new ReviewEngine(newCurrentDeckId);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Could not instantiate ReviewEngine");
            throw new RuntimeException(e);
        }

        // JPanel that wraps the various buttons together to keep them aligned.
        JPanel buttonBox = new JPanel();

        // Set the board's preferred size.
        board.setPreferredSize(new Dimension(450, 450));

        // Add board to a JPanel wrapper to help keep it aligned and to prevent the dimensions of the board from
        // distorting.
        JPanel boardWrapper = new JPanel();
        boardWrapper.add(board);

        // Instantiate five JButtons, add them to the selfRating array, and register an action listener for each.
        for (int i = 0; i < selfRating.length; i++) {
            selfRating[i] = new JButton(String.valueOf(i));
            selfRating[i].addActionListener(this);
            buttonBox.add(selfRating[i]);
        }

        // Wrap the "Show Answer" and "Back" buttons in a panel to keep them aligned.
        JPanel lowerBtnWrapper = new JPanel();
        lowerBtnWrapper.add(showAnswer);
        lowerBtnWrapper.add(backBtn);

        // Create a panel to wrap the two arrow buttons and keep them aligned.
        JPanel arrowBox = new JPanel();
        arrowBox.add(leftArrow);
        arrowBox.add(rightArrow);

        // Add infoPanel to a wrapper to help keep it aligned.
        JScrollPane scrollPane = new JScrollPane();
        JPanel infoWrapper = new JPanel();
        scrollPane.add(infoPanel);
        infoWrapper.add(scrollPane);

        // leftCol wraps all the elements on the left-half of the menu.
        JPanel leftCol = new JPanel();

        // Set GridBagLayout to keep the components in each wrapper panel centered.
        infoWrapper.setLayout(new GridBagLayout());
        lowerBtnWrapper.setLayout(new GridBagLayout());
        boardWrapper.setLayout(new GridBagLayout());
        buttonBox.setLayout(new GridBagLayout());
        arrowBox.setLayout(new GridBagLayout());

        // Add a GridLayout to the chess board to keep the JPanels inside in a tight grid.
        GridLayout gLayout = new GridLayout(8, 8);
        board.setLayout(gLayout);

        // Set these JPanels to a horizontal BoxLayout to keep the leftCol and boardWrapper panel aligned.
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        // Add the different wrapper panels to the leftCol JPanel, which hold all the elements on the left-half of
        // the menu.
        leftCol.add(infoWrapper);
        leftCol.add(arrowBox);
        leftCol.add(buttonBox);
        leftCol.add(lowerBtnWrapper);

        // Set a minimum size for each panel, which is used by BoxLayout as a preferred (but not guaranteed) minimum
        // size.
        arrowBox.setMinimumSize(new Dimension(550, 100));
        lowerBtnWrapper.setMinimumSize(new Dimension(550, 100));
        leftCol.setMinimumSize(new Dimension(550, 600));
        boardWrapper.setMinimumSize(new Dimension(450, 600));
        buttonBox.setMinimumSize(new Dimension(550, 100));
        scrollPane.setMinimumSize(new Dimension(550, 300));
        infoWrapper.setMinimumSize(new Dimension(550, 250));

        // Prevent the user from changing the text that displays in infoPanel.
        infoPanel.setEditable(false);

        // Set a larger font for the line titles and other info displayed by infoPanel.
        infoPanel.setFont(new Font("Sans", Font.BOLD, 24));

        // Register action listeners for the different buttons.
        showAnswer.addActionListener(this);
        rightArrow.addActionListener(this);
        leftArrow.addActionListener(this);
        backBtn.addActionListener(this);

        // Add the wrappers for the right and left sides together in one pane.
        pane.add(leftCol);
        pane.add(boardWrapper);

        // Add 64 JPanels of the appropriate color to the board JPanel, each representing a square.
        this.renderBoard();

        MainMenuGUI.resetFrame();

        // Pull a new card from the deck and show it to the user.
        this.promptUser();

        // Repaint the elements to incorporate the changes made by promptUser.
        pane.revalidate();
        pane.repaint();
    }

    private void renderBoard() {
        // Create an array of Square objects.
        squares = new Square[64];

        // Offset allows us to alternate the order of the light and dark squares for every row, so that the chess board
        // can be created properly.
        boolean offset = true;

        // Iterate 64 times, once for every square on the board.
        for (int i = 1; i <= 64; i++) {
            JPanel panel = new JPanel();

            // Alternate between odd and even, setting a different panel color each time. If the offset is set to false,
            // flip the order in which light/dark squares are added.
            if (i % 2 != 0 && offset) { panel.setBackground(Color.WHITE); }
            if (i % 2 == 0 && offset) { panel.setBackground(Color.decode("#428D44")); }
            if (i % 2 != 0 && !offset) { panel.setBackground(Color.decode("#428D44")); }
            if (i % 2 == 0 && !offset) { panel.setBackground(Color.WHITE); }

            // Instantiate a new Square object and pass it a reference to the given panel.
            squares[i - 1] = new Square(panel);

            // Add the panel to the board.
            board.add(panel);

            // If we have reached the end of an 8-square row, flip the offset value.
            if (i % 8 == 0) { offset = !offset; }
        }
        pane.revalidate();
        pane.repaint();
    }

    /**
     * This method changes the appearance of the board to display a new chess position.
     * @param fen This string represents the desired board position using standard FEN chess notation.
     */
    private void paintFEN(String fen, boolean isWhite) {
        // Remove any labels attached to the array of 64 Squares.
        for (Square sq : squares) { sq.removePiece(); }

        // Split the FEN string to separate different pieces of meta-data (like castle availability).
        String[] metaArr = fen.split(" ");

        // The first element in the metaArr is the board position. Get this String and separate it by "/" to get each
        // of the 8 rows as a separate string.
        String[] posArr = metaArr[0].split("/");

        // The outer for loop iterates through each String, which represents a row of the board.
        int index = 0;
        for (String row : posArr) {
            // The inner for loop iterates through characters in the row, each of which represents a square in the row.
            for (int rowIndex = 0; rowIndex < row.length(); rowIndex++) {
                // Get the character at the current index in the row.
                char currentChar = row.charAt(rowIndex);

                // If the character is not a numeric value, it is a letter representing a piece.
                if (!Character.isDigit(currentChar)) {

                    int sqIndex = index;
                    if (!isWhite) {
                        sqIndex = Math.abs(index - 64) - 1;
                    }

                    // Get the Square object at the appropriate index.
                    Square sq = squares[sqIndex];

                    // Call the charToPiece method to instantiate the appropriate Piece based on the FEN string,
                    // and use the setter in Square to pass it a reference to the new Piece object.
                    sq.setPiece(this.charToPiece(currentChar, index));
                }

                // If the character is a numeric value, that value indicates the number of consecutive blank squares in
                // that row. Because of this, we need to increment the index by a value equal to the number represented
                // by currentChar. We also subtract 49 to convert from ASCII.
                if (Character.isDigit(currentChar)) { index = index + (int)currentChar - 49; }

                // Increment the index to move onto the next square.
                index = index + 1;
            }
        }

        // Repaint the board component with the updated information.
        board.revalidate();
        board.repaint();
    }

    /**
     * This method receives a character representation of a chess piece and the position of that piece on the board and
     * instantiates a Piece object using those two pieces of information.
     * @param c A char representing the type of piece to be instantiated.
     * @param newPos The current position of the piece on the chess board.
     * @return Returns a subclass of the abstract Piece class.
     */
    private Piece charToPiece(char c, int newPos) {
        // This switch case statement returns different Piece objects depending on the given char. In FEN notation,
        // upper-case letters indicate white pieces and lower-case letters indicate black pieces.
        switch (c) {
            case 'P':
                return new Pawn(newPos, 'w');
            case 'B':
                return new Bishop(newPos, 'w');
            case 'N':
                return new Knight(newPos, 'w');
            case 'R':
                return new Rook(newPos, 'w');
            case 'K':
                return new King(newPos, 'w');
            case 'Q':
                return new Queen(newPos, 'w');
            case 'p':
                return new Pawn(newPos, 'b');
            case 'b':
                return new Bishop(newPos, 'b');
            case 'n':
                return new Knight(newPos, 'b');
            case 'r':
                return new Rook(newPos, 'b');
            case 'k':
                return new King(newPos, 'b');
            case 'q':
                return new Queen(newPos, 'b');
            default:
                // If the operation fails, throw an exception.
                throw new IllegalArgumentException("Can't create Piece with given parameters!");
        }
    }

    /**
     * Square is a non-static nested class that holds information about a given square on the displayed chess board.
     */
    private static class Square {
        // Each Square has a reference to an associated panel that it can modify.
        private final JPanel panel;

        // Instantiate Square and pass the associated panel.
        public Square(JPanel newPanel) {
            panel = newPanel;
        }

        // Setter for Piece.
        public void setPiece(Piece newPiece) {
            // Validate input.
            if (newPiece == null) { throw new IllegalArgumentException("Can't set null piece!"); }

            // Store the reference to the Piece as an instance variable.
            // Squares can have an associated Piece object, although some squares will have no Piece and be empty.

            // Paint the piece on the GUI.
            try {
                // Get the ImageIcon associated with the piece.
                ImageIcon image = newPiece.getImage();

                // Convert the ImageIcon to an Image to allow resizing.
                Image temp = image.getImage();

                // Resize the Image based on the size of the panel.
                Image temp2 = temp.getScaledInstance(panel.getWidth(), panel.getHeight(), Image.SCALE_SMOOTH);

                // Convert the Image into a new ImageIcon.
                ImageIcon image2 = new ImageIcon(temp2);

                // Insert the new ImageIcon into a label and add it to the panel.
                JLabel label = new JLabel(image2, JLabel.CENTER);
                panel.add(label);
            } catch (NoSuchFieldException ex) {
                // If the Piece object is unable to return an image, it will throw an exception.
                System.out.println("setPiece encountered an error");
                System.out.println(ex.getMessage());
            }
        }

        // Method to remove the Piece from the panel.
        public void removePiece() {
            panel.removeAll();
        }
    }

    /**
     * This method puts a new position on the board. It also disables the rating and arrow buttons. Once the user is
     * ready to see the answer, they hit "Show Answer" to see the hidden information associated with the card.
     */
    public void promptUser() {
        try {
            // Get any card due to be reviewed from the deck.
            ReviewEngine.ReviewCard revCard = revEng.getNextCard();

            // Extract the relevant information from the ReviewCard object and store it as instance variables.
            currentCardId = revCard.getId();
            beforeFEN = revCard.getBeforeFEN();
            afterFEN = revCard.getAfterFEN();
            lineName = revCard.getLineName();
            orderInLine = revCard.getOrderInLine();

            // Allow the user to click the "Show Answer" button. If this try block throws an error, the show button
            // should not be enabled, as there are no more cards to show.
            showAnswer.setEnabled(true);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("promptUser encountered an error");
            System.out.println(ex.getMessage());
        } catch (RuntimeException ex) {
            beforeFEN = defaultFEN;
            System.out.println(ex.getMessage());
        }

        // Update the board GUI with the new beforeFEN string.
        paintFEN(beforeFEN, orderInLine % 2 != 0);

        // Generate a label string based on whether the sequence of the move in the line is even or odd, which allows
        // us to infer whether it is a white or black move.
        String toMove = "";
        if (orderInLine % 2 == 0) { toMove = "BLACK TO MOVE"; }
        if (orderInLine % 2 != 0) { toMove = "WHITE TO MOVE"; }

        // Display the name of the current line and toMove on the info panel.
        infoPanel.setText(lineName + "\n" + toMove);

        // Disable the arrows and rating buttons.
        rightArrow.setEnabled(false);
        leftArrow.setEnabled(false);
        for (JButton jb : selfRating) {
            jb.setEnabled(false);
        }

        // Repaint the GUI.
        pane.revalidate();
        pane.repaint();
    }

    /**
     * This method is triggered when the user hits the "Show Answer" button, which indicates that they have made their
     * best guess about the position and are ready to see the answer, i.e. correct chess move given the starting
     * position.
     */
    public void showPos() {
        // Change the position on the board to show the answer immediately.
        paintFEN(afterFEN, orderInLine % 2 != 0);

        // Disable the showAnswer button.
        showAnswer.setEnabled(false);

        // Enable the arrow and rating buttons.
        rightArrow.setEnabled(true);
        leftArrow.setEnabled(true);
        for (JButton jb : selfRating) {
            jb.setEnabled(true);
        }

        // Repaint the GUI.
        pane.revalidate();
        pane.repaint();
    }

    /**
     * Action Listener for this GUI panel.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Show the answer and toggle the components when the user hits "Show Answer."
        if (e.getSource() == showAnswer) { this.showPos(); }

        // Toggle between the board positions before and after the relevant move when the user clicks the arrows.
        if (e.getSource() == rightArrow) {
            paintFEN(afterFEN, orderInLine % 2 != 0);
        }
        if (e.getSource() == leftArrow) {
            paintFEN(beforeFEN, orderInLine % 2 != 0);
        }

        // If the user hits the back button, return to the main menu.
        if (e.getSource() == backBtn) {
            // Refresh the list of cards displayed by the main menu and switch panels using CardLayout.
            mainMenu.mainReturn();
        }

        // Check if the action event was triggered by any of the self-rating JButtons.
        for (int i = 0; i < selfRating.length; i++) {
            if (e.getSource() == selfRating[i]) {
                try {
                    // Update the card using the grade, which is the input for the SuperMemo2 spaced-repetition
                    // algorithm.
                    revEng.updateCard(i, currentCardId);
                } catch (SQLException | ClassNotFoundException ex) {
                    System.out.println("selfRating Action Listener encountered an error");
                    System.out.println(ex.getMessage());
                }

                // Prompt the user with a new card to review.
                this.promptUser();
            }
        }
    }
}
