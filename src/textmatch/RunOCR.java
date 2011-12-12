package textmatch;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.sikuli.script.*;
import org.sikuli.script.natives.FindResult;
import org.sikuli.script.natives.FindResults;
import org.sikuli.script.natives.Mat;
import org.sikuli.script.natives.Vision;

public class RunOCR {

    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File(args[0]));
        TextRecognizer recognizer = TextRecognizer.getInstance();
        Rectangle rect = new java.awt.Rectangle(img.getWidth(), img.getHeight());
        ScreenImage simg = new ScreenImage(rect, img);
        List<Match> matches = recognizer.listText(simg, new Region(rect));
        for (Match x : matches) {
            System.out.println(x.text());
        }
        /*
        System.out.println(recognizer.recognize(img));
        Mat mat = OpenCV.convertBufferedImageToMat(img);
        FindResults results = Vision.findBlobs(mat);
        for (int i = 0; i < results.size(); ++i) {
            FindResult r = results.get(i);
            BufferedImage slicedImg = img.getSubimage(r.getX(), r.getY(), r.getW(), r.getH());
            Rectangle curRect = new java.awt.Rectangle(r.getW(), r.getH());
            List<Match> cm = recognizer.listText(new ScreenImage(curRect, slicedImg), new Region(curRect));
            for (Match ic : cm) {
                System.out.println(ic.text());
            }
        }
        */
    }

}
