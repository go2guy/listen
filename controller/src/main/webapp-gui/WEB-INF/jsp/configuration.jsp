<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>Configuration</title>
    
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-system-configuration-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/configuration-min.css"/>">

    <meta name="body-class" content="application-configuration"/>
    <meta name="page-title" content="Configuration"/>
  </head>
  <body>
                <div class="tab-container">
                  <ul class="tabs">
                    <li><a href="#">Phone Numbers</a></li>
                    <li><a href="#">Notifications</a></li>
                    <listen:ifLicensed feature="CONFERENCING">
                      <li><a href="#">Conferencing</a></li>
                    </listen:ifLicensed>
                    <li><a href="#">Alerts</a></li>
                    <li><a href="#">Authentication</a></li>
                    <li><a href="#">SPOT</a></li>
                    <li><a href="#">Android</a></li>
                  </ul>

                  <div class="tab-content-default">
                    <form id="dnis-mapping-form">
                      <fieldset>
                        <legend>Phone Numbers</legend>
                        <table>
                          <tbody>
                            <tr>
                              <td colspan="6" class="buttons">
                                <button type="button" class="button-add" id="add-dnis-mapping" title="Add a new phone number">Add</button>
                                <button type="submit" class="button-save" title="Save phone numbers">Save</button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>

                  <div class="tab-content">
                    <form id="notifications-form">
                      <fieldset>
                        <legend>Notifications</legend>
                        <table>
                          <tbody>
                            <tr><td><label for="smtp-server">SMTP Server</label></td><td><input type="text" id="smtp-server" name="smtp-server"/></td></tr>
                            <tr><td><label for="smtp-username">SMTP Username</label></td><td><input type="text" id="smtp-username" name="smtp-username"/></td></tr>
                            <tr><td><label for="smtp-password">SMTP Password</label></td><td><input type="password" id="smtp-password" name="smtp-password"/></td></tr>
                            <tr><td><label for="from-address">From Address</label></td><td><input type="text" id="from-address" name="from-address"/></td></tr>
                            <tr><td><label for="direct-access-number">Direct Access Number</label></td>
                              <td>
                                <select id="direct-voicemail-access-number-select">
              					</select>
                              </td>
                            </tr>
                            <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save mail settings">Save</button></td></tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>
                  
                  <listen:ifLicensed feature="CONFERENCING">
                    <div class="tab-content">
                      <form id="conferencing-configuration-form">
                        <fieldset>
                          <legend>Conferencing</legend>
                          <table>
                            <tbody>
                              <tr>
                                <td><label for="conferencing-configuration-pinLength" class="required">PIN length</label></td>
                                <td><input type="text" id="conferencing-configuration-pinLength" name="conferencing-configuration-pinLength"/></td>
                              </tr>
                            </tbody>
                          </table>
                          <div class="help">Any numbers that are configured as mapping to the Conferencing application can be given a label to be used when sending conference invitations so all invitees will know which number to call to access the conference.</div>
                          <button type="button" class="button-add" id="add-conference-bridge-number" title="Add a new conference bridge number">Add Conference Number Label</button>
                          <table id="conferencing-bridge-numbers">
                            <tbody>
                              <tr>
                                <td colspan="6" class="buttons">
                                  <button type="submit" class="button-save" title="Save configuration">Save</button>
                                </td>
                              </tr>
                            </tbody>
                          </table>
                        </fieldset>
                      </form>
                    </div>
                  </listen:ifLicensed>

                  <div class="tab-content">
                    <form id="alerts-configuration-form">
                      <fieldset>
                        <legend>Alerts</legend>
                        <div class="help">Interact's Realize&trade; monitoring system can send alerts if the Listen&trade; system goes down. Enter the URL for Realize&trade; and the Alert to be updated. When the Alternate Pager Number changes, the configured Alert will be updated with the new number.</div>
                        <table>
                          <tbody>
                            <tr>
                              <td><label for="alerts-configuration-realizeUrl">Realize URL</label></td>
                              <td><input type="text" id="alerts-configuration-realizeUrl" name="alerts-configuration-realizeUrl"/></td>
                            </tr>
                            <tr>
                              <td><label for="alerts-configuration-realizeAlertName">Realize Alert Name</label></td>
                              <td><input type="text" id="alerts-configuration-realizeAlertName" name="alerts-configuration-realizeAlertName"/></td>
                            </tr>
                            <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save configuration">Save</button></td></tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>

                  <div class="tab-content">
                    <form id="sysconfig-authentication-form">
                      <fieldset>
                        <legend>Authentication</legend>
                        <table>
                          <tbody>
                            <tr>
                              <td colspan="2">
                                <input type="checkbox" name="sysconfig-authentication-activeDirectoryEnabled" id="sysconfig-authentication-activeDirectoryEnabled"/>
                                <label for="sysconfig-authentication-activeDirectoryEnabled">Use Active Directory?</label>
                              </td>
                            </tr>
                            <tr><td><label for="sysconfig-authentication-activeDirectoryServer">Active Directory Server</label></td><td><input type="text" id="sysconfig-authentication-activeDirectoryServer" name="sysconfig-authentication-activeDirectoryServer"/></td></tr>
                            <tr><td><label for="sysconfig-authentication-activeDirectoryDomain">Active Directory Domain</label></td><td><input type="text" id="sysconfig-authentication-activeDirectoryDomain" name="sysconfig-authentication-activeDirectoryDomain"/></td></tr>
                            <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save authentication settings">Save</button></td></tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>

                  <div class="tab-content">
                    <fieldset>
                      <legend>SPOT Systems</legend>
                      <table id="sysconfig-spot-systems">
                        <tbody></tbody>
                      </table>
                    </fieldset>
                  </div>

                  <div class="tab-content">
                    <form id="sysconfig-google-form">
                      <fieldset>
                        <legend>Android</legend>
                        <div class="help">The Google account with either the corresponding password or authorization token are required to enable Android cloud to device messaging.</div>
                        <table>
                          <tbody>
                            <tr>
                              <td colspan="2">
                                <input type="checkbox" name="sysconfig-c2dm-enabled" id="sysconfig-c2dm-enabled"/>
                                <label for="sysconfig-c2dm-enabled">Use Android Cloud to Device Messaging?</label>
                              </td>
                            </tr>
                            <tr><td><label for="sysconfig-google-account">Google Account</label></td><td><input type="text" id="sysconfig-google-account" name="sysconfig-google-account"/></td></tr>
                            <tr><td><label for="sysconfig-google-password">Password</label></td><td><input type="password" id="sysconfig-google-password" name="sysconfig-google-password"/></td></tr>
                            <tr><td><label for="sysconfig-google-authtoken">Authorization Token</label></td><td><input type="text" id="sysconfig-google-authtoken" name="sysconfig-google-authtoken"/></td></tr>
                            <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save Google account settings">Save</button></td></tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>

                  <div class="cleaner">&nbsp;</div>
                </div>
                
    <table class="templates">
      <tbody>
        <tr id="dnis-row-template">
          <td>Number</td>
          <td><input type="text" value="" class="dnis-mapping-number"/></td>
          <td>maps to</td>
          <td>
            <select class="dnis-row-select">
              <listen:ifLicensed feature="CONFERENCING">
                <option value="conferencing">Conferencing</option>
              </listen:ifLicensed>
              <listen:ifLicensed feature="VOICEMAIL">
                <option value="mailbox">Mailbox</option>
                <option value="voicemail">Voicemail</option>
                <option value="directVoicemail">Direct Voicemail</option>
              </listen:ifLicensed>
              <option value="custom">Custom</option>
            </select>
          </td>
          <td><input type="text" value="" class="dnis-mapping-custom-destination"/></td>
          <td><button type="button" class="icon-delete" title="Remove this phone number"></button></td>
        </tr>
        
        <tr id="conference-bridge-row-template">
          <td>Label</td>
          <td><input type="text" value="" class="conference-bridge-number-label"/></td>
          <td>Number</td>
          <td>
            <select>
            </select>
          </td>
          <td><button type="button" class="icon-delete" title="Remove this phone number"></button></td>
        </tr>
      </tbody>
    </table>
  </body>
</html>