import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddLinesGUI implements ActionListener {
    private List<String[]> lines;
    private final DefaultListModel<String> listModel;
    private final Connection conn;
    private final JPanel pane;
    private final int deckID;

    public AddLinesGUI(JPanel outerPane, int deckPK) throws ClassNotFoundException, SQLException {
        pane = outerPane;
        deckID = deckPK;

        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        conn = DriverManager.getConnection(jbdcUrl);
        conn.setAutoCommit(false);

        listModel = new DefaultListModel<>();
        JList<String> listComp = new JList<>(listModel);
        listComp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listComp.setFixedCellWidth(600);
        listComp.setFixedCellHeight(30);

        JScrollPane scroller = new JScrollPane(listComp);
        scroller.setSize(600, 400);

        this.updateLines("", "");
        this.updateListComp();
        pane.removeAll();
        pane.add(scroller);
        pane.revalidate();
        pane.repaint();
    }

    private void updateListComp() {
        listModel.removeAllElements();
        for (int i = 0; i < lines.size(); i++) {
            String line = Arrays.toString(lines.get(i));
            listModel.add(i, line);
        }
        pane.revalidate();
        pane.repaint();
    }

    private void updateLines(String eco, String searchTerm) throws SQLException {
        if (eco == null || searchTerm == null) {
            throw new IllegalArgumentException("Can't pass null string to queryLines!");
        }

        eco = eco + "%";
        searchTerm = "%" + searchTerm + "%";
        StringBuilder query = new StringBuilder();
        query.append("SELECT ID, NAME, LINE, ECO ");
        query.append("FROM LINES ");
        query.append("WHERE ECO LIKE ? ");
        query.append("OR NAME LIKE ? ");
        PreparedStatement lineQ = conn.prepareStatement(query.toString());
        lineQ.setString(1, eco);
        lineQ.setString(2, searchTerm);

        ResultSet rs = lineQ.executeQuery();
        lines = new ArrayList<>();
        while (rs.next()) {
            String[] arr = {
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)
            };
            lines.add(arr);
        }
        conn.commit();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
