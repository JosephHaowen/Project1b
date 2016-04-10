<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CS5300_Project1a_ht398</title>
</head>
<body>
<form action="forward" method="get">
netid:ht398</br>
session:<%=request.getAttribute("session") %></br>
version:<%=request.getAttribute("version") %></br>
expireTime:<%=request.getAttribute("expire") %></br>
message:<%=request.getAttribute("message") %></br>
</br>
<input type="submit" name="act" value="refresh"/></br>
<input type="submit" name="act" value="replace"/>
<input type="text" name="message"/></br>
<input type="submit" name="act" value="logout"/></br>

cookie:<%=request.getAttribute("cookie") %>
</form>
</body>
</html>