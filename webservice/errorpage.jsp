<%@ page import="java.util.*" %>
   <%@ page import="java.util.*" %>
   <%@ page import="java.io.File" %>
   <%@ page import="java.io.BufferedInputStream" %>
   <%@ page import="java.io.InputStream" %>
   <%@ page import="java.io.OutputStream" %>
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

//String tmpdir = ((File)getServletContext().getAttribute("javax.servlet.context.tempdir")).getPath();

String requestedURL = (String)request.getAttribute("javax.servlet.forward.request_uri");
if (requestedURL != null && requestedURL.startsWith("/gimgs/") && requestedURL.endsWith(".png")) {
String basepath = requestedURL.substring(7);
String[] s = basepath.substring(0, basepath.length() - 4).split("-");
if (s.length == 6) {
int iw = Integer.parseInt(s[0]);
int ih = Integer.parseInt(s[1]);
int sx = Integer.parseInt(s[2]);
int sy = Integer.parseInt(s[3]);
int w = Integer.parseInt(s[4]);
int h = Integer.parseInt(s[5]);

BufferedImage img = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
for (int x = sx; x < sx + w; ++x) {
    for (int y = sy; y < sy + h; ++y) {
        img.setRGB(x, y, 0x5500C8C8);
    }
}

response.setStatus(200);
response.setContentType("image/png");
OutputStream os = response.getOutputStream();
ImageIO.write(img, "png", os);
os.close();


//response.setStatus(200);
//out.println(tmpdir + basepath);

ImageIO.write(img, "png", new File("/tmp/" + basepath));

} else {
out.println("wrong num args ");
}
} else {
out.println("not found");
}


%>
