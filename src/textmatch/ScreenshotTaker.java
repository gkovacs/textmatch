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
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.html.HTMLDocument;

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
    
    private BufferedImage prevImg;
    private final List<String> msgstrings;
    private final String screenshotSavePath;
    private boolean savingScreenshots;
    private final Robot robot;
    private JFrame frame;
    private JLabel picLabel;
    private JLabel matchCount;
    private JEditorPane textArea;
    private JEditorPane textAreaSeen;
    
    private HashSet<String> matchedMsgStrs;
    
    private int[] prevData = new int[0];
    private int[] curData = new int[0];
    
    private int screenshotNum = 0;
    
    private final POMsgSource msgsrc;
    
    public ScreenshotTaker(final POMsgSource msgsrc, String screenshotSavePath) throws Exception {
    	this.msgsrc = msgsrc;
    	this.msgstrings = msgsrc.getMsgStrings();
    	this.matchedMsgStrs = new HashSet<String>();
    	this.screenshotSavePath = screenshotSavePath;
    	if (screenshotSavePath.equals(""))
    		savingScreenshots = false;
    	else
    		savingScreenshots = true;
    	robot = new Robot();
        initializeXInteraction();
    }
    
    public void show() {
    	frame.setVisible(true);
    }
    
    public void guisetup() {
        frame = new JFrame("Screenshot Gatherer");
        //frame.setFocusable(false);
        
        frame.setLayout(null);
        frame.setSize(1000, 600);
        //frame.setVisible(true);
        
        matchCount = new JLabel();
        matchCount.setLocation(0, 0);
        matchCount.setSize(100, 30);
        
        picLabel = new JLabel();
        picLabel.setLocation(0, 30);
        picLabel.setSize(600, 550);
        frame.add(picLabel);
        
        //picLabel.repaint();
        
        

        
        frame.add(matchCount);
        
        
        updateMatchCount();
        
        textArea = new JEditorPane();
        textArea.setLocation(0, 0);
        textArea.setSize(200, 600);
        
        JScrollPane scrollable = new JScrollPane(textArea);
        scrollable.setLocation(600, 0);
        scrollable.setSize(200, 600);
        scrollable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollable);
        
        textAreaSeen = new JEditorPane();
        textAreaSeen.setLocation(0, 0);
        textAreaSeen.setSize(200, 600);
        
        JScrollPane scrollable2 = new JScrollPane(textAreaSeen);
        scrollable2.setLocation(800, 0);
        scrollable2.setSize(200, 600);
        scrollable2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        //frame.add(textArea);
        frame.add(scrollable2);
        
        //
    }
    
    private void updateMatchCount() {
        matchCount.setText(matchedMsgStrs.size() + " / " + msgstrings.size());
    }
    
    private boolean sameImages(BufferedImage img, BufferedImage img2) {
    	if (img == null || img2 == null)
    		return false;
    	int[] img1data = new int[img.getWidth() * img.getHeight() * 3];
    	int[] img2data = new int[img2.getWidth() * img2.getHeight() * 3];
    	img.getData().getPixels(0, 0, img.getWidth(), img.getHeight(), img1data);
    	img2.getData().getPixels(0, 0, img2.getWidth(), img2.getHeight(), img2data);
    	return arraysEqual(img1data, img2data);
    }
    
    // returns true if new image is actually different from previous
    private BufferedImage captureNewImage() {
    	refreshInfo();
    	return robot.createScreenCapture(new Rectangle(getX(), getY(), getWidth(), getHeight()));
    }
    
    public void addNewAnnotations(HashMap<String, MsgAnnotation> annotations) throws Exception {
        for (String msgstr : annotations.keySet()) {
        	matchedMsgStrs.add(msgstr);
        }
        String curMsgSource = "";
        String curMsgSourceFound = "";
        StringBuilder msgsToBeFound = new StringBuilder();
        msgsToBeFound.append("<html><body>");
        StringBuilder msgsFound = new StringBuilder();
        msgsFound.append("<html><body>");
        for (String msgidblock : msgsrc.splitIntoMsgIdBlocks()) {
        	String msgstr = msgsrc.textFromMsgIdBlock(msgidblock);
        	if (msgstr.isEmpty())
        		continue;
        	String msgsource = msgsrc.msgSourceConciseFromMsgIdBlock(msgidblock);
        	if (matchedMsgStrs.contains(msgstr)) {
            	if (!msgsource.isEmpty() && !msgsource.equals(curMsgSourceFound)) {
            		curMsgSourceFound = msgsource;
            		msgsFound.append("<b>" + msgsource + "</b><br/>\n");
            	}
            	msgsFound.append(msgstr + "<br/>\n");
        	} else {
        		if (!msgsource.isEmpty() && !msgsource.equals(curMsgSource)) {
            		curMsgSource = msgsource;
            		msgsToBeFound.append("<b>" + msgsource + "</b><br/>\n");
            	}
        		msgsToBeFound.append(msgstr + "<br/>\n");
        	}
        }
        msgsToBeFound.append("</body></html>");
        msgsFound.append("</body></html>");
        textArea.setContentType("text/html");
        textArea.setText( msgsToBeFound.toString());
        textAreaSeen.setContentType("text/html");
        textAreaSeen.setText( msgsFound.toString());
        updateMatchCount();
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
            
            //if (curtime < prev_screenshot_time + 250) {
            //    continue;
            //}
            //captureNewImage();
            BufferedImage img = captureNewImage();
            if (sameImages(img, prevImg))
            	continue;
            
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
            	BufferedImage newImg = captureNewImage();
        		if (!sameImages(img, newImg)) {
            		tx.interrupt();
            		break;
            	}
            	tx.join(1000);
        	}

        	//tx.join();
        	HashMap<String, MsgAnnotation> annotations = retv.value;
        	if (annotations == null)
        		continue;
        	
        	origNumMatchedMsgStrs = matchedMsgStrs.size();
        	
        	addNewAnnotations(annotations);

            
            annotateScreenshot(displayImage, annotations);
        	
            //HashMap<String, MsgAnnotation> annotations = Main.msgToAnnotationsWithTimeout(msgstrings, GCollectionUtils.singleElemList(imgMatches), 3000);
            //if (annotations == null)
            // 	continue;
            
            }
            if (origNumMatchedMsgStrs != matchedMsgStrs.size()) {
                //updateMatchCount();
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
            prevImg = img;
        }
    }
    
    public static void main(String[] args) throws Exception {
        List<String> msgfilecontents = new ArrayList<String>();
        if (args.length > 0)
            msgfilecontents = readLines(new FileReader(args[0]));
        POMsgSource msgsrc = new POMsgSource(msgfilecontents);
        final List<String> msgstrings = msgsrc.getMsgStrings();
        String screenshotSavePath = "";
        List<String> existingScreenshotPaths = new ArrayList<String>();
        if (args.length > 1) {
        	 screenshotSavePath = args[1] + "/";
        	 File screenshotDir = new File(screenshotSavePath);
         	if (!screenshotDir.exists()) {
         		screenshotDir.mkdirs();
         	} else {
         		for (String filename : screenshotDir.list()) {
         			filename = screenshotSavePath + filename;
         			existingScreenshotPaths.add(filename);
         		}
         	}
        }
        ScreenshotTaker st = new ScreenshotTaker(msgsrc, screenshotSavePath);
        st.guisetup();
        st.addNewAnnotations(new HashMap<String, MsgAnnotation>());
        for (String filename : existingScreenshotPaths) {
        	// add those messages that are covered by existing screenshots to matchedMsgStrs
        	System.out.println(filename);
 			List<ImgMatch> matches = Main.getImgMatches(filename);
 			HashMap<String, MsgAnnotation> annotations = Main.msgToAnnotations(msgstrings, GCollectionUtils.singleElemList(matches));
 			st.addNewAnnotations(annotations);
        }
        st.show();
        st.runme();

        


        //long prev_screenshot_time = 0;

    }
    static {
        System.loadLibrary("textmatch_ScreenshotTaker");
    }
}
