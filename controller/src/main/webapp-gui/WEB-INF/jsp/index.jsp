<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.interact.listen.license.License" %>
<%@ page import="com.interact.listen.license.ListenFeature" %>
<%@ page import="com.interact.listen.resource.User" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html><%
User user = (User)session.getAttribute("user"); %>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title>Listen</title>
    <link rel="SHORTCUT ICON" href="./resources/app/images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="./resources/yui-2.8.0r4/reset-fonts/reset-fonts.css">
    <link rel="stylesheet" type="text/css" href="./resources/jquery/css/excite-bike/jquery-ui-1.8rc3.custom.css">
    <link rel="stylesheet" type="text/css" href="./resources/app/css/all-min.css">
    <link rel="stylesheet" type="text/css" href="./resources/app/css/index-min.css">
    <script type="text/javascript" src="./resources/jquery/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="./resources/jquery/jquery-ui-1.8rc3.custom.min.js"></script>
    <script type="text/javascript" src="./resources/app/js/index-min.js"></script><%
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
    <script type="text/javascript" src="./resources/app/js/conferencing-min.js"></script><%
    if(user != null && user.getIsAdministrator()) { %>
    <script type="text/javascript" src="./resources/app/js/conferencing-admin-min.js"></script><%
    }
}
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
    <script type="text/javascript" src="./resources/app/js/voicemail-min.js"></script><%
}
if(License.isLicensed(ListenFeature.FINDME)) { %>
    <script type="text/javascript" src="./resources/app/js/findme-min.js"></script><%
}
if(user != null && user.getIsAdministrator()) { %>
    <script type="text/javascript" src="./resources/app/js/administration-min.js"></script><%
} %>
  </head>
  <body>
    <div id="wrapper">
      <div id="wrapper-main">
        <div id="header">
          <div id="logo"><img src="resources/app/images/new/listen_logo_50x24.png" alt="Listen"/></div>
          <div id="userInfo">Hi, <%= user.getUsername() %>! | <a href="/logout" id="logoutButton" name="logoutButton">Logout</a></div>
        </div><%
if(License.isLicensed(ListenFeature.CONFERENCING)
    || License.isLicensed(ListenFeature.VOICEMAIL)
    || License.isLicensed(ListenFeature.FINDME)
    || (user != null && user.getIsAdministrator())) { %>
        <div id="main-menu">
          <ul><%
    if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
            <li id="menu-conferencing">Conferencing</li><%
    }
    if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
            <li id="menu-voicemail">Voicemail</li><%
    }
    if(License.isLicensed(ListenFeature.FINDME)) { %>
            <li id="menu-findme">FindMe</li><%
    }
    if(user != null && user.getIsAdministrator()) { %>
            <li id="menu-administration">Administration</li><%
    } %>
          </ul>
        </div>
        <div id="main-menu-handle" title="Show/Hide Menu"></div><%
} %>

        <div id="notification"></div>
        <div id="main">

<%
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
          <div id="conference-application" class="application"><%
if(user != null && user.getIsAdministrator()) { %>
            <div id="conference-list" class="window">
              <div class="panel">
                <div class="panel-header">
                  <div class="panel-title">Conference List</div>
                </div>
                <div class="panel-content">
                  <table>
                    <thead>
                      <tr>
                        <th>Description</th>
                        <th>Status</th><!--
                        <th>Callers</th>
                        <th>Duration</th>-->
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
                    </tbody>
                  </table>
                </div>
              </div>
            </div><%
} %>
            <div id="conference-window" class="window">
              <div class="window-header">
                <div id="conference-title" class="window-title">Conference ###</div>
                <div class="conference-menu">
                  <div class="left-buttons" id="record-button-div">
                    <button id="record-button" class="record-button">Record</button>
                  </div>
                  <div class="right-buttons">
                    <button id="schedule-button" class="schedule-button">Schedule</button>
                  </div>
                </div>
              </div>
              <div class="left">
                <div id="conference-callers" class="panel">
                  <div class="panel-header">
                    <div class="panel-title">Current Callers (<span id="conference-caller-count">0</span>)</div>
                    <div class="panel-menu">
                      <button id="outdial-show" name="outdial-show" class="outdial-button">OnDemand</button>
                      <div id="outdial-dialog" class="inline-dialog">
                        <form name="outdial-form" id="outdial-form">
                          <div class="form-error-message"></div>
                          <div>
                            <label for="outdial-number">Phone number to dial:</label> <input type="text" name="outdial-number" id="outdial-number"/>
                          </div>
                          <div>
                            <button class="cancel-button" name="outdial-cancel" id="outdial-cancel">Cancel</button>
                            <button class="outdial-button" name="outdial-submit" id="outdial-submit">Make Call</button>
                          </div>
                        </form>
                      </div>
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
                <div id="conference-recordings" class="panel">
                  <div class="panel-header">
                    <div class="panel-title">Recordings</div>
                  </div>
                  <div class="panel-content">
                    <ul id="recordings-list"></ul>
                  </div>
                </div>
              </div>
            </div>
          </div><!-- conference-application --><%
}
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
          <div id="voicemail-application" class="application">Voicemail Content</div><%
}
if(License.isLicensed(ListenFeature.FINDME)) { %>
          <div id="findme-application" class="application">FindMe Content</div><%
}
if(user != null && user.getIsAdministrator()) { %>
          <div id="administration-application" class="application">
            <div class="window">
              <div class="panel">
                <div class="panel-header"><div class="panel-title">System Configuration</div></div>
                <div class="panel-content">
                  <form id="dnis-mapping-form">
                    <fieldset>
                      <legend>DNIS Mapping</legend>
                      <table>
                        <tbody>
                          <tr><td></td><td colspan="3" class="buttons"><button class="add-button" id="add-dnis-mapping">Add</button><button type="submit" class="save-button">Save</button><!--<button class="cancel-button">Reset</button>--></td></tr>
                        </tbody>
                      </table>
                    </fieldset>
                  </form>

                  <fieldset>
                    <legend>Accounts</legend>
                    <button id="create-new-account-button" class="add-button">Create New Account</button>
                  </fieldset>

                  <form id="mail-form">
                    <fieldset>
                      <legend>Mail</legend>
                      <table>
                        <tbody>
                          <tr><td><label for="smtp-server">SMTP Server</label></td><td><input type="text" id="smtp-server" name="smtp-server"/></td></tr>
                          <tr><td><label for="smtp-username">SMTP Username</label></td><td><input type="text" id="smtp-username" name="smtp-username"/></td></tr>
                          <tr><td><label for="smtp-password">SMTP Password</label></td><td><input type="password" id="smtp-password" name="smtp-password"/></td></tr>
                          <tr><td><label for="from-address">From Address</label></td><td><input type="text" id="from-address" name="from-address"/></td></tr>
                          <tr><td></td><td class="buttons"><button type="submit" class="save-button">Save</button><!--<button class="cancel-button">Reset</button>--></td></tr>
                        </tbody>
                      </table>
                    </fieldset>
                  </form>
                </div>
              </div>
            </div>
          </div><%
} %>
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
    </div><%
if(user != null && user.getIsAdministrator()) { %>
    <div id="provisionAccountDialog" class="dialog">
      <div class="form-error-message"></div>
      <form id="provisionAccountForm" name="provisionAccountForm" method="POST" action="/ajax/provisionAccount">
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
    </div><%
} %>
    <div id="scheduleConferenceDialog" class="dialog">
      <div class="form-error-message"></div>
      <form id="scheduleConferenceForm" name="scheduleConferenceForm" method="POST" action="/ajax/scheduleConference">
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
              <th><label for="scheduleConferenceSubject">Subject of email</label></th>
              <td><textarea id="scheduleConferenceSubject" name="scheduleConferenceSubject"></textarea>
            </tr>
            <tr>
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