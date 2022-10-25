import javax.swing.*;
import javax.xml.transform.Result;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class CreateDeckGUI implements ActionListener {
    private JButton createButton;
    private JTextField enterName;
    private JFrame popup;
    private final JPanel pane;
    private final Connection conn;

    public CreateDeckGUI(JPanel outerPane, Connection outerConn) {
        // Both of these variables are mutable and shared across threads. They must be synchronized anytime they are
        // modified by this class.
        conn = outerConn;
        pane = outerPane;

        popup = new JFrame();
        popup.setSize(400, 300);
        popup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel createPane = new JPanel();
        popup.add(createPane);

        JLabel headLabel = new JLabel("CREATE A NEW DECK");
        enterName = new JTextField(15);
        createButton = new JButton("CREATE");
        createButton.addActionListener(this);
        createPane.add(headLabel);
        createPane.add(enterName);
        createPane.add(createButton);
        popup.setVisible(true);
    }

    private void createDeck(String name) throws ClassNotFoundException, SQLException {
        synchronized (conn) {
            PreparedStatement createStmt = conn.prepareStatement("INSERT INTO DECKS(ID, NAME) VALUES(NULL, ?)");
            createStmt.setString(1, name.strip());
            createStmt.executeUpdate();
        }
        // Pane is a mutable JPanel shared between threads, so it needs to be synchronized.
        synchronized (pane) {
            pane.revalidate();
            pane.repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createButton)   {
            if (!enterName.getText().isEmpty()) {
                String name = enterName.getText();
                try {
                    this.createDeck(name);
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
                popup.dispose();
            }
        }
    }
}
