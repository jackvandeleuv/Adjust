import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import javax.swing.*;

public final class Pawn extends Piece {

    public Pawn(int newPos, char newTeam) {
        super(newPos, newTeam);
    }

    @Override
    public int getValidDestinations() {
        return  0;
    }

    @Override
    public ImageIcon getImage() throws NoSuchFieldException {
        if (super.getTeam() == 'w') {
            ImageIcon image = new ImageIcon("img/Chess_plt45.svg.png");
            return image;
        }
        if (super.getTeam() == 'b') {
            ImageIcon image = new ImageIcon("img/Chess_pdt45.svg.png");
            return image;
        }
        throw new NoSuchFieldException("This piece has no color/team!");
    }
}
