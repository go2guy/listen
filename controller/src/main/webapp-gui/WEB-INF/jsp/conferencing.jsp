<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>Conferencing</title>

    <script type="text/javascript">
<%
/* this is pretty hacky, but is the quickest change while refactoring the GUI - we can come back to it later and make it mo' better */
String id = request.getParameter("id");
if(id != null && !id.trim().equals("")) { %>
    var CONFERENCE_ID = '<%= id %>';<%
} else { %>
    var CONFERENCE_ID;<%
} %>
    </script>
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-conferencing-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/conferencing-min.css"/>">
    <meta name="body-class" content="application-conferencing"/>
    <meta name="page-title" content="Conferencing"/>
  </head>
  <body>
                  <div class="tab-container">
                    <ul class="tabs">
                      <li><a href="#">Main</a></li>
                      <li><a href="#">Scheduling</a></li>
                      <li><a href="#">Recordings</a></li>
                    </ul>

                    <div class="tab-content-default">
                      <div class="left">
                        <div class="panel">
                          <div class="panel-content">
                            <table>
                              <tbody>
                                <tr><td>Conference Description</td><td id="conference-info-description" class="conference-info-value"></td></tr>
                                <tr><td>Conference Status</td><td id="conference-info-status" class="conference-info-value"></td></tr>
                                <tr>
                                  <td>
                                    <div class="control-panel-button">
                                      <button type="button" id="outdial-show" name="outdial-show" class="button-outdial">OnDemand</button>
                                    </div>
                                    <div id="outdial-dialog" class="inline-dialog">
                                      <form name="outdial-form" id="outdial-form">
                                        <div>
                                          <label for="outdial-number">Phone number to dial:</label> <input type="text" name="outdial-number" id="outdial-number"/>
                                        </div>
                                        <div>
                                          <label for="outdial-interrupt">Interrupt when answered:</label> <input type="checkbox" id="outdial-interrupt" name="outdial-interrupt" value="interrupt"/>
                                        </div>
                                        <div>
                                          <button type="button" class="button-cancel" name="outdial-cancel" id="outdial-cancel">Cancel</button>
                                          <button type="button" class="button-outdial" name="outdial-submit" id="outdial-submit">Make Call</button>
                                        </div>
                                      </form>
                                    </div>
                                    <div id="record-button-div" class="control-panel-button">
                                      <button type="button" id="record-button" class="button-record">Record</button>
                                    </div>
                                  </td>
                                </tr>
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
                              <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                            </div>
                            <div class="cleaner">&nbsp;</div>
                          </div>
                        </div>
                      </div>
                      <div class="right">
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
                      </div>
                      <div class="cleaner">&nbsp;</div>
                    </div>
                    <div class="tab-content">
                      <div class="panel">
                        <div class="panel-header"><div class="title">Future Scheduled Conferences</div></div>
                        <div class="panel-content">
                          <ul id="scheduled-conference-table" class="data-table">
                            <li class="placeholder">No conferences have been scheduled.</li>
                          </ul>
                          <div class="pagination" id="scheduled-conference-pagination">
                            <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                          </div>
                        </div>
                      </div>

                      <div class="panel">
                        <div class="panel-header"><div class="title">Schedule a Conference</div></div>
                        <div class="panel-content">
                          <form id="scheduleConferenceForm">
                            <fieldset>
                              <table>
                                <caption>This will send an email to the specified recipients with a date, time, phone number, and PIN.</caption>
                                <tbody>
                                  <tr>
                                    <th><label for="scheduleConferenceDate" class="required">Date &amp; Time</label></th>
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
                                    <th><label for="scheduleConferenceActiveParticipants">Active caller email addresses</label></th>
                                    <td><textarea id="scheduleConferenceActiveParticipants" name="scheduleConferenceActiveParticipants"></textarea></td>
                                  </tr>
                                  <tr>
                                    <th><label for="scheduleConferencePassiveParticipants">Passive caller email addresses</label></th>
                                    <td><textarea id="scheduleConferencePassiveParticipants" name="scheduleConferencePassiveParticipants"></textarea></td>
                                  </tr>
                                  <tr>
                                    <td colspan="2" class="buttons">
                                      <button type="submit" class="button-schedule">Send Emails</button>
                                    </td>
                                  </tr>
                                </tbody>
                              </table>
                            </fieldset>
                          </form>
                        </div>
                      </div>
                      
                      <div class="panel">
                        <div class="panel-header"><div class="title">Past Scheduled Conferences</div></div>
                        <div class="panel-content">
                          <ul id="historic-scheduled-conference-table" class="data-table">
                            <li class="placeholder">No past conferences.</li>
                          </ul>
                          <div class="pagination" id="historic-scheduled-conference-pagination">
                            <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                          </div>
                        </div>
                      </div>
                      
                    </div>
                    <div class="tab-content">

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
                            <div class="pagination" id="conference-recording-pagination">
                              <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                            </div>

                    </div>
                    <div class="cleaner">&nbsp;</div>
                  </div>
                  
                <div class="conference-notloaded">
                  Conference not found.
                </div>
                
    <table class="templates">
      <tbody>
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
      </tbody>
    </table>
    
    <li class="templates">
      <li id="scheduled-conference-row-template">
        <div class="scheduled-conference-cell-when"></div>
        <div class="scheduled-conference-cell-topic"></div>
        <div class="scheduled-conference-cell-callers"></div>
        <div class="scheduled-conference-cell-view"></div>
        <div class="scheduled-conference-cell-notes"></div>
        <div class="scheduled-conference-cell-activeCallers"></div>
        <div class="scheduled-conference-cell-passiveCallers"></div>
        <div class="cleaner">&nbsp;</div>
      </li>
    </li>
  </body>
</html>