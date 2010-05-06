<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.interact.listen.resource.User" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title>Listen</title>
    <link rel="SHORTCUT ICON" href="./resources/app/images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="./resources/yui-2.8.0r4/reset-fonts/reset-fonts.css">
    <link rel="stylesheet" type="text/css" href="./resources/jquery/css/excite-bike/jquery-ui-1.8rc3.custom.css">
<!--    <link rel="stylesheet" type="text/css" href="./resources/app/common-min.css">-->
    <link rel="stylesheet" type="text/css" href="./resources/app/all-min.css">
    <link rel="stylesheet" type="text/css" href="./resources/app/index-min.css">
    <script type="text/javascript" src="./resources/jquery/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="./resources/jquery/jquery-ui-1.8rc3.custom.min.js"></script>
    <script type="text/javascript" src="./resources/app/index-min.js"></script>
  </head>
  <body>
    <div id="wrapper">
      <div id="wrapper-main">
<%
User user = (User)session.getAttribute("user");
%>
        <div id="header">
          <div id="logo"><img src="resources/app/images/new/listen_logo_50x24.png"/></div>
          <div id="userInfo">Hi, <%= user.getUsername() %>! | <a href="/logout" id="logoutButton" name="logoutButton">Logout</a></div>
        </div>
<%
if(user != null && user.getIsAdministrator()) {
%>
        <div id="subheader">
          <button id="create-new-account-button" class="add-button">Create New Account</button>
        </div>
<%
}
%>
        <div id="notification"></div>
        <div id="main">
          <div id="conference-window" class="window">
            <div class="conference-header">
              <div id="conference-title" class="conference-title">Conference ###</div>
              <div class="conference-menu">
                <div id="conference-status-icon"></div>
                <div id="conference-status-message"></div>
                <div class="buttons"><button id="schedule-button" class="schedule-button">Schedule</button></div>
              </div>
            </div>
            <div class="left">
              <div id="conference-callers" class="panel">
                <div class="panel-header">
                  <div class="panel-title">Current Callers (<span id="conference-caller-count">0</span>)</div>
                  <div class="panel-menu">
                    <button class="add-button" readonly="readonly" disabled="disabled">Add</button>
                  </div>
                </div>
                <div class="panel-content">
                  <ul id="caller-list"></ul>
                </div>
              </div>
            </div>
            <div class="right">
              <div id="conference-pins" class="panel">
                <div class="panel-header">
                  <div class="panel-title">Pins (<span id="conference-pin-count">0</span>)</div>
                  <div class="panel-menu">
                    <button class="add-button" readonly="readonly" disabled="disabled">Add</button>
                  </div>
                </div>
                <div class="panel-content">
                  <ul id="pin-list"></ul>
                </div>
              </div>
              <div id="conference-history" class="panel">
                <div class="panel-header">
                  <div class="panel-title">Recent History</div>
                </div>
                <div class="panel-content">
                  <ul id="history-list"></ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div><!-- wrapper-main -->
    </div><!-- wrapper -->
    <div id="footer"><!--
      Listen &copy;2010 <a href="http://www.iivip.com">Interact Incorporated</a> | <a href="#">Terms of Use</a>-->
    </div>

    <div id="templates">
      <li id="caller-row-template" class="caller-row">
        <div class="caller-user-icon"></div>
        <div class="caller-number"></div>
        <div class="caller-drop-icon"></div>
        <div class="caller-mute-icon"></div>
      </li>
      <li id="history-row-template" class="history-row">
        <div class="history-content"></div>
      </li>
      <li id="pin-row-template" class="pin-row">
        <div class="pin-number"></div>
        <div class="pin-type"></div>
        <div class="pin-remove"></div>
      </li>
    </div>

<%
if(user != null && user.getIsAdministrator()) {
%>
    <div id="provisionAccountDialog" class="dialog">
      <div class="form-error-message"></div>
      <form id="provisionAccountForm" name="provisionAccountForm" method="POST" action="/provisionAccount">
        <table>
          <caption>Creates a new subscriber, user, and conference</caption>
          <tbody>
            <tr>
              <th><label for="number">Subscriber Number</label></th>
              <td><input type="text" id="provisionAccountNumber" name="provisionAccountNumber"/></td>
            </tr>
            <tr>
              <th><label for="provisionAccountUsername">Username</label></th>
              <td><input type="text" id="provisionAccountUsername" name="provisionAccountUsername"/></td>
            </tr>
            <tr>
              <th><label for="provisionAccountPassword">Password</label></th>
              <td><input type="password" id="provisionAccountPassword" name="provisionAccountPassword"/></td>
            </tr>
            <tr>
              <th><label for="provisionAccountPasswordConfirm">Confirm Password</label></th>
              <td><input type="password" id="provisionAccountPasswordConfirm" name="provisionAccountPasswordConfirm"/></td>
            </tr>
            <tr>
              <td colspan="2" class="buttons"><input type="submit" id="provisionAccountSubmit" name="provisionAccountSubmit" value="Create New Account"/></td>
            </tr>
          </tbody>
        </table>
      </form>
    </div>
<%
}
%>

    <div id="scheduleConferenceDialog" class="dialog">
      <div class="form-error-message"></div>
      <form id="scheduleConferenceForm" name="scheduleConferenceForm" method="POST" action="/scheduleConference">
        <table>
          <caption>
          This will send an email to the specified recipients with a date, time, phone number, and pin number.
          </caption>
          <tbody>
            <tr>
              <th><label for="scheduleConferenceDate">Date & Time</label></th>
              <td>
                <input type="text" id="scheduleConferenceDate" name="scheduleConferenceDate"/><br/>
                <select id="scheduleConferenceTimeHour" name="scheduleConferenceTimeHour">
                  <option value="1">1</option>
                  <option value="2">2</option>
                  <option value="3">3</option>
                  <option value="4">4</option>
                  <option value="5">5</option>
                  <option value="6">6</option>
                  <option value="7">7</option>
                  <option value="8">8</option>
                  <option value="9">9</option>
                  <option value="10">10</option>
                  <option value="11">11</option>
                  <option value="12">12</option>
                </select>
                <select id="scheduleConferenceTimeMinute" name="scheduleConferenceTimeMinute">
                  <option value="00">00</option>
                  <option value="15">15</option>
                  <option value="30">30</option>
                  <option value="45">45</option>
                </select>
                <select id="scheduleConferenceTimeAmPm" name="scheduleConferenceTimeAmPm">
                  <option value="AM">AM</option>
                  <option value="PM">PM</option>
                </select>
              </td>
            </tr>
            <tr>
              <th><label for="scheduleConferenceActiveParticipants">Active caller email addresses (comma-separated):</label></th>
              <td><textarea id="scheduleConferenceActiveParticipants" name="scheduleConferenceActiveParticipants"></textarea></td>
            </tr>
            <tr>
              <th><label for="scheduleConferencePassiveParticipants">Passive caller email addresses (comma-separated):</label></th>
              <td><textarea id="scheduleConferencePassiveParticipants" name="scheduleConferencePassiveParticipants"></textarea></td>
            </tr>
            <tr>
              <th><label for="scheduleConferenceDescription">Memo to include in email</label></th>
              <td><textarea id="scheduleConferenceDescription" name="scheduleConferenceDescription"></textarea>
            </tr>
            <tr>
              <td colspan="2" class="buttons"><input type="submit" id="scheduleConferenceSubmit" name="scheduleConferenceSubmit" value="Schedule Conference"/></td>
            </tr>
          </tbody>
        </table>
      </form>
    </div>
  </body>
</html>