<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>Subscribers</title>
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-subscribers-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/subscribers-min.css"/>">

    <meta name="body-class" content="application-attendant"/>
    <meta name="page-title" content="Attendant"/>
  </head>
  <body>
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
                            <th></th>
                          </tr>
                        </thead>
                        <tbody></tbody>
                      </table>
                      <div class="pagination" id="subscribers-pagination">
                        <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                      </div>
                      <div class="cleaner">&nbsp;</div>
                    </div>
                  </div>
                </div>
                <div class="right">
                  <div class="panel">
                    <div class="panel-header"><div class="title">Subscriber Info</div></div>
                    <div class="panel-content">
                      <form id="subscriber-form">
                        <fieldset>
                          <input type="hidden" id="subscriber-form-id" name="subscriber-form-id"/>
                          <table>
                            <tbody>
                              <tr><td><label for="subscriber-form-username" class="required">Username</label></td><td><input type="text" id="subscriber-form-username" name="subscriber-form-username"/></td></tr>
                              <tr><td>Account Type</td><td id="subscriber-form-accountType">Local</td></tr>
                              <tr><td><label for="subscriber-form-password" class="required">Password</label></td><td><input type="password" id="subscriber-form-password" name="subscriber-form-password"/></td></tr>
                              <tr><td><label for="subscriber-form-confirmPassword" class="required">Confirm Password</label></td><td><input type="password" id="subscriber-form-confirmPassword" name="subscriber-form-confirmPassword"/></td></tr>
                              <tr><td><label for="subscriber-form-realName">Real Name</label></td><td><input type="text" id="subscriber-form-realName" name="subscriber-form-realName"/></td></tr>
                              <tr><td><label for="subscriber-form-isAdmin">Administrator</label></td><td><input type="checkbox" id="subscriber-form-isAdmin" name="subscriber-form-isAdmin" value="enableAdmin"/></td></tr>
                              <listen:ifLicensed feature="VOICEMAIL">
                                <tr><td><label for="subscriber-form-voicemailPin">Voicemail Passcode</label></td><td><input type="text" id="subscriber-form-voicemailPin" name="subscriber-form-voicemailPin" maxlength="10"/></td></tr>
                              </listen:ifLicensed>
                              <tr><td colspan="2"><label>Access Numbers</label></td></tr>
                              <tr>
                                <td colspan="2">
                                  <table id="subscriber-form-accessNumbersTable">
                                    <tbody>
                                      <tr>
                                        <td colspan="3" class="buttons">
                                          <button type="button" class="button-add" id="subscriber-form-addAccessNumber" title="New access number">New access number</button>
                                        </td>
                                      </tr>
                                    </tbody>
                                  </table>
                                </td>
                              </tr>

                              <listen:ifLicensed feature="VOICEMAIL">
                                <tr><td colspan="2"><label for="subscriber-form-enableEmailNotification">Send e-mail when voicemail received</label></td></tr>
                                <tr>
                                  <td colspan="2">
                                    <input type="checkbox" id="subscriber-form-enableEmailNotification" name="subscriber-form-enableEmailNotification" value="enableEmail"/>
                                    <input type="text" id="subscriber-form-emailAddress" name="subscriber-form-emailAddress"/>
                                    <button type="button" class="button-save" id="subscriber-form-testEmail-button" name="subscriber-form-testEmail-button" title="Test Email Address">Verify</button>
                                  </td>
                                </tr>
                                <tr><td colspan="2"><label for="subscriber-form-enableSmsNotification">Send SMS when voicemail received</label></td></tr>
                                <tr>
                                  <td colspan="2">
                                    <input type="checkbox" id="subscriber-form-enableSmsNotification" name="subscriber-form-enableSmsNotification" value="enableSms"/>
                                    <input type="text" id="subscriber-form-smsAddress" name="subscriber-form-smsAddress"/>
                                    <button type="button" class="button-save" id="subscriber-form-testSms-button" name="subscriber-form-testSms-button" title="Test SMS Address">Verify</button>
                                  </td>
                                </tr>
                                <tr><td><label for="subscriber-form-paging">Page on new voicemail</label></td><td><input type="checkbox" id="subscriber-form-paging" name="subscriber-form-paging" value="enablePaging"/></td></tr>
                                <tr><td><label for="subscriber-form-transcription">Transcribe new voicemail</label></td><td><input type="checkbox" id="subscriber-form-transcription" name="subscriber-form-transcription" value="enableTranscription"/></td></tr>
                                <tr>
                                  <td><label for="subscriber-form-voicemailPlaybackOrder">Voicemail Playback Order</label></td>
                                  <td>
                                    <select name="subscriber-form-voicemailPlaybackOrder" id="subscriber-form-voicemailPlaybackOrder">
                                      <option value="NEWEST_TO_OLDEST" selected="selected">Newest to Oldest</option>
                                      <option value="OLDEST_TO_NEWEST">Oldest to Newest</option>
                                    </select>
                                  </td>
                                </tr>
                              </listen:ifLicensed>

                              <tr>
                                <td colspan="2" class="buttons">
                                  <button type="submit" class="button-add" id="subscriber-form-add-button" name="subscriber-form-add-button" title="Add">Add</button>
                                  <button type="submit" class="button-edit" id="subscriber-form-edit-button" name="subscriber-form-edit-button" title="Edit">Edit</button>
                                  <button type="reset" class="button-cancel" id="subscriber-form-cancel-button" name="subscriber-form-cancel-button" title="Cancel Edit">Cancel Edit</button>
                                </td>
                              </tr>
                            </tbody>
                          </table>
                        </fieldset>
                      </form>
                    </div>
                  </div>
                </div>
                
    <table class="templates">
      <tbody>
        <tr id="subscriber-row-template">
          <td class="subscriber-cell-username"></td>
          <td class="subscriber-cell-accessNumbers"></td>
          <td class="subscriber-cell-lastLogin"></td>
          <td class="subscriber-cell-editButton"></td>
          <td class="subscriber-cell-deleteButton"></td>
        </tr>
        
        <tr id="accessNumber-row-template">
          <td><input type="text" class="accessNumber-row-number" value=""/></td>
          <td><input type="checkbox" class="accessNumber-row-messageLight"/>&nbsp;<label>Message Light</label></td>
          <td><button type="button" class="icon-delete" title="Remove this phone number"></button></td>
        </tr>
      </tbody>
    </table>
  </body>
</html>