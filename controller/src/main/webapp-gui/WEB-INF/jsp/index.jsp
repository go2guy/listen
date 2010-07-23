<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.interact.listen.license.License" %>
<%@ page import="com.interact.listen.license.ListenFeature" %>
<%@ page import="com.interact.listen.resource.Subscriber" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html><%
Subscriber subscriber = (Subscriber)session.getAttribute("subscriber"); %>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title>Listen</title>
    <link rel="SHORTCUT ICON" href="./resources/app/images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="./resources/yui-2.8.0r4/reset-fonts/reset-fonts.css">
    <link rel="stylesheet" type="text/css" href="./resources/jquery/skin/css/custom-theme/jquery-ui-1.8.2.custom.css">
    <link rel="stylesheet" type="text/css" href="./resources/app/css/all-min.css">
    <link rel="stylesheet" type="text/css" href="./resources/app/css/index-min.css">
    <script type="text/javascript" src="./resources/jquery/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="./resources/jquery/jquery-ui-1.8rc3.custom.min.js"></script>
    <script type="text/javascript" src="./resources/jquery/plugins/jquery.simplemodal-1.3.5.min.js"></script>
    <script type="text/javascript" src="./resources/app/js/index-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/server-min.js"></script><%
if(subscriber != null && subscriber.getIsAdministrator()) { %>
    <script type="text/javascript" src="./resources/app/js/app-system-configuration-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/app-subscribers-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/app-history-min.js"></script><%
}
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
    <script type="text/javascript" src="./resources/app/js/app-voicemail-min.js"></script><%
}
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
    <script type="text/javascript" src="./resources/app/js/app-conferencing-min.js"></script><%
    if(subscriber != null && subscriber.getIsAdministrator()) { %>
    <script type="text/javascript" src="./resources/app/js/app-conference-list-min.js"></script><%
    }
}
if(License.isLicensed(ListenFeature.FINDME)) { %>
    <script type="text/javascript" src="./resources/app/js/app-findme-min.js"></script><%
} %>
    <script type="text/javascript" src="./resources/app/js/app-profile-min.js"></script>
  </head>
  <body>
    <div id="header">
      <div class="logo"><img src="resources/app/images/new/listen_logo_50x24.png" alt="Listen"/></div>
      <div class="info"><b><%= subscriber.getUsername() %></b>&nbsp;&bull;&nbsp;<a href="#" id="profile-button" name="profile-button" title="Settings">Settings</a>&nbsp;<a href="/logout" id="logoutButton" name="logoutButton">Logout</a></div>
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
                <div class="conference-content">
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
                        <div class="pagination" id="conference-caller-pagination">
                          <button class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button class="icon-pageright"></button>
                        </div>
                        <div class="cleaner">&nbsp;</div>
                      </div>
                    </div>
                  </div>
                  <div class="right">
                    <div class="panel">
                      <div class="panel-header"><div class="title">Control Panel</div></div>
                      <div class="panel-content">
                        <!-- outdial -->
                        <div class="control-panel-button">
                          <button id="outdial-show" name="outdial-show" class="button-outdial">OnDemand</button>
                        </div>
                        <div id="outdial-dialog" class="inline-dialog">
                          <form name="outdial-form" id="outdial-form">
                            <div class="form-error-message"></div>
                            <div>
                              <label for="outdial-number">Phone number to dial:</label> <input type="text" name="outdial-number" id="outdial-number"/>
                            </div>
                            <div>
                              <button class="button-cancel" name="outdial-cancel" id="outdial-cancel">Cancel</button>
                              <button class="button-outdial" name="outdial-submit" id="outdial-submit">Make Call</button>
                            </div>
                          </form>
                        </div>
                        <div id="record-button-div" class="control-panel-button">
                          <button id="record-button" class="button-record">Record</button>
                        </div>
                        <div class="control-panel-button">
                          <button id="schedule-show" class="button-schedule">Schedule</button>
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
                </div><!-- conference-content -->
                <div class="conference-notloaded">
                  Conference not found.
                </div>
              </div>
            </div><%
}

if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
            <div id="voicemail-application" class="application">
              <div class="application-header"><div class="title">Voicemail (<span id="voicemail-new-count">0</span> New)</div></div>
              <div class="application-content">
                <table id="voicemail-table">
                  <tbody>
                    <tr class="placeholder"><td colspan="4">No voicemail</td></tr>
                  </tbody>
                </table>
                <div class="pagination" id="voicemail-pagination">
                  <button class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button class="icon-pageright"></button>
                </div>
                <div class="cleaner">&nbsp;</div>
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

if(subscriber != null && subscriber.getIsAdministrator()) { %>
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
                        <tr><td colspan="6" class="buttons"><button class="button-add" id="add-dnis-mapping" title="Add a new DNIS mapping">Add</button><button type="submit" class="button-save" title="Save DNIS mappings">Save</button></td></tr>
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
                        <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save mail settings">Save</button><!--<button class="button-cancel">Reset</button>--></td></tr>
                      </tbody>
                    </table>
                  </fieldset>
                </form><%

  if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
                <form id="conferencing-configuration-form">
                  <fieldset>
                    <legend>Conferencing</legend>
                    <div class="form-error-message"></div>
                    <div class="form-success-message"></div>
                    <table>
                      <tbody>
                        <tr><td><label for="conferencing-configuration-pinLength">PIN length</label></td><td><input type="text" id="conferencing-configuration-pinLength" name="conferencing-configuration-pinLength"/></td></tr>
                        <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save configuration">Save</button></td></tr>
                      </tbody>
                    </table>
                  </fieldset>
                </form><%
  } %>
              </div>
            </div>
            
            <div id="conference-list-application" class="application">
              <div class="application-header"><div class="title">Conference List</div></div>
              <div class="application-content">
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
                  <button class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button class="icon-pageright"></button>
                </div>
                <div class="cleaner">&nbsp;</div>
              </div>
            </div>

            <div id="subscribers-application" class="application">
              <div class="application-header"><div class="title">Subscribers</div></div>
              <div class="application-content">
                <div class="left">
                  <div class="panel">
                    <div class="panel-header"><div class="title">Subscriber List</div></div>
                    <div class="panel-content">
                      <table id="subscribers-table" class="data-table">
                        <thead>
                          <tr>
                            <th>Username</th>
                            <th>Access Numbers</th>
                            <th>Last Login</th>
                            <th></th>
                          </tr>
                        </thead>
                        <tbody></tbody>
                      </table>
                      <div class="pagination" id="subscribers-pagination">
                        <button class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button class="icon-pageright"></button>
                      </div>
                      <div class="cleaner">&nbsp;</div>
                    </div>
                  </div>
                </div>
                <div class="right">
                  <div class="panel">
                    <div class="panel-header"><div class="title">Subscriber Info</div></div>
                    <div class="panel-content">
                      <form id="subscriber-form" onsubmit="return false;">
                        <div class="form-error-message"></div>
                        <div class="form-success-message"></div>
                        <fieldset>
                          <input type="hidden" id="subscriber-form-id" name="subscriber-form-id"/>
                          <table>
                            <tbody>
                              <tr><td><label for="subscriber-form-username">Username</label></td><td><input type="text" id="subscriber-form-username" name="subscriber-form-username"/></td></tr>
                              <tr><td><label for="subscriber-form-password">Password</label></td><td><input type="password" id="subscriber-form-password" name="subscriber-form-password"/></td></tr>
                              <tr><td><label for="subscriber-form-confirmPassword">Confirm Password</label></td><td><input type="password" id="subscriber-form-confirmPassword" name="subscriber-form-confirmPassword"/></td></tr>
                              <tr><td><label for="subscriber-form-realName">Real Name</label></td><td><input type="text" id="subscriber-form-realName" name="subscriber-form-realName"/></td></tr>
                              <tr><td><label for="subscriber-form-voicemailPin">Voicemail Pin</label></td><td><input type="text" id="subscriber-form-voicemailPin" name="subscriber-form-voicemailPin" maxlength="10"/></td></tr>
                              <tr><td colspan="2"><label for="subscriber-form-accessNumbers">Access Numbers (comma-separated)</label></td>
                              </tr>
                              <tr>
                                <td colspan="2"><%
if(subscriber.getIsAdministrator()) { %>
                                  <textarea rows="4" cols="25" id="subscriber-form-accessNumbers" name="subscriber-form-accessNumbers"></textarea><%
} else { %>
                                  <%= subscriber.accessNumberString() %><%
} %>
                                </td>
                              </tr>
                              <tr><td colspan="2"><label for="subscriber-form-enableEmailNotification">Send e-mail when voicemail received</label></td></tr>
                              <tr>
                                <td colspan="2">
                                  <input type="checkbox" id="subscriber-form-enableEmailNotification" name="subscriber-form-enableEmailNotification" value="enableEmail"/>
                                  <input type="text" id="subscriber-form-emailAddress" name="subscriber-form-emailAddress"/>
                                  <button class="button-save" id="subscriber-form-testEmail-button" name="subscriber-form-testEmail-button" title="Test Email Address" onclick="LISTEN.SUBSCRIBERS.testEmailAddress();return false;">Verify</button>
                                </td>
                              </tr>
                              <tr><td colspan="2"><label for="subscriber-form-enableSmsNotification">Send SMS when voicemail received</label></td></tr>
                              <tr>
                                <td colspan="2">
                                  <input type="checkbox" id="subscriber-form-enableSmsNotification" name="subscriber-form-enableSmsNotification" value="enableSms"/>
                                  <input type="text" id="subscriber-form-smsAddress" name="subscriber-form-smsAddress"/>
                                  <button class="button-save" id="subscriber-form-testSms-button" name="subscriber-form-testSms-button" title="Test SMS Address" onclick="LISTEN.SUBSCRIBERS.testSmsAddress();return false;">Verify</button>
                                </td>
                              </tr>
                              <tr>
                                <td colspan="2" class="buttons">
                                  <button class="button-add" id="subscriber-form-add-button" name="subscriber-form-add-button" title="Add" onclick="LISTEN.SUBSCRIBERS.addSubscriber();return false;">Add</button>
                                  <button class="button-edit" id="subscriber-form-edit-button" name="subscriber-form-edit-button" title="Edit" onclick="LISTEN.SUBSCRIBERS.editSubscriber();return false;">Edit</button>
                                  <button class="button-cancel" id="subscriber-form-cancel-button" name="subscriber-form-cancel-button" title="Cancel Edit" onclick="LISTEN.SUBSCRIBERS.resetForm();return false;">Cancel Edit</button>
                                </td>
                              </tr>
                            </tbody>
                          </table>
                        </fieldset>
                      </form>
                    </div>
                  </div>
                </div>
                <div class="cleaner">&nbsp;</div>
              </div>
            </div>

            <div id="history-application" class="application">
              <div class="application-header"><div class="title">History</div></div>
              <div class="application-content">
                <ul id="history-list">
                  <li class="placeholder">No history records</li>
                </ul>
                <div class="pagination" id="history-pagination">
                  <button class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button class="icon-pageright"></button>
                </div>
                <div class="cleaner">&nbsp;</div>
              </div>
            </div><%
}

if(subscriber != null) { %>
            <div id="profile-application" class="application">
              <div class="application-header"><div class="title">Profile Info</div></div>
              <div class="application-content">
                <form id="profile-form" onsubmit="return false;">
                  <div class="form-error-message"></div>
                  <div class="form-success-message"></div>
                  <fieldset>
                    <input type="hidden" id="profile-form-id" name="profile-form-id"/>
                    <table>
                      <tbody>
                        <tr><td><label for="profile-form-username">Username</label></td><td><input type="text" id="profile-form-username" name="profile-form-username"/></td></tr>
                        <tr><td><label for="profile-form-password">Password</label></td><td><input type="password" id="profile-form-password" name="profile-form-password"/></td></tr>
                        <tr><td><label for="profile-form-confirmPassword">Confirm Password</label></td><td><input type="password" id="profile-form-confirmPassword" name="profile-form-confirmPassword"/></td></tr>
                        <tr><td><label for="profile-form-realName">Real Name</label></td><td><input type="text" id="profile-form-realName" name="profile-form-realName"/></td></tr>                        
                        <tr><td><label for="profile-form-number">Access Numbers</label></td><td id="profile-form-accessNumbers" name="profile-form-accessNumbers"></td></tr>
                        <tr><td><label for="profile-form-voicemailPin">Voicemail Pin</label></td><td><input type="text" id="profile-form-voicemailPin" name="profile-form-voicemailPin" maxlength="10"/></td></tr>
                        <tr>
                          <td><label for="profile-form-enableEmailNotification">Send e-mail when voicemail received</label></td>
                          <td>
                            <input type="checkbox" id="profile-form-enableEmailNotification" name="profile-form-enableEmailNotification" value="enableEmail"/>
                            <input type="text" id="profile-form-emailAddress" name="profile-form-emailAddress"/>
                            <button class="button-save" id="profile-form-testEmail-button" name="profile-form-testEmail-button" title="Test Email Address" onclick="LISTEN.PROFILE.testEmailAddress();return false;">Verify</button>
                          </td>
                        </tr>
                        <tr>
                          <td><label for="profile-form-enableSmsNotification">Send SMS when voicemail received</label></td>
                          <td>
                            <input type="checkbox" id="profile-form-enableSmsNotification" name="profile-form-enableSmsNotification" value="enableSms"/>
                            <input type="text" id="profile-form-smsAddress" name="profile-form-smsAddress"/>
                            <button class="button-save" id="profile-form-testSms-button" name="profile-form-testSms-button" title="Test SMS Address" onclick="LISTEN.PROFILE.testSmsAddress();return false;">Verify</button>
                          </td>
                        </tr>
                        <tr>
                          <td colspan="2" class="buttons">
                            <button class="button-edit" id="profile-form-edit-button" name="profile-form-edit-button" title="Edit" onclick="LISTEN.PROFILE.editSubscriber();return false;">Save</button>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </fieldset>
                </form>
                <div class="application-header"><div class="title">Pager Info</div></div>
                <div class="application-content">
                  <form id="pager-form" onsubmit="return false;">
                    <div class="form-error-message"></div>
                    <div class="form-success-message"></div>
                    <fieldset>
                      <table>
                        <tbody>
                          <tr><td><label for="pager-form-number">Pager Number</label></td><td id="pager-form-number" name="pager-form-number"></td></tr>
                          <tr><td><label for="pager-form-alternate-number">Alternate Number</label></td><td><input type="text" id="pager-form-alternate-number" name="pager-form-alternate-number" maxlength="14"/></td></tr>
                          <tr>
                             <td colspan="2" class="buttons">
                             <button class="button-edit" id="pager-form-edit-button" name="pager-form-edit-button" title="Edit" onclick="LISTEN.PROFILE.editPagerInfo();return false;">Save</button>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </fieldset>
                  </form>
                </div>
              </div>
            </div> <%
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

if(subscriber != null && subscriber.getIsAdministrator()) { %>
          <hr style="width: 75%;"/>
          <div class="menu">
            <ul>
              <li id="menu-sysconfig">Configuration</li><%
    if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
              <li id="menu-conference-list">Conferences</li><%
    } %>
              <li id="menu-subscribers">Subscribers</li>
              <li id="menu-history">History</li>
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

        <tr id="conferencehistory-row-template">
          <td class="conferencehistory-cell-date"></td>
          <td class="conferencehistory-cell-description"></td>
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
          <td class="conference-cell-callerCount"></td>
          <td class="conference-cell-view"></td>
        </tr>

        <tr id="subscriber-row-template">
          <td class="subscriber-cell-username"></td>
          <td class="subscriber-cell-accessNumbers"></td>
          <td class="subscriber-cell-lastLogin"></td>
          <td class="subscriber-cell-editButton"></td>
        </tr>

        <tr id="voicemail-row-template">
          <td class="voicemail-cell-readStatus"></td>
          <td class="voicemail-cell-from"></td>
          <td class="voicemail-cell-received"></td>
          <td class="voicemail-cell-duration"></td>
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
              <option value="voicemail">Voicemail</option>
              <option value="directVoicemail">Direct Voicemail</option><%
} %>
              <option value="custom">Custom</option>
            </select>
          </td>
          <td><input type="text" value="" class="dnis-mapping-custom-destination"/></td>
          <td><button class="icon-delete" title="Remove this DNIS mapping"></button></td>
        </tr>
      </tbody></table>

      <ul>
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
                 From <select id="scheduleConferenceTimeHour" name="scheduleConferenceTimeHour"><%
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
                </select> until <select id="scheduleConferenceEndTimeHour" name="scheduleConferenceEndTimeHour"><%
for(int i = 1; i <= 12; i++) { %>
                   <option value="<%= i %>"><%= i %></option><%
} %>
                </select>
                <select id="scheduleConferenceEndTimeMinute" name="scheduleConferenceEndTimeMinute">
                  <option value="00">00</option>
                  <option value="15">15</option>
                  <option value="30">30</option>
                  <option value="45">45</option>
                </select>
                <select id="scheduleConferenceEndTimeAmPm" name="scheduleConferenceTimeEndAmPm">
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
                <button class="button-cancel">Cancel</button>
                <button class="button-schedule">Send Emails</button>
              </td>
            </tr>
          </tbody>
        </table>
      </form>
    </div><!-- schedule conference dialog -->
    <div id="communication-error">Server is unavailable, please wait...</div>
    <div id="pinglatency"></div>
    <div id="latency"></div>
  </body>
</html>