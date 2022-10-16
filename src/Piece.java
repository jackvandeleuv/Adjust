import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JLabel;

public abstract class Piece {
    private int pos;
    private final char team;

    public Piece(int newPos, char newTeam) {
        pos = newPos;
        team = newTeam;
    }

    public int getPos() {
        return pos;
    }

    public char getTeam() {
        return team;
    }

    public abstract BufferedImage getImage() throws Exception;

    public void setPos(int newPos) {
        pos = newPos;
    }

    public abstract int getValidDestinations();
}
