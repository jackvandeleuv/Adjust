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
    public BufferedImage getImage() throws Exception {
        if (super.getTeam() == 'w') {
            BufferedImage image = ImageIO.read(new File("img/Chess_blt45.svg.png"));
            return image;
        }
        if (super.getTeam() == 'b') {
            BufferedImage image = ImageIO.read(new File("img/Chess_bdt45.svg.png"));
            return image;
        }
        throw new Exception("This piece has no color/team!");
    }

}
