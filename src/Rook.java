import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class Rook extends Piece {

    public Rook(int newPos, char newTeam) {
        super(newPos, newTeam);
    }

    @Override
    public int getValidDestinations() {
        return  0;
    }

    @Override
    public char getImage() throws Exception {
        if (super.getTeam() == 'w') {
//            BufferedImage image = ImageIO.read(new File("img/Chess_rlt45.svg.png"));
//            return image;
            return 'R';
        }
        if (super.getTeam() == 'b') {
//            BufferedImage image = ImageIO.read(new File("img/Chess_rdt45.svg.png"));
//            return image;
            return 'r';
        }
        throw new Exception("This piece has no color/team!");
    }

}
