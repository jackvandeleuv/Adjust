import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class Knight extends Piece {

    public Knight(int newPos, char newTeam) {
        super(newPos, newTeam);
    }

    @Override
    public int getValidDestinations() {
        return  0;
    }

    @Override
    public ImageIcon getImage() throws NoSuchFieldException {
        if (super.getTeam() == 'w') {
            ImageIcon image = new ImageIcon("img/Chess_nlt45.svg.png");
            return image;
        }
        if (super.getTeam() == 'b') {
            ImageIcon image = new ImageIcon("img/Chess_ndt45.svg.png");
            return image;
        }
        throw new NoSuchFieldException("This piece has no color/team!");
    }

}
