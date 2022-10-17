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
    public char getImage() throws Exception {
        if (super.getTeam() == 'w') {
//            return ImageIO.read(new File("img/Chess_blt45.svg.png"));
            return 'B';
        }
        if (super.getTeam() == 'b') {
//            return ImageIO.read(new File("img/Chess_bdt45.svg.png"));
            return 'b';
        }
        throw new Exception("This piece has no color/team!");
    }

}
