import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class MainMenuGUI implements ActionListener {
    List<DeckListItem> deckList = new ArrayList<>();
    private final JButton createBtn = new JButton("CREATE");
    private final JButton modifyBtn = new JButton("MODIFY");
    private final JButton renameBtn = new JButton("RENAME");
    private final JButton deleteBtn = new JButton("DELETE");
    private final JButton reviewBtn = new JButton("REVIEW");
    private final JPanel mainPane = new JPanel();
    private final CardLayout controller = new CardLayout();
    private final JPanel container = new JPanel();
    private final DefaultListModel<DeckListItem> decksModel = new DefaultListModel<>();
    private final JList<DeckListItem> decksListComp = new JList<>(decksModel);
    private final JPanel cardsPane = new JPanel();
    private final JPanel boardPane = new JPanel();

    public MainMenuGUI() throws SQLException, ClassNotFoundException {
        JFrame window = new JFrame();
        window.setSize(1000, 600);
        // Passing null centers the JFrame in the middle of the screen.
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        container.setLayout(controller);
        container.add(mainPane, "main");
        container.add(boardPane, "board");
        container.add(cardsPane, "cards");
        window.add(container);

        BoxLayout boxLayout = new BoxLayout(mainPane, BoxLayout.Y_AXIS);
        mainPane.setLayout(boxLayout);
        mainPane.setBorder(new EmptyBorder(0, 15, 0, 15));

        JPanel titlePane = new JPanel();
        titlePane.setPreferredSize(new Dimension(800, 120));
        JLabel title = new JLabel("ADJUST");
        title.setFont(new Font("Sans", Font.BOLD, 64));
        titlePane.add(title);

        createBtn.addActionListener(this);
        modifyBtn.addActionListener(this);
        renameBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        reviewBtn.addActionListener(this);

        createBtn.setPreferredSize(new Dimension(120, 45));
        modifyBtn.setPreferredSize(new Dimension(120, 45));
        renameBtn.setPreferredSize(new Dimension(120, 45));
        deleteBtn.setPreferredSize(new Dimension(120, 45));
        reviewBtn.setPreferredSize(new Dimension(120, 45));

        JPanel btnPane = new JPanel();
        btnPane.setLayout(new GridBagLayout());

        btnPane.setPreferredSize(new Dimension(800, 100));
        btnPane.add(createBtn);
        btnPane.add(reviewBtn);
        btnPane.add(modifyBtn);
        btnPane.add(renameBtn);
        btnPane.add(deleteBtn);

        decksListComp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        decksListComp.setFixedCellWidth(800);
        decksListComp.setFixedCellHeight(70);
        JScrollPane scroller = new JScrollPane(decksListComp);
        scroller.setPreferredSize(new Dimension(800, 350));

        mainPane.add(titlePane);
        mainPane.add(scroller);
        mainPane.add(btnPane);

        this.updateDeckModel();

        window.setVisible(true);
    }

    private void updateDeckList() throws SQLException {
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

        long currentTime = System.currentTimeMillis();
        PreparedStatement toReviewStmt = Main.conn.prepareStatement(toReviewQ.toString());
        PreparedStatement cardTotalsStmt = Main.conn.prepareStatement(cardTotalsQ.toString());
        toReviewStmt.setLong(1, currentTime);

        ResultSet rs1 = cardTotalsStmt.executeQuery();
        ResultSet rs2 = toReviewStmt.executeQuery();

        deckList.clear();
        while (rs1.next()) {
            DeckListItem deck = new DeckListItem(rs1.getInt(1));
            deck.setCardTotal(rs1.getInt(2));
            deckList.add(deck);
        }

        int index = 0;
        while (rs2.next()) {
            DeckListItem deck = deckList.get(index);
            deck.setName(rs2.getString(1));
            deck.setReviewCount(rs2.getInt(2));
            index = index + 1;
        }
        Main.conn.commit();
    }

    public void updateDeckModel() throws SQLException, ClassNotFoundException {
        this.updateDeckList();

        decksModel.removeAllElements();
        for (int i = 0; i < deckList.size(); i++) {
            decksModel.add(i, deckList.get(i));
        }
        mainPane.revalidate();
        mainPane.repaint();
    }

    private void deleteDeck(int deckPK) throws ClassNotFoundException, SQLException {
        PreparedStatement deleteDeck = Main.conn.prepareStatement("DELETE FROM DECKS WHERE ID = ?");
        deleteDeck.setInt(1, deckPK);
        deleteDeck.executeUpdate();
        Main.conn.commit();

        StringBuilder delRelQuery = new StringBuilder();
        delRelQuery.append("DELETE FROM CARDS_TO_MOVES WHERE CARDS_ID IN ( ");
        delRelQuery.append("SELECT ID FROM CARDS WHERE DECKS_ID = ?) ");
        PreparedStatement delRels = Main.conn.prepareStatement(delRelQuery.toString());
        delRels.setInt(1, deckPK);
        delRels.executeUpdate();
        Main.conn.commit();

        PreparedStatement deleteCards = Main.conn.prepareStatement("DELETE FROM CARDS WHERE DECKS_ID = ?");
        deleteCards.setInt(1, deckPK);
        deleteCards.executeUpdate();
        Main.conn.commit();

        this.updateDeckModel();
        mainPane.revalidate();
        mainPane.repaint();
    }

    private void createDeck(String name) throws ClassNotFoundException, SQLException {
        PreparedStatement createStmt = Main.conn.prepareStatement("INSERT INTO DECKS(ID, NAME) VALUES(NULL, ?)");
        createStmt.setString(1, name.strip());
        createStmt.executeUpdate();
        Main.conn.commit();

        this.updateDeckModel();
        mainPane.revalidate();
        mainPane.repaint();
    }

    private void renameDeck(int pk, String name) throws ClassNotFoundException, SQLException {
        PreparedStatement renameStmt = Main.conn.prepareStatement("UPDATE DECKS SET NAME = ? WHERE ID = ?");
        renameStmt.setString(1, name.strip());
        renameStmt.setInt(2, pk);
        renameStmt.executeUpdate();
        Main.conn.commit();

        this.updateDeckModel();
        mainPane.revalidate();
        mainPane.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteBtn) {
            // If the user clicks delete, get the index of the item that was selected when they clicked delete.
            int index = decksListComp.getSelectedIndex();
            // -1 is returned by getSelectedIndex if not row was selected when the user clicked delete.
            if (index != -1) {
                DeckListItem deck = decksModel.getElementAt(index);
                int pk = deck.getDeckPK();
                try {
                    this.deleteDeck(pk);
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (e.getSource() == createBtn) {
            String deckName = JOptionPane.showInputDialog("Enter the name for your new deck:");
            if (deckName != null && !deckName.isBlank()) {
                try {
                    this.createDeck(deckName);
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (e.getSource() == renameBtn) {
            int index = decksListComp.getSelectedIndex();
            if (index != -1) {
                DeckListItem deck = decksModel.getElementAt(index);
                int pk = deck.getDeckPK();
                String newName = JOptionPane.showInputDialog("Enter the updated name:");
                if (newName != null && !newName.isBlank()) {
                    try {
                        this.renameDeck(pk, newName);
                    } catch (SQLException | ClassNotFoundException ex){
                        throw new RuntimeException(ex);
                    }
                }
            }
        }

        if (e.getSource() == modifyBtn) {
            int index = decksListComp.getSelectedIndex();
            if (index != -1) {
                DeckListItem deck = decksModel.getElementAt(index);
                int deckPK = deck.getDeckPK();
                try {
                    cardsPane.removeAll();
                    new AddCardsGUI(cardsPane, deckPK, container, controller, this);
                    controller.show(container, "cards");
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (e.getSource() == reviewBtn) {
            int selIndex = decksListComp.getSelectedIndex();
            if (selIndex != -1) {
                DeckListItem selDeck = decksModel.get(selIndex);
                int selDeckPK = selDeck.getDeckPK();
                try {
                    boardPane.removeAll();
                    new BoardGUI(selDeckPK, boardPane, container, controller, this);
                    controller.show(container, "board");
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

        }
    }

    public class DeckListItem {
        private final int deckPK;
        private String name;
        private int cardTotal;
        private int reviewCount;

        public DeckListItem(int newDeckPK) {
            deckPK = newDeckPK;
        }

        public int getDeckPK() {
            return deckPK;
        }

        public void setReviewCount(int newReviewCount) {reviewCount = newReviewCount;}
        public void setCardTotal(int newCardTotal) {cardTotal = newCardTotal;}
        public void setName(String newName) {
            name = newName;
        }

        @Override
        public String toString() {
            return name + " DUE: " + reviewCount + "/" + cardTotal;
        }
    }
}


