import java.sql.*;

public final class InitDB  {
    public static void makeTables() throws ClassNotFoundException {
        try {
            Statement stmt = Main.conn.createStatement();

//            stmt.execute("DROP TABLE IF EXISTS LINES");
//            StringBuilder linesQ = new StringBuilder();
//            linesQ.append("CREATE TABLE IF NOT EXISTS LINES(");
//            linesQ.append("ID INTEGER PRIMARY KEY,");
//            linesQ.append("NAME TEXT,");
//            linesQ.append("LINE TEXT,");
//            linesQ.append("ECO TEXT)");
//            stmt.execute(linesQ.toString());

//            stmt.execute("DROP TABLE IF EXISTS MOVES");
//            StringBuilder movesQ = new StringBuilder();
//            movesQ.append("CREATE TABLE IF NOT EXISTS MOVES(");
//            movesQ.append("ID INTEGER PRIMARY KEY,");
//            movesQ.append("ORDER_IN_LINE INTEGER,");
//            movesQ.append("BEFORE_FEN TEXT,");
//            movesQ.append("AFTER_FEN TEXT,");
//            movesQ.append("LINES_ID INTEGER,");
//            movesQ.append("FOREIGN KEY (LINES_ID) REFERENCES LINES(ID))");
//            stmt.execute(movesQ.toString());

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

            Main.conn.commit();

        } catch (SQLException e) {
            System.out.println("Error connecting to db");
            e.printStackTrace();
        }
    }

//    public static void addTestData() throws SQLException, ClassNotFoundException {
//        Class.forName("org.sqlite.JDBC");
//        String jbdcUrl = "jdbc:sqlite:database.db";
//
//        Connection connection = DriverManager.getConnection(jbdcUrl);
//        System.out.println("Successfully connected to DB!");
//        Statement stmt = connection.createStatement();
//
//        stmt.execute("INSERT INTO DECKS (ID, NAME) VALUES(1, 'MY DEFENSE')");
//        stmt.execute("INSERT INTO DECKS (ID, NAME) VALUES(2, 'MY SYSTEM')");
//
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(1, 1, 0, 2.5, 1, 1666027078524)");
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(2, 1, 0, 2.5, 2, 1665940678524)");
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(3, 1, 0, 2.5, 3, 1665940678524)");
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(4, 1, 0, 2.5, 3, 1665940678524)");
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(5, 1, 0, 2.5, 3, 1665854278524)");
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(6, 2, 0, 2.5, 3, 1665854278524)");
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(7, 2, 0, 2.5, 3, 1665854278524)");
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(8, 2, 0, 2.5, 3, 1665508678524)");
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(9, 2, 0, 2.5, 3, 1665508678524)");
//        stmt.execute("INSERT INTO CARDS (ID, DECKS_ID, REP_NUMBER, EASY_FACTOR, IR_INTERVAL, LAST_REVIEW) VALUES(10, 2, 0, 2.5, 3, 1665508678524)");
//
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(1, 1)");
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(2, 2)");
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(3, 3)");
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(4, 4)");
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(5, 5)");
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(6, 6)");
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(7, 7)");
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(8, 8)");
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(9, 9)");
//        stmt.execute("INSERT INTO CARDS_TO_MOVES (CARDS_ID, MOVES_ID) VALUES(10, 10)");
//
//        connection.close();
//        System.out.println("Connection closed!");
//    }

    public static void queryDB() throws ClassNotFoundException, SQLException {
        Statement stmt = Main.conn.createStatement();

        stmt.execute("SELECT MOVES_ID, CARDS_ID FROM CARDS_TO_MOVES");
        ResultSet rs = stmt.getResultSet();

        while (rs.next()) {
            System.out.println("+++++++++");
            System.out.println("MOVES:");
            System.out.println(rs.getInt(1));
            System.out.println("CARDS:");
            System.out.println(rs.getInt(2));
            System.out.println("+++++++++");
        }

        stmt.execute("SELECT ID FROM CARDS");
        ResultSet rs2 = stmt.getResultSet();
        while (rs2.next()) {
            System.out.println(rs2.getInt(1));
        }

        stmt.execute("SELECT ID FROM DECKS");
        ResultSet rs3 = stmt.getResultSet();
        while (rs3.next()) {
            System.out.println(rs3.getInt(1));
        }

        Main.conn.commit();

        System.out.println("Connection closed!");
    }
}
