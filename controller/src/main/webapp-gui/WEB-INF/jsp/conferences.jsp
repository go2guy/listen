<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>Conference List</title>
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-conference-list-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/conferences-min.css"/>">
    <meta name="body-class" content="application-conferences"/>
    <meta name="page-title" content="Conference List"/>
  </head>
  <body>
    <table id="conference-list-table" class="data-table">
      <thead>
        <tr>
          <th>Description</th>
          <th>Status</th>
          <th>Callers</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr class="placeholder"><td colspan="4">No conferences</td></tr>
      </tbody>
    </table>
    <div class="pagination" id="conference-list-pagination">
      <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
    </div>
    
    <table class="templates">
      <tbody>
        <tr id="conference-row-template">
          <td class="conference-cell-description"></td>
          <td class="conference-cell-status"></td>
          <td class="conference-cell-callerCount"></td>
          <td class="conference-cell-view"></td>
        </tr>
      </tbody>
    </table>
  </body>
</html>