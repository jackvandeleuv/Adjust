import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * This class provides a static method to initialize the database for this application.
 */
public final class InitDB  {
    /**
     * This method creates empty tables to populate the database.
     */
    public static void makeTables() {
        try {
            // Create a statement to process our database transactions.
            Statement stmt = Main.conn.createStatement();

            // Drop any table named DECKS.
            stmt.execute("DROP TABLE IF EXISTS DECKS");

            // Create a new DECKS table with an INTEGER PRIMARY KEY that is an alias for ROWID.
            stmt.execute("CREATE TABLE IF NOT EXISTS DECKS(" +
                            "ID INTEGER PRIMARY KEY," +
                            "NAME TEXT)");
            Main.conn.commit();

            // Create a new CARDS table with an INTEGER PRIMARY KEY that is an alias for ROWID. The values REP_NUMBER,
            // EASY_FACTOR, IR_INTERVAL, and LAST_REVIEW each refer to a parameter in the SuperMemo2 algorithm, which
            // is used by the ReviewEngine Class to decide when to show the card to the user.
            stmt.execute("DROP TABLE IF EXISTS CARDS");
            stmt.execute("CREATE TABLE IF NOT EXISTS CARDS(" +
                            "ID INTEGER PRIMARY KEY," +
                            "REP_NUMBER REAL," +
                            "EASY_FACTOR REAL," +
                            "IR_INTERVAL REAL," +
                            "LAST_REVIEW REAL," +
                            "DECKS_ID TEXT," +
                            "FOREIGN KEY (DECKS_ID) REFERENCES DECKS(ID))");
            Main.conn.commit();

            // Drop any existing table called CARDS_TO_MOVES.
            stmt.execute("DROP TABLE IF EXISTS CARDS_TO_MOVES");

            // Create a new CARDS_TO_MOVES intermediate table that specifies the relationships between CARDS to MOVES.
            stmt.execute("CREATE TABLE IF NOT EXISTS CARDS_TO_MOVES(" +
                            "CARDS_ID INTEGER," +
                            "MOVES_ID INTEGER," +
                            "FOREIGN KEY (CARDS_ID) REFERENCES CARDS(ID)," +
                            "FOREIGN KEY (MOVES_ID) REFERENCES MOVES(ID))");
            Main.conn.commit();

            // Drop any existing table called LINES.
            stmt.execute("DROP TABLE IF EXISTS LINES");

            // Create a new LINES table with an INTEGER PRIMARY KEY that is an alias for ROWID. LINE is the sequence of
            // moves comprising the opening, written in Standard Algebraic Notation. ECO is the Encyclopedia of Chess
            // Openings identification code.
            stmt.execute("CREATE TABLE IF NOT EXISTS LINES(" +
                            "ID INTEGER PRIMARY KEY," +
                            "NAME TEXT," +
                            "LINE TEXT," +
                            "ECO TEXT)");
            Main.conn.commit();

            // Drop any existing table called MOVES.
            stmt.execute("DROP TABLE IF EXISTS MOVES");

            // Create a new MOVES table with an INTEGER PRIMARY KEY that is an alias for ROWID. ORDER_IN_LINE is the
            // position of the move in the opening. E.g. in the line "1. e4 e6 2. d4 d5," "d4" is at position 3.
            // BEFORE_FEN and AFTER_FEN represent the state of the board before and after the move is made using
            // Forsyth-Edwards Notation.
            stmt.execute("CREATE TABLE IF NOT EXISTS MOVES(" +
                        "ID INTEGER PRIMARY KEY," +
                        "ORDER_IN_LINE INTEGER," +
                        "BEFORE_FEN TEXT," +
                        "AFTER_FEN TEXT," +
                        "LINES_ID INTEGER," +
                        "FOREIGN KEY (LINES_ID) REFERENCES LINES(ID))");
            Main.conn.commit();

        } catch (SQLException e) {
            System.out.println("Error connecting to db");
            e.printStackTrace();
        }
    }

    public static void checkRows() throws SQLException {
        Statement stmt = Main.conn.createStatement();
        stmt.execute("SELECT DECKS.NAME, COALESCE(COUNT(CARDS.ID), 0) " +
                "                            FROM DECKS LEFT JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID " +
                "                            WHERE (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) " +
                "                            OR CARDS.ID IS NULL " +
                "                            GROUP BY DECKS.ID " +
                "                            ORDER BY DECKS.NAME DESC ");
        ResultSet rs = stmt.getResultSet();
        while (rs.next()) {
            System.out.print(rs.getString(1));
            System.out.print(" | ");
            System.out.print(rs.getInt(2));
            System.out.println("\n");
        }
    }


    /**
     * This method is for debugging. It provides a template for querying the database.
     * @throws SQLException If an operation cannot be performed, throw an error.
     */
    public static void queryDB() throws SQLException {
        Statement stmt = Main.conn.createStatement();

        stmt.execute("SELECT CARDS_TO_MOVES.MOVES_ID, CARDS_TO_MOVES.CARDS_ID, CARDS.DECKS_ID FROM CARDS_TO_MOVES JOIN CARDS ON CARDS.ID = CARDS_TO_MOVES.CARDS_ID");
        ResultSet rs = stmt.getResultSet();

        System.out.println("CARDS_TO_MOVES:");
        while (rs.next()) {
            System.out.println("+++++++++");
            System.out.println("MOVES:");
            System.out.println(rs.getInt(1));
            System.out.println("CARDS:");
            System.out.println(rs.getInt(2));
            System.out.println("+++++++++");
            System.out.println("DECK");
            System.out.println(rs.getInt(3));
            System.out.println("|||||||||||||||||||");
        }

        System.out.println("CARDS:");
        stmt.execute("SELECT ID, DECKS_ID, IR_INTERVAL FROM CARDS");
        ResultSet rs2 = stmt.getResultSet();
        while (rs2.next()) {
            System.out.println("+++++++++++++++");
            System.out.println("CARDS.ID:");
            System.out.println(rs2.getInt(1));
            System.out.println("DECKS_ID:");
            System.out.println(rs2.getInt(2));
            System.out.println("IR_INTERVAL:");
            System.out.println(rs2.getInt(3));
            System.out.println("+++++++++++++++");
        }

        System.out.println("DECKS:");
        stmt.execute("SELECT ID, NAME FROM DECKS");
        ResultSet rs3 = stmt.getResultSet();
        while (rs3.next()) {
            System.out.println(rs3.getInt(1));
            System.out.println(rs3.getString(2));
        }
    }
}
