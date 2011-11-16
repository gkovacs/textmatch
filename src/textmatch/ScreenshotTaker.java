package textmatch;


import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import static textmatch.GCollectionUtils.*;

public class ScreenshotTaker {
    
    private static native void initializeXInteraction();
    private static native void refreshInfo();
    private static native int getX();
    private static native int getY();
    private static native int getWidth();
    private static native int getHeight();
    
    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        JFrame frame = new JFrame("Display image");
        frame.setFocusable(false);
        JLabel picLabel = new JLabel();
        frame.add(picLabel);
        frame.setVisible(true);
        frame.setSize(900, 700);
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
            prev_screenshot_time = curtime;
            picLabel.setIcon(new ImageIcon(img));
        }
    }
    static {
        System.loadLibrary("textmatch_ScreenshotTaker");
    }
}
