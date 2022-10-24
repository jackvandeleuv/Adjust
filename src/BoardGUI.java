import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.*;
import java.util.*;
import java.util.List;

public final class BoardGUI implements ActionListener {
    private final ReviewEngine revEng;
    private final JPanel boardWrapper;
    private final JButton[] selfRating;
    private final JButton showAnswer;
    private final JPanel infoPanel;
    private final JPanel leftCol;
    private final JPanel buttonBox;
    private Square[] squares;
    private String lineNameMain;
    private String lineNameVariation;
    private String beforeFEN;
    private String afterFEN;
    private JButton leftArrow;
    private JButton rightArrow;
    private int currentCardId;
    private int currentDeckId;

    public BoardGUI(int newCurrentDeckId, JFrame window) throws InterruptedException {
        revEng = new ReviewEngine();
        currentDeckId = newCurrentDeckId;

        JPanel pane = new JPanel();

        boardWrapper = new JPanel();
        GridLayout gLayout = new GridLayout(8, 8);
        boardWrapper.setLayout(gLayout);
        Dimension dimension = new Dimension(400, 400);
        boardWrapper.setPreferredSize(dimension);
        this.renderBoard();

        leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, 1));

        selfRating = new JButton[6];
        for (int i = 0; i < selfRating.length; i++) {
            selfRating[i] = new JButton(String.valueOf(i));
            selfRating[i].addActionListener(this);
        }

        showAnswer = new JButton("Show Answer");
        showAnswer.addActionListener(this);

        rightArrow = new JButton(">");
        rightArrow.addActionListener(this);
        leftArrow = new JButton("<");
        leftArrow.addActionListener(this);

        infoPanel = new JPanel();
        buttonBox = new JPanel();
        leftCol.add(infoPanel);
        leftCol.add(buttonBox);

        pane.add(leftCol);
        pane.add(boardWrapper);

        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));

        window.add(pane);

        window.setVisible(true);

        this.promptUser(currentDeckId);
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
                panel.setBackground(Color.GREEN);
            }
            if (i % 2 != 0 && !offset) {
                panel.setBackground(Color.GREEN);
            }
            if (i % 2 != 0 && offset) {
                panel.setBackground(Color.WHITE);
            }
            squares[i - 1] = new Square(panel);
            boardWrapper.add(panel);

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
                    sq.setPiece(this.charToPiece(currentChar, squareNum));
                }

                if (Character.isDigit(currentChar)) {
                    squareNum = squareNum + (int)currentChar - 49;
                }

                squareNum = squareNum + 1;
            }
        }
        boardWrapper.revalidate();
        boardWrapper.repaint();
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

        public void setPiece(Piece newPiece) {
            if (newPiece == null) {
                throw new IllegalArgumentException("Can't set null piece!");
            }

            // !!! Find way to clone the piece to maintain encapsulation.
            piece = newPiece;

            try {
                ImageIcon image = piece.getImage();
                Image temp = image.getImage();
                Image temp2 = temp.getScaledInstance(panel.getWidth(), panel.getHeight(), Image.SCALE_SMOOTH);
                ImageIcon image2 = new ImageIcon(temp2);
                JLabel label = new JLabel(image2, JLabel.CENTER);
                panel.add(label);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        public void removeLabel() {
            panel.removeAll();
        }
    }

    public void promptUser(int deckId) {
        try {
            ReviewEngine.ReviewCard revCard = revEng.getNextCard(deckId);

            if (revCard.getId() == -1) {
                return;
            }

            currentCardId = revCard.getId();
            beforeFEN = revCard.getBeforeFEN();
            afterFEN = revCard.getAfterFEN();
            lineNameMain = revCard.getName();
            lineNameVariation = revCard.getLine();
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        }

        this.paintFEN(beforeFEN);

        infoPanel.removeAll();
        JLabel toMoveLabel = new JLabel("WHITE TO MOVE");
        infoPanel.add(toMoveLabel);

        buttonBox.removeAll();
        buttonBox.add(showAnswer);

        leftCol.revalidate();
        leftCol.repaint();
    }

    public void showResults() {
        this.paintFEN(afterFEN);

        infoPanel.removeAll();
        StringBuilder lineName = new StringBuilder();
        lineName.append(lineNameMain);
        lineName.append(" ");
        lineName.append(lineNameVariation);
        JLabel lineLabel = new JLabel(lineName.toString());
        infoPanel.add(lineLabel);

        buttonBox.removeAll();
        buttonBox.add(leftArrow);
        buttonBox.add(rightArrow);
        for (int i = 0; i < selfRating.length; i++) {
            buttonBox.add(selfRating[i]);
        }
        leftCol.revalidate();
        leftCol.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == showAnswer) {
            this.showResults();
        }

        if (e.getSource() == rightArrow) {
            this.paintFEN(afterFEN);
        }

        if (e.getSource() == leftArrow) {
            this.paintFEN(beforeFEN);
        }

        for (int i = 0; i < selfRating.length; i++) {
            if (e.getSource() == selfRating[i]) {
                try {
                    revEng.updateCard(i + 1, currentCardId);
                } catch (SQLException | ClassNotFoundException ex) {
                    System.out.println(ex.getMessage());
                }

                this.promptUser(currentDeckId);
            }
        }
    }
}
