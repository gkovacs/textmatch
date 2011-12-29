<%@ page import="java.util.*" %>
   <%@ page import="java.util.*" %>
   <%@ page import="java.io.OutputStream" %>
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


response.setHeader("Cache-Control", "max-age=315360000,public");

String dim = request.getParameter("dim");
dim = dim.substring(0, dim.length() - 4);
String[] s = dim.split("-");
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

response.setContentType("image/png");
OutputStream os = response.getOutputStream();
ImageIO.write(img, "png", os);
os.close();

%>
