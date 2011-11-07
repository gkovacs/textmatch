package textmatch;
import org.sikuli.script.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import java.util.*;
import javax.imageio.*;

import static java.lang.Math.*;
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
    
    public static MatchResults bestMatch(String msgstr, List<List<ImgMatch>> matchesAcrossImages, List<HashMap<String, Integer>> ngramsForImages, HashMap<String, Integer> occurrenceCount) {
        double bestratio = -1.0;
        double bestlength = -1.0;
        List<ImgMatch> bestmatch = null;
        String templateMatchText = "";
        char[] msg = msgstr.toCharArray();
        final int maxMsgLength;
        boolean isTemplated = (count(msg, SUBCHAR) != 0);
        if (isTemplated)
            maxMsgLength = msg.length*3;
        else
            maxMsgLength = msg.length*3/2;
        /*
        HashMap<String, Integer> ngramsForCurImg = ngrams(msgstr.replace(SUBCHAR, ' '), 3);
        int bestImgIdx = 0;
        if (!isTemplated) {
            double bestNgramMatch = 0.0;
            for (int i = 0; i < ngramsForImages.size(); ++i) {
                double matchValue = ngramMatchFraction(ngramsForCurImg, ngramsForImages.get(i));
                if (matchValue > bestNgramMatch) {
                    bestImgIdx = i;
                    bestNgramMatch = matchValue;
                }
            }
            if (bestNgramMatch < 0.5)
                return null;
        }
        */
        //List<ImgMatch> matches = matchesAcrossImages.get(bestImgIdx);
        for (int i = 0; i < matchesAcrossImages.size(); ++i) {
            //if (!isTemplated && i != bestImgIdx) {
            //    continue;
            //}
            List<ImgMatch> matches = matchesAcrossImages.get(i);
            for (List<ImgMatch> match : substrings(matches)) {
                String xstr = join(match, " ");
                int numOccurrences = 0;
                if (occurrenceCount.containsKey(xstr))
                    numOccurrences = occurrenceCount.get(xstr);
                if (numOccurrences >= xstr.length()*3/2)
                    continue;
                //if (blacklist.contains(xstr))
                //    continue;
                char[] x = xstr.toCharArray();
                if (x.length*3 < msg.length*2)
                    continue;
                if (x.length > maxMsgLength)
                    continue;
                if (x.length == 0)
                    continue;
                Pair<double[][], CharTree[][]> p = LCSMatrixTemplated(msg, x);
                double curratio = LCSTemplatedScore(msg, x, p.Item1, p.Item2);
                curratio -= ((double)numOccurrences) / x.length;
                if (curratio >= bestratio) {
                    String templateText = charTreeToString(lastElem(p.Item2));
                    double textlen = lastElem(p.Item1);
                    if (curratio == bestratio) {
                        if (templateText.length() > templateMatchText.length()) {
                            continue;
                        } else if (templateText.length() == templateMatchText.length()) {
                            if (textlen < bestlength)
                                continue;
                        }
                    }
                    bestmatch = match;
                    bestratio = curratio;
                    bestlength = textlen;
                    templateMatchText = templateText;
                }
            }
        }
        return new MatchResults(bestmatch, bestratio, templateMatchText);
    }
    
    
    
    public static HashMap<String, String> msgToAnnotations(List<String> msgstrings, List<List<ImgMatch>> matchesAcrossImages) {
        HashMap<String, String> output = new HashMap<String, String>();
        
        List<HashMap<String, Integer>> ngramsForImages = null; //new ArrayList<HashMap<String, Integer>>();
        /*
        for (List<ImgMatch> matches : matchesAcrossImages) {
            String s = join(matches, " ");
            ngramsForImages.add(ngrams(s, 3));
        }
        */
        List<Pair<Double, String>> msgStrToScore = new ArrayList<Pair<Double, String>>();
        HashMap<String, Integer> occurrenceCount = new HashMap<String, Integer>();
        for (String msgstr : msgstrings) {
            MatchResults m = bestMatch(msgstr, matchesAcrossImages, ngramsForImages, occurrenceCount);
            if (m == null)
                continue;
            double bestratio = m.ratio;
            List<ImgMatch> bestmatch = m.match;
            String templateMatchText = m.templateMatchText;
            if (bestratio >= 1/1.5) {
                /*
                Region spanningRegion = spannedRegion(bestmatch);
                String filename = bestmatch.get(0).getImgName();
                MsgAnnotation annotation = new MsgAnnotation(filename, spanningRegion, getSubstitutedStrings(templateMatchText));
                System.err.println(annotation.toString());
                output.put(msgstr, annotation.toString());
                */
                msgStrToScore.add(makePair(bestratio, msgstr));
            }
        }
        Collections.sort(msgStrToScore, new PairFirstComparator<Double>(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if (o1 > o2)
                    return 1;
                if (o2 > o1)
                    return -1;
                return 0;
            }
        }));
        Collections.reverse(msgStrToScore);
        for (Pair<Double, String> p : msgStrToScore) {
            String msgstr = p.Item2;
            MatchResults m = bestMatch(msgstr, matchesAcrossImages, ngramsForImages, occurrenceCount);
            if (m == null)
                continue;
            double bestratio = m.ratio;
            List<ImgMatch> bestmatch = m.match;
            String templateMatchText = m.templateMatchText;
            String imgText = join(bestmatch, " ");
            if (bestratio >= 1/1.5) {
                Region spanningRegion = spannedRegion(bestmatch);
                String filename = bestmatch.get(0).getImgName();
                MsgAnnotation annotation = new MsgAnnotation(filename, spanningRegion, getSubstitutedStrings(templateMatchText));
                System.err.println(annotation.toString());
                System.err.println(join(bestmatch, " "));
                output.put(msgstr, annotation.toString());
                for (List<ImgMatch> x : substrings(bestmatch)) {
                    // increment occurrence count for every substring of the matched text
                    incrementMap(occurrenceCount, join(x, " "));
                }
                //blacklist.add(join(bestmatch, " "));
                //msgStrToScore.add(makePair(bestratio, msgstr));
            }
        }
        return output;
    }
    
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        List<String> msgfilecontents = readLines(new FileReader(args[0]));
        POMsgSource msgsrc = new POMsgSource(msgfilecontents);
        List<String> msgstrings = msgsrc.getMsgStrings();
        HashSet<List<ImgMatch>> matchesAcrossImagesH = new HashSet<List<ImgMatch>>();
        for (int i = 1; i < args.length; ++i) {
            matchesAcrossImagesH.add(getImgMatches(args[i]));
        }
        List<List<ImgMatch>> matchesAcrossImages = new ArrayList<List<ImgMatch>>();
        for (List<ImgMatch> x : matchesAcrossImagesH)
            matchesAcrossImages.add(x);
        HashMap<String, String> annotations = msgToAnnotations(msgstrings, matchesAcrossImages);
        String annotatedmsgfile = msgsrc.makeAnnotatedMsgFile(annotations);
        System.out.println(annotatedmsgfile);
    }

}
