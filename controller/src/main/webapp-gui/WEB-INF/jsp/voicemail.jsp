<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>Voicemail</title>
    <script type="text/javascript" src="./resources/app/js/app-voicemail-min.js"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/voicemail-min.css"/>">
    
    <meta name="body-class" content="application-voicemail"/>
    <meta name="page-title" content="Voicemail"/>
  </head>
  <body>
  
    <ol id="voicemail-header" class="clearfix">
      <li class="buttons"></li>
      <li class="from"><a href="#">From</a> <span></span></li>
      <li class="received"><a href="#">Received</a> <span></span></li>
      <li class="play"><label for="bubble-new">New voicemails on top</label> <input type="checkbox" id="bubble-new"/></li>
    </ol>
  
    <ol id="voicemail-list" class="clearfix">
        <li class="placeholder">You do not have any voicemail</li>
    </ol>
  
    <div id="pagination" class="pagination">
      <a href="#">Prev</a>
      <span class="pagination-current">0-0</span>of<span class="pagination-total">0</span>
      <a href="#">Next</a>
    </div>
    
    <ul class="templates">
      <li id="voicemail-template" class="new-voicemail">
        <ol class="clearfix">
          <li class="buttons">
            <button type="button" class="icon-delete" title="Delete"></button>
            <button type="button" class="icon-unread"></button>
            <button type="button" class="icon-download" title="Download"></button>
          </li>
          <li class="from"></li>
          <li class="received"></li>
          <li class="play"></li>
          <li class="transcription">
            <div class="transcription-bubble"></div>
          </li>
        </ol>
      </li>
    </ul>
  </body>
</html>