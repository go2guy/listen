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
    <script type="text/javascript" src="./resources/jquery/plugins/jquery-color/jquery.color.min.js"></script>
    <script type="text/javascript" src="./resources/jquery/plugins/jquery.simplemodal-1.3.5.min.js"></script>
    <script type="text/javascript" src="./resources/app/js/index-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/server-min.js"></script><%
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
    <script type="text/javascript" src="./resources/app/js/app-conferencing-min.js"></script><%
    if(user != null && user.getIsAdministrator()) { %>
    <script type="text/javascript" src="./resources/app/js/app-conference-list-min.js"></script><%
    }
}
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
    <script type="text/javascript" src="./resources/app/js/app-voicemail-min.js"></script><%
}
if(License.isLicensed(ListenFeature.FINDME)) { %>
    <script type="text/javascript" src="./resources/app/js/app-findme-min.js"></script><%
}
if(user != null && user.getIsAdministrator()) { %>
    <script type="text/javascript" src="./resources/app/js/app-system-configuration-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/app-users-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/app-callhistory-min.js"></script><%
} %>
  </head>
  <body>
    <div id="header">
      <div class="logo"><img src="resources/app/images/new/listen_logo_50x24.png" alt="Listen"/></div>
      <div class="info"><b><%= user.getUsername() %></b>&nbsp;&bull;&nbsp;<a href="/logout" id="logoutButton" name="logoutButton">Logout</a></div>
    </div>
    <div class="column-mask">
      <div class="two-column">
        <div class="content-column-wrapper">
          <div class="content-column">
            <div id="notification"></div><%

if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
            <div id="conferencing-application" class="application">
              <div class="application-header"><div class="title">Conferencing</div></div>
              <div class="application-content">
                <div class="left">
                  <div class="panel">
                    <div class="panel-content">
                      <table>
                        <tbody>
                          <tr><td>Conference Description</td><td id="conference-info-description" class="conference-info-value"></td></tr>
                          <tr><td>Conference Status</td><td id="conference-info-status" class="conference-info-value"></td></tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                  <div class="panel">
                    <div class="panel-header"><div class="title">On The Call (<span id="conference-caller-count">0</span>)</div></div>
                    <div class="panel-content">
                      <table id="conference-caller-table">
                        <tbody>
                          <tr class="placeholder"><td colspan="3">Nobody</td></tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
                <div class="right">
                  <div class="panel">
                    <div class="panel-header"><div class="title">Control Panel</div></div>
                    <div class="panel-content">
                      <!-- outdial -->
                      <div class="control-panel-button">
                        <button id="outdial-show" name="outdial-show" class="outdial-button">OnDemand</button>
                      </div>
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
                      <div id="record-button-div" class="control-panel-button">
                        <button id="record-button" class="record-button">Record</button>
                      </div>
                      <div class="control-panel-button">
                        <button id="schedule-show" class="schedule-button">Schedule</button>
                      </div>
                    </div>
                  </div>
                  <div class="panel">
                    <div class="panel-header"><div class="title">Available PINs (<span id="conference-pin-count">0</span>)</div></div>
                    <div class="panel-content">
                      <table id="conference-pin-table">
                        <tbody>
                          <tr class="placeholder"><td colspan="3">No PINs available</td></tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                  <div class="panel">
                    <div class="panel-header"><div class="title">Recent History</div></div>
                    <div class="panel-content">
                      <table id="conference-history-table">
                        <tbody>
                          <tr class="placeholder"><td colspan="2">No history records</td></tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                  <div class="panel">
                    <div class="panel-header"><div class="title">Recent Recordings</div></div>
                    <div class="panel-content">
                      <table id="conference-recording-table" class="data-table">
                        <thead>
                          <tr>
                            <th>Date</th>
                            <th>Duration</th>
                            <th>Size</th>
                            <th></th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr class="placeholder"><td colspan="4">No recordings</td></tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
                <div class="cleaner">&nbsp;</div>
              </div>
            </div><%
}

if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
            <div id="voicemail-application" class="application">
              <div class="application-header"><div class="title">Voicemail (<span id="voicemail-new-count">0</span> New)</div></div>
              <div class="application-content">
                <table id="voicemail-table">
                  <tbody>
                    <tr class="placeholder"><td colspan="3">No voicemail</td></tr>
                  </tbody>
                </table>
              </div>
            </div><%
} 

if(License.isLicensed(ListenFeature.FINDME)) { %>
            <div id="findme-application" class="application">
              <div class="application-header"><div class="title">FindMe</div></div>
              <div class="application-content">
                FindMe Stuff
              </div>
            </div><%
}

if(user != null && user.getIsAdministrator()) { %>
            <div id="sysconfig-application" class="application">
              <div class="application-header"><div class="title">Configuration</div></div>
              <div class="application-content">

                <form id="dnis-mapping-form">
                  <fieldset>
                    <legend>DNIS Mappings</legend>
                    <div class="form-error-message"></div>
                    <div class="form-success-message"></div>
                    <table>
                      <tbody>
                        <tr><td></td><td colspan="3" class="buttons"><button class="add-button" id="add-dnis-mapping" title="Add a new DNIS mapping">Add</button><button type="submit" class="save-button" title="Save DNIS mappings">Save</button><!--<button class="cancel-button">Reset</button>--></td></tr>
                      </tbody>
                    </table>
                  </fieldset>
                </form>

                <form id="mail-form">
                  <fieldset>
                    <legend>Mail</legend>
                    <div class="form-error-message"></div>
                    <div class="form-success-message"></div>
                    <table>
                      <tbody>
                        <tr><td><label for="smtp-server">SMTP Server</label></td><td><input type="text" id="smtp-server" name="smtp-server"/></td></tr>
                        <tr><td><label for="smtp-username">SMTP Username</label></td><td><input type="text" id="smtp-username" name="smtp-username"/></td></tr>
                        <tr><td><label for="smtp-password">SMTP Password</label></td><td><input type="password" id="smtp-password" name="smtp-password"/></td></tr>
                        <tr><td><label for="from-address">From Address</label></td><td><input type="text" id="from-address" name="from-address"/></td></tr>
                        <tr><td></td><td class="buttons"><button type="submit" class="save-button" title="Save mail settings">Save</button><!--<button class="cancel-button">Reset</button>--></td></tr>
                      </tbody>
                    </table>
                  </fieldset>
                </form>

              </div>
            </div>
            
            <div id="conference-list-application" class="application">
              <div class="application-header"><div class="title">Conference List</div></div>
              <div class="application-content">
                <table id="conference-list-table" class="data-table">
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
                    <tr class="placeholder"><td colspan="2">No conferences</td></tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div id="users-application" class="application">
              <div class="application-header"><div class="title">Users</div></div>
              <div class="application-content">
                <div class="left">
                  <div class="panel">
                    <div class="panel-header"><div class="title">User List</div></div>
                    <table id="users-table" class="data-table">
                      <thead>
                        <tr>
                          <th>Username</th>
                          <th>Number</th>
                          <th>Last Login</th>
                          <th></th>
                        </tr>
                      </thead>
                      <tbody></tbody>
                    </table>
                  </div>
                </div>
                <div class="right">
                  <div class="panel">
                    <div class="panel-header"><div class="title">User Info</div></div>
                    <form id="user-form" onsubmit="return false;">
                      <div class="form-error-message"></div>
                      <div class="form-success-message"></div>
                      <fieldset>
                        <input type="hidden" id="user-form-id" name="user-form-id"/>
                        <table>
                          <tbody>
                            <tr><td><label for="user-form-username">Username</label></td><td><input type="text" id="user-form-username" name="user-form-username"/></td></tr>
                            <tr><td><label for="user-form-password">Password</label></td><td><input type="password" id="user-form-password" name="user-form-password"/></td></tr>
                            <tr><td><label for="user-form-confirmPassword">Confirm Password</label></td><td><input type="password" id="user-form-confirmPassword" name="user-form-confirmPassword"/></td></tr>
                            <tr><td><label for="user-form-number">Number</label></td><td><input type="text" id="user-form-number" name="user-form-number"/></td></tr>
                            <tr>
                              <td></td>
                              <td class="buttons">
                                <button class="save-button" id="user-form-add-button" name="user-form-add-button" title="Add" onclick="LISTEN.USERS.addUser();return false;">Add</button>
                                <button class="edit-button" id="user-form-edit-button" name="user-form-edit-button" title="Edit" onclick="LISTEN.USERS.editUser();return false;">Edit</button>
                                <button class="cancel-button" id="user-form-cancel-button" name="user-form-cancel-button" title="Cancel Edit" onclick="LISTEN.USERS.resetForm();return false;">Cancel Edit</button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>
                </div>
                <div class="cleaner">&nbsp;</div>
              </div>
            </div>
            
            <div id="callhistory-application" class="application">
              <div class="application-header"><div class="title">Call History</div></div>
              <div class="application-content">
                <table id="callhistory-table" class="data-table">
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Subscriber</th>
                      <th>Service</th>
                      <th>Duration</th>
                      <th>ANI</th>
                      <th>DNIS</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr class="placeholder"><td colspan="6">No history</td></tr>
                  </tbody>
                </table>
              </div>
            </div><%
} %>

          </div>
        </div>
        <div class="menu-column"><%
if(License.isLicensed(ListenFeature.CONFERENCING)
    || License.isLicensed(ListenFeature.VOICEMAIL)
    || License.isLicensed(ListenFeature.FINDME)) { %>
          <div class="menu">
            <ul><%
    if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
              <li id="menu-conferencing">Conferencing</li><%
    }
    if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
              <li id="menu-voicemail">Voicemail</li><%
    }
    if(License.isLicensed(ListenFeature.FINDME)) { %>
              <li id="menu-findme">FindMe</li><%
    } %>
            </ul>
          </div><%
}

if(user != null && user.getIsAdministrator()) { %>
          <hr style="width: 75%;"/>
          <div class="menu">
            <ul>
              <li id="menu-sysconfig">Configuration</li><%
    if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
              <li id="menu-conference-list">Conferences</li><%
    } %>
              <li id="menu-users">Users</li>
              <li id="menu-callhistory">Call History</li>
            </ul>
          </div><%
} %>
        </div>
      </div>
    </div>

    <div id="footer">
      Listen &copy;2010 Interact Incorporated, <a href="http://www.iivip.com/">iivip.com</a>
    </div>

    <div id="templates">
      <table><tbody>
        <tr id="caller-row-template">
          <td class="caller-cell-number"></td>
          <td class="caller-cell-muteIcon"></td>
          <td class="caller-cell-dropIcon"></td>
        </tr>

        <tr id="history-row-template">
          <td class="history-cell-date"></td>
          <td class="history-cell-description"></td>
        </tr>

        <tr id="pin-row-template">
          <td class="pin-cell-type"></td>
          <td class="pin-cell-number"></td>
          <td class="pin-cell-removeIcon"></td>
        </tr>

        <tr id="recording-row-template">
          <td class="recording-cell-dateCreated"></td>
          <td class="recording-cell-duration"></td>
          <td class="recording-cell-fileSize"></td>
          <td class="recording-cell-download"></td>
        </tr>

        <tr id="conference-row-template">
          <td class="conference-cell-description"></td>
          <td class="conference-cell-status"></td>
          <td class="conference-cell-view"></td>
        </tr>

        <tr id="user-row-template">
          <td class="user-cell-username"></td>
          <td class="user-cell-number"></td>
          <td class="user-cell-lastLogin"></td>
          <td class="user-cell-editButton"></td>
        </tr>

        <tr id="voicemail-row-template">
          <td class="voicemail-cell-readStatus"></td>
          <td class="voicemail-cell-from"></td>
          <td class="voicemail-cell-received"></td>
          <td class="voicemail-cell-download"></td>
        </tr>

        <tr id="dnis-row-template">
          <td>Number</td>
          <td><input type="text" value="" class="dnis-mapping-number"/></td>
          <td>maps to</td>
          <td>
            <select><%
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
              <option value="conferencing">Conferencing</option><%
}
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
              <option value="mailbox">Mailbox</option>
              <option value="voicemail">Voicemail</option><%
} %>
              <option value="custom">Custom</option>
            </select>
          </td>
          <td><input type="text" value="" class="dnis-mapping-custom-destination"/></td>
          <td><button class="delete-button" title="Remove this DNIS mapping"></button></td>
        </tr>

        <tr id="callhistory-row-template">
          <td class="callhistory-cell-date"></td>
          <td class="callhistory-cell-subscriber"></td>
          <td class="callhistory-cell-service"></td>
          <td class="callhistory-cell-duration"></td>
          <td class="callhistory-cell-ani"></td>
          <td class="callhistory-cell-dnis"></td>
        </tr>
      </tbody></table>
    </div>

    <div id="scheduleConferenceDialog" class="inline-dialog">
      <div class="form-error-message"></div>
      <form id="scheduleConferenceForm">
        <table>
          <caption>This will send an email to the specified recipients with a date, time, phone number, and PIN.</caption>
          <tbody>
            <tr>
              <th><label for="scheduleConferenceDate">Date &amp; Time</label></th>
              <td>
                <input type="text" id="scheduleConferenceDate" name="scheduleConferenceDate"/><br/>
                 <select id="scheduleConferenceTimeHour" name="scheduleConferenceTimeHour"><%
for(int i = 1; i <= 12; i++) { %>
                   <option value="<%= i %>"><%= i %></option><%
} %>
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
              <td><input type="text" id="scheduleConferenceSubject" name="scheduleConferenceSubject"/></td>
            </tr>
            <tr>
              <th><label for="scheduleConferenceDescription">Memo to include in email</label></th>
              <td><textarea id="scheduleConferenceDescription" name="scheduleConferenceDescription"></textarea>
            </tr>
            <tr>
              <td colspan="2">Enter the email addresses (comma-separated) for participants who should receive the appropriate PIN in the fields below.</td>
            </tr>
            <tr>
              <th><label for="scheduleConferenceActiveParticipants">Active PIN</label></th>
              <td><textarea id="scheduleConferenceActiveParticipants" name="scheduleConferenceActiveParticipants"></textarea></td>
            </tr>
            <tr>
              <th><label for="scheduleConferencePassiveParticipants">Passive PIN</label></th>
              <td><textarea id="scheduleConferencePassiveParticipants" name="scheduleConferencePassiveParticipants"></textarea></td>
            </tr>
            <tr>
              <td colspan="2" class="buttons">
                <button class="cancel-button">Cancel</button>
                <button class="schedule-button">Send Emails</button>
              </td>
            </tr>
          </tbody>
        </table>
      </form>
    </div><!-- schedule conference dialog -->
  </body>
</html>