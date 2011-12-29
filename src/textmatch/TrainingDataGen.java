package textmatch;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static textmatch.CharTree.charTreeToString;
import static textmatch.GCollectionUtils.*;
import static textmatch.GStringUtils.*;
import static textmatch.LCS.*;

import java.awt.Rectangle;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TrainingDataGen {

    public static List<MatchResults> orderMatches(String msgstr, List<List<ImgMatch>> matchesAcrossImages /*, List<HashMap<String, Integer>> ngramsForImages*/) {
        List<MatchResults> matchresults = new ArrayList<MatchResults>();
        
        final char[] msg = msgstr.toCharArray();
        final int maxMsgLength;
        final boolean isTemplated = (count(msg, SUBCHAR) != 0);
        
        final int filterCutoff = (isTemplated ? msgstr.indexOf(SUBCHAR) : msg.length);
        
        //final HashMap<String, Integer> ngramsForCurMsg = ngrams(msgstr.substring(0, filterCutoff), 2);
        
        if (isTemplated)
            maxMsgLength = msg.length*3;
        else
            maxMsgLength = msg.length*3/2;
        
        for (int i = 0; i < matchesAcrossImages.size(); ++i) {
            //if (!isTemplated && i != bestImgIdx) {
            //    continue;
            //}
            final List<ImgMatch> matches = matchesAcrossImages.get(i);
            for (final IntPair matchIdxs : substrings(matches.size(), new IntPairAcceptor() {

                @Override
                public boolean isAccepted(int j, int i) {
                    //List<ImgMatch> nMatch = slice(matches, j, i);
                    ImgMatch[] nMatch = arraySlice(matches, j, i);
                    int totalarea = totalArea(nMatch);
                    int spanningarea = spanningArea(nMatch);
                    if (totalarea * 4 >= spanningarea * 5)
                        return false;
                    String xstr = join(nMatch, " ");
                    if (xstr.length() > maxMsgLength)
                        return false;
                    if (xstr.length() == 0)
                        return false;
                    //if (xstr.length() > 4 && xstr.length() < filterCutoff) {
                        //HashMap<String, Integer> ngramsForCurSubstring = ngrams(xstr, 2);
                        //if (ngramMatchFraction(ngramsForCurSubstring, ngramsForCurMsg) < 0.5)
                        //    return false;
                    //}
                    
                    return true;
                }
                
            })) {
                ImgMatch[] match = arraySlice(matches, matchIdxs.Item1, matchIdxs.Item2);
                String xstr = join(match, " ");
                char[] x = xstr.toCharArray();
                if (match.length < 1)
                    continue;
                if (x.length*3 < msg.length*2)
                    continue;
                Pair<double[][], CharTree[][]> p = LCSMatrixTemplated(msg, x);
                double curratio = LCSTemplatedScore(msg, x, p.Item1, p.Item2);
                
                String templateText = charTreeToString(lastElem(p.Item2));
                
                
                MatchResults matchresult = new MatchResults(toList(match), curratio, templateText, matchIdxs);
                matchresults.add(matchresult);
            }
        }
        Collections.sort(matchresults, new Comparator<MatchResults>() {

            @Override
            public int compare(MatchResults o1, MatchResults o2) {
                return (int)(100000 * (o2.ratio - o1.ratio));
            }
            
        });
        
        HashSet<String> seenImages = new HashSet<String>();
        List<MatchResults> filteredMatchResults = new ArrayList<MatchResults>();
        for (MatchResults x : matchresults) {
            String srcimg = x.match.get(0).getImgName();
            if (seenImages.contains(srcimg))
                continue;
            seenImages.add(srcimg);
            filteredMatchResults.add(x);
        }
        
        return limitLength(filteredMatchResults, 10);
        
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        // 1st arg: po file
        // 2nd and following: png files
        POMsgSource msgsrc = new POMsgSource(new FileInputStream(args[0]));
        List<List<ImgMatch>> matchesAcrossImages = new ArrayList<List<ImgMatch>>();
        for (int i = 1; i < args.length; ++i) {
            matchesAcrossImages.add(Main.getImgMatches(args[i]));
        }
        
        //List<HashMap<String, Integer>> ngramsForImages = Main.getNgramsForImages(matchesAcrossImages);
        
        HashMap<String, String> annotationStrings = new HashMap<String, String>();
        for (String msgblock : msgsrc.splitIntoMsgIdBlocks()) {
            String msgstr = msgsrc.textFromMsgIdBlock(msgblock);
            List<MatchResults> matchresults = orderMatches(msgstr, matchesAcrossImages /*, ngramsForImages*/);
            for (MatchResults x : matchresults) {
                Rectangle spanningRegion = spannedRectangle(x.match);
                String filename = x.match.get(0).getImgName();
                MsgAnnotation annotation = new MsgAnnotation(filename, spanningRegion, getSubstitutedStrings(x.templateMatchText), join(x.match, " "));
                //annotationStrings.put(msgstr, annotation.toString());
                System.err.println(annotation.toString());
                System.out.println("#* " + annotation.toString());
            }
            System.out.println(msgblock);
        }
        //String annotatedmsgfile = msgsrc.makeAnnotatedMsgFile(annotationStrings);
        //System.out.println(annotatedmsgfile);
    }

}
