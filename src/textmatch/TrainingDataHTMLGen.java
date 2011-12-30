package textmatch;

import static textmatch.GImageUtils.*;
import static textmatch.HTMLGen.imagesDir;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import static textmatch.GCollectionUtils.*;
import static textmatch.GStringUtils.*;
import static textmatch.POMsgSource.*;
import static textmatch.GIOUtils.*;
import static textmatch.LCS.*;



public class TrainingDataHTMLGen {
    
    
    
    /*
    public static String htmlFromAnnotations(HashMap<String, List<MsgAnnotation>> annotations, List<Pair<String, String>> base64EncodedFiles, String auxtext) throws Exception {
        List<Pair<List<MsgAnnotation>, String>> annotatedMsgBlocks = new ArrayList<Pair<List<MsgAnnotation>, String>>();
        for (String x : annotations.keySet()) {
            List<MsgAnnotation> annotation = annotations.get(x);
            annotatedMsgBlocks.add(new Pair<MsgAnnotation, String>(annotation, x));
        }
        Collections.sort(annotatedMsgBlocks, new PairFirstComparator<MsgAnnotation>(new MsgAnnotationRegionComparator()));
        return htmlFromAnnotations(annotatedMsgBlocks, base64EncodedFiles, auxtext);
    }
    */
    
    public static String htmlFromAnnotations(List<Pair<List<MsgAnnotation>, String>> annotatedMsgBlocks, List<Pair<String, String>> base64EncodedFiles, String auxtext) throws Exception {
        HashMap<String, Dimension> imageSizes = new HashMap<String, Dimension>();
        for (Pair<String, String> p : base64EncodedFiles) {
            BufferedImage img = imageFromBase64Encoded(p.Item2);
            imageSizes.put(p.Item1, new Dimension(img.getWidth(), img.getHeight()));
        }
        
        int i = 0;
        List<String> j = new ArrayList<String>();
        // contains the javascript stuff, namely the body of the draw method
        List<String> h = new ArrayList<String>();
        // contains the html stuff, namely the canvas ids and text
        for (Pair<List<MsgAnnotation>, String> annotationAndText : annotatedMsgBlocks) {
            List<MsgAnnotation> annotationList = annotationAndText.Item1;
            String blockText = annotationAndText.Item2;
            
            String blockTextMinusForeign = excludeForeignMsgStr(blockText);
            String msgtext = textFromMsgIdBlock(blockText);
            String foreigntext = foreignTextFromMsgIdBlock(blockText);
            
            //byte[] msgtextB = msgtext.getBytes("UTF-8");
            //String base64msgtext = DatatypeConverter.printBase64Binary(msgtextB);
            String encmsgtext = URLEncoder.encode(msgtext, "UTF-8");
            int totalImageWidth = 0;
            int maxImageHeight = 0;
            for (MsgAnnotation annotation : annotationList) {
                int imageWidth = imageSizes.get(annotation.filename).width + 10;
                int imageHeight = imageSizes.get(annotation.filename).height;
                totalImageWidth += imageWidth;
                maxImageHeight = Math.max(imageHeight, maxImageHeight);
            }
            h.add("<div style='width: " + totalImageWidth + "; height: " + (maxImageHeight + 10) + "; overflow: auto; white-space: nowrap;'>");
            int jc = 0;
            for (MsgAnnotation annotation : annotationList) {
                //h.add("<img src='" + annotation.filename +  "'>");
                Integer[] coordinates = new Integer[] {annotation.x, annotation.y, annotation.w, annotation.h};
                String coordargs = join(coordinates, ",");
                
                
                
                String canvasname = "c" + encmsgtext + "-" + jc;                
                int imageWidth = imageSizes.get(annotation.filename).width;
                int imageHeight = imageSizes.get(annotation.filename).height;
                //BufferedImage highlight = transparentRectangle(new Dimension(imageWidth, imageHeight), annotation.x, annotation.y, annotation.w, annotation.h);
                //String highlightData = "data:image/png;base64," + base64EncodeImage(highlight);
                String highlightData = imagesDir + imageWidth + "-" + imageHeight + "-" + annotation.x + "-" + annotation.y + "-" + annotation.w + "-" + annotation.h + ".png";
                
                if (!imagesDir.startsWith("http") && !new File("../" + highlightData).exists() && new File("../" + imagesDir).exists()) {
                    ImageIO.write(GImageUtils.transparentRectangle(new Dimension(imageWidth, imageHeight), annotation.x, annotation.y, annotation.w, annotation.h), "png", new File("../" + highlightData));
                }
                
                //if (!new File(highlightData).exists()) {
                //    BufferedImage highlight = transparentRectangle(new Dimension(imageWidth, imageHeight), annotation.x, annotation.y, annotation.w, annotation.h);
                //    ImageIO.write(highlight, "png", new File(highlightData));
                //}
                //j.add("annotate('" + canvasname + "', '" + annotation.filename + "', " + coordargs + ")");
                String filename = annotation.filename;
                while (count(filename, '/') > 1) {
                    filename = filename.substring(filename.indexOf('/') + 1);
                }
                h.add("<img id='" + canvasname + "' width=" + imageWidth + " height=" + imageHeight + " src='" + highlightData + "' style='background-image: url(\"" + filename + "\")'></img>");
                //h.add("<canvas id='" + canvasname + "' width=" + imageWidth + " height=" + imageHeight + "></canvas>");
                jc += 1;
            }
            h.add("</div>");
            h.add("<div style='width: " + totalImageWidth + "; height: 30; overflow: auto; white-space: nowrap;'>");
            int totalImgW = 0;
            jc = 0;
            for (MsgAnnotation annotation : annotationList) {
                int imageWidth = imageSizes.get(annotation.filename).width + 10;
                String id = "'q-" + encmsgtext + "-" + jc + "'";
                String style = " style='position: absolute; left: " + totalImgW + "' ";
                String style2 = " style='position: absolute; left: " + (totalImgW + 20) + "' ";
                h.add("<input type='checkbox' name=" + id + " id=" + id + style + totalImgW + "' /><label for=" + id + style2 + ">Image " + jc + " Matches</label>");
                totalImgW += imageWidth;
                jc += 1;
            }
            String id = "'qn-" + encmsgtext + "'";
            h.add("<input type='checkbox' name=" + id + " id=" + id + " style='position: absolute; left: " + totalImgW + "' /><label for=" + id + " style='position: absolute; left: " + (totalImgW + 20) + "; border: none'>None Match</div>");
            h.add("</div>");



            h.add("<b>" + msgtext + "</b><br/>");
            h.add(msgSourceConciseFromMsgIdBlock(blockText) + "<br/><br/>");
            //h.add("<input type='text' size=100 value='" + foreigntext + "' name='t-" + base64msgtext + "' />");
            //h.add("<p>" + blockTextMinusForeign.replace("\n", "<br/>").replace("~~~~~~", "~~~").replace("~~~", "<br/>#& ") + "</p>");
            i += 1;
        }
        List<String> o = new ArrayList<String>();
        o.add("<html>");
        o.add("<head>");
        /*
        o.add("<style type='text/css'>");
        o.add("div.viewport {");
        o.add("overflow: auto");
        o.add("white-space: nowrap");
        o.add("}");
        o.add("</style>");
        */
        /*
        o.add("<script type='application/javascript'>");
        
        //o.add("imageMap = {}");
        o.add("base64data = {} ");
        for (Pair<String, String> p : base64EncodedFiles) {
            //o.add("imageMap['" + p.Item1 + "'] = new Image()");
            //o.add("imageMap['" + p.Item1 + "'].src = 'data:image/png;base64," + p.Item2 + "'");
            //o.add("imageMap['" + p.Item1 + "'].src = '" + p.Item1 + "'");
            //o.add("base64data['" + p.Item1 + "'] = 'url(data:image/png;base64," + p.Item2 + ")'");
            o.add("base64data['" + p.Item1 + "'] = 'url(" + p.Item1 + ")'");
        }
        
        
        o.add("function annotate(canvasname, imagefile, x, y, w, h) {");
        o.add("var img = document.getElementById(canvasname)");
        o.add("img.style.backgroundImage = base64data[imagefile]");
        //o.add("var canvas = document.getElementById(canvasname)");
        //o.add("var ctx = canvas.getContext('2d')");
        //o.add("ctx.drawImage(imageMap[imagefile], 0, 0)");
        //o.add("ctx.fillStyle = 'rgba(0, 200, 200, 0.5)'");
        //o.add("ctx.fillRect (x, y, w, h)");
        o.add("}");
        
        o.add("window.onload = function() {");
        o.add(join(j, "\n"));
        o.add("}");
        o.add("</script>");
        */
        o.add("</head>");
        o.add("<body>");
        o.add("<form method='post' action='http://gkovacs.xvm.mit.edu:8080/textmatch-manual-labeled.jsp'>");
        o.add("<input type='submit' name='button' value='Download Annotated Message File' /><br/><br/>");
        o.add(auxtext);
        o.add(join(h, "\n"));
        o.add("</form>");
        o.add("</body>");
        o.add("</html>");
        return join(o, "\n");
    }
    
    public static String htmlFromAnnotations(List<Pair<List<MsgAnnotation>, String>> annotatedMsgBlocks, String auxtext) throws Exception {
        List<String> filenames = new ArrayList<String>();
        for (Pair<List<MsgAnnotation>, String> annotationAndText : annotatedMsgBlocks) {
            List<MsgAnnotation> annotationList = annotationAndText.Item1;
            for (MsgAnnotation annotation : annotationList) {
                if (!filenames.contains(annotation.filename))
                    filenames.add(annotation.filename);
            }
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
        //String base64AnnotatedMsgFile = DatatypeConverter.printBase64Binary(join(msgfilelines, "\n").getBytes());
        String urlencAnnotatedMsgFile = URLEncoder.encode(join(msgfilelines, "\n"), "UTF-8");
        String msgfilename = args[0];
        POMsgSource msgsrc = new POMsgSource(msgfilelines);
        
        String auxtext = "<input type='hidden' name='origmsgfile' value='" + urlencAnnotatedMsgFile + "'> \n" +
        "<input type='hidden' name='origmsgfilename' value='" + msgfilename + "'>";
        
        List<String> msgblocks = msgsrc.splitIntoMsgIdBlocks();
        
        List<Pair<List<MsgAnnotation>, String>> annotatedMsgBlocks = new ArrayList<Pair<List<MsgAnnotation>, String>>();
        HashSet<String> checkedMsgBlocks = new HashSet<String>();
        for (String x : msgblocks) {
            List<MsgAnnotation> checkedAnnotationList = checkedAnnotationListFromMsgIdBlock(x);
            if (checkedAnnotationList.size() != 0)
                continue;
            else if (noMatchesForMsgIdBlock(x))
                continue;
            
            List<MsgAnnotation> annotationList = annotationListFromMsgIdBlock(x);
            if (annotationList.size() == 0)
                continue;
            //String msgtext = textFromMsgIdBlock(x);
            annotatedMsgBlocks.add(makePair(annotationList, x));
        }
        //Collections.sort(annotatedMsgBlocks, new PairFirstComparator<MsgAnnotation>(new MsgAnnotationRegionComparator()));
        System.out.println(htmlFromAnnotations(annotatedMsgBlocks, auxtext));
    }
}
