package textmatch;


import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.PageAttributes.OriginType;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
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
    
    private BufferedImage img;
    private final List<String> msgstrings;
    private final String screenshotSavePath;
    private boolean savingScreenshots;
    private final Robot robot;
    private JFrame frame;
    private JLabel picLabel;
    private JLabel matchCount;
    
    private HashSet<String> matchedMsgStrs;
    
    private int[] prevData = new int[0];
    private int[] curData = new int[0];
    
    private int screenshotNum = 0;
    
    public ScreenshotTaker(final List<String> msgstrings, String screenshotSavePath, HashSet<String> matchedMsgStrs) throws Exception {
    	this.msgstrings = msgstrings;
    	this.matchedMsgStrs = matchedMsgStrs;
    	this.screenshotSavePath = screenshotSavePath;
    	if (screenshotSavePath.equals(""))
    		savingScreenshots = false;
    	else
    		savingScreenshots = true;
    	robot = new Robot();
        initializeXInteraction();
    }
    
    public void guisetup() {
        frame = new JFrame("Display image");
        frame.setFocusable(false);
        picLabel = new JLabel();
        frame.setLayout(null);
        frame.add(picLabel);
        picLabel.setSize(900, 600);
        //picLabel.repaint();
        frame.setVisible(true);
        frame.setSize(900, 700);
        
        matchCount = new JLabel();
        matchCount.setSize(100, 30);
        frame.add(matchCount);
        updateMatchCount();
    }
    
    private void updateMatchCount() {
        matchCount.setText(matchedMsgStrs.size() + " / " + msgstrings.size());
    }
    
    // returns true if new image is actually different from previous
    private boolean captureNewImage() {
    	img = robot.createScreenCapture(new Rectangle(getX(), getY(), getWidth(), getHeight()));
        //long curtime = System.currentTimeMillis();
        if (curData.length != img.getWidth() * img.getHeight() * 3)
            curData = new int[img.getWidth() * img.getHeight() * 3];
        img.getData().getPixels(0, 0, img.getWidth(), img.getHeight(), curData);
        if (!arraysEqual(curData, prevData)) {
            prevData = curData;
       //     prev_screenshot_time = curtime;
            return true;
        }
        return false;
    }
    
    private void annotateScreenshot(BufferedImage displayImage, HashMap<String, MsgAnnotation> annotations) {
    	Graphics2D g = displayImage.createGraphics();
        for (String msgstr : annotations.keySet()) {
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
    
    public void runme() throws Exception {
        while (true) {
            if (frame.isFocused())
                continue;
            refreshInfo();
            
            //if (curtime < prev_screenshot_time + 250) {
            //    continue;
            //}
            captureNewImage();
            boolean containsNewMsgStrs = false;
            int origNumMatchedMsgStrs = 0;
            BufferedImage displayImage = deepCopy(img);
            if (msgstrings.size() > 0) {
            final List<ImgMatch> imgMatches = Main.getImgMatches(img, "");
            /*
            if (imgMatches.size() > 300) {
                System.err.println("too many words");
                continue;
            }
            */
            
          final MutableValue<HashMap<String, MsgAnnotation>> retv = new MutableValue<HashMap<String, MsgAnnotation>>();
        	Thread tx = new Thread(new Runnable() {

    		@Override
    		public void run() {
    			// TODO Auto-generated method stub
    			try {
    				retv.value = Main.msgToAnnotations(msgstrings, GCollectionUtils.singleElemList(imgMatches));
    			} catch (Exception e) {
    				
    			}
    		}
        	   
           });
        	tx.start();
        	while (tx.isAlive()) {
            	if (captureNewImage()) {
            		tx.interrupt();
            		break;
            	}
            	tx.join(1000);
        	}
            
        	HashMap<String, MsgAnnotation> annotations = retv.value;
        	if (annotations == null)
        		continue;
        	
        	origNumMatchedMsgStrs = matchedMsgStrs.size();
            for (String msgstr : annotations.keySet()) {
            	matchedMsgStrs.add(msgstr);
            }
            
            annotateScreenshot(displayImage, annotations);
        	
            //HashMap<String, MsgAnnotation> annotations = Main.msgToAnnotationsWithTimeout(msgstrings, GCollectionUtils.singleElemList(imgMatches), 3000);
            //if (annotations == null)
            // 	continue;
            
            }
            if (origNumMatchedMsgStrs != matchedMsgStrs.size()) {
                updateMatchCount();
                containsNewMsgStrs = true;
            }
          //  prev_screenshot_time = curtime;
            if (savingScreenshots && containsNewMsgStrs) {
            	while (new File(screenshotSavePath + screenshotNum + ".png").exists()) {
                    ++screenshotNum;
            	}
            	ImageIO.write(img, "png", new File(screenshotSavePath + screenshotNum + ".png"));
            }
            picLabel.setIcon(new ImageIcon(displayImage));
        }
    }
    
    public static void main(String[] args) throws Exception {
        List<String> msgfilecontents = new ArrayList<String>();
        if (args.length > 0)
            msgfilecontents = readLines(new FileReader(args[0]));
        POMsgSource msgsrc = new POMsgSource(msgfilecontents);
        final List<String> msgstrings = msgsrc.getMsgStrings();
        String screenshotSavePath = "";
        HashSet<String> matchedMsgStrs = new HashSet<String>();
        if (args.length > 1) {
        	 screenshotSavePath = args[1] + "/";
        	 File screenshotDir = new File(screenshotSavePath);
         	if (!screenshotDir.exists()) {
         		screenshotDir.mkdirs();
         	} else {
         		// add those messages that are covered by existing screenshots to matchedMsgStrs
         		for (String filename : screenshotDir.list()) {
         			filename = screenshotSavePath + filename;
         			System.out.println(filename);
         			List<ImgMatch> matches = Main.getImgMatches(filename);
         			HashMap<String, MsgAnnotation> annotations = Main.msgToAnnotations(msgstrings, GCollectionUtils.singleElemList(matches));
         			for (String x : annotations.keySet()) {
         				matchedMsgStrs.add(x);
         			}
         		}
         	}
        }
        ScreenshotTaker st = new ScreenshotTaker(msgstrings, screenshotSavePath, matchedMsgStrs);
        st.guisetup();
        st.runme();

        


        //long prev_screenshot_time = 0;

    }
    static {
        System.loadLibrary("textmatch_ScreenshotTaker");
    }
}
