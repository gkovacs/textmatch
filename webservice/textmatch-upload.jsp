<html>
<body>
<%
/*
String name = request.getParameter("password");
if (!name.toLowerCase().equals("geza kovacs")) {
out.println("sorry");
return;
}
*/
%>
<form action="textmatch-show.jsp" method="post" enctype="multipart/form-data">

  Message (po/pot) file:
  <input type="file" name="first" />

<br/>
  Image (png) files: 
  <input type="file" name="second" accept="image/png" multiple="" />
  
  <br />
  <input type="submit" name="button" value="Upload" />
  
</form>
</body>
</html>
