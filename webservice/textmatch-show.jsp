<%@ page import="java.util.*" %>
   <%@ page import="java.util.*" %>
   <%@ page import="java.io.File" %>
   <%@ page import="java.io.BufferedInputStream" %>
   <%@ page import="java.io.InputStream" %>
   <%@ page import="org.apache.commons.fileupload.servlet.*" %>
   <%@ page import="org.apache.commons.fileupload.disk.*"%>
   <%@ page import="org.apache.commons.fileupload.servlet.*"%>
   <%@ page import="org.apache.commons.fileupload.disk.*"%>
   <%@ page import="org.apache.commons.fileupload.*"%>
   <%@ page import="javax.imageio.*"%>
   <%@ page import="java.awt.image.BufferedImage"%>
   <%@ page import="javax.imageio.stream.MemoryCacheImageInputStream" %>
   <%@ page import="javax.imageio.stream.ImageInputStream" %>
   <%@ page import="javax.xml.bind.DatatypeConverter" %>
   <%@ page import="textmatch.*" %>
   <%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
//System.setProperty("java.awt.headless", "false");
int maxMemSize = 50 * 1024 * 1024; // 50 MB
boolean isMultipart = ServletFileUpload.isMultipartContent(request);
DiskFileItemFactory factory = new DiskFileItemFactory();
factory.setSizeThreshold(maxMemSize);
ServletFileUpload upload = new ServletFileUpload(factory);
upload.setSizeMax(maxMemSize);

List fileItems = upload.parseRequest(request);

List<BufferedImage> images = new ArrayList<BufferedImage>();
List<String> base64EncodedImages = new ArrayList<String>();
List<String> imageNames = new ArrayList<String>();

POMsgSource msgsrc = null;

Iterator iter = fileItems.iterator();
while (iter.hasNext()) {

FileItem item = (FileItem)iter.next();

String filename = item.getName();
if (filename == null) continue;
int dotIdx = filename.lastIndexOf(".");
if (dotIdx == -1) continue;

InputStream fileStream = new BufferedInputStream(item.getInputStream());

String extension = filename.substring(dotIdx + 1);
if (extension.equals("po") || extension.equals("pot")) {
msgsrc = new POMsgSource(fileStream);
continue;
} else if (!extension.equals("png")) {
continue;
}


ImageInputStream imageStream = new MemoryCacheImageInputStream(fileStream);
//out.println(fileStream == null);

BufferedImage bi = null;

bi = ImageIO.read(imageStream);

/*
Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName("png");
while (bi == null && imageReaders.hasNext()) {
ImageReader imageReader = (ImageReader)imageReaders.next();
try {
imageReader.setInput(imageStream, true);
ImageReadParam param = imageReader.getDefaultReadParam();
bi = imageReader.read(0, param);
} catch (Exception e) {
    continue;
}
    
}
*/
if (bi == null) {
continue;
}

images.add(bi);

byte[] fileData = item.get();
String base64data = DatatypeConverter.printBase64Binary(fileData);

base64EncodedImages.add(base64data);
//out.println(bi.getWidth());
imageNames.add(filename);

//out.println("<img src=\"data:image/png;base64," + base64data + "\" </img>");

}



if (msgsrc == null) {
out.println("need to supply a message (po) file");
return;
}


List<String> msgstrings = new ArrayList<String>();
HashMap<String, String> msgstringToBlock = new HashMap<String, String>();
for (String x : msgsrc.splitIntoMsgIdBlocks()) {
String msgstr = msgsrc.textFromMsgIdBlock(x);
if (msgstr.equals("")) continue;
msgstrings.add(msgstr);
msgstringToBlock.put(msgstr, x);
}

Collections.sort(msgstrings, new StringLengthComparator());
Collections.reverse(msgstrings);
List<List<ImgMatch>> matchesAcrossImages = new ArrayList<List<ImgMatch>>();
for (int i = 0; i < imageNames.size(); ++i) {
    matchesAcrossImages.add(Main.getImgMatches(images.get(i), imageNames.get(i)));
}

HashMap<String, MsgAnnotation> annotations = Main.msgToAnnotations(msgstrings, matchesAcrossImages);
HashMap<String, MsgAnnotation> annotatedBlocks = new HashMap<String, MsgAnnotation>();
for (String x : annotations.keySet()) {
String blockText = msgstringToBlock.get(x);
MsgAnnotation annotation = annotations.get(x);
annotatedBlocks.put(blockText, annotation);
}

List<Pair<String, String>> base64EncodedFiles = new ArrayList<Pair<String, String>>();
for (int i = 0; i < imageNames.size(); ++i) {
    base64EncodedFiles.add(new Pair<String, String>(imageNames.get(i), base64EncodedImages.get(i)));
}

out.println(HTMLGen.htmlFromAnnotations(annotatedBlocks, base64EncodedFiles));

%>
