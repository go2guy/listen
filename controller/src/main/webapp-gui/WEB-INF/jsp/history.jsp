<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>History</title>
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-history-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/history-min.css"/>">

    <meta name="body-class" content="application-history"/>
    <meta name="page-title" content="History"/>
  </head>
  <body>
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