import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/*
 * The ReviewEngine class provides two public methods that read and write respectively from the database. This is used
 * during review session to get new cards for the user to review and update cards based on the results.
 *
 * superMemoAlgo method in this class is an implementation of the SuperMemo2 algorithm, an open-source, free-to-use
 * algorithm developed by SuperMemo World. To learn more, please visit the following website:
 *
 * https://www.supermemo.com
 * Algorithm SM-2, (C) Copyright SuperMemo World, 1991.
 */
public final class ReviewEngine {
    // This instance variable is the primary key of the deck currently being reviewed. There is one deck per review
    // object, as only one deck is reviewed at a time.
    private final int deckId;

    /**
     * Constructor for ReviewEngine.
     * @param newDeckId Primary key of the deck currently being reviewed
     * @throws ClassNotFoundException If the JDBC Class cannot be found, throw an exception.
     * @throws SQLException If a database operation cannot be performed, throw an exception.
     */
    public ReviewEngine(int newDeckId) throws ClassNotFoundException, SQLException {deckId = newDeckId;}

    /**
     * Gets the first card identified based on the given parameters.
     * @return Returns a ReviewCard object, which contains data derived from a single row of the CARDS table.
     * @throws SQLException If a database operation cannot be performed, throw an exception.
     * @throws ClassNotFoundException If the JDBC Class cannot be found, throw an exception.
     */
    public ReviewCard getNextCard() throws SQLException, ClassNotFoundException {
        // This query pulls a single card from the database. As we are interested in information about the LINE and
        // MOVE associated with the CARD, we must join those tables. The query filters by the primary key DECKS_ID.

        // The statement: (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) filters cards based on IR_INTERVAL,
        // which is the amount of time (in days) that we should wait before showing the card to the user again. To
        // achieve this, we find the difference in milliseconds between the current moment in time and the last review,
        // and compare it to the IR_INTERVAL (there are 86400000 milliseconds in one day).
        String query = "SELECT CARDS.ID, LINES.NAME, LINES.LINE, " +
                        "MOVES.BEFORE_FEN, MOVES.AFTER_FEN, MOVES.ORDER_IN_LINE " +
                        "FROM CARDS JOIN CARDS_TO_MOVES ON CARDS.ID = CARDS_TO_MOVES.CARDS_ID " +
                        "JOIN MOVES ON CARDS_TO_MOVES.MOVES_ID = MOVES.ID " +
                        "JOIN LINES ON MOVES.LINES_ID = LINES.ID " +
                        "WHERE CARDS.DECKS_ID = ? " +
                        "AND (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) " +
                        "LIMIT 1 ";

        // Get the current UNIX time-stamp on this machine.
        long currentTime = System.currentTimeMillis();

        // Create a parameterized query.
        PreparedStatement preStmt = Main.conn.prepareStatement(query);
        preStmt.setInt(1, deckId);
        preStmt.setLong(2, currentTime);

        // Execute the query.
        ResultSet rs = preStmt.executeQuery();

        // Get the following columns from the result set. Because we used LIMIT 1, we only need to perform this
        // operation once.
        int id = rs.getInt("ID");
        String name = rs.getString("NAME");
        String line = rs.getString("LINE");
        String beforeFEN = rs.getString("BEFORE_FEN");
        String afterFEN = rs.getString("AFTER_FEN");
        int orderInLine = rs.getInt("ORDER_IN_LINE");

        // Validate the different String values and return a new ReviewCard using the queried information.
        if (name != null && line != null && beforeFEN != null && afterFEN != null) {
            return new ReviewCard(id, name, line, beforeFEN, afterFEN, orderInLine);
        } else {
            // If a ReviewCard was not successfully generated, throw an exception.
           throw new RuntimeException("ReviewCard could not be successfully generated");
        }
    }

    /**
     *
     * @param grade This parameter is the self-rating assigned by the user.
     * @param cardId This parameter is the primary key of the card that should be updated.
     * @throws SQLException If a database operation cannot be performed, throw an exception.
     * @throws ClassNotFoundException If the JDBC Class cannot be found, throw an exception.
     */
    public void updateCard(int grade, int cardId) throws SQLException, ClassNotFoundException {
        // Select the relevant card using the primary key.
        String query = "SELECT REP_NUMBER, EASY_FACTOR, IR_INTERVAL FROM CARDS WHERE ID = ?";
        PreparedStatement preStmt = Main.conn.prepareStatement(query);
        preStmt.setInt(1, cardId);

        // Execute a parameterized query.
        ResultSet rs = preStmt.executeQuery();

        // Get the REP_NUMBER, IR_INTERVAL, and EASY_FACTOR. These are the inputs for the SuperMemo algorithm.
        int repNum = rs.getInt("REP_NUMBER");
        long interval = rs.getLong("IR_INTERVAL");
        double easFactor = rs.getDouble("EASY_FACTOR");

        // Call the method that implements the SuperMemo algorithm. Pass three parameters currently attached to the
        // card, along with the user-identified self-rating. The result is an array of doubles, the elements of which
        // are the new values for each of the three SuperMemo parameters.
        double[] memoResult = this.superMemoAlgo(grade, repNum, easFactor, interval);

        // Update the card with three new parameters and a new "LAST_REVIEW" timestamp reflecting the current time.
        String query2 = "UPDATE CARDS " +
                        "SET REP_NUMBER = ?, " +
                        "EASY_FACTOR = ?, " +
                        "IR_INTERVAL = ?, " +
                        "LAST_REVIEW = ? " +
                        "WHERE ID = ? ";

        PreparedStatement preStmt2 = Main.conn.prepareStatement(query2);

        // Get the current UNIX timestamp from the system.
        long currentTime = System.currentTimeMillis();
        preStmt2.setDouble(1, memoResult[0]);
        preStmt2.setDouble(2, memoResult[1]);
        preStmt2.setDouble(3, memoResult[2]);
        preStmt2.setLong(4, currentTime);
        preStmt2.setInt(5, cardId);

        // Execute the parameterized query.
        preStmt2.executeUpdate();

        // Commit the transaction.
        Main.conn.commit();
    }

    /**
     * Implementation of the open-source SuperMemo2 Algorithm:
     * Algorithm SM-2, (C) Copyright SuperMemo World, 1991
     *
     * @param grade The self-rating chosen by the user.
     * @param repNum The Repetition Number: how many times the card has been successfully reviewed.
     * @param easFactor Easiness Factor: how easy the card is.
     * @param interval Inter-Repetition Interval: the number of days to wait before the next review.
     * @return Returns a double array with three values: repNum, easFactor, and interval.
     */
    private double[] superMemoAlgo(int grade, int repNum, double easFactor, long interval) {
        if (grade >= 3) {
            // If this conditional is triggered, the user gave a positive grade (3, 4, or 5).
            if (repNum == 0) {
                // Set the interval based on the Repetition Number already associated with the card.
                interval = 1;
            } else if (repNum == 1) {
                interval = 6;
            } else {
                interval = Math.round(interval * easFactor);
            }
            // Increment Repetition Number, as we just successfully completed a repetition.
            repNum = repNum + 1;
        } else {
            // If the user gave a "failing grade" (0, 1, 2), set interval to one day, and Repetition Number to 0.
            repNum = 0;
            interval = 1;
        }

        // Adjust the Easiness Factor based on the grade.
        easFactor = easFactor + (.1 - (5 - grade) * (.08 + (5 - grade) * .02));

        // If the easiness factor ended up below 1.3, set it to 1.3 as a floor.
        if (easFactor < 1.3) {
            easFactor = 1.3;
        }

        // Package the three updated parameters in an array of doubles.
        double[] resultArr = {repNum, easFactor, interval};
        return resultArr;
    }

    /**
     * This non-static nested class allows us to instantiate ReviewCard objects and return them to external classes.
     * Each review card represents a card in the CARDS table.
     */
    public final class ReviewCard {
        // ID is the primary key for the card.
        private final int id;

        // Name of the line associated with the card.
        private final String lineName;

        // Name of the move associated with the card.
        private final String lineMoves;

        // Position of the board before the move is made, represented in standard FEN chess notation.
        private final String beforeFEN;

        // Position of the board after the move is made, represented in standard FEN chess notation.
        private final String afterFEN;

        // The sequence of the move in the line. The sequence starts with 1, which is the first move made by white
        // in the sequence of chess moves.
        private final int orderInLine;

        // Instantiate the ReviewCard with parameters representing the different fields we need to display the card
        // to the user.
        ReviewCard(int newId, String newName, String newLine, String newBeforeFEN, String newAfterFEN,
                   int newOrderInLine) {
            id = newId;
            lineName = newName;
            lineMoves = newLine;
            beforeFEN = newBeforeFEN;
            afterFEN = newAfterFEN;
            orderInLine = newOrderInLine;
        }

        // Getters for the instance variables in this object.
        public int getId() { return id; }
        public String getLineName() { return lineName; }
        public String getLineMoves() { return lineMoves; }
        public String getBeforeFEN() { return beforeFEN; }
        public String getAfterFEN() { return afterFEN; }
        public int getOrderInLine() {return orderInLine;}
    }
}
