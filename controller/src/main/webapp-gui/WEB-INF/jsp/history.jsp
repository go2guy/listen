<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<%@ page import="com.interact.listen.ServletUtil" %>
<%@ page import="com.interact.listen.resource.Subscriber" %>
<html>
  <head>
    <title>History</title>
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-history-min.js"/>"></script><%
Subscriber subscriber = ServletUtil.currentSubscriber(request);
/* this is ugly. the ideal solution would be to have a separate history-admin.js file that
   adds on the filtering functionality to the regular history application javascript. */ %>

    <script type="text/javascript">var allowFilter = <%= subscriber.getIsAdministrator() ? "true" : "false" %>;</script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/history-min.css"/>">

    <meta name="body-class" content="application-history"/>
    <meta name="page-title" content="History"/>
  </head>
  <body><%
if(subscriber.getIsAdministrator()) { %>
    <fieldset>
        Filter by name <input type="text" id="username-filter"/> <button type="button" class="button-save" id="filter-submit">Filter</button><button type="button" class="button-delete" id="filter-clear">Clear</button>
    </fieldset><%
} %>
    <ul id="history-list">
      <li class="placeholder">No history records</li>
    </ul>
    <div class="pagination" id="history-pagination">
      <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
    </div>
    <div class="cleaner">&nbsp;</div>
    
    <ul class="templates">
      <li id="history-call-template" class="history-row-unknown">
        <div class="history-call-date" title="Call start date"></div>
        <div class="history-call-type" title="Call history">Call</div>
        <div class="history-call-subscriber"></div>
        <div class="history-call-description"></div>
        <div class="history-call-duration" title="Duration"></div>
      </li>
      <li id="history-action-template" class="history-row-unknown">
        <div class="history-action-date" title="Date"></div>
        <div class="history-action-type" title="Action history">Action</div>
        <div class="history-action-subscriber"></div>
        <div class="history-action-description"></div>
        <div class="history-action-onSubscriber"></div>
      </li>
    </ul>
  </body>
</html>