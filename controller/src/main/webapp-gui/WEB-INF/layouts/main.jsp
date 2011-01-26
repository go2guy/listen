<%@ taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<%@ page import="com.interact.listen.HibernateUtil" %>
<%@ page import="com.interact.listen.license.License" %>
<%@ page import="com.interact.listen.license.ListenFeature" %>
<%@ page import="com.interact.listen.resource.Subscriber" %>
<%@ page import="com.interact.listen.resource.Voicemail" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<decorator:usePage id="decoratedPage"/> 
<html>
  <head>
    <title>Listen - <decorator:title/> - Interact Incorporated</title>
    <link rel="SHORTCUT ICON" href="./resources/app/images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/yui-2.8.0r4/reset-fonts/reset-fonts.css"/>">
    <link rel="stylesheet" type="text/css" href="./resources/jquery/skin/css/custom-theme/jquery-ui-1.8.2.custom.css">
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/listen-min.css"/>">

    <% /* CONTEXT is used by javascript to build correct links to application servlets */ %>
    <script type="text/javascript">
var CONTEXT = '<%= request.getContextPath() %>';
    </script>

    <script type="text/javascript" src="./resources/jquery/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="./resources/jquery/jquery-ui-1.8rc3.custom.min.js"></script>
    <script type="text/javascript" src="./resources/jquery/plugins/jquery.simplemodal-1.3.5.min.js"></script>
    <script type="text/javascript" src="./resources/json.org/json2-min.js"></script>
    
    <!-- application javascript -->
    <script type="text/javascript" src="./resources/app/js/util-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/listen-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/server-min.js"></script>
    <decorator:head/>
  </head>
  <body class="<%= decoratedPage.getProperty("meta.body-class") %>"><%
/* FIXME replace this with a tag */
Subscriber subscriber = (Subscriber)session.getAttribute("subscriber"); %>
    <div id="container" class="clearfix">
      <ul id="user-info">
        <li class="first"><%= subscriber.friendlyName() %></li>
        <li><a href="<listen:resource path="/settings"/>">Settings</a></li>
        <li><a href="<listen:resource path="/logout"/>">Logout</a></li>
      </ul>

      <ul id="user-menu">
        <listen:ifLicensed feature="CONFERENCING">
          <li id="menu-conferencing"><a href="<listen:resource path="/conferencing"/>">Conferencing</a></li>
        </listen:ifLicensed>
        <listen:ifLicensed feature="VOICEMAIL"><%
/* this is super-gross! */
Long newVoicemailCount = Voicemail.countNewBySubscriber(HibernateUtil.getSessionFactory().getCurrentSession(), subscriber); %>
          <li id="menu-voicemail"><a href="<listen:resource path="/voicemail"/>">Voicemail (<span id="voicemail-new-count"><%= newVoicemailCount %></span>)</a></li>
        </listen:ifLicensed>
        <listen:ifLicensed feature="FINDME">
          <li id="menu-findme"><a href="<listen:resource path="/findme"/>">Find Me / Follow Me</a></li>
        </listen:ifLicensed><%
if(subscriber.getIsAdministrator()) { %>
        <listen:ifLicensed feature="ATTENDANT">
          <li id="menu-attendant"><a href="<listen:resource path="/attendant"/>">Attendant</a></li>
        </listen:ifLicensed>
        <li id="menu-configuration"><a href="<listen:resource path="/configuration"/>">Configuration</a></li>
        <listen:ifLicensed feature="CONFERENCING">
          <li id="menu-conferences"><a href="<listen:resource path="/conferences"/>">Conferences</a></li>
        </listen:ifLicensed>
        <li id="menu-subscribers"><a href="<listen:resource path="/subscribers"/>">Subscribers</a></li>
        <li id="menu-acdgroups"><a href="<listen:resource path="/acdgroups"/>">ACD Groups</a></li>
        <li id="menu-history"><a href="<listen:resource path="/history"/>">History</a></li><%
} %>
      </ul>

      <div id="content" class="clearfix">
        <h1 id="page-title" class="clearfix"><%= decoratedPage.getProperty("meta.page-title") %></h1>
        <decorator:body/>
      </div>
      
      <div id="footer">
        Listen &copy;2010 Interact Incorporated, <a href="http://www.iivip.com/">iivip.com</a>
        </div>
    </div>

    <div id="pinglatency"></div>
    <div id="latency"></div>
    <div id="modal-overlay"></div>
  </body>
</html>
