package textmatch;

import static textmatch.GStringUtils.*;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
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
    private final List<String> msgidblocks;
    private final String screenshotSavePath;
    private boolean savingScreenshots;
    private final Robot robot;
    private JFrame frame;
    private JLabel picLabel;
    private JLabel matchCount;
    private JTextPane textArea;
    private JTextPane textAreaSeen;
    
    private JCheckBox recordingCheckbox;
    
    private JButton saveScreenshotButton;
    
    private HashSet<String> matchedMsgStrs;
    
    private int[] prevData = new int[0];
    private int[] curData = new int[0];
    
    private int screenshotNum = 0;
    
    private final POMsgSource msgsrc;
    
    private final HashSet<String> processedImages = new HashSet<String>();
    
    public ScreenshotTaker(final POMsgSource msgsrc, String screenshotSavePath) throws Exception {
    	this.msgsrc = msgsrc;
    	this.msgidblocks = msgsrc.splitIntoMsgIdBlocks();
    	this.msgstrings = msgsrc.getMsgStrings();
    	this.matchedMsgStrs = new HashSet<String>();
    	this.screenshotSavePath = screenshotSavePath;
    	if (screenshotSavePath.equals(""))
    		savingScreenshots = false;
    	else {
    		savingScreenshots = true;
    		for (String x : new File(screenshotSavePath).list()) {
    		    x = stripSuffix(x, ".png");
    		    try {
    	            int q = Integer.parseInt(x);
    	            screenshotNum = Math.max(q, screenshotNum);
    		    } catch (Exception e) {
    		        
    		    }
    		}
    	}
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
        matchCount.setSize(150, 30);
        
        picLabel = new JLabel();
        picLabel.setLocation(0, 30);
        picLabel.setSize(600, 550);
        frame.add(picLabel);
        
        //picLabel.repaint();
        
        

        
        frame.add(matchCount);
        
        
        updateMatchCount();
        
        textArea = new JTextPane();
        textArea.setLocation(0, 0);
        textArea.setSize(200, 600);
        
        JScrollPane scrollable = new JScrollPane(textArea);
        scrollable.setLocation(600, 0);
        scrollable.setSize(200, 600);
        scrollable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollable);
        
        textAreaSeen = new JTextPane();
        textAreaSeen.setLocation(0, 0);
        textAreaSeen.setSize(200, 600);
        
        JScrollPane scrollable2 = new JScrollPane(textAreaSeen);
        scrollable2.setLocation(800, 0);
        scrollable2.setSize(200, 600);
        scrollable2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        //frame.add(textArea);
        frame.add(scrollable2);
        
        recordingCheckbox = new JCheckBox();
        recordingCheckbox.setLocation(150, 0);
        recordingCheckbox.setSize(270, 30);
        recordingCheckbox.setText("Automatically Save Screenshots");
        frame.add(recordingCheckbox);
        
        
        saveScreenshotButton = new JButton();
        saveScreenshotButton.setLocation(420, 0);
        saveScreenshotButton.setSize(180, 30);
        saveScreenshotButton.setText("Save Screenshot");
        frame.add(saveScreenshotButton);
    }
    
    private void updateMatchCount() {
        matchCount.setText(matchedMsgStrs.size() + " / " + msgstrings.size() + " messages");
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
        HashSet<String> newMsgStrs = new HashSet<String>();
    	for (String msgstr : annotations.keySet()) {
    		if (!matchedMsgStrs.contains(msgstr)) {
            	matchedMsgStrs.add(msgstr);
            	newMsgStrs.add(msgstr);
    		}
        }
    	final HashMap<String, Integer> numOccurrencesOfSources = new HashMap<String, Integer>();
    	// gnome-mouse-properties => #of times it occurs on the current screenshot
    	for (String msgidblock : this.msgidblocks) {
    		String msgfsrc = msgsrc.msgSourceConciseFromMsgIdBlock(msgidblock);
    		if (!numOccurrencesOfSources.containsKey(msgfsrc))
    			numOccurrencesOfSources.put(msgfsrc, 0);
    		String msgstr = msgsrc.textFromMsgIdBlock(msgidblock);
    		if (annotations.keySet().contains(msgstr)) {
    			numOccurrencesOfSources.put(msgfsrc, numOccurrencesOfSources.get(msgfsrc) + 1);
    		}
    	}
    	
    	List<List<String>> msgsGroupedBySource = msgsrc.groupByMsgSouce(msgidblocks);
    	Collections.sort(msgsGroupedBySource, new Comparator<List<String>>() {

			@Override
			public int compare(List<String> o1, List<String> o2) {
				try {
					String src1 = msgsrc.msgSourceConciseFromMsgIdBlock(o1.get(0));
					String src2 = msgsrc.msgSourceConciseFromMsgIdBlock(o2.get(0));
					return numOccurrencesOfSources.get(src2) - numOccurrencesOfSources.get(src1);
				} catch (Exception e) {
					return 0;
				}
			}
    		
    	});
    	
        //String curMsgSource = "";
        //String curMsgSourceFound = "";
        StringBuilder msgsToBeFound = new StringBuilder();
        msgsToBeFound.append("<html><body>");
        msgsToBeFound.append("<h3>Messages Not Found:</h3>");
        StringBuilder msgsFound = new StringBuilder();
        msgsFound.append("<html><body>");
        msgsFound.append("<h3>Messages Found:</h3>");
        //for (String msgidblock : msgsrc.splitIntoMsgIdBlocks()) {
        for (List<String> msgidblockGroup : msgsGroupedBySource) {
        	String msgsource = msgsrc.msgSourceConciseFromMsgIdBlock(msgidblockGroup.get(0));
    		msgsFound.append("<p><b>" + msgsource + "</b></p>\n");
    		msgsToBeFound.append("<p><b>" + msgsource + "</b></p>\n");
    		for (String msgidblock : msgidblockGroup) {
    			String msgstr = msgsrc.textFromMsgIdBlock(msgidblock);
    	        if (msgstr.isEmpty())
    	        	continue;
    	        if (matchedMsgStrs.contains(msgstr)) {
                	if (newMsgStrs.contains(msgstr)) {
                    	msgsFound.append("<p bgcolor='#FFFF00'>" + msgstr + "</p>\n");
                	} else if (annotations.keySet().contains(msgstr)) {
                  	msgsFound.append("<p bgcolor='#00FFFF'>" + msgstr + "</p>\n");
    	          } else {
                    	msgsFound.append("<p>" +  msgstr + "</p>\n");
                	}
    			} else {
    				msgsToBeFound.append("<p>" + msgstr + "</p>\n");
    			}
    		}
        }
        /*
          String msgstr = msgsrc.textFromMsgIdBlock(msgidblock);
        	if (msgstr.isEmpty())
        		continue;
        	String msgsource = msgsrc.msgSourceConciseFromMsgIdBlock(msgidblock);
        	if (matchedMsgStrs.contains(msgstr)) {
            	if (!msgsource.isEmpty() && !msgsource.equals(curMsgSourceFound)) {
            		curMsgSourceFound = msgsource;
            		msgsFound.append("<p><b>" + msgsource + "</b></p>\n");
            	}
            	if (newMsgStrs.contains(msgstr)) {
                	msgsFound.append("<p bgcolor='#FFFF00'>" + msgstr + "</p>\n");
            	} else {
                	msgsFound.append("<p>" +  msgstr + "</p>\n");
            	}
        	} else {
        		if (!msgsource.isEmpty() && !msgsource.equals(curMsgSource)) {
            		curMsgSource = msgsource;
            		msgsToBeFound.append("<p><b>" + msgsource + "</b></p>\n");
            	}
        		msgsToBeFound.append("<p>" + msgstr + "</p>\n");
        	}
        }
        */
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
            g.setColor(new Color(0, 255, 255, 100));
            Rectangle rect = new Rectangle(annotation.x, annotation.y, annotation.w, annotation.h);
            g.fill(rect);
            //g.draw(rect);
            //displayImage.getRaster().setPixels(annotation.x, annotation.y, annotation.w, annotation.h, values);
        }
    }
    
    private String hashable(List<ImgMatch> imgMatches) {
        StringBuilder b = new StringBuilder();
        for (ImgMatch x : imgMatches) {
            b.append(x.text() + "\n");
        }
        return b.toString();
    }
    
    public void runme() throws Exception {
        BufferedImage img = null;
        boolean imgCaptured = false;
        String imgMatchesHashable = null;
        List<ImgMatch> imgMatches = null;
        
        
        while (true) {
            if (frame.isFocused()) {
                imgCaptured = false;
                continue;
            }
            if (!recordingCheckbox.isSelected()) {
                imgCaptured = false;
                continue;
            }
            
            //if (curtime < prev_screenshot_time + 250) {
            //    continue;
            //}
            //captureNewImage();
            if (!imgCaptured) {
                img = captureNewImage();
                if (sameImages(img, prevImg))
                    continue;
                imgMatches = Main.getImgMatches(img, "");
                imgMatchesHashable = hashable(imgMatches);
            }
            
            if (frame.isFocused()) {
                imgCaptured = false;
                continue;
            }
            if (!recordingCheckbox.isSelected()) {
                imgCaptured = false;
                continue;
            }
            
            boolean containsNewMsgStrs = false;
            int origNumMatchedMsgStrs = 0;
            
            BufferedImage displayImage = null;
            if (msgstrings.size() == 0) {
                displayImage = img;
            } else {
            
            /*
            if (imgMatches.size() > 300) {
                System.err.println("too many words");
                continue;
            }
            */
            
          final MutableValue<HashMap<String, MsgAnnotation>> retv = new MutableValue<HashMap<String, MsgAnnotation>>();
          final List<ImgMatch> imgMatchesF = imgMatches;
          Thread tx = new Thread(new Runnable() {

    		@Override
    		public void run() {
    			// TODO Auto-generated method stub
    			try {
    				retv.value = Main.msgToAnnotationsTwoPass(msgstrings, GCollectionUtils.singleElemList(imgMatchesF));
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
        	   
           });
        	tx.start();
        	boolean interrupted = false;

        	while (tx.isAlive()) {
        	    if (!recordingCheckbox.isSelected()) continue;
        	    if (frame.isFocused()) continue;
            	BufferedImage newImg = captureNewImage();
            	if (frame.isFocused()) continue;
            	if (!recordingCheckbox.isSelected()) continue;
            	if (!sameImages(img, newImg)) {
        		    List<ImgMatch> newImgMatches = Main.getImgMatches(newImg, "");
        		    String newImgMatchesHashable = hashable(newImgMatches);
        		    if (!processedImages.contains(newImgMatchesHashable)) {
        		        interrupted = true;
        		        tx.interrupt();
                        imgCaptured = true;
                        img = newImg;
                        imgMatches = newImgMatches;
                        imgMatchesHashable = newImgMatchesHashable;
                        break;
        		    }
            	}
            	tx.join(1000);
        	}

        	//tx.join();
        	HashMap<String, MsgAnnotation> annotations = retv.value;
        	if (interrupted || annotations == null)
        		continue;
        	
        	imgCaptured = false;
        	processedImages.add(imgMatchesHashable);
        	
        	displayImage = deepCopy(img);
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
 			HashMap<String, MsgAnnotation> annotations = Main.msgToAnnotationsTwoPass(msgstrings, GCollectionUtils.singleElemList(matches));
 			st.addNewAnnotations(annotations);
            st.processedImages.add(st.hashable(matches));
        }
        st.show();
        st.runme();

        


        //long prev_screenshot_time = 0;

    }
    static {
        System.loadLibrary("textmatch_ScreenshotTaker");
    }
}
