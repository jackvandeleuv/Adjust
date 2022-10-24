import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.List;

public final class ModDecksGUI implements ActionListener {
    private JList<String> deckList;
    private JPanel deckPane;
    private JButton createBtn;
    private JButton modifyBtn;
    private JButton renameBtn;
    private JButton deleteBtn;
    private List<Integer> deckPKs;
    private JPanel pane;

    public ModDecksGUI(JPanel newPane) throws SQLException, ClassNotFoundException {
        pane = newPane;
        pane.removeAll();
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

        pane.add(header);
        pane.add(leftBar);
        pane.add(deckPane);

        pane.revalidate();
        pane.repaint();
    }

    private void makeDeckList() throws SQLException, ClassNotFoundException {
        QueryDB queryDB = new QueryDB();
        QueryDB.DeckSummary deckSummary = queryDB.getDecksSummary();
        List<String> names = deckSummary.getNameList();
        List<Integer> reviewCounts = deckSummary.getReviewCounts();
        List<Integer> cardTotals = deckSummary.getCardTotals();
        deckPKs = deckSummary.getDeckPKs();
        String[] deckLabels = new String[names.size()];

        if (names.size() == 0) {
            deckPane.add(new JLabel("To create a deck, click CREATE!"));
        }

        if (names.size() != 0) {
            for (int i = 0; i < deckLabels.length; i++) {
                StringBuilder label = new StringBuilder();
                label.append(names.get(i));
                label.append("  DUE: ");
                label.append(reviewCounts.get(i));
                label.append("  TOTAL: ");
                label.append(cardTotals.get(i));
                deckLabels[i] = label.toString();
            }

            deckList = new JList<>(deckLabels);
            deckPane.add(deckList);
        }
    }


    private void deleteDeck(int pk) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM DECKS WHERE ID = ?");
        deleteStmt.setInt(1, pk);
        deleteStmt.executeUpdate();
        conn.close();
        this.makeDeckList();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteBtn) {
            int index = deckList.getSelectedIndex();
            int pk = deckPKs.get(index);
            try {
                this.deleteDeck(pk);
            } catch (ClassNotFoundException | SQLException ex) {
                throw new RuntimeException(ex);
            }
            pane.revalidate();
            pane.repaint();
        }

        // Make a new thread to prevent freezing!
        if (e.getSource() == createBtn) {
            new CreateDeckGUI(pane);
        }
    }
}
