import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.*;

public abstract class Piece {
    private int pos;
    private int xAxis;
    private int yAxis;
    private final char team;


    public Piece(int newPos, char newTeam) {
        pos = newPos;
        team = newTeam;
    }

    public int getPos() {
        return pos;
    }

    public char getTeam() {
        return team;
    }

    public void setPos(int newPos) {
        pos = newPos;
    }

    public void setxAxis(int newX) {
        xAxis = newX;
    }

    public void setyAxis(int newY) {
        yAxis = newY;
    }

    public abstract int getValidDestinations();

    public abstract char getImage() throws Exception;

//    @Override
//    public void paintComponent(Graphics g) {
//        try {
//            BufferedImage bImage = this.getImage();
//            Image resizedImage = bImage.getScaledInstance(100, 100, BufferedImage.SCALE_DEFAULT);
//            System.out.println(xAxis);
//            System.out.println(yAxis);
//            g.drawImage(resizedImage, xAxis, yAxis, this);
//        } catch (Exception ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
}
