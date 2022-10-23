import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QueryDB {

    public QueryDB() {}

    public static Map<String, Integer> getDecksSummary() throws ClassNotFoundException, SQLException {

        StringBuilder query = new StringBuilder();
        // It might be possible to COALESCE the default names into the custom names?
        query.append("SELECT DECKS.CUSTOM_NAME, COUNT(CARDS.ID) FROM DECKS ");
        query.append("JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        query.append("WHERE (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) ");
        query.append("GROUP BY DECKS.ID ");

        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        PreparedStatement preStmt = conn.prepareStatement(query.toString());

        long currentTime = System.currentTimeMillis();
        preStmt.setLong(1, currentTime);
        ResultSet rs = preStmt.executeQuery();

        HashMap<String, Integer> decksSummary = new HashMap<String, Integer>();

        while (rs.next()) {
            String customName = rs.getString(1);
            int toReview = rs.getInt(2);
            decksSummary.put(customName, toReview);
        }

        return decksSummary;
    }
}
