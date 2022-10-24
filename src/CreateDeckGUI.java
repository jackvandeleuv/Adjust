import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateDeckGUI implements ActionListener {
    private JButton createButton;
    private JTextField enterName;
    private JFrame popup;
    private JPanel pane;

    public CreateDeckGUI(JPanel outerPane) {
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
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);

        PreparedStatement createStmt = conn.prepareStatement("INSERT INTO DECKS(ID, NAME) VALUES(NULL, ?)");
        createStmt.setString(1, name.strip());
        createStmt.executeUpdate();

        conn.commit();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createButton)   {
            if (enterName.getText().isEmpty()) {
                enterName.setToolTipText("Enter a name for your deck!");
            }
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
