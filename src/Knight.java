import javax.swing.ImageIcon;

/**
 * Extension of the Piece class that defines behavior for Knights.
 * @author Jack Vandeleuv
 */
public final class Knight extends Piece {

    // Superclass constructor uses pos and team to make this a valid Piece.
    public Knight(int newPos, char newTeam) {
        super(newPos, newTeam);
    }

    // Currently returns zero as a placeholder.
    @Override
    public int getValidDestinations() {
        return 0;
    }

    @Override
    public ImageIcon getImage() throws NoSuchFieldException {
        // If the piece is on the white team, return the appropriate image.
        if (super.getTeam() == 'w') {
            return new ImageIcon("img/Chess_nlt45.svg.png");
        }

        // If the piece is on the black team, return the appropriate image.
        if (super.getTeam() == 'b') {
            return new ImageIcon("img/Chess_ndt45.svg.png");
        }

        throw new NoSuchFieldException("This piece has no color/team!");
    }
}
