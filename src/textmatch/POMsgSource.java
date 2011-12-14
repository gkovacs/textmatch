package textmatch;


import static textmatch.LCS.*;
import java.util.*;

import java.io.*;

import static textmatch.GStringUtils.*;

import static textmatch.GIOUtils.*;

public class POMsgSource {
    private List<String> lines = null;
    
    public POMsgSource(List<String> lines) throws Exception {
        this.lines = lines;
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
    
    public static String msgSourceFromMsgIdBlock(String msgidblock) throws Exception {
    	for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("#: ")) {
                String annotationText = line.substring(3);
                return annotationText;

            }
        }
        return "";
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
    
    
    
    public static String textFromMsgIdBlock(String msgidblock) throws Exception {
        List<String> curmsg = new ArrayList<String>();
        boolean active = false;
        for (String line : readLines(new StringReader(msgidblock))) {
            if (line.startsWith("msgid \"")) {
                active = true;
                line = stripPrefix(line, "msgid \"");
            }
            else if (line.startsWith("msgstr \"")) {
                active = false;
            }
            if (line.startsWith("#"))
                continue;
            if (active) {
                line = stripPrefix(line, "\"");
                line = stripSuffix(line, "\"");
                line = line.replace("\\\"", "\"");
                line = line.replace("%s", Character.toString(SUBCHAR));
                line = line.replace("\\n", " ");
                line = line.replace("_", "");
                curmsg.add(line);
            }
        }
        return join(curmsg, "").trim();
    }
    
    public static String foreignTextFromMsgIdBlock(String msgidblock) throws Exception {
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
