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
    <link rel="stylesheet" type="text/css" href="./resources/app/common-min.css">
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
          <div id="userInfo">Hi, <%= user.getUsername() %>! | <a href="#">Settings</a> | <a href="/logout" id="logoutButton" name="logoutButton">Logout</a></div>
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
        <div id="main">
          <div id="conference-window" class="window">
            <div class="conference-header">
              <div id="conference-title" class="conference-title">Conference ###</div>
              <div class="conference-menu"><button id="schedule-button" class="schedule-button">Schedule</button></div>
            </div>
            <div class="left">
              <div id="conference-callers" class="panel">
                <div class="panel-title">Current Callers (<span id="conference-caller-count">0</span>)</div>
                <div class="panel-menu">
                  <button class="add-button" readonly="readonly" disabled="disabled">Add</button>
                </div>
                <div class="panel-content">
                  <ul id="caller-list"></ul>
                </div>
              </div>
            </div>
            <div class="right">
              <div id="conference-pins" class="panel">
                <div class="panel-title">Pins (<span id="conference-pin-count">0</span>)</div>
                <div class="panel-menu">
                  <button class="add-button" readonly="readonly" disabled="disabled">Add</button>
                </div>
                <div class="panel-content">
                  <ul id="pin-list"></ul>
                </div>
              </div>
              <div id="conference-history" class="panel">
                <div class="panel-title">Recent History</div>
                <div class="panel-content">
                  <ul id="history-list"></ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div><!-- wrapper-main -->
    </div><!-- wrapper -->
    <div id="footer">
      Listen &copy;2010 <a href="http://www.iivip.com">Interact Incorporated</a> | <a href="#">Terms of Use</a>
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
      <form id="provisionAccountForm" name="provisionAccountForm" method="POST" action="/provisionAccount">
        <fieldset>
          <div class="errors" style="display: none;"></div>
          <label for="number">Subscriber Number
            <input type="text" id="provisionAccountNumber" name="provisionAccountNumber"/>
          </label>
          <label for="provisionAccountUsername">Username
            <input type="text" id="provisionAccountUsername" name="provisionAccountUsername"/>
          </label>
          <label for="provisionAccountPassword">Password
            <input type="password" id="provisionAccountPassword" name="provisionAccountPassword"/>
          </label>
        </fieldset>

        <fieldset class="buttons">
          <input type="submit" id="provisionAccountSubmit" name="provisionAccountSubmit" value="Create New Account"/>
        </fieldset>
      </form>
    </div>
<%
}
%>

    <div id="scheduleConferenceDialog" class="dialog">
      <form id="scheduleConferenceForm" name="scheduleConferenceForm" method="POST" action="/scheduleConference">
        <fieldset>
          <div class="errors" style="display: none;"></div>
          <label for="scheduleConferenceDate">Date
            <input type="text" id="scheduleConferenceDate" name="scheduleConferenceDate"/>
          </label>
          <label for="scheduleConferenceTime">Time
            <div id="scheduleConferenceTime" name="scheduleConferenceTime" style="display: inline">
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
            </div>
          </label>
          <label for="scheduleConferenceDescription">Description
            <input type="text" id="scheduleConferenceDescription" name="scheduleConferenceDescription"/>
          </label>
          <label for="scheduleConferenceActiveParticipants">Active Participants
            <input type="text" id="scheduleConferenceActiveParticipants" name="scheduleConferenceActiveParticipants"/>
          </label>
          <label for="scheduleConferencePassiveParticipants">Passive Participants
            <input type="text" id="scheduleConferencePassiveParticipants" name="scheduleConferencePassiveParticipants"/>
          </label>
        </fieldset>

        <fieldset class="buttons">
          <input type="submit" id="scheduleConferenceSubmit" name="scheduleConferenceSubmit" value="Schedule Conference"/>
        </fieldset>
      </form>
    </div>
  </body>
</html>