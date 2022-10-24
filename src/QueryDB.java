import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class QueryDB {

    public QueryDB() {}

    public DeckSummary getDecksSummary() throws ClassNotFoundException, SQLException {

        StringBuilder toReviewQ = new StringBuilder();
        toReviewQ.append("SELECT DECKS.NAME, COUNT(CARDS.ID) ");
        toReviewQ.append("FROM DECKS JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        toReviewQ.append("WHERE (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) ");
        toReviewQ.append("GROUP BY DECKS.ID ");
        toReviewQ.append("ORDER BY DECKS.NAME DESC ");

        StringBuilder cardTotalsQ = new StringBuilder();
        cardTotalsQ.append("SELECT COUNT(CARDS.ID) ");
        cardTotalsQ.append("FROM CARDS JOIN DECKS ON CARDS.DECKS_ID = DECKS.ID ");
        cardTotalsQ.append("GROUP BY DECKS.ID ");
        cardTotalsQ.append("ORDER BY DECKS.NAME DESC ");

        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        PreparedStatement reviewStmt = conn.prepareStatement(toReviewQ.toString());
        PreparedStatement totalsStmt = conn.prepareStatement(cardTotalsQ.toString());

        long currentTime = System.currentTimeMillis();
        reviewStmt.setLong(1, currentTime);
        ResultSet rs1 = reviewStmt.executeQuery();
        ResultSet rs2 = totalsStmt.executeQuery();

        List<String> nameList = new ArrayList<String>();
        List<Integer> reviewCount = new ArrayList<Integer>();
        List<Integer> cardTotals = new ArrayList<Integer>();

        while (rs1.next()) {
            nameList.add(rs1.getString(1));
            reviewCount.add(rs1.getInt(2));
        }

        while (rs2.next()) {
            cardTotals.add(rs2.getInt(1));
        }

        return new DeckSummary(nameList, reviewCount, cardTotals);
    }

    public final class DeckSummary {
        private final List<String> nameList;
        private final List<Integer> reviewCounts;

        private final List<Integer> cardTotals;
        public DeckSummary(List<String> newNameList, List<Integer> newReviewCounts, List<Integer> newCardTotals) {
            nameList = newNameList;
            reviewCounts = newReviewCounts;
            cardTotals = newCardTotals;
        }

        public List<String> getNameList() {
            return nameList;
        }

        public List<Integer> getReviewCounts() {
            return reviewCounts;
        }

        public List<Integer> getCardTotals() {
            return cardTotals;
        }
    }
}
