<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<%@ page import="com.interact.listen.EmailerUtil" %>
<%@ page import="com.interact.listen.resource.Subscriber" %>
<html>
  <head>
    <title>Settings</title>
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-profile-min.js"/>"></script>

    <meta name="body-class" content="application-settings"/>
    <meta name="page-title" content="Settings"/>
  </head>
  <body>
  <%
Subscriber subscriber = (Subscriber)session.getAttribute("subscriber"); %>
                <div class="tab-container">
                  <ul class="tabs">
                    <li><a href="#">Account</a></li>
                    <li><a href="#">After Hours Pager</a></li>
                  </ul>
                  <div class="tab-content-default">
                    <form id="profile-form">
                      <fieldset>
                        <legend>Account</legend>
                        <input type="hidden" id="profile-form-id" name="profile-form-id"/>
                        <table>
                          <tbody>
                            <tr><td><label for="profile-form-username">Username</label></td><td><input type="text" id="profile-form-username" name="profile-form-username"<%= !subscriber.getIsAdministrator() ? " readonly=\"readonly\" class=\"disabled\"" : "" %>/></td></tr>
                            <tr><td>Account Type</td><td id="profile-form-accountType"></td></tr> <%
    if(!subscriber.getIsActiveDirectory()) { %>
                            <tr><td><label for="profile-form-password">Password</label></td><td><input type="password" id="profile-form-password" name="profile-form-password"/></td></tr>
                            <tr><td><label for="profile-form-confirmPassword">Confirm Password</label></td><td><input type="password" id="profile-form-confirmPassword" name="profile-form-confirmPassword"/></td></tr> <%
    } %>
                            <tr><td><label for="profile-form-realName">Real Name</label></td><td><input type="text" id="profile-form-realName" name="profile-form-realName"/></td></tr>                        
                            <tr><td><label for="profile-form-workEmailAddress">Work Email Address</label></td><td><input type="text" id="profile-form-workEmailAddress" name="profile-form-workEmailAddress"/></td></tr>                        

                              <tr><td colspan="2"><label for="profile-form-acessNumbersTable>Access Numbers</label></td></tr>
                              <tr>
                                <td colspan="2">
                                  <table id="profile-form-accessNumbersTable">
                                    <tbody>
                                      <tr>
                                        <td colspan="3" class="buttons">
                                          <button type="button" class="button-add" id="profile-form-addAccessNumber" title="New access number">New access number</button>
                                        </td>
                                      </tr>
                                    </tbody>
                                  </table>
                                </td>
                              </tr>


                            <listen:ifLicensed feature="VOICEMAIL">
                              <tr><td><label for="profile-form-voicemailPin">Voicemail Passcode</label></td><td><input type="text" id="profile-form-voicemailPin" name="profile-form-voicemailPin" maxlength="10"/></td></tr>
                              <tr>
                                <td><label for="profile-form-enableEmailNotification">Send e-mail when voicemail received</label></td>
                                <td>
                                  <input type="checkbox" id="profile-form-enableEmailNotification" name="profile-form-enableEmailNotification" value="enableEmail"/>
                                  <input type="text" id="profile-form-emailAddress" name="profile-form-emailAddress"/>
                                  <button type="button" class="button-save" id="profile-form-testEmail-button" name="profile-form-testEmail-button" title="Test Email Address">Verify</button>
                                </td>
                              </tr>
                              <tr>
                                <td><label for="profile-form-enableSmsNotification">Send SMS when voicemail received</label></td>
                                <td>
                                  <input type="checkbox" id="profile-form-enableSmsNotification" name="profile-form-enableSmsNotification" value="enableSms"/>
                                  <input type="text" id="profile-form-smsAddress" name="profile-form-smsAddress"/>
                                  <button type="button" class="button-save" id="profile-form-testSms-button" name="profile-form-testSms-button" title="Test SMS Address">Verify</button>
                                </td>
                              </tr>
                              <tr><td><label for="profile-form-paging">Send SMS notification for voicemails until read</label></td><td><input type="checkbox" id="profile-form-paging" name="profile-form-paging" value="enablePaging"/></td></tr>
                              <tr>
                              <tr><td><label for="profile-form-transcription">Transcribe new voicemail</label></td><td><input type="checkbox" id="profile-form-transcription" name="profile-form-transcription" value="enableTranscription"/></td></tr>
                                <td><label for="profile-form-voicemailPlaybackOrder">Voicemail Playback Order</label></td>
                                <td>
                                  <select name="profile-form-voicemailPlaybackOrder" id="profile-form-voicemailPlaybackOrder">
                                    <option value="NEWEST_TO_OLDEST">Newest to Oldest</option>
                                    <option value="OLDEST_TO_NEWEST">Oldest to Newest</option>
                                  </select>
                                </td>
                              </tr>
                            </listen:ifLicensed>
                            <tr>
                              <td colspan="2" class="buttons">
                                <button type="submit" class="button-edit" id="profile-form-edit-button" name="profile-form-edit-button" title="Edit">Save</button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>
                  <div class="tab-content">
                    <form id="pager-form">
                      <fieldset>
                        <legend>After Hours Pager</legend>
                        <table>
                          <tbody>
                            <tr><td><label for="pager-form-number">Pager Number</label></td><td id="pager-form-number" name="pager-form-number"></td></tr>
                            <tr>
                              <td><label for="pager-form-alternate-number">Alternate Number</label></td><td><input type="text" id="pager-form-alternate-number" name="pager-form-alternate-number" maxlength="14"/></td>
                              <td>
                                <select id="pager-form-alternate-address" name="pager-form-alternate-address"><%
    for(EmailerUtil.SmsEmailAddress entry : EmailerUtil.SmsEmailAddress.values()) { %>
                                     <option value="<%= entry.getEmailAddress() %>"><%= entry.getProvider() %></option><%
    } %>
                                </select>
                              </td>
                            </tr>
                            <tr><td><label for="pager-form-page-prefix">Page Prefix</label></td><td><input type="text" id="pager-form-page-prefix" name="pager-form-page-prefix" maxlength="20"/></td></tr>
                            <tr>
                              <td colspan="2" class="buttons">
                                <button type="submit" class="button-edit" id="pager-form-edit-button" name="pager-form-edit-button" title="Edit">Save</button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>
                  <div class="cleaner">&nbsp;</div>
                </div>
                
	<table class="templates">
      <tbody>
        <tr id="profile-accessNumber-row-template">
          <td><input type="text" class="accessNumber-row-number" value="" size="15"/></td>
          <td><input type="checkbox" disabled="disabled" class="accessNumber-row-messageLight"/>&nbsp;<label>Message Light</label></td>
          <td><select class="accessNumber-row-numberType">
          		<option value="EXTENSION" disabled="disabled">Extension</option>
          		<option value="VOICEMAIL" disabled="disabled">Voicemail</option>
          		<option value="MOBILE" selected="selected">Mobile</option>
          		<option value="HOME">Home</option>
          		<option value="OTHER">Other</option>
          </select></td>
          <td><input type="checkbox" class="accessNumber-row-publicNumber"/>&nbsp;<label>Public</label></td>
          <td><button type="button" class="icon-delete" title="Remove this phone number"></button></td>
        </tr>
      </tbody>
    </table>
                
  </body>
</html>