import java.sql.*;

public final class InitDB  {
    public static void makeTables() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        try {
            Connection connection = DriverManager.getConnection(jbdcUrl);
            System.out.println("Successfully connected to DB!");
            Statement stmt = connection.createStatement();

//            stmt.execute("DROP TABLE IF EXISTS LINES");
            StringBuilder linesQ = new StringBuilder();
            linesQ.append("CREATE TABLE IF NOT EXISTS LINES(");
            linesQ.append("ID INTEGER PRIMARY KEY,");
            linesQ.append("NAME TEXT,");
            linesQ.append("LINE TEXT,");
            linesQ.append("ECO TEXT)");
            stmt.execute(linesQ.toString());

//            stmt.execute("DROP TABLE IF EXISTS MOVES");
            StringBuilder movesQ = new StringBuilder();
            movesQ.append("CREATE TABLE IF NOT EXISTS MOVES(");
            movesQ.append("ID INTEGER PRIMARY KEY,");
            movesQ.append("ORDER_IN_LINE INTEGER,");
            movesQ.append("BEFORE_FEN TEXT,");
            movesQ.append("AFTER_FEN TEXT,");
            movesQ.append("LINES_ID INTEGER,");
            movesQ.append("FOREIGN KEY (LINES_ID) REFERENCES LINES(ID))");
            stmt.execute(movesQ.toString());

            stmt.execute("DROP TABLE IF EXISTS DECKS");
            StringBuilder decksQ = new StringBuilder();
            decksQ.append("CREATE TABLE IF NOT EXISTS DECKS(");
            decksQ.append("ID INTEGER PRIMARY KEY,");
            decksQ.append("NAME TEXT)");
            stmt.execute(decksQ.toString());

            stmt.execute("DROP TABLE IF EXISTS CARDS");
            StringBuilder cardsQ = new StringBuilder();
            cardsQ.append("CREATE TABLE IF NOT EXISTS CARDS(");
            cardsQ.append("ID INTEGER PRIMARY KEY,");
            cardsQ.append("REP_NUMBER REAL,");
            cardsQ.append("EASY_FACTOR REAL,");
            cardsQ.append("IR_INTERVAL REAL,");
            cardsQ.append("LAST_REVIEW REAL,");
            cardsQ.append("DECKS_ID TEXT,");
            cardsQ.append("FOREIGN KEY (DECKS_ID) REFERENCES DECKS(ID))");
            stmt.execute(cardsQ.toString());

            stmt.execute("DROP TABLE IF EXISTS CARDS_TO_MOVES");
            StringBuilder cardsMovesQ = new StringBuilder();
            cardsMovesQ.append("CREATE TABLE IF NOT EXISTS CARDS_TO_MOVES(");
            cardsMovesQ.append("CARDS_ID INTEGER,");
            cardsMovesQ.append("MOVES_ID INTEGER,");
            cardsMovesQ.append("FOREIGN KEY (CARDS_ID) REFERENCES CARDS(ID),");
            cardsMovesQ.append("FOREIGN KEY (MOVES_ID) REFERENCES MOVES(ID))");
            stmt.execute(cardsMovesQ.toString());

            connection.close();
            System.out.println("Connection closed!");

        } catch (SQLException e) {
            System.out.println("Error connecting to db");
            e.printStackTrace();
        }
    }

    public static void addTestData() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";

        Connection connection = DriverManager.getConnection(jbdcUrl);
        System.out.println("Successfully connected to DB!");
        Statement stmt = connection.createStatement();

        stmt.execute("INSERT INTO DECKS (ID, NAME) VALUES(1, 'MY FRENCH DEFENSE')");
        stmt.execute("INSERT INTO DECKS (ID, NAME) VALUES(2, 'MY LONDON SYSTEM')");

        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(1, 1, 0, 2.5, 1, 1666027078524)");
        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(2, 1, 0, 2.5, 2, 1665940678524)");
        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(3, 1, 0, 2.5, 3, 1665940678524)");
        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(4, 1, 0, 2.5, 3, 1665940678524)");
        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(5, 1, 0, 2.5, 3, 1665854278524)");
        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(6, 2, 0, 2.5, 3, 1665854278524)");
        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(7, 2, 0, 2.5, 3, 1665854278524)");
        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(8, 2, 0, 2.5, 3, 1665508678524)");
        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(9, 2, 0, 2.5, 3, 1665508678524)");
        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(10, 2, 0, 2.5, 3, 1665508678524)");

        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(1, 1)");
        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(2, 2)");
        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(3, 3)");
        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(4, 4)");
        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(5, 5)");
        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(6, 6)");
        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(7, 7)");
        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(8, 8)");
        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(9, 9)");
        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(10, 10)");

        stmt.execute("INSERT INTO LINES (ID, NAME, ECO, LINE) VALUES(1,  'FRENCH DEFENSE', 'C00', '1. e4 d3')");
        stmt.execute("INSERT INTO LINES (ID, NAME, ECO, LINE) VALUES(2, 'LONDON SYSTEM', 'C001', '1. e4 c3')");

        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(1, 1, 2, 'rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1', 'rnbqkbnr/pppp1ppp/4p3/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1')");
        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(2, 1, 4, 'rnbqkbnr/pppp1ppp/4p3/8/3PP3/8/PPP2PPP/RNBQKBNR b KQkq d3 0 1', 'rnbqkbnr/ppp2ppp/4p3/3p4/3PP3/8/PPP2PPP/RNBQKBNR w KQkq d6 0 1')");
        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(3, 1, 6, 'rnbqkbnr/ppp2ppp/4p3/3pP3/3P4/8/PPP2PPP/RNBQKBNR b KQkq - 0 1', 'rnbqkbnr/pp3ppp/4p3/2ppP3/3P4/8/PPP2PPP/RNBQKBNR w KQkq c6 0 1')");
        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(4, 1, 4, 'rnbqkbnr/pppp1ppp/4p3/8/3P4/4P3/PPP2PPP/RNBQKBNR b KQkq - 0 1', 'rnbqkbnr/ppp2ppp/4p3/3p4/3P4/4P3/PPP2PPP/RNBQKBNR w KQkq d6 0 1')");
        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(5, 1, 6, 'rnbqkbnr/ppp2ppp/4p3/3pP3/8/3P4/PPP2PPP/RNBQKBNR b KQkq - 0 1', 'rnbqkbnr/pp3ppp/4p3/2ppP3/8/3P4/PPP2PPP/RNBQKBNR w KQkq c6 0 1')");
        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(6, 2, 3, 'rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq d6 0 1', 'rnbqkbnr/ppp1pppp/8/3p4/3P1B2/8/PPP1PPPP/RN1QKBNR b KQkq - 0 1')");
        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(7, 2, 5, 'rnbqkb1r/ppp1pppp/5n2/3p4/3P1B2/8/PPP1PPPP/RN1QKBNR w KQkq - 0 1', 'rnbqkb1r/ppp1pppp/5n2/3p4/3P1B2/4P3/PPP2PPP/RN1QKBNR b KQkq - 0 1')");
        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(8, 2, 7, 'r1bqkb1r/ppp1pppp/2n2n2/3p4/3P1B2/4P3/PPP2PPP/RN1QKBNR b KQkq - 0 1', 'r1bqkb1r/ppp1pppp/2n2n2/3p4/3P1B2/2P1P3/PP3PPP/RN1QKBNR b KQkq - 0 1')");
        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(9, 2, 5, 'rn1qkbnr/ppp1pppp/8/3p1b2/3P1B2/8/PPP1PPPP/RN1QKBNR w KQkq - 0 1', 'rn1qkbnr/ppp1pppp/8/3p1b2/3P1B2/5N2/PPP1PPPP/RN1QKB1R b KQkq - 0 1')");
        stmt.execute("INSERT INTO MOVES (ID, LINES_ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN) VALUES(10, 2, 7, 'rn1qkbnr/pp2pppp/2p5/3p1b2/3P1B2/5N2/PPP1PPPP/RN1QKB1R b KQkq - 0 1', 'rn1qkbnr/pp2pppp/2p5/3p1b2/3P1B2/4PN2/PPP2PPP/RN1QKB1R b KQkq - 0 1')");

        connection.close();
        System.out.println("Connection closed!");
    }

    public static void newTestCards() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";

        Connection connection = DriverManager.getConnection(jbdcUrl);
        Statement stmt = connection.createStatement();
//        System.out.println("Successfully connected to DB!");
//        PreparedStatement cardStmt = connection.prepareStatement("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, " +
//                "EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(?, 1, ?, 2.5, 1, ?)");
//        PreparedStatement joinStmt = connection.prepareStatement("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) " +
//                "VALUES(1, ?)");
//
//
//        stmt.execute("INSERT INTO DECKS (ID, DEFAULT_NAME, CUSTOM_NAME) VALUES(3, 'Queens Gambit', 'Queens Gambit')");
//
//        for (int i = 0; i < 14; i++) {
//            cardStmt.setInt(1, i + 1);
//            cardStmt.setInt(2, 3);
//            cardStmt.setLong(3, System.currentTimeMillis());
//            cardStmt.executeUpdate();
//            joinStmt.setInt(1, 26808 + i);
//            joinStmt.executeUpdate();
//        }
        stmt.execute("SELECT ID, LINES_ID FROM MOVES");
        ResultSet rs = stmt.getResultSet();
        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }

        connection.close();
        System.out.println("Connection closed!");
    }
}
