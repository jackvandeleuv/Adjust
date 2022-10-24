import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class King extends Piece {

    public King(int newPos, char newTeam) {
        super(newPos, newTeam);
    }

    @Override
    public int getValidDestinations() {
        return  0;
    }

    @Override
    public ImageIcon getImage() throws Exception {
        if (super.getTeam() == 'w') {
            ImageIcon image = new ImageIcon("img/Chess_plt45.svg.png");
            return image;
        }
        if (super.getTeam() == 'b') {
            ImageIcon image = new ImageIcon("img/Chess_pdt45.svg.png");
            return image;
        }
        throw new Exception("This piece has no color/team!");
    }

}
