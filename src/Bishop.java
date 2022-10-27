import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class Bishop extends Piece {
    public Bishop(int newPos, char newTeam) {
        super(newPos, newTeam);
    }

    @Override
    public int getValidDestinations() {
        return  0;
    }

    @Override
    public ImageIcon getImage() throws NoSuchFieldException {
        if (super.getTeam() == 'w') {
            ImageIcon image = new ImageIcon("img/Chess_blt45.svg.png");
            return image;
        }
        if (super.getTeam() == 'b') {
            ImageIcon image = new ImageIcon("img/Chess_bdt45.svg.png");
            return image;
        }
        throw new NoSuchFieldException("This piece has no color/team!");
    }

}
