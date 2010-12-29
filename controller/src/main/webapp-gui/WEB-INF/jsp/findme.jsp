<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>Find Me / Follow Me</title>
    <script type="text/javascript" src="./resources/app/js/app-findme-min.js"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/findme-min.css"/>">
    
    <meta name="body-class" content="application-findme"/>
    <meta name="page-title" content="Find Me / Follow Me"/>
  </head>
  <body>
    <div class="help">
      <b>Find Me / Follow Me</b> lets you control how you're contacted when someone tries to call you. You can have the call forwarded to several numbers at once, different numbers in succession, or any combination of both.
    </div>

    <div class="when-somebody-calls">When somebody calls me,</div>

    <div class="if-i-dont-answer">
      <span>If I don't answer</span>
      <select>
        <option selected="selected" value="voicemail">Send the caller to my voicemail</option>
        <option value="dial">Dial...</option>
      </select>
    </div>

    <div class="save-button-container">
      <button class="button-save" id="findme-save">Save</button>
    </div>
  </body>
</html>