import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class QueryDB {

    public QueryDB() {}

    public List<DeckSummary> getDeckSummaries() throws ClassNotFoundException, SQLException {
        StringBuilder cardTotalsQ = new StringBuilder();
        cardTotalsQ.append("SELECT DECKS.ID, COALESCE(COUNT(CARDS.ID), 0) ");
        cardTotalsQ.append("FROM DECKS LEFT JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        cardTotalsQ.append("GROUP BY DECKS.ID ");
        cardTotalsQ.append("ORDER BY DECKS.NAME DESC ");

        StringBuilder toReviewQ = new StringBuilder();
        toReviewQ.append("SELECT DECKS.NAME, COALESCE(COUNT(CARDS.ID), 0) ");
        toReviewQ.append("FROM DECKS LEFT JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        toReviewQ.append("WHERE (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) ");
        toReviewQ.append("OR CARDS.ID IS NULL ");
        toReviewQ.append("GROUP BY DECKS.ID ");
        toReviewQ.append("ORDER BY DECKS.NAME DESC ");

        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        conn.setAutoCommit(false);
        PreparedStatement totalsStmt = conn.prepareStatement(cardTotalsQ.toString());
        PreparedStatement reviewStmt = conn.prepareStatement(toReviewQ.toString());

        long currentTime = System.currentTimeMillis();
        reviewStmt.setLong(1, currentTime);
        ResultSet rs1 = totalsStmt.executeQuery();
        ResultSet rs2 = reviewStmt.executeQuery();

        List<DeckSummary> sumList = new ArrayList<>();
        while (rs1.next()) {
            DeckSummary sum = new DeckSummary(rs1.getInt(1));
            sum.setCardTotal(rs1.getInt(2));
            sumList.add(sum);
        }

        int index = 0;
        while (rs2.next()) {
            DeckSummary sum = sumList.get(index);
            sum.setName(rs2.getString(1));
            sum.setReviewCount(rs2.getInt(2));
            index = index + 1;
        }

        conn.commit();

        return sumList;
    }

    public final class DeckSummary {
        private String name;
        private int reviewCount;
        private int cardTotal;
        private final int deckPK;

        public DeckSummary(int newDeckPK) {
            deckPK = newDeckPK;
        }

        public String getNameList() {
            return name;
        }

        public int getReviewCounts() {
            return reviewCount;
        }

        public int getDeckPK() {
            return deckPK;
        }

        public void setReviewCount(int newReviewCount) {reviewCount = newReviewCount;}
        public void setCardTotal(int newCardTotal) {cardTotal = newCardTotal;}
        public void setName(String newName) {name = newName;}

        @Override
        public String toString() {
            return name + " DUE: " + reviewCount + "/" + cardTotal;
        }
    }
}
