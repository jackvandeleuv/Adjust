import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public final class BoardGUI implements ActionListener {
    private final JFrame window;
    private final JPanel boardWrapper;
    private final JButton[] selfRating;
    private final JButton showAnswer;
    private final JPanel infoPanel;
    private final JPanel leftCol;
    private final JPanel buttonBox;
    private Square[] squares;
    private String beforeFEN = "rnbqkbnr/pp3ppp/4p3/2ppP3/3P4/8/PPP2PPP/RNBQKBNR b KQkq e3 0 1";
    private String afterFEN = "rnbqkbnr/pp3ppp/4p3/2ppP3/3P4/5N2/PPP2PPP/RNBQKB1R b KQkq e3 0 1";
    private String beforeFEN2 = "rnb1kbnr/pp3ppp/1q2p3/2ppP3/3P4/3B1N2/PPP2PPP/RNBQK2R b KQkq e3 0 1";
    private String afterFEN2 = "r1b1kbnr/pp3ppp/1qn1p3/2ppP3/3P4/3B1N2/PPP2PPP/RNBQK2R b KQkq e3 0 1";

    public BoardGUI() throws InterruptedException {
        window = new JFrame();
        window.setSize(800, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        infoPanel = new JPanel();
        buttonBox = new JPanel();
        leftCol.add(infoPanel);
        leftCol.add(buttonBox);

        pane.add(leftCol);
        pane.add(boardWrapper);

        pane.setLayout(new BoxLayout(pane, 0));

        window.add(pane);

        window.setVisible(true);

        this.processFen(afterFEN);

        this.promptUser(0);
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

    private void processFen(String fen) {
        for (Square s : squares) {
            s.removeLabel();
        }

        String[] metaArr = fen.split(" ");
        String[] posArr = metaArr[0].split("/");

        int squareNum = 0;
        for (String row : posArr) {
            for (int chIndex = 0; chIndex < row.length(); chIndex++) {
                char currentChar = row.charAt(chIndex);

                if (!Character.isDigit(currentChar)) {
                    Square s = squares[squareNum];
                    s.setPiece(this.charToPiece(currentChar, squareNum));
                }

                if (Character.isDigit(currentChar)) {
                    squareNum = squareNum + (int)currentChar - 49;
                }

                squareNum = squareNum + 1;
            }
        }
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
                JLabel squareLabel = new JLabel(String.valueOf(piece.getImage()));
                Font font = new Font("Arial", Font.BOLD, 35);
                squareLabel.setFont(font);
                panel.add(squareLabel);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        public void removeLabel() {
            panel.removeAll();
        }
    }

    public void promptUser(int i) {
        if (i == 0) {
            this.processFen(beforeFEN);
        }
        if (i == 1) {
            this.processFen(beforeFEN2);
        }
        infoPanel.removeAll();
        JLabel lineName = new JLabel("French Defense, Agincourt Variation");
        infoPanel.add(lineName);

        buttonBox.removeAll();
        buttonBox.add(showAnswer);

        leftCol.revalidate();
        leftCol.repaint();
    }

    public void showResults() {
        infoPanel.removeAll();
        JLabel toMoveLabel = new JLabel("WHITE TO MOVE");
        infoPanel.add(toMoveLabel);

        buttonBox.removeAll();
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

        for (int i = 0; i < selfRating.length; i++) {
            if (e.getSource() == selfRating[i]) {
                System.out.println(i);
                this.promptUser(i);
            }
        }
    }
}
