package textmatch;
import org.sikuli.script.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import java.util.*;
import javax.imageio.*;

import static textmatch.GStringUtils.*;
import static textmatch.GIOUtils.*;
import static textmatch.GCollectionUtils.*;
import static textmatch.LCS.*;
import static textmatch.CharTree.*;

public class Main {

    public static List<ImgMatch> getImgMatches(String imagefile) throws Exception {
        BufferedImage img = ImageIO.read(new File(imagefile));
        TextRecognizer recognizer = TextRecognizer.getInstance();
        //Mat mat = OpenCV.convertBufferedImageToMat(img.getImage());
        Rectangle rect = new java.awt.Rectangle(img.getWidth(), img.getHeight());
        ScreenImage simg = new ScreenImage(rect, img);
        List<Match> matches = recognizer.listText(simg, new Region(rect));
        List<ImgMatch> output = new ArrayList<ImgMatch>();
        for (Match m : matches) {
            if (m.w < 0 || m.h < 0)
                continue;
            output.add(new ImgMatch(m, imagefile));
        }
        return output;
    }
    
    public static Region spannedRegion(List<? extends Region> regions) {
        int minx = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxy = Integer.MIN_VALUE;
        for (Region r : regions) {
            if (r.x < minx)
                minx = r.x;
            if (r.y < miny)
                miny = r.y;
            if (r.x + r.w > maxx)
                maxx = r.x + r.w;
            if (r.y + r.h > maxy)
                maxy = r.y + r.h;
        }
        int w = maxx - minx;
        int h = maxy - miny;
        return new Region(minx, miny, w, h);
    }
    
    public static HashMap<String, String> msgToAnnotations(List<String> msgstrings, List<List<ImgMatch>> matchesAcrossImages) {
        HashMap<String, String> output = new HashMap<String, String>();
        for (String msgstr : msgstrings) {
            double bestratio = -1.0;
            int bestlength = -1;
            List<ImgMatch> bestmatch = null;
            String templateMatchText = "";
            char[] msg = msgstr.toCharArray();
            for (List<ImgMatch> matches : matchesAcrossImages) {
                for (List<ImgMatch> match : substrings(matches)) {
                    String xstr = join(match, " ");
                    char[] x = xstr.toCharArray();
                    if (msg.length > x.length*1.5)
                        continue;
                    if (count(msg, '%') == 0 && (x.length > msg.length*1.5))
                        continue;
                    if (match.isEmpty())
                        continue;
                    Pair<int[][], CharTree[][]> p = LCSMatrixTemplated(msg, x);
                    double curratio = LCSTemplatedScore(msg, x, p.Item1, p.Item2);
                    if (curratio >= bestratio) {
                        int textlen = lastElem(p.Item1);
                        if (curratio == bestratio) {
                            if (textlen < bestlength) {
                                continue;
                            }
                        }
                        bestmatch = match;
                        bestratio = curratio;
                        bestlength = textlen;
                        templateMatchText = charTreeToString(lastElem(p.Item2));
                    }
                }
            }
            if (bestratio >= 1/1.5) {
                Region spanningRegion = spannedRegion(bestmatch);
                String filename = bestmatch.get(0).getImgName();
                MsgAnnotation annotation = new MsgAnnotation(filename, spanningRegion, getSubstitutedStrings(templateMatchText));
                System.err.println(annotation.toString());
                output.put(msgstr, annotation.toString());
            }
        }
        return output;
    }
    
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        List<String> msgfilecontents = readLines(new FileReader(args[0]));
        POMsgSource msgsrc = new POMsgSource(msgfilecontents);
        List<String> msgstrings = msgsrc.getMsgStrings();
        List<List<ImgMatch>> matchesAcrossImages = new ArrayList<List<ImgMatch>>();
        for (int i = 1; i < args.length; ++i) {
            matchesAcrossImages.add(getImgMatches(args[i]));
        }
        HashMap<String, String> annotations = msgToAnnotations(msgstrings, matchesAcrossImages);
        String annotatedmsgfile = msgsrc.makeAnnotatedMsgFile(annotations);
        System.out.println(annotatedmsgfile);
    }

}
