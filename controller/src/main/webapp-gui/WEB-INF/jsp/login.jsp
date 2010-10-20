<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title>Listen</title>
    <link rel="SHORTCUT ICON" href="./resources/app/images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="./resources/yui-2.8.0r4/reset-fonts/reset-fonts.css">
    <link rel="stylesheet" type="text/css" href="./resources/app/css/all-min.css">
    <link rel="stylesheet" type="text/css" href="./resources/app/css/login-min.css">
    <script type="text/javascript" src="./resources/jquery/jquery-1.4.2.min.js"></script>
  </head>
  <body>
    <div id="left">
      <div id="logo"><img src="./resources/app/images/new/listen_logo_172x73.png"/></div>
    </div>
    <div id="right">
      <div id="login">
        <form id="loginForm" name="loginForm" method="post" action="<%= request.getContextPath() %>/login">
          <fieldset> 
<%
Map<String, String> errors = (Map<String, String>)request.getAttribute("errors");
if(errors == null)
{
    errors = new HashMap<String, String>();
}
String username = (String)request.getAttribute("username");
if(username == null)
{
    username = "";
}
%>
            <label for="username">Username</label>
            <input type="text" id="username" name="username" class="text<%= errors.containsKey("username") ? " error" : "" %>" value="<%= username %>"/>
<%
if(errors.containsKey("username"))
{
%>
            <div class="error"><div class="exclamation">!</div><%= errors.get("username") %></div>
<%
}
%>
            <label for="password">Password</label>
            <input type="password" id="password" name="password" class="text<%= errors.containsKey("username") ? " error" : "" %>"/>
<%
if(errors.containsKey("password"))
{
%>
            <div class="error"><div class="exclamation">!</div><%= errors.get("password") %></div>
<%
}
%>
            <input class="button" type="submit" id="loginButton" name="loginButton" value="Login"/>
          </fieldset>
        </form>
      </div>
    </div>
    <script type="text/javascript">$('#username').focus();</script>
  </body>
</html>
