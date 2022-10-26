import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public final class ReviewEngine {
    public ReviewCard getNextCard(int deckId) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        conn.setAutoCommit(false);

        StringBuilder query = new StringBuilder();
        query.append("SELECT CARDS.ID, LINES.NAME, LINES.LINE, MOVES.BEFORE_FEN, MOVES.AFTER_FEN ");
        query.append("FROM CARDS JOIN CARDS_TO_MOVES ON CARDS.ID = CARDS_TO_MOVES.CARDS_ID ");
        query.append("JOIN MOVES ON CARDS_TO_MOVES.MOVES_ID = MOVES.ID ");
        query.append("JOIN LINES ON MOVES.LINES_ID = LINES.ID ");
        query.append("WHERE CARDS.DECKS_ID = ? ");
        query.append("AND (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) ");
        query.append("LIMIT 1 ");

        long currentTime = System.currentTimeMillis();
        PreparedStatement preStmt = conn.prepareStatement(query.toString());
        preStmt.setInt(1, deckId);
        preStmt.setLong(2, currentTime);

        ResultSet rs = preStmt.executeQuery();

        int id = rs.getInt("ID");
        String name = rs.getString("NAME");
        String line = rs.getString("LINE");
        String beforeFEN = rs.getString("BEFORE_FEN");
        String afterFEN = rs.getString("AFTER_FEN");

        conn.commit();

        if (name != null && line != null && beforeFEN != null && afterFEN != null) {
            return new ReviewCard(id, name, line, beforeFEN, afterFEN);
        } else {
            return new ReviewCard(-1, "", "", "", "");
        }
    }

    public void updateCard(int grade, int cardId) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        conn.setAutoCommit(false);

        String query = "SELECT REP_NUMBER, EASY_FACTOR, IR_INTERVAL FROM CARDS WHERE ID = ?";
        PreparedStatement preStmt = conn.prepareStatement(query);
        preStmt.setInt(1, cardId);

        ResultSet rs = preStmt.executeQuery();
        int repNum = rs.getInt("REP_NUMBER");
        long interval = rs.getLong("IR_INTERVAL");
        double easFactor = rs.getDouble("EASY_FACTOR");

        conn.commit();

        double[] memoResult = this.superMemoAlgo(grade, repNum, easFactor, interval);
//
//        System.out.println(cardId);
//        for (double d : memoResult) {
//            System.out.println(d);
//        }
//        System.out.println("\n");

        StringBuilder query2 = new StringBuilder();
        query2.append("UPDATE CARDS ");
        query2.append("SET REP_NUMBER = ?, ");
        query2.append("EASY_FACTOR = ?, ");
        query2.append("IR_INTERVAL = ?, ");
        query2.append("LAST_REVIEW = ? ");
        query2.append("WHERE ID = ? ");

        PreparedStatement preStmt2 = conn.prepareStatement(query2.toString());
        long currentTime = System.currentTimeMillis();
        preStmt2.setDouble(1, memoResult[0]);
        preStmt2.setDouble(2, memoResult[1]);
        preStmt2.setDouble(3, memoResult[2]);
        preStmt2.setLong(4, currentTime);
        preStmt2.setInt(5, cardId);

//        System.out.println(repNum);
//        System.out.println(easFactor);
//        System.out.println(interval);
//        System.out.println(cardId);

        preStmt2.executeUpdate();
        conn.commit();
    }

    private double[] superMemoAlgo(int grade, int repNum, double easFactor, long interval) {
        if (grade >= 3) {
            if (repNum == 0) {
                interval = 1;
            } else if (repNum == 1) {
                interval = 6;
            } else {
                interval = Math.round(interval * easFactor);
            }
            repNum = repNum + 1;
        } else {
            repNum = 0;
            interval = 1;
        }
        easFactor = easFactor + (.1 - (5 - grade) * (.08 + (5 - grade) * .02));
        if (easFactor < 1.3) {
            easFactor = 1.3;
        }
        double[] resultArr = {repNum, easFactor, interval};
        return resultArr;
    }

    public final class ReviewCard {
        private final int id;
        private final String name;
        private final String line;
        private final String beforeFEN;
        private final String afterFEN;

        ReviewCard(int newId, String newName, String newLine, String newBeforeFEN, String newAfterFEN) {
            id = newId;
            name = newName;
            line = newLine;
            beforeFEN = newBeforeFEN;
            afterFEN = newAfterFEN;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLine() {
            return line;
        }

        public String getBeforeFEN() {
            return beforeFEN;
        }

        public String getAfterFEN() {
            return afterFEN;
        }
    }
}
