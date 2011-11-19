package textmatch;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.sikuli.script.*;

public class ErrorReproduction {

    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("/home/geza/workspace/textmatch/gnome-keyboard-shortcuts.png"));
        TextRecognizer recognizer = TextRecognizer.getInstance();
        Rectangle rect = new java.awt.Rectangle(img.getWidth(), img.getHeight());
        ScreenImage simg = new ScreenImage(rect, img);
        List<Match> matches = recognizer.listText(simg, new Region(rect));
    }

}
