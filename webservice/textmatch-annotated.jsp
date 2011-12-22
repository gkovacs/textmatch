<%@ page import="java.util.*" %>
   <%@ page import="java.util.*" %>
   <%@ page import="java.io.File" %>
   <%@ page import="java.io.BufferedInputStream" %>
   <%@ page import="java.io.InputStream" %>
   <%@ page import="java.io.ByteArrayInputStream" %>
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

String base64origmsg = request.getParameter("origmsgfile");
String origmsgfilename = request.getParameter("origmsgfilename");

response.setContentType("text/plain");
response.setHeader("Content-Disposition", "attachment;filename=annotated-" + origmsgfilename);

byte[] origmsgB = DatatypeConverter.parseBase64Binary(base64origmsg);

String origmsg = new String(origmsgB, "UTF8");

POMsgSource msgsrc = new POMsgSource(new ByteArrayInputStream(origmsgB));

List<String> msgblocks = msgsrc.splitIntoMsgIdBlocks();

for (int i = 0; i < msgblocks.size(); ++i) {
String msgblock = msgblocks.get(i);
String msgtext = msgsrc.textFromMsgIdBlock(msgblock);
String withoutforeigntext = msgsrc.excludeForeignMsgStr(msgblock);

byte[] msgtextB = msgtext.getBytes("UTF-8");
String base64msgtext = DatatypeConverter.printBase64Binary(msgtextB);

String foreigntext = request.getParameter("t-" + base64msgtext);
if (foreigntext == null) {
out.println(msgblock);
} else {
out.println(withoutforeigntext + "\nmsgstr \"" + foreigntext + "\"\n" );
}
}

//out.println(origmsg);

%>
