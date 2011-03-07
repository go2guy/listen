<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>PBX</title>
    
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-pbx-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/lib/forms.css"/>">
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/pbx-min.css"/>">

    <meta name="body-class" content="application-pbx"/>
    <meta name="page-title" content="PBX"/>
  </head>
  <body>
    <div class="tab-container">
      <ul class="tabs">
        <li><a href="#">Calling Restrictions</a></li>
      </ul>

      <div class="tab-content-default">
        <form>
          <fieldset id="page-buttons">
            <button type="submit" class="button-save">Save</button>
            <button type="button" class="button-add" id="new-restriction">Add Another Restriction</button>
          </fieldset>
        </form>
      </div>
    </div>

    <fieldset id="template" class="restriction">
      <button type="button" class="button-delete delete-restriction">Delete</button>
          
      <label class="first">
        <select class="directive">
          <option value="deny">Deny</option>
          <option value="allow">Allow</option>
        </select>
      </label>

      <label>
        destination number
        <input type="text" class="destination"/>
      </label>

      <label>
        for
        <select class="target">
          <option value="Everyone">Everyone</option>
          <option value="Subscribers">Subscriber(s)</option>
        </select>
      </label>

      <button type="button" class="button-add more-subscribers">More Subscribers</button>
    </fieldset>
  </body>
</html>