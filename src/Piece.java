import javax.swing.*;

/**
 * The Piece class is an abstract class that provides the blueprint for the different piece objects involved in
 * this program's representation of chess. Currently, the board does not allow players to drag and drop pieces; however,
 * once that feature is implemented this structure will become important, as the classes will define the different
 * legal moves that can be made by the different pieces.
 * @author Jack Vandeleuv
 */
public abstract class Piece {
    // This variable indicates board position.
    private int pos;

    // This variable indicates which player the piece belongs to (white or black).
    private final char team;

    public Piece(int newPos, char newTeam) {
        pos = newPos;
        team = newTeam;
    }

    public char getTeam() {
        return team;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int newPos) {
        pos = newPos;
    }

    // This abstract method will be implemented by specific pieces like King and Pawn. It will define the valid
    // positions to which this piece can move on a given turn.
    public abstract int getValidDestinations();

    // Returns an image icon representation of the Piece.
    public abstract ImageIcon getImage() throws NoSuchFieldException;
}
