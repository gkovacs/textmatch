package textmatch;


import static textmatch.LCS.*;
import static textmatch.GCollectionUtils.*;
import java.util.*;

import java.io.*;

import static textmatch.GStringUtils.*;

import static textmatch.GIOUtils.*;

public class POMsgSource {
    private List<String> lines = null;
    
    public POMsgSource(InputStream stream) throws Exception {
        this.lines = readLines(stream);
    }
    
    public POMsgSource(List<String> lines) throws Exception {
        this.lines = lines;
    }
    
    public POMsgSource(String[] lines) throws Exception {
        this.lines = toList(lines);
    }
    
    public POMsgSource(String contents) throws Exception {
        this.lines = splitToList(contents, '\n');
    }
    
    public List<String> getMsgStrings() throws Exception {
        List<String> msgidblocks = splitIntoMsgIdBlocks();
        List<String> msglist = new ArrayList<String>();
        for (String x : msgidblocks) {
            msglist.add(textFromMsgIdBlock(x));
        }
        while (msglist.contains(""))
            msglist.remove("");
        return msglist;
    }
    
    public static MsgAnnotation annotationFromMsgIdBlock(String msgidblock) throws Exception {
        for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("#& ")) {
                String annotationText = line.substring(3);
                return new MsgAnnotation(annotationText);

            }
        }
        return null;
    }
    
    public static List<MsgAnnotation> annotationListFromMsgIdBlock(String msgidblock) throws Exception {
        List<MsgAnnotation> output = new ArrayList<MsgAnnotation>();
        for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("#* ")) {
                String annotationText = line.substring(3);
                output.add(new MsgAnnotation(annotationText));

            }
        }
        return output;
    }
    
    public static List<MsgAnnotation> checkedAnnotationListFromMsgIdBlock(String msgidblock) throws Exception {
        List<MsgAnnotation> output = new ArrayList<MsgAnnotation>();
        for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("#% ")) {
                String annotationText = line.substring(3);
                try {
                    output.add(new MsgAnnotation(annotationText));
                } catch (Exception e) {
                    
                }
            }
        }
        return output;
    }
    
    // returns true if we manually specified that are no matches for the given block
    public static boolean noMatchesForMsgIdBlock(String msgidblock) throws Exception {
        for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("#% nomatches")) {
                return true;
            }
        }
        return false;
    }
    
    public static String msgSourceFromMsgIdBlock(String msgidblock) throws Exception {
    	for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("#: ")) {
                String annotationText = line.substring(3);
                return annotationText;

            }
        }
        return "";
    }
    
    public static String msgSourceConciseFromMsgIdBlock(String msgidblock) throws Exception {
    	String fullMsgSrc = msgSourceFromMsgIdBlock(msgidblock);
    	int lastColonIdx = fullMsgSrc.lastIndexOf(':');
    	if (lastColonIdx != -1) {
    		fullMsgSrc = fullMsgSrc.substring(0, lastColonIdx);
    	}
    	int lastSlashIdx = fullMsgSrc.lastIndexOf('/');
    	if (lastSlashIdx != -1) {
    		fullMsgSrc = fullMsgSrc.substring(lastSlashIdx + 1);
    	}
    	int lastDotIdx = fullMsgSrc.lastIndexOf('.');
    	if (lastDotIdx != -1 && fullMsgSrc.length() <= lastDotIdx + 4) {
    		fullMsgSrc = fullMsgSrc.substring(0, lastDotIdx);
    	}
    	return fullMsgSrc;
    	
    }
    
    public static String excludeForeignMsgStr(String msgidblock) throws Exception {
        List<String> curmsg = new ArrayList<String>();
        for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("msgstr \"")) {
                break;
            }
            curmsg.add(line);
        }
        return join(curmsg, "\n");
    }
    
    public static String excludeManualAnnotation(String msgidblock) throws Exception {
        List<String> curmsg = new ArrayList<String>();
        for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("#~ ")) {
                continue;
            }
            curmsg.add(line);
        }
        return join(curmsg, "\n");
    }
    
    public static List<List<String>> groupByMsgSouce(List<String> msgidblocks) throws Exception {
    	HashMap<String, Integer> msgSrcToIdx = new HashMap<String, Integer>();
    	List<List<String>> groupedMsgs = new ArrayList<List<String>>();
    	for (String msgidblock : msgidblocks) {
    		String msgsrc = msgSourceConciseFromMsgIdBlock(msgidblock);
    		int idxInGroupedMsgs;
    		if (!msgSrcToIdx.containsKey(msgsrc)) {
    			idxInGroupedMsgs = groupedMsgs.size();
    			msgSrcToIdx.put(msgsrc, idxInGroupedMsgs);
    			groupedMsgs.add(new ArrayList<String>());
    		} else {
    			idxInGroupedMsgs = msgSrcToIdx.get(msgsrc);
    		}
    		groupedMsgs.get(idxInGroupedMsgs).add(msgidblock);
    	}
    	return groupedMsgs;
    }
    
    public static String textFromMsgIdBlock(String msgidblock) throws Exception {
        List<String> curmsg = new ArrayList<String>();
        boolean active = false;
        for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("msgid \"")) {
                active = true;
                line = stripPrefix(line, "msgid \"");
            }
            else if (line.startsWith("msgstr \"") || line.startsWith("msgid_plural \"")) {
                active = false;
            }
            if (line.startsWith("#"))
                continue;
            if (active) {
                line = stripPrefix(line, "\"");
                line = stripSuffix(line, "\"");
                line = line.replace("\\\"", "\"");
                line = line.replace("%s", Character.toString(SUBCHAR));
                line = line.replace("%d", Character.toString(SUBCHAR));
                line = line.replace("%1", Character.toString(SUBCHAR));
                line = line.replace("%2", Character.toString(SUBCHAR));
                line = line.replace("%3", Character.toString(SUBCHAR));
                line = line.replace("\\n", " ");
                line = line.replace("_", "");
                curmsg.add(line);
            }
        }
        return join(curmsg, "").trim();
    }
    
    public static String foreignTextFromMsgIdBlock(String msgidblock) throws Exception {
        // TODO: plurals; (msgstr[0], msgstr[1])
        List<String> curmsg = new ArrayList<String>();
        boolean active = false;
        for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("msgstr \"")) {
                active = true;
                line = stripPrefix(line, "msgstr \"");
            }
            if (line.startsWith("#"))
                continue;
            if (active) {
                line = stripPrefix(line, "\"");
                line = stripSuffix(line, "\"");
                line = line.replace("\\\"", "\"");
                line = line.replace("\\n", " ");
                line = line.replace("_", "");
                curmsg.add(line);
            }
        }
        return join(curmsg, "").trim();
    }
    
    public List<String> splitIntoMsgIdBlocks() {
        List<String> blocks = new ArrayList<String>();
        List<String> curmsg = new ArrayList<String>();
        List<String> startSignals = new ArrayList<String>();
        // order in this list is the order in which they usually appear in the file
        startSignals.add("#& ");
        startSignals.add("#% ");
        startSignals.add("#* ");
        startSignals.add("#. ");
        startSignals.add("#: ");
        startSignals.add("msgid \"");
        startSignals.add("msgstr \"");
        String lastSeenStartSignal = "msgstr \"";
        for (String line : lines) {
            for (String x : startSignals) {
                if (line.startsWith(x)) {
                    if (lastSeenStartSignal.equals("msgstr \"") ||
                       (lastSeenStartSignal.equals("msgid \"") && !x.equals("msgstr \""))) {
                        // start of new block
                        if (curmsg.size() > 0) {
                            blocks.add(join(curmsg, "\n"));
                            curmsg.clear();
                        }
                    }
                    lastSeenStartSignal = x;
                }
            }
            curmsg.add(line);
        }
        if (curmsg.size() > 0) {
            blocks.add(join(curmsg, "\n"));
        }
        return blocks;
    }
    
    public String makeAnnotatedMsgFile(HashMap<String, String> msgToAnnotation) throws Exception {
        List<String> builder = new ArrayList<String>();
        List<String> msgidblocks = splitIntoMsgIdBlocks();
        for (String x : msgidblocks) {
            String msgtext = textFromMsgIdBlock(x);
            if (msgToAnnotation.containsKey(msgtext)) {
                builder.add("#& " + msgToAnnotation.get(msgtext));
            }
            builder.add(x);
        }
        return join(builder, "\n");
    }
}
