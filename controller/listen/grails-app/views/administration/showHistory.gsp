<%--
  Created by IntelliJ IDEA.
  User: bjohnston
  Date: 12/15/2016
  Time: 1:42 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
</head>

<body>

<div class="bean">
    <table id="twoColumns">
        <tbody>
        <tr>
            <th><g:message code="callHistory.startDate.label"/></th>
            <td><ii:formatDate style="long" date="${callHistory.dateTime.toDate()}"/></td>
        </tr>

</div>

</body>
</html>