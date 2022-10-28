import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.*;

public final class BoardGUI implements ActionListener {
    private final ReviewEngine revEng;
    private final JPanel board;
    private final JButton[] selfRating;
    private final JButton showAnswer;
    private final JTextArea infoPanel;
    private final JPanel leftCol;
    private final JPanel buttonBox = new JPanel();
    private Square[] squares;
    private String lineNameMain;
    private String lineMoves;
    private String beforeFEN;
    private String afterFEN;
    private JButton leftArrow;
    private JButton rightArrow;
    private int currentCardId;
    private int orderInLine;
    private int currentDeckId;
    private final JPanel pane;
    private final JButton backBtn = new JButton("Back");
    private final JPanel container;
    private final CardLayout controller;
    private final MainMenuGUI mainMenu;
    private final JPanel arrowBox = new JPanel();

    public BoardGUI(int newCurrentDeckId, JPanel boardPane, JPanel outerContainer, CardLayout outerController, MainMenuGUI mainGUI) throws InterruptedException {
        mainMenu = mainGUI;
        container = outerContainer;
        controller = outerController;
        pane = boardPane;

        currentDeckId = newCurrentDeckId;
        try {
            revEng = new ReviewEngine(currentDeckId);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Could not instantiate ReviewEngine");
            throw new RuntimeException(e);
        }

        board = new JPanel();
        GridLayout gLayout = new GridLayout(8, 8);
        board.setLayout(gLayout);
        board.setPreferredSize(new Dimension(450, 450));

        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));

        JPanel boardWrapper = new JPanel();
        boardWrapper.add(board);
        boardWrapper.setMinimumSize(new Dimension(450, 600));
        boardWrapper.setLayout(new GridBagLayout());

        buttonBox.setMinimumSize(new Dimension(550, 100));
        selfRating = new JButton[6];
        for (int i = 0; i < selfRating.length; i++) {
            selfRating[i] = new JButton(String.valueOf(i));
            selfRating[i].addActionListener(this);
            buttonBox.add(selfRating[i]);
        }

        showAnswer = new JButton("Show Answer");
        showAnswer.addActionListener(this);

        rightArrow = new JButton(">");
        rightArrow.addActionListener(this);
        leftArrow = new JButton("<");
        leftArrow.addActionListener(this);
        backBtn.addActionListener(this);

        JPanel lowerBtnWrapper = new JPanel();
        lowerBtnWrapper.add(showAnswer);
        lowerBtnWrapper.add(backBtn);
        lowerBtnWrapper.setMinimumSize(new Dimension(550, 100));
        lowerBtnWrapper.setLayout(new GridBagLayout());

        arrowBox.add(leftArrow);
        arrowBox.add(rightArrow);
        arrowBox.setMinimumSize(new Dimension(550, 100));

        leftCol = new JPanel();

        JPanel infoPanelWrapper = new JPanel();
        infoPanel = new JTextArea();
        infoPanel.setEditable(false);
        infoPanel.setFont(new Font("Sans", Font.BOLD, 24));
        infoPanel.setMinimumSize(new Dimension(550, 250));
        infoPanel.setBounds(new Rectangle(550, 150));
        infoPanelWrapper.add(infoPanel);
        infoPanelWrapper.setLayout(new GridBagLayout());
        infoPanelWrapper.setMinimumSize(new Dimension(550, 300));

        buttonBox.setLayout(new GridBagLayout());
        arrowBox.setLayout(new GridBagLayout());

        leftCol.add(infoPanelWrapper);
        leftCol.add(arrowBox);
        leftCol.add(buttonBox);
        leftCol.add(lowerBtnWrapper);

        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setMinimumSize(new Dimension(550, 600));

        pane.add(leftCol);
        pane.add(boardWrapper);

        this.renderBoard();
        this.promptUser(currentDeckId);

        pane.revalidate();
        pane.repaint();
    }

    private void renderBoard() {
        squares = new Square[64];
        boolean offset = true;
        for (int i = 1; i <= 64; i++) {
            JPanel panel = new JPanel();
            if (i % 2 == 0 && !offset) {
                panel.setBackground(Color.WHITE);
            }
            if (i % 2 == 0 && offset) {
                panel.setBackground(Color.decode("#428D44"));
            }
            if (i % 2 != 0 && !offset) {
                panel.setBackground(Color.decode("#428D44"));
            }
            if (i % 2 != 0 && offset) {
                panel.setBackground(Color.WHITE);
            }
            squares[i - 1] = new Square(panel);
            board.add(panel);

            if (i % 8 == 0) {
                offset = !offset;
            }
        }
    }

    private void paintFEN(String fen) {
        for (Square sq : squares) {
            sq.removeLabel();
        }

        String[] metaArr = fen.split(" ");
        String[] posArr = metaArr[0].split("/");

        int squareNum = 0;
        for (String row : posArr) {
            for (int chIndex = 0; chIndex < row.length(); chIndex++) {
                char currentChar = row.charAt(chIndex);

                if (!Character.isDigit(currentChar)) {
                    Square sq = squares[squareNum];
                    if (sq.panelPainted()) {
                        sq.setPiece(this.charToPiece(currentChar, squareNum));
                    }
                }

                if (Character.isDigit(currentChar)) {
                    squareNum = squareNum + (int)currentChar - 49;
                }

                squareNum = squareNum + 1;
            }
        }
        board.revalidate();
        board.repaint();
    }

    private Piece charToPiece(char c, int newPos) {
        switch (c) {
            case 'P':
                return new Pawn(newPos, 'w');
            case 'B':
                return new Bishop(newPos, 'w');
            case 'N':
                return new Knight(newPos, 'w');
            case 'R':
                return new Rook(newPos, 'w');
            case 'K':
                return new King(newPos, 'w');
            case 'Q':
                return new Queen(newPos, 'w');
            case 'p':
                return new Pawn(newPos, 'b');
            case 'b':
                return new Bishop(newPos, 'b');
            case 'n':
                return new Knight(newPos, 'b');
            case 'r':
                return new Rook(newPos, 'b');
            case 'k':
                return new King(newPos, 'b');
            case 'q':
                return new Queen(newPos, 'b');
            default:
                throw new IllegalArgumentException("Can't create Piece with given parameters!");
        }
    }

    public final class Square {
        private final JPanel panel;
        private Piece piece;

        public Square(JPanel newPanel) {
            panel = newPanel;
        }

        public boolean panelPainted() {
            if (panel.getHeight() != 0) {
                return true;
            }
            return false;
        }

        public void setPiece(Piece newPiece) {
            if (newPiece == null) {
                throw new IllegalArgumentException("Can't set null piece!");
            }

            piece = newPiece;

            try {
                ImageIcon image = piece.getImage();
                Image temp = image.getImage();
                Image temp2 = temp.getScaledInstance(panel.getWidth(), panel.getHeight(), Image.SCALE_SMOOTH);
                ImageIcon image2 = new ImageIcon(temp2);
                JLabel label = new JLabel(image2, JLabel.CENTER);
                panel.add(label);
            } catch (NoSuchFieldException ex) {
                System.out.println("setPiece encountered an error");
                System.out.println(ex.getMessage());
            }
        }
        public void removeLabel() {
            panel.removeAll();
        }
    }

    public void promptUser(int deckId) {
        try {
            ReviewEngine.ReviewCard revCard = revEng.getNextCard();

            if (revCard.getId() == -1) {
                throw new RuntimeException("ReviewEngine failed to return a valid ReviewCard object.");
            }

            currentCardId = revCard.getId();
            beforeFEN = revCard.getBeforeFEN();
            afterFEN = revCard.getAfterFEN();
            lineNameMain = revCard.getName();
            orderInLine = revCard.getOrderInLine();
            System.out.println(currentCardId);
            System.out.println(beforeFEN);
            System.out.println(afterFEN);
            System.out.println(lineNameMain);

        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("promptUser encountered an error");
            System.out.println(ex.getMessage());
        }

        this.paintFEN(beforeFEN);

        String toMove = "";
        if (orderInLine % 2 == 0) {
            toMove = "BLACK TO MOVE";
        }

        if (orderInLine % 2 != 0) {
            toMove = "WHITE TO MOVE";
        }

        infoPanel.setText(lineNameMain + "\n" + toMove);

        showAnswer.setEnabled(true);

        rightArrow.setEnabled(false);
        leftArrow.setEnabled(false);
        for (JButton jb : selfRating) {
            jb.setEnabled(false);
        }

        pane.revalidate();
        pane.repaint();
    }

    public void showResults() {
        this.paintFEN(afterFEN);

        showAnswer.setEnabled(false);

        rightArrow.setEnabled(true);
        leftArrow.setEnabled(true);
        for (JButton jb : selfRating) {
            jb.setEnabled(true);
        }

        pane.revalidate();
        pane.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("beforeFEN:");
        System.out.println(beforeFEN);
        System.out.println("afterFEN:");
        System.out.println(afterFEN);

        if (e.getSource() == showAnswer) {
            this.showResults();
        }

        if (e.getSource() == rightArrow) {
            this.paintFEN(afterFEN);
        }

        if (e.getSource() == leftArrow) {
            this.paintFEN(beforeFEN);
        }

        if (e.getSource() == backBtn) {
            try {
                mainMenu.updateDeckModel();
                controller.show(container, "main");
            } catch (SQLException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        for (int i = 0; i < selfRating.length; i++) {
            if (e.getSource() == selfRating[i]) {
                try {
                    revEng.updateCard(i + 1, currentCardId);
                } catch (SQLException | ClassNotFoundException ex) {
                    System.out.println("selfRating actionListenerer encountered an error");
                    System.out.println(ex.getMessage());
                }

                this.promptUser(currentDeckId);
            }
        }
    }
}
