package textmatch;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import org.sikuli.script.*;
import org.sikuli.script.natives.FindResult;
import org.sikuli.script.natives.FindResults;
import org.sikuli.script.natives.Mat;
import org.sikuli.script.natives.Vision;

public class RunOCR {

    public static void main(String[] args) throws Exception {
        List<ImgMatch> matches = Main.getImgMatches(args[0]);
        for (ImgMatch x : matches) {
            System.out.println(x.text());
        }
    }

}
