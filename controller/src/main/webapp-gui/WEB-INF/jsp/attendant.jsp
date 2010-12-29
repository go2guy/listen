<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.interact.listen.license.License" %>
<%@ page import="com.interact.listen.license.ListenFeature" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<html>
  <head>
    <title>Attendant</title>
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-attendant-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/attendant-min.css"/>">

    <meta name="body-class" content="application-attendant"/>
    <meta name="page-title" content="Attendant"/>
  </head>
  <body>
    <div id="attendant-menu-list-container">
      <table id="attendant-menu-list">
        <thead>
          <tr>
            <td colspan="3"><button type="button" id="attendant-menu-add-new-menu" class="button-add">New Menu</button></td>
          </tr>
        </thead>
        <tbody></tbody>
      </table>
    </div>

    <div id="attendant-menu-builder-container-placeholder"></div>

    <div id="attendant-menu-builder-container">
      <div id="attendant-menu-name"><label>Name:</label>&nbsp;<input type="text"/></div>

      <div id="attendant-menu-information">
        <input type="hidden" id="attendant-menu-id" name="attendant-menu-id" value=""/>
        <div id="attendant-menu-information-caption">When caller enters this menu...</div>
        <div id="attendant-menu-information-input">
          <label for="attendant-menu-audio-file">Play</label>
          <select id="attendant-menu-audio-file" name="attendant-menu-audio-file" size="1"></select>
        </div>
        <div id="attendant-menu-audio-file-transcription">
          This section would have a transcription of the audio file that is selected in the box above.
        </div>
      </div>

      <div id="attendant-menu-actions">
        <div id="attendant-menu-actions-caption">After playing the audio file...</div>
        <div id="attendant-menu-actions-container"></div>
        <div class="attendant-menu-button-container">
          <button id="attendant-menu-add-new-action" type="button" class="button-add">Add Action</button>
        </div>
        <div id="attendant-menu-actions-caption">Otherwise...</div>
        <div id="attendant-menu-actions-default-action-container"></div>
        <div id="attendant-menu-actions-timeout-action-container"></div>
        <div class="attendant-menu-button-container">
          <button type="button" class="button-cancel" id="attendant-menu-cancel">Cancel</button>
          <button type="button" class="button-save" id="attendant-menu-save">Save Menu</button>
        </div>
      </div>
    </div>
    
    <table class="templates">
      <tr id="attendant-menu-list-row-template">
        <td class="attendant-menu-list-cell-name"></td>
        <td class="attendant-menu-list-cell-edit"></td>
        <td class="attendant-menu-list-cell-delete"></td>
      </tr>
    </table>
    
    <div id="attendant-menu-action-template" class="attendant-menu-action">
        <div class="attendant-menu-action-delete"></div>
        <div class="attendant-menu-action-keypressLabel">
          <label>If Caller Presses</label>
        </div>
        <div class="attendant-menu-action-defaultLabel">
          <label>If Caller Presses Something Else</label>
        </div>
        <div class="attendant-menu-action-timeoutLabel">
          <label>If Caller Waits 5 Seconds</label><!-- TODO use property value -->
        </div>
        <div class="attendant-menu-action-keypressInput">
          <input type="text" class="attendant-menu-number-input"/>
        </div>
        <div class="attendant-menu-action-actionSelect">
          <select>
            <option value="DialNumber">Dial A Number</option>
            <option value="DialPressedNumber">Dial What They Pressed</option>
            <option value="GoToMenu">Go To A Menu</option>
            <option value="LaunchApplication">Launch An Application</option>
          </select>
        </div>
        <div class="attendant-menu-action-menuSelect">
          <select></select>
        </div>
        <div class="attendant-menu-action-dialNumberInput">
          <input type="text" class="attendant-menu-number-input"/>
        </div>
        <div class="attendant-menu-action-launchApplicationSelect">
          <select><% /* ordered alphabetically */
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
            <option value="conferencing">Conferencing</option><%
}
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
            <option value="directVoicemail">Direct Voicemail</option>
            <option value="mailbox">Mailbox</option>
            <option value="voicemail">Voicemail</option><%
} %>
            <option value="custom">Custom</option>
          </select>
        </div>
        <div class="attendant-menu-action-launchApplicationCustomInput">
          <input type="text"/>
        </div>
      </div>
  </body>
</html>