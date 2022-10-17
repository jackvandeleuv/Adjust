import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public final class ReviewEngine {
    public static Map<Integer, List<String>> getNextCard(int deckId) throws SQLException, ClassNotFoundException {
        Map<Integer, List<String>> resultMap = new HashMap<Integer, List<String>>();

        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection connection = DriverManager.getConnection(jbdcUrl);
        connection.setAutoCommit(false);
        System.out.println("Successfully connected to DB!");

        StringBuilder query = new StringBuilder();
        query.append("SELECT CARDS.ID, OPENINGS.NAME, OPENINGS.LINE, MOVES.BEFORE_FEN, MOVES.AFTER_FEN ");
        query.append("FROM CARDS JOIN CARDS_TO_MOVES ON CARDS.ID = CARDS_TO_MOVES.CARDS_ID ");
        query.append("JOIN MOVES ON CARDS_TO_MOVES.MOVES_ID = MOVES.ID ");
        query.append("JOIN OPENINGS ON MOVES.OPENINGS_ID = OPENINGS.ID ");
        query.append("WHERE CARDS.DECKS_ID = ? ");
        query.append("AND ((? - CARDS.LAST_REVIEW) / 86400000) < CARDS.IR_INTERVAL ");
        query.append("LIMIT 1 ");


        long currentTime = System.currentTimeMillis();
        PreparedStatement preStmt = connection.prepareStatement(query.toString());
        preStmt.setInt(1, deckId);
        preStmt.setLong(2, currentTime);

        ResultSet rs = preStmt.executeQuery();

        int id = rs.getInt("ID");
        String name = rs.getString("NAME");
        String line = rs.getString("LINE");
        String beforeFEN = rs.getString("BEFORE_FEN");
        String afterFEN = rs.getString("AFTER_FEN");
        List<String> strResults = new ArrayList<String>();
        strResults.add(name);
        strResults.add(line);
        strResults.add(beforeFEN);
        strResults.add(afterFEN);
        resultMap.put(id, strResults);

        rs.close();
        preStmt.close();
        connection.commit();
        System.out.println("Connection closed!");

        return resultMap;
    }

    public static void updateCard() {

    }

    private static double[] superMemoAlgo(int grade, int repNum, double easFactor, int interval) {

    }
}
