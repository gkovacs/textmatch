package textmatch;

import java.io.*;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import static textmatch.GCollectionUtils.*;
import static textmatch.GStringUtils.*;
import static textmatch.POMsgSource.*;
import static textmatch.GIOUtils.*;
import static textmatch.LCS.*;

public class HTMLGen {
    
    public static String htmlFromAnnotations(HashMap<String, MsgAnnotation> annotations, List<Pair<String, String>> base64EncodedFiles, String auxtext) throws Exception {
        List<Pair<MsgAnnotation, String>> annotatedMsgBlocks = new ArrayList<Pair<MsgAnnotation, String>>();
        for (String x : annotations.keySet()) {
            MsgAnnotation annotation = annotations.get(x);
            annotatedMsgBlocks.add(new Pair<MsgAnnotation, String>(annotation, x));
        }
        Collections.sort(annotatedMsgBlocks, new PairFirstComparator<MsgAnnotation>(new MsgAnnotationRegionComparator()));
        return htmlFromAnnotations(annotatedMsgBlocks, base64EncodedFiles, auxtext);
    }
    
    public static String htmlFromAnnotations(List<Pair<MsgAnnotation, String>> annotatedMsgBlocks, List<Pair<String, String>> base64EncodedFiles, String auxtext) throws Exception {
        int i = 0;
        List<String> j = new ArrayList<String>();
        // contains the javascript stuff, namely the body of the draw method
        List<String> h = new ArrayList<String>();
        // contains the html stuff, namely the canvas ids and text
        for (Pair<MsgAnnotation, String> annotationAndText : annotatedMsgBlocks) {
            MsgAnnotation annotation = annotationAndText.Item1;
            String blockText = annotationAndText.Item2;
            String blockTextMinusForeign = excludeForeignMsgStr(blockText);
            String msgtext = textFromMsgIdBlock(blockText);
            String foreigntext = foreignTextFromMsgIdBlock(blockText);
            
            byte[] msgtextB = msgtext.getBytes("UTF-8");
            String base64msgtext = DatatypeConverter.printBase64Binary(msgtextB);
            
            Integer[] coordinates = new Integer[] {annotation.x, annotation.y, annotation.w, annotation.h};
            String coordargs = join(coordinates, ",");
            String canvasname = "c" + i;
            j.add("annotate('" + canvasname + "', '" + annotation.filename + "', " + coordargs + ")");
            h.add("<canvas id='" + canvasname + "'></canvas>");
            h.add("<p><b>" + substituteIntoTemplateMarked(msgtext, annotation.templateSubstitutions) + "</b></p>");
            h.add("<input type='text' size=100 value='" + foreigntext + "' name='t-" + base64msgtext + "' />");
            h.add("<p>" + blockTextMinusForeign.replace("\n", "<br/>").replace("~~~~~~", "~~~").replace("~~~", "<br/>#& ") + "</p>");
            i += 1;
        }
        List<String> o = new ArrayList<String>();
        o.add("<html>");
        o.add("<head>");
        o.add("<script type='application/javascript'>");
        o.add("base64data = {} ");
        for (Pair<String, String> p : base64EncodedFiles) {
            o.add("base64data['" + p.Item1 + "'] = '" + p.Item2 + "'");
        }
        o.add("function annotate(canvasname, imagefile, x, y, w, h) {");
        o.add("var canvas = document.getElementById(canvasname)");
        o.add("var img = new Image()");
        o.add("img.onload = function() {");
        o.add("canvas.width = img.width");
        o.add("canvas.height = img.height");
        o.add("var ctx = canvas.getContext('2d')");
        o.add("ctx.drawImage(img, 0, 0)");
        o.add("ctx.fillStyle = 'rgba(0, 200, 200, 0.5)'");
        o.add("ctx.fillRect (x, y, w, h)");
        o.add("}");
        o.add("img.src = 'data:image/png;base64,' + base64data[imagefile]");
        o.add("}");
        o.add("window.onload = function() {");
        o.add(join(j, "\n"));
        o.add("}");
        o.add("</script>");
        o.add("</head>");
        o.add("<body>");
        o.add("<form method='POST' action='textmatch-annotated.jsp'>");
        o.add("<input type='submit' name='button' value='Download Annotated Message File' /><br/><br/>");
        o.add(auxtext);
        o.add(join(h, "\n"));
        o.add("</form>");
        o.add("</body>");
        o.add("</html>");
        return join(o, "\n");
    }
    
    public static String htmlFromAnnotations(List<Pair<MsgAnnotation, String>> annotatedMsgBlocks, String auxtext) throws Exception {
        List<String> filenames = new ArrayList<String>();
        for (Pair<MsgAnnotation, String> annotationAndText : annotatedMsgBlocks) {
            MsgAnnotation annotation = annotationAndText.Item1;
            if (!filenames.contains(annotation.filename))
                filenames.add(annotation.filename);
        }
        List<Pair<String, String>> base64EncodedFiles = new ArrayList<Pair<String, String>>();
        byte[] buffer = new byte[1024*1024*1];
        for (String filename : filenames) {
            int numBytes = new FileInputStream(filename).read(buffer);
            byte[] nbuf = new byte[numBytes];
            for (int q = 0; q < numBytes; ++q) {
                nbuf[q] = buffer[q];
                buffer[q] = 0;
            }
            String base64data = DatatypeConverter.printBase64Binary(nbuf);
            base64EncodedFiles.add(new Pair<String, String>(filename, base64data));
        }
        return htmlFromAnnotations(annotatedMsgBlocks, base64EncodedFiles, auxtext);
    }
    
    public static void main(String[] args) throws Exception {
        // args[0]: an annotated po file
        List<String> msgfilelines = readLines(new FileReader(args[0]));
        POMsgSource msgsrc = new POMsgSource(msgfilelines);
        List<String> msgblocks = msgsrc.splitIntoMsgIdBlocks();
        
        List<Pair<MsgAnnotation, String>> annotatedMsgBlocks = new ArrayList<Pair<MsgAnnotation, String>>();
        for (String x : msgblocks) {
            MsgAnnotation annotation = annotationFromMsgIdBlock(x);
            if (annotation == null)
                continue;
            //String msgtext = textFromMsgIdBlock(x);
            annotatedMsgBlocks.add(makePair(annotation, x));
        }
        Collections.sort(annotatedMsgBlocks, new PairFirstComparator<MsgAnnotation>(new MsgAnnotationRegionComparator()));
        System.out.println(htmlFromAnnotations(annotatedMsgBlocks, ""));
    }
}
