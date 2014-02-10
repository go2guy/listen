<%@ page import="org.joda.time.DateTime" %>
<html>
  <head>
    <title><g:message code="page.attendant.holidays.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="attendant"/>
    <meta name="button" content="holidays"/>
    <style type="text/css">
        .col-date { width: 18%; }
        .col-startTime { width: 10%; }
        .col-endTime { width: 10%; }
        .col-useMenu { width: 20%; }
        .col-prompt {
            width: 35%;
            padding-left: 20px;
        }
        .col-button {
            text-align: center;
            width: 16%;
        }

        fieldset.vertical {
            background: #F0F0F0;
            padding: 1px 10px 10px 10px;
        }

        fieldset.vertical .date-fields select {
            display: inline;
            width: auto;
        }

        fieldset.vertical input[type=file] {
            display: block;
        }

        fieldset.vertical .or-option,
        fieldset.vertical .or-separator {
            display: inline-block;
            margin-right: 10px;
        }

        .hidden { display: none; }
        .inline { display: inline; }

        .holidayTimePicker { padding-left: 30px; }

        .eventLine { padding-bottom: 10px; }
    </style>
  </head>
  <body>
    <g:if test="${flash.errorMessage}">
        <script type="text/javascript">
            listen.showErrorMessage('${flash.errorMessage}')
        </script>
    </g:if>
    <listen:infoSnippet summaryCode="page.attendant.holidays.snippet.summary" contentCode="page.attendant.holidays.snippet.content"/>

    <g:uploadForm controller="attendant" action="addHoliday" method="post">
      <fieldset class="vertical">
        <h3><g:message code="page.attendant.holidays.add.header"/></h3>
        <label for="date"><g:message code="promptOverride.date.label"/></label>
        <div style="display: inline">
            <ul class="date-fields eventLine">
              <joda:datePicker name="date" value="${newPromptOverride?.date}"/>
              <g:checkBox id="allDayCheckbox" name="allDayCheckbox" value="" class="checkbox allDayCheckbox"
                          checked="true" onchange="toggleAllDay(this, this.value)"/>
              <label for="allDayCheckbox" style="display: inline;" class="allDayCheckboxLabel">All day</label>
              <div id="holidayTimePickerDiv" class="holidayTimePicker hidden" >
                <label for="holidayStart" style="display: inline;">Start:
                    <listen:timePicker name="holidayStart" value='${new DateTime().withHourOfDay(0).withMinuteOfHour(0)}'/>
                </label>
                <label for="holidayEnd" style="display: inline;">End:
                    <listen:timePicker name="holidayEnd" value='${new DateTime().withHourOfDay(23).withMinuteOfHour(59)}'/>
                </label>
              </div>

            </ul>
        </div>

        <div class="eventLine">
            <label for="optionsPrompt" class="or-option">
              <g:message code="page.attendant.holidays.select.prompt.label"/>
              <listen:rawAttendantPromptSelect name="optionsPrompt" value="${newPromptOverride?.optionsPrompt}"/>
            </label>

            <div class="or-separator"><g:message code="page.attendant.holidays.or.separator"/></div>

            <label for="uploadedPrompt" class="or-option">
              <g:message code="page.attendant.holidays.upload.prompt.label"/>
              <input type="file" name="uploadedPrompt" id="uploadedPrompt"/>
            </label>
        </div>

        <div class="eventLine">
            <label for="useMenu.id"><g:message code="promptOverride.useMenu.label"/></label>
            <listen:menuGroupSelect name="useMenu.id" value="${newPromptOverride?.useMenu?.id}"/>
        </div>

        <ul class="form-buttons">
          <li><g:submitButton name="add" value="${g.message(code: 'page.attendant.holidays.button.add.label')}"/></li>
          <li><g:link controller="attendant" action="holidays"><g:message code="default.button.discard.label"/></g:link></li>
        </ul>
      </fieldset>
    </g:uploadForm>

    <table>
      <thead>
        <g:sortableColumn class="col-date" property="date" title="${g.message(code: 'promptOverride.date.label')}"/>
        <g:sortableColumn class="col-startTime" property="startTime" title="Start Time"/>
        <g:sortableColumn class="col-endTime" property="endTime" title="End Time"/>
        <g:sortableColumn class="col-prompt" property="optionsPrompt" title="${g.message(code: 'promptOverride.optionsPrompt.label')}"/>
        <g:sortableColumn class="col-useMenu" property="useMenu" title="${g.message(code: 'promptOverride.useMenu.label')}"/>
        <th></th>
        <th></th>
      </thead>
      <tbody>
        <g:if test="${promptOverrideList.size() > 0}">
          <g:each in="${promptOverrideList}" var="promptOverride" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <td class="col-date"><joda:format value="${promptOverride.startDate}" pattern="MMMM d, yyyy"/></td>
              <td class="col-startTime"><joda:format value="${promptOverride.startDate}" pattern="HH:mm"/></td>
              <td class="col-endTime"><joda:format value="${promptOverride.endDate}" pattern="HH:mm"/></td>
              <td class="col-prompt">${fieldValue(bean: promptOverride, field: 'optionsPrompt')}</td>
              <td class="col-useMenu">${fieldValue(bean: promptOverride.useMenu, field: 'name')}</td>
              <g:form controller="attendant" action="deleteHoliday">
                <td class="col-button">
                  <g:hiddenField name="id" value="${promptOverride.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </g:if>
        <g:else>
          <tr><td colspan="4"><g:message code="page.attendant.holidays.noHolidays"/></td></tr>
        </g:else>
      </tbody>
    </table>
    <script type="text/javascript">

        function toggleAllDay(e, value) {
            var checkbox = $('#allDayCheckbox')[0];
            if(checkbox.checked)
            {
                $('#holidayTimePickerDiv').removeClass('inline');
                $('#holidayTimePickerDiv').addClass('hidden');
            }
            else
            {
                $('#holidayTimePickerDiv').removeClass('hidden');
                $('#holidayTimePickerDiv').addClass('inline');
            }
            return true;
        }

        function getStartTime(e) {
            var start = {
                startTime: {
                    h: 00,
                    m: 00
                }
            }

            return start;
        }

$(document).ready(function() {
    $('#uploadedPrompt').change(function(e) {
        var hasUpload = $(e.target).val() != '';
        if(hasUpload) {
            $('#optionsPrompt').attr('disabled', 'disabled').attr('readonly', 'readonly');
        } else {
            $('#optionsPrompt').removeAttr('disabled').removeAttr('readonly');
        }
    })
});
    </script>
  </body>
</html>