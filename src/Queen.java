import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class Queen extends Piece {

    public Queen(int newPos, char newTeam) {
        super(newPos, newTeam);
    }

    @Override
    public int getValidDestinations() {
        return  0;
    }

    @Override
    public ImageIcon getImage() throws NoSuchFieldException {
        if (super.getTeam() == 'w') {
            ImageIcon image = new ImageIcon("img/Chess_qlt45.svg.png");
            return image;
        }
        if (super.getTeam() == 'b') {
            ImageIcon image = new ImageIcon("img/Chess_qdt45.svg.png");
            return image;
        }
        throw new NoSuchFieldException("This piece has no color/team!");
    }

}
