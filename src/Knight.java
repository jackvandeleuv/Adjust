import javax.imageio.ImageIO;
import javax.swing.*;
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
    public char getImage() throws Exception {
        if (super.getTeam() == 'w') {
//            BufferedImage image = ImageIO.read(new File("img/Chess_nlt45.svg.png"));
//            return image;
            return 'K';
        }
        if (super.getTeam() == 'b') {
//            BufferedImage image = ImageIO.read(new File("img/Chess_ndt45.svg.png"));
//            return image;
            return 'k';
        }
        throw new Exception("This piece has no color/team!");
    }

}
