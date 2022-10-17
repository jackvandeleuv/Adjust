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
    public char getImage() throws Exception {
        if (super.getTeam() == 'w') {
//            BufferedImage image = ImageIO.read(new File("img/Chess_plt45.svg.png"));
//            return image;
            return 'P';
        }
        if (super.getTeam() == 'b') {
//            BufferedImage image = ImageIO.read(new File("img/Chess_pdt45.svg.png"));
//            return image;
            return 'p';
        }
        throw new Exception("This piece has no color/team!");
    }
}
