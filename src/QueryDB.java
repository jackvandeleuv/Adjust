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

        // Overall card totals may be added to MainGUI soon. Might be possible to get rid of the DECKS.ID selection
        // at this point.
        StringBuilder cardTotalsQ = new StringBuilder();
        cardTotalsQ.append("SELECT COUNT(CARDS.ID), DECKS.ID ");
        cardTotalsQ.append("FROM CARDS JOIN DECKS ON CARDS.DECKS_ID = DECKS.ID ");
        cardTotalsQ.append("GROUP BY DECKS.ID ");
        cardTotalsQ.append("ORDER BY DECKS.NAME DESC ");

        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        conn.setAutoCommit(false);
        PreparedStatement reviewStmt = conn.prepareStatement(toReviewQ.toString());
        PreparedStatement totalsStmt = conn.prepareStatement(cardTotalsQ.toString());

        long currentTime = System.currentTimeMillis();
        reviewStmt.setLong(1, currentTime);
        ResultSet rs1 = reviewStmt.executeQuery();
        ResultSet rs2 = totalsStmt.executeQuery();

        List<String> nameList = new ArrayList<String>();
        List<Integer> reviewCount = new ArrayList<Integer>();
        List<Integer> cardTotals = new ArrayList<Integer>();
        List<Integer> deckPKs = new ArrayList<Integer>();

        while (rs1.next()) {
            nameList.add(rs1.getString(1));
            reviewCount.add(rs1.getInt(2));
        }

        while (rs2.next()) {
            cardTotals.add(rs2.getInt(1));
            deckPKs.add(rs2.getInt(2));
        }

        conn.commit();

        return new DeckSummary(nameList, reviewCount, cardTotals, deckPKs);
    }

    public final class DeckSummary {
        private final List<String> nameList;
        private final List<Integer> reviewCounts;
        private final List<Integer> cardTotals;
        private final List<Integer> deckPKs;

        public DeckSummary(List<String> newNameList, List<Integer> newReviewCounts, List<Integer> newCardTotals, List<Integer> newDeckPKs) {
            nameList = newNameList;
            reviewCounts = newReviewCounts;
            cardTotals = newCardTotals;
            deckPKs = newDeckPKs;
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

        public List<Integer> getDeckPKs() {
            return deckPKs;
        }
    }
}
