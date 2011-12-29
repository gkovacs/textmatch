<%@ page import="java.util.*" %>
   <%@ page import="java.util.*" %>
   <%@ page import="java.io.File" %>
   <%@ page import="java.io.BufferedInputStream" %>
   <%@ page import="java.io.InputStream" %>
   <%@ page import="java.io.ByteArrayOutputStream" %>
   <%@ page import="java.io.ByteArrayInputStream" %>
   <%@ page import="java.io.PrintWriter" %>
   <%@ page import="java.net.URLDecoder" %>
   <%@ page import="java.net.URLEncoder" %>
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

ByteArrayOutputStream byteout = new ByteArrayOutputStream();

PrintWriter pw = new PrintWriter(byteout);

//String base64origmsg = request.getParameter("origmsgfile");
String encorigmsg = request.getParameter("origmsgfile");
String origmsgfilename = request.getParameter("origmsgfilename");

String newmsgfilename = origmsgfilename;
if (newmsgfilename.contains("/"))
  newmsgfilename = newmsgfilename.substring(newmsgfilename.lastIndexOf("/") + 1);
if (newmsgfilename.contains(".")) {
  String extension = newmsgfilename.substring(newmsgfilename.indexOf("."));
  newmsgfilename = newmsgfilename.substring(0, newmsgfilename.indexOf("."));
  if (newmsgfilename.contains("-")) {
    try {
      int curval = Integer.parseInt(newmsgfilename.substring(newmsgfilename.lastIndexOf("-") + 1));
      String sansnum = newmsgfilename.substring(0, newmsgfilename.lastIndexOf("-"));
      newmsgfilename = sansnum + "-" + (curval + 1) + extension;
    } catch (Exception e) {
      newmsgfilename = newmsgfilename + "-1" + extension;
    }
  } else {
    newmsgfilename = newmsgfilename + "-1" + extension;
  }
}

response.setContentType("text/plain");
response.setHeader("Content-Disposition", "attachment;filename=" + newmsgfilename);

//byte[] origmsgB = DatatypeConverter.parseBase64Binary(base64origmsg);

//String origmsg = new String(origmsgB, "UTF8");

String origmsg = URLDecoder.decode(encorigmsg, "UTF-8");

//POMsgSource msgsrc = new POMsgSource(new ByteArrayInputStream(origmsgB));

POMsgSource msgsrc = new POMsgSource(origmsg);

List<String> msgblocks = msgsrc.splitIntoMsgIdBlocks();

for (int i = 0; i < msgblocks.size(); ++i) {
String msgblock = msgblocks.get(i);

String msgtext = msgsrc.textFromMsgIdBlock(msgblock);
String withoutforeigntext = msgsrc.excludeForeignMsgStr(msgblock);
String withoutManualAnnotation = msgsrc.excludeManualAnnotation(msgblock);

List<MsgAnnotation> annotationList = null;
try {
annotationList = msgsrc.annotationListFromMsgIdBlock(msgblock);
} catch (Exception e) {
throw new RuntimeException(msgblock);
}

//byte[] msgtextB = msgtext.getBytes("UTF-8");
//String base64msgtext = DatatypeConverter.printBase64Binary(msgtextB);
String encmsgtext = URLEncoder.encode(msgtext, "UTF-8");

String matchedNothing = request.getParameter("qn-" + encmsgtext);
if (matchedNothing != null) {
pw.println("#~ nomatches");
} else {
for (int jc = 0; jc < 10; ++jc) {
String selected = request.getParameter("q-" + encmsgtext + "-" + jc);
if (selected == null) continue;
if (annotationList.size() == 0) continue;
pw.println("#~ " + annotationList.get(jc));
}
}
pw.println(withoutManualAnnotation);

}

pw.flush();
response.setContentLength(byteout.size());
byteout.writeTo(response.getOutputStream());

%>
