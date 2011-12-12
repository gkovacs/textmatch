package textmatch;


import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import static textmatch.GCollectionUtils.*;
import static textmatch.GIOUtils.readLines;

public class ScreenshotTaker {
    
    private static native void initializeXInteraction();
    private static native void refreshInfo();
    private static native int getX();
    private static native int getY();
    private static native int getWidth();
    private static native int getHeight();
    
    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
       }
    
    public static void main(String[] args) throws Exception {
        List<String> msgfilecontents = new ArrayList<String>();
        if (args.length > 0)
            msgfilecontents = readLines(new FileReader(args[0]));
        POMsgSource msgsrc = new POMsgSource(msgfilecontents);
        List<String> msgstrings = msgsrc.getMsgStrings();
        int numMsgStrings = msgstrings.size();
        Robot robot = new Robot();
        JFrame frame = new JFrame("Display image");
        frame.setFocusable(false);
        JLabel picLabel = new JLabel();
        frame.setLayout(null);
        frame.add(picLabel);
        
        //picLabel.setLocation(new Point(0, 0));
        //picLabel.setBounds(0, 0, 900, 600);
        picLabel.setSize(900, 600);
        //picLabel.repaint();
        frame.setVisible(true);
        frame.setSize(900, 700);
        JLabel matchCount = new JLabel();
        HashSet<String> matchedMsgStrs = new HashSet<String>();
        matchCount.setText(matchedMsgStrs.size() + " / " + numMsgStrings);
        matchCount.setSize(100, 30);
        frame.add(matchCount);
        initializeXInteraction();
        int[] prevData = new int[0];
        int[] curData = new int[0];
        long prev_screenshot_time = 0;
        while (true) {
            if (frame.isFocused())
                continue;
            refreshInfo();
            BufferedImage img = robot.createScreenCapture(new Rectangle(getX(), getY(), getWidth(), getHeight()));
            long curtime = System.currentTimeMillis();
            if (curData.length != img.getWidth() * img.getHeight() * 3)
                curData = new int[img.getWidth() * img.getHeight() * 3];
            img.getData().getPixels(0, 0, img.getWidth(), img.getHeight(), curData);
            if (!arraysEqual(curData, prevData)) {
                prevData = curData;
                prev_screenshot_time = curtime;
                continue;
            }
            if (curtime < prev_screenshot_time + 250) {
                continue;
            }
            BufferedImage displayImage = deepCopy(img);
            if (msgstrings.size() > 0) {
            List<ImgMatch> imgMatches = Main.getImgMatches(img, "");
            if (imgMatches.size() > 300) {
                System.err.println("too many words");
                continue;
            }
            HashMap<String, MsgAnnotation> annotations = Main.msgToAnnotations(msgstrings, GCollectionUtils.singleElemList(imgMatches));
            Graphics2D g = displayImage.createGraphics();
            for (String msgstr : annotations.keySet()) {
            	matchedMsgStrs.add(msgstr);
            	MsgAnnotation annotation = annotations.get(msgstr);
                /*int[] values = new int[annotation.w * annotation.h * 3];
                for (int i = 0; i < annotation.w * annotation.h; ++i) {
                    values[3*i] = 255;
                    values[3*i + 1] = 0;
                    values[3*i + 2] = 0;
                }*/
                /*
                for (int y = annotation.y; y < annotation.y + annotation.h; ++y) {
                    for (int x = annotation.x; x < annotation.x + annotation.w; ++x) {
                        int[] curpixel = new int[3];
                        displayImage.getRaster().getPixel(x, y, curpixel);
                        //int r = curpixel[0];
                        //int g = 255; //curpixel[1];
                        //int b = curpixel[2];
                        curpixel[1] = 255;
                        displayImage.getRaster().setPixel(x, y, curpixel);
                    }
                }
                */
                //g.setComposite(new Composite());
                g.setColor(new Color(0 ,255, 255, 100));
                Rectangle rect = new Rectangle(annotation.x, annotation.y, annotation.w, annotation.h);
                g.fill(rect);
                //g.draw(rect);
                //displayImage.getRaster().setPixels(annotation.x, annotation.y, annotation.w, annotation.h, values);
            }
            }
            matchCount.setText(matchedMsgStrs.size() + " / " + numMsgStrings);
            prev_screenshot_time = curtime;
            picLabel.setIcon(new ImageIcon(displayImage));
        }
    }
    static {
        System.loadLibrary("textmatch_ScreenshotTaker");
    }
}
