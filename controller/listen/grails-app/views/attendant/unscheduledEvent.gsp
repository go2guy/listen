<%@ page import="org.joda.time.DateTime" %>
<html>
  <head>
    <title><g:message code="page.attendant.unscheduledEvent.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="attendant"/>
    <meta name="button" content="unscheduledEvent"/>
    <style type="text/css">

        fieldset.vertical {
            background: #F0F0F0;
            padding: 1px 10px 10px 10px;
        }

        .hidden { display: none; }

        .eventDetail {
            padding-top: 20px;
            /*color: red;*/
            font-weight: bold;
        }

        .startEventButton
        {
            color: white;
            background-color: green;
        }

        input[type=submit].endEventButton,
        input[type=button].endEventButton,
        input[type=reset].endEventButton,
        button.endEventButton{
            color: white;
            background-color: red;
        }

        input[type=submit].startEventButton,
        input[type=button].startEventButton,
        input[type=reset].startEventButton,
        button.endEventButton{
            color: white;
            background-color: green;
        }

        input[type=submit].inactiveButton,
        input[type=button].inactiveButton,
        input[type=reset].inactiveButton,
        button.inactiveButton{
            color: black;
            background-color: #d3d3d3;
        }

        .form-buttons
        {
            padding-left: 60px;
        }


    </style>
  </head>
  <body>
    <listen:infoSnippet summaryCode="page.attendant.unscheduledEvent.snippet.summary" contentCode="page.attendant.unscheduledEvent.snippet.content"/>
    <g:uploadForm controller="attendant" action="updateUnscheduledEvent" method="post">
      <fieldset class="vertical">
        <h3><g:message code="page.attendant.holidays.unscheduledEvent.header"/></h3>
          <div>
              <label for="optionsPrompt" class="or-option">
                  <g:message code="page.attendant.holidays.select.prompt.label"/>
                  <listen:rawAttendantPromptSelect name="optionsPrompt" value="${currentEvent ? currentEvent.optionsPrompt : lastEvent.optionsPrompt}" disabled="${currentEvent ? 'true' : 'false'}"/>
              </label>
          </div>

          <label for="useMenu.id"><g:message code="promptOverride.useMenu.label"/></label>
          <listen:menuGroupSelect name="useMenu.id" value="${currentEvent ? currentEvent.useMenu?.id : lastEvent.useMenu?.id}" disabled="${currentEvent ? 'true' : 'false'}"/>

          <div id="currentEventDetails" class="eventDetail ${currentEvent ? '' : 'hidden'}">
            <h3>Current Event</h3>
            <ul style="padding-left: 10px;">Started: <joda:format value="${currentEvent?.startDate}" pattern="hh:mm aa"/></ul>
            <ul style="padding-left: 10px;">Ends: <joda:format value="${currentEvent?.endDate}" pattern="hh:mm aa"/></ul>
          </div>

          <div id="activeEventDetails" class="eventDetail ${activeEvent && !currentEvent ? '' : 'hidden'}">
              <h3>There is currently an active event.</h3>
          </div>

            <ul class="form-buttons">
              <g:hiddenField name="id" value="${currentEvent ? currentEvent.id : null}"/>
              <li><g:submitButton class="${currentEvent ? 'endEventButton' : 'startEventButton'} ${activeEvent && !currentEvent ? 'inactiveButton' : ''}" name="eventStatus"
                                  value="${currentEvent ? 'End Event' : 'Start Event'}"
                                  disabled="${activeEvent && !currentEvent ? 'true' : 'false'}"/></li>
%{--
              <button type="button" class="eventButton" id="eventButton"
                  value="Start Event"
                  onclick="buttClicked(this, this.value)">Voicemail</button>
--}%
            </ul>
      </fieldset>
    </g:uploadForm>
    <script type="text/javascript">
    </script>
  </body>
</html>