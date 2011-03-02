<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<%@ page import="com.interact.listen.EmailerUtil" %>
<%@ page import="com.interact.listen.ServletUtil" %>
<%@ page import="com.interact.listen.resource.Subscriber" %>
<html>
  <head>
    <title>Settings</title>
    <script type="text/javascript" src="<listen:resource path="/resources/app/js/app-profile.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/lib/forms.css"/>">
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/settings.css"/>">

    <meta name="body-class" content="application-settings"/>
    <meta name="page-title" content="Settings"/>
  </head>
  <body>
  <%
Subscriber subscriber = ServletUtil.currentSubscriber(request); %>
    <div class="tab-container">
      <ul class="tabs">
        <li><a href="#">General</a></li>
        <listen:ifLicensed feature="VOICEMAIL">
          <li><a href="#">Voicemail</a></li>
        </listen:ifLicensed>
        <li><a href="#">Phone Numbers</a></li>
        <li><a href="#">After Hours Pager</a></li>
      </ul>
      <div class="tab-content-default">
        <form id="generalSettingsForm">
          <fieldset class="vertical">
            <label for="username">
              Username
              <input type="text" id="username" class="disabled" disabled="disabled" readonly="readonly"/>
            </label>
              
            <label for="accountType">
              Account Type
              <input type="text" id="accountType" class="disabled" disabled="disabled" readonly="readonly"/>
            </label>
                  
            <fieldset class="side-by-side clearfix">
              <label for="password">
                Password
                <input type="password" id="password"/>
              </label>
            
              <label for="passwordConfirm">
                Confirm
                <input type="password" id="passwordConfirm"/>
              </label>
            </fieldset>
                      
            <label for="realName">
              Real Name
              <input type="text" id="realName"/>
            </label>
          
            <label for="emailAddress">
              Email Address
              <input type="text" id="emailAddress"/>
            </label>

            <input type="submit" class="first" value="Save"/>
          </fieldset>
        </form>
      </div>
                  
      <listen:ifLicensed feature="VOICEMAIL">

        <div class="tab-content">
          <form id="voicemailSettingsForm">
            <fieldset class="vertical">
              <label for="voicemailPasscode">
                Voicemail Passcode
                <input type="text" id="voicemailPasscode"/>
                <span class="annotation">4 numbers, used when calling the IVR</span>
              </label>

              <label for="playbackOrder">
                Playback Order
                <select id="playbackOrder">
                  <option value="NEWEST_TO_OLDEST">Newest to Oldest</option>
                  <option value="OLDEST_TO_NEWEST">Oldest to Newest</option>
                </select>
                <span class="annotation">Order voicemails are played on the IVR</span>
              </label>

              <h2>When I receive a new voicemail:</h2>
              <fieldset class="group">
                <label for="transcribeVoicemail">
                    <input type="checkbox" class="inline" id="transcribeVoicemail"/>
                    Transcribe the voicemail
                    <span class="annotation">If checked, messages below will not be sent until the transcription is complete</span>
                </label>

                <label for="sendEmail">
                    <input type="checkbox" class="inline" id="sendEmail"/>
                    Send me an email
                </label>

                <fieldset class="group" id="sendEmailOptions">
                  <label for="sendEmailUseCurrent">
                    <input type="radio" class="inline" id="sendEmailUseCurrent" name="sendEmailUse"/>
                    Use my current email address (<span id="sendEmailMyAddress">Not Set</span>)
                  </label>

                  <label for="sendEmailUseAnother">
                    <input type="radio" class="inline" id="sendEmailUseAnother" name="sendEmailUse"/>
                    Use another address
                  </label>

                  <fieldset class="side-by-side clearfix">
                    <label for="sendEmailOtherAddress">
                      <input type="text" id="sendEmailOtherAddress"/>
                    </label>
                  </fieldset>
                  
                  <label>
                      <button class="button" id="sendTestEmail">Send Test Email</button>
                  </label>
                  
                  <label for="sendEmailRestrictTime">
                    <input type="checkbox" class="inline" id="sendEmailRestrictTime"/>
                    Only send during certain times
                  </label>

                  <fieldset class="time-restrictions" id="sendEmailTimeRestrictions">
                    <button type="button" id="addEmailTimeRestriction">Add Time Period</button>
                  </fieldset>

                </fieldset>

                <label for="sendSms">
                  <input type="checkbox" class="inline" id="sendSms"/>
                  Send me an SMS
                </label>
                        
                <fieldset class="group" id="sendSmsOptions">
                  <fieldset class="side-by-side clearfix">
                    <label for="sendSmsNumber">
                      Phone Number
                      <input type="text" id="sendSmsNumber"/>
                    </label>

                    <label for="sendSmsNumberProvider">
                      Service Provider
                      <listen:mobileProviderSelect id="sendSmsNumberProvider" withEmpty="true"/>
                    </label>

                    <label>
                      &nbsp;
                      <button class="button" id="sendTestSms">Send Test SMS</button>
                    </label>
                  </fieldset>
                          
                  <label for="keepSendingSms">
                    <input type="checkbox" class="inline" id="keepSendingSms"/>
                    Send the SMS every 10 minutes until I listen to the voicemail
                  </label>
                  
                  <label for="sendSmsRestrictTime">
                    <input type="checkbox" class="inline" id="sendSmsRestrictTime"/>
                    Only send during certain times
                  </label>
                  
                  <fieldset class="time-restrictions" id="sendSmsTimeRestrictions">
                    <button type="button" id="addSmsTimeRestriction">Add Time Period</button>
                  </fieldset>
                </fieldset>
              </fieldset>

              <input type="submit" class="first" value="Save"/>
            </fieldset>
          </form>
        </div>
      </listen:ifLicensed>
                  
      <div class="tab-content" id="phoneNumbersTab">
        <form id="phoneNumbersForm">
          <fieldset id="phoneNumbersButtons" class="vertical">
            <input type="submit" class="first" value="Save"/>
            <button type="button" id="addAnotherNumber">Add Another Number</button>
          </fieldset>
        </form>
      </div>

      <div class="tab-content">
        <form id="afterHoursForm">
          <fieldset class="vertical">
            <label for="pagerNumber">
              Pager Number
              <input type="text" id="pagerNumber" class="disabled" disabled="disabled" readonly="readonly"/>
            </label>
          
            <fieldset class="side-by-side">
              <label for="alternatePagerNumber">
                Alternate Number
                <input type="text" id="alternatePagerNumber" name="alternatePagerNumber" maxlength="14"/>
              </label>
            
              <label for="alternatePagerNumberProvider">
                Service Provider
                <listen:mobileProviderSelect id="alternatePagerNumberProvider"/>
              </label>
            </fieldset>
          
            <label for="pagePrefix">
              Page Prefix
              <input type="text" id="pagePrefix" name="pagePrefix" maxlength="20"/>
              <span class="annotation">Will be prefixed to pager SMS messages</span>
            </label>
          
            <input type="submit" class="first" value="Save"/>
          </fieldset>
        </form>
      </div>
    </div>

	<div class="templates">
      <fieldset id="phoneNumberTemplate" class="vertical">
        <fieldset class="side-by-side">

          <label>
            Phone Number
            <input type="text" class="phone-number"/>
            <span class="annotation">e.g. 14024768786 or 101</span>
          </label>

          <label>
            Category
            <select class="phone-number-category">
              <option value="HOME">Home</option>
              <option value="MOBILE">Mobile</option>
              <option value="OTHER">Other</option>
            </select>
          </label>

<!--          <label class="phone-number-service-provider">
            Service Provider
            <listen:mobileProviderSelect/>
          </label>-->
        </fieldset>

        <label>
          Forward this number to
          <input type="text" class="forwarded-to"/>
          <span class="annotation">Leave blank to disable forwarding</span>
        </label>

<!--        <label>
          <input type="checkbox" class="inline message-light"/>
          Supports message light indicator
        </label>-->

        <label>
          <input type="checkbox" class="inline public-number"/>
          Show in other peoples' contact lists
        </label>

        <button type="button" class="first delete-button">Delete This Number</button>
      </fieldset>
      
      <fieldset class="period-selector" id="period-selector-template">
        <label>
          From <input type="text" class="time from-time" value="8:00"/>
        </label>
        <ul class="togglebox meridian from-meridiem"><li class="am default">AM</li><li class="pm">PM</li></ul>
        <label>
          To <input type="text" class="time to-time" value="5:00"/>
        </label>
        <ul class="togglebox meridian to-meridiem"><li class="am">AM</li><li class="pm default">PM</li></ul>
        <label>On&nbsp;</label>
        <fieldset class="togglebox-group">
          <ul class="togglebox monday"><li class="on">M</li><li class="off default">M</li></ul>
          <ul class="togglebox tuesday"><li class="on">T</li><li class="off default">T</li></ul>
          <ul class="togglebox wednesday"><li class="on">W</li><li class="off default">W</li></ul>
          <ul class="togglebox thursday"><li class="on">T</li><li class="off default">T</li></ul>
          <ul class="togglebox friday"><li class="on">F</li><li class="off default">F</li></ul>
          <ul class="togglebox saturday"><li class="on">S</li><li class="off default">S</li></ul>
          <ul class="togglebox sunday last"><li class="on">S</li><li class="off default">S</li></ul>
        </fieldset>
        <button type="button">Delete</button>
      </fieldset>
    </div>
  </body>
</html>