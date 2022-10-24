import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class QueryDB {

    public QueryDB() {}

    public DeckSummary getDecksSummary() throws ClassNotFoundException, SQLException {

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

        List<String> nameList = new ArrayList<String>();
        List<Integer> reviewCount = new ArrayList<Integer>();

        while (rs.next()) {
            nameList.add(rs.getString(1));
            reviewCount.add(rs.getInt(2));
        }

        return new DeckSummary(nameList, reviewCount);
    }

    public final class DeckSummary {
        private final List<String> nameList;
        private final List<Integer> reviewCounts;
        public DeckSummary(List<String> newNameList, List<Integer> newReviewCounts) {
            nameList = newNameList;
            reviewCounts = newReviewCounts;
        }

        public List<String> getNameList() {
            return nameList;
        }

        public List<Integer> getReviewCounts() {
            return reviewCounts;
        }
    }
}
