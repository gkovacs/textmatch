package textmatch;

import java.io.*;
import java.util.*;

import static textmatch.GCollectionUtils.*;
import static textmatch.GStringUtils.*;
import static textmatch.POMsgSource.*;
import static textmatch.GIOUtils.*;
import static textmatch.LCS.*;

public class HTMLGen {
    
    public static void main(String[] args) throws Exception {
        // args[0]: an annotated po file
        List<String> msgfilelines = readLines(new FileReader(args[0]));
        POMsgSource msgsrc = new POMsgSource(msgfilelines);
        List<String> msgblocks = msgsrc.splitIntoMsgIdBlocks();
        List<String> j = new ArrayList<String>();
        // contains the javascript stuff, namely the body of the draw method
        List<String> h = new ArrayList<String>();
        // contains the html stuff, namely the canvas ids and text
        int i = 0;
        List<Pair<MsgAnnotation, String>> annotatedMsgBlocks = new ArrayList<Pair<MsgAnnotation, String>>();
        for (String x : msgblocks) {
            MsgAnnotation annotation = annotationFromMsgIdBlock(x);
            if (annotation == null)
                continue;
            //String msgtext = textFromMsgIdBlock(x);
            annotatedMsgBlocks.add(makePair(annotation, x));
        }
        Collections.sort(annotatedMsgBlocks, new PairFirstComparator<MsgAnnotation>(new MsgAnnotationRegionComparator()));
        for (Pair<MsgAnnotation, String> annotationAndText : annotatedMsgBlocks) {
            MsgAnnotation annotation = annotationAndText.Item1;
            String blockText = annotationAndText.Item2;
            String msgtext = textFromMsgIdBlock(blockText);
            Integer[] coordinates = new Integer[] {annotation.x, annotation.y, annotation.w, annotation.h};
            String coordargs = join(coordinates, ",");
            String canvasname = "c" + i;
            j.add("annotate('" + canvasname + "', '" + annotation.filename + "', " + coordargs + ")");
            h.add("<canvas id='" + canvasname + "'></canvas>");
            h.add("<p><b>" + substituteIntoTemplateMarked(msgtext, annotation.templateSubstitutions) + "</b></p>");
            h.add("<p>" + blockText.replace("\n", "<br/>").replace("~~~~~~", "~~~").replace("~~~", "<br/>#& ") + "</p>");
            i += 1;
        }
        List<String> o = new ArrayList<String>();
        o.add("<html>");
        o.add("<head>");
        o.add("<script type='application/javascript'>");
        o.add("function annotate(canvasname, imagefile, x, y, w, h) {");
        o.add("var canvas = document.getElementById(canvasname)");
        o.add("var img = new Image()");
        o.add("img.src = imagefile");
        o.add("canvas.width = img.width");
        o.add("canvas.height = img.height");
        o.add("var ctx = canvas.getContext('2d')");
        o.add("ctx.drawImage(img, 0, 0)");
        o.add("ctx.fillStyle = 'rgba(0, 200, 200, 0.5)'");
        o.add("ctx.fillRect (x, y, w, h)");
        o.add("}");
        o.add("function draw() {");
        o.add(join(j, "\n"));
        o.add("}");
        o.add("</script>");
        o.add("</head>");
        o.add("<body onload='draw()'>");
        o.add(join(h, "\n"));
        o.add("</body>");
        o.add("</html>");
        System.out.println(join(o, "\n"));
    }
}
