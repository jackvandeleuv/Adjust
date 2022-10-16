import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public final class BoardGUI implements ActionListener {
    private final JFrame window;
    private final JPanel boardWrapper;
    private Square[] squares;
    private String beforeFEN;
    private String afterFEN = "r1b1kbnr/pp3ppp/1qn1p3/2ppP3/3P4/2P2N2/PP3PPP/RNBQKB1R b KQkq e3 0 1";

    public BoardGUI() {
        window = new JFrame();
        window.setSize(500, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        boardWrapper = new JPanel();
        GridLayout gLayout = new GridLayout(8, 8);
        boardWrapper.setLayout(gLayout);

        this.renderBoard();

        window.add(boardWrapper);
        window.setVisible(true);

        this.processFen(afterFEN);

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

        public void setPiece(Piece p) {
            if (p == null) {
                throw new IllegalArgumentException("Can't set null piece!");
            }

            // !!! Find way to clone the piece to maintain encapsulation.
            piece = p;
            try {
                BufferedImage bImage = piece.getImage();
                Image image = bImage.getScaledInstance(panel.getWidth(), panel.getHeight(), Image.SCALE_SMOOTH);
                ImageIcon iIcon = new ImageIcon(image);
                JLabel imageLabel = new JLabel();
                imageLabel.setIcon(iIcon);
                panel.add(imageLabel);
            } catch (IOException ioe) {
                System.out.println("Threw new IOException:");
                System.out.println(ioe.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
