import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class InitDB  {
    public static void makeTables() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        try {
            Connection connection = DriverManager.getConnection(jbdcUrl);
            System.out.println("Successfully connected to DB!");
            Statement stmt = connection.createStatement();

            stmt.execute("DROP TABLE IF EXISTS OPENINGS");
            StringBuilder openingQ = new StringBuilder();
            openingQ.append("CREATE TABLE IF NOT EXISTS OPENINGS(");
            openingQ.append("ID INTEGER PRIMARY KEY,");
            openingQ.append("NAME TEXT,");
            openingQ.append("LINE TEXT)");
            stmt.execute(openingQ.toString());

            stmt.execute("DROP TABLE IF EXISTS MOVES");
            StringBuilder movesQ = new StringBuilder();
            movesQ.append("CREATE TABLE IF NOT EXISTS MOVES(");
            movesQ.append("ID INTEGER PRIMARY KEY,");
            movesQ.append("ORDER_IN_LINE INTEGER,");
            movesQ.append("BEFORE_FEN TEXT,");
            movesQ.append("AFTER_FEN TEXT,");
            movesQ.append("OPENING_ID INTEGER,");
            movesQ.append("FOREIGN KEY (OPENING_ID) REFERENCES OPENING(ID))");
            stmt.execute(movesQ.toString());

            stmt.execute("DROP TABLE IF EXISTS DECKS");
            StringBuilder decksQ = new StringBuilder();
            decksQ.append("CREATE TABLE IF NOT EXISTS DECKS(");
            decksQ.append("ID INTEGER PRIMARY KEY,");
            decksQ.append("DEFAULT_NAME TEXT,");
            decksQ.append("CUSTOM_NAME TEXT)");
            stmt.execute(decksQ.toString());

            stmt.execute("DROP TABLE IF EXISTS CARDS");
            StringBuilder cardsQ = new StringBuilder();
            cardsQ.append("CREATE TABLE IF NOT EXISTS CARDS(");
            cardsQ.append("ID INTEGER PRIMARY KEY,");
            cardsQ.append("REP_NUMBER INTEGER,");
            cardsQ.append("EASY_FACTOR REAL,");
            cardsQ.append("IR_INTERVAL INTEGER,");
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

            stmt.close();
            System.out.println("Connection closed!");

        } catch (SQLException e) {
            System.out.println("Error connecting to db");
            e.printStackTrace();
        }
    }
}
