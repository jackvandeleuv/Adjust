import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ModDecksGUI implements ActionListener {
    private JList<String> deckList;
    private final JPanel deckPane;
    private final JButton createBtn;
    private final JButton modifyBtn;
    private final JButton renameBtn;
    private final JButton deleteBtn;

    private List<String> nameList;
    private List<Integer> reviewCount;
    private List<Integer> cardTotals;
    private List<Integer> deckPKs;
    private final JPanel pane;

    private final Connection conn;

    public ModDecksGUI(JPanel newPane) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        conn = DriverManager.getConnection(jbdcUrl);
        synchronized (conn) {
            conn.setAutoCommit(false);
        }

        pane = newPane;
        JLabel title = new JLabel("MODIFY DECKS");
        createBtn = new JButton("CREATE");
        createBtn.addActionListener(this);
        modifyBtn = new JButton("MODIFY");
        modifyBtn.addActionListener(this);
        renameBtn = new JButton("RENAME");
        renameBtn.addActionListener(this);
        deleteBtn = new JButton("DELETE");
        deleteBtn.addActionListener(this);

        JPanel header = new JPanel();
        header.add(title);
        JPanel leftBar = new JPanel();
        leftBar.add(createBtn);
        leftBar.add(modifyBtn);
        leftBar.add(renameBtn);
        leftBar.add(deleteBtn);
        deckPane = new JPanel();

        this.makeDeckList();

        synchronized (pane) {
            pane.removeAll();
            pane.add(header);
            pane.add(leftBar);
            pane.add(deckPane);
            pane.revalidate();
            pane.repaint();
        }
    }

    private void getUpdatedSummaries() throws SQLException {
        StringBuilder toReviewQ = new StringBuilder();
        toReviewQ.append("SELECT DECKS.NAME, COUNT(CARDS.ID) ");
        toReviewQ.append("FROM DECKS JOIN CARDS ON DECKS.ID = CARDS.DECKS_ID ");
        toReviewQ.append("WHERE (? - CARDS.LAST_REVIEW) > (CARDS.IR_INTERVAL * 86400000) ");
        toReviewQ.append("GROUP BY DECKS.ID ");
        toReviewQ.append("ORDER BY DECKS.NAME DESC ");

        StringBuilder cardTotalsQ = new StringBuilder();
        cardTotalsQ.append("SELECT COUNT(CARDS.ID), DECKS.ID ");
        cardTotalsQ.append("FROM CARDS JOIN DECKS ON CARDS.DECKS_ID = DECKS.ID ");
        cardTotalsQ.append("GROUP BY DECKS.ID ");
        cardTotalsQ.append("ORDER BY DECKS.NAME DESC ");

        nameList = new ArrayList<String>();
        reviewCount = new ArrayList<Integer>();
        cardTotals = new ArrayList<Integer>();
        deckPKs = new ArrayList<Integer>();

        synchronized (conn) {
            long currentTime = System.currentTimeMillis();
            PreparedStatement toReviewStmt = conn.prepareStatement(toReviewQ.toString());
            PreparedStatement cardTotalsStmt = conn.prepareStatement(cardTotalsQ.toString());
            toReviewStmt.setLong(1, currentTime);

            ResultSet rs1 = toReviewStmt.executeQuery();
            ResultSet rs2 = cardTotalsStmt.executeQuery();

            while (rs1.next()) {
                nameList.add(rs1.getString(1));
                reviewCount.add(rs1.getInt(2));
            }

            while (rs2.next()) {
                cardTotals.add(rs2.getInt(1));
                deckPKs.add(rs2.getInt(2));
            }

            conn.commit();
        }

    }

    private void makeDeckList() throws SQLException, ClassNotFoundException {
        this.getUpdatedSummaries();
        String[] deckLabels = new String[nameList.size()];

        if (nameList.size() == 0) {
            deckPane.add(new JLabel("To create a deck, click CREATE!"));
        }

        if (nameList.size() != 0) {
            for (int i = 0; i < deckLabels.length; i++) {
                StringBuilder label = new StringBuilder();
                label.append(nameList.get(i));
                label.append("  DUE: ");
                label.append(reviewCount.get(i));
                label.append("  TOTAL: ");
                label.append(cardTotals.get(i));
                deckLabels[i] = label.toString();
            }

            deckList = new JList<>(deckLabels);
            deckPane.add(deckList);
        }
    }


    private void deleteDeck(int pk) throws ClassNotFoundException, SQLException {
        synchronized (conn) {
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM DECKS WHERE ID = ?");
            deleteStmt.setInt(1, pk);
            deleteStmt.executeUpdate();
            conn.commit();
        }
        this.makeDeckList();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteBtn && deckList != null) {
            // If the user clicks delete, get the index of the item that was selected when they clicked delete.
            int index = deckList.getSelectedIndex();
            // -1 is returned by getSelectedIndex if not row was selected when the user clicked delete.
            if (index != -1) {
                int pk = deckPKs.get(index);
                try {
                    this.deleteDeck(pk);
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
                synchronized (pane) {
                    pane.revalidate();
                    pane.repaint();
                }
            }
        }

        if (e.getSource() == createBtn) {
            CreateDeckThread cdt = new CreateDeckThread();
            cdt.start();
        }
    }

    public class CreateDeckThread extends Thread {
        @Override
        public void run() {
            new CreateDeckGUI(pane, conn);
        }
    }
}
