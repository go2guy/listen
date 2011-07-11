<html>
  <head>
    <title><g:message code="page.attendant.holidays.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="attendant"/>
    <meta name="button" content="holidays"/>
    <style type="text/css">
.col-date { width: 32%; }
.col-menuGroup { width: 26%; }
.col-prompt { width: 26%; }
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
    </style>
  </head>
  <body>
    <listen:infoSnippet summaryCode="page.attendant.holidays.snippet.summary" contentCode="page.attendant.holidays.snippet.content"/>

    <g:uploadForm controller="attendant" action="addHoliday" method="post">
      <fieldset class="vertical">
        <h3><g:message code="page.attendant.holidays.add.header"/></h3>
        <label for="date"><g:message code="promptOverride.date.label"/></label>
        <span class="date-fields">
          <joda:datePicker name="date" value="${newPromptOverride?.date}"/>
        </span>

        <label for="menuGroup"><g:message code="promptOverride.menuGroup.label"/></label>
        <listen:menuGroupSelect name="menuGroup.id" value="${newPromptOverride?.menuGroup?.id}"/>

        <label for="optionsPrompt" class="or-option">
          <g:message code="page.attendant.holidays.select.prompt.label"/>
          <listen:rawPromptSelect name="optionsPrompt" value="${newPromptOverride?.optionsPrompt}"/>
        </label>

        <div class="or-separator"><g:message code="page.attendant.holidays.or.separator"/></div>

        <label for="uploadedPrompt" class="or-option">
          <g:message code="page.attendant.holidays.upload.prompt.label"/>
          <input type="file" name="uploadedPrompt" id="uploadedPrompt"/>
        </label>

        <ul class="form-buttons">
          <li><g:link controller="attendant" action="holidays"><g:message code="default.button.discard.label"/></g:link></li>
          <li><g:submitButton name="add" value="${g.message(code: 'page.attendant.holidays.button.add.label')}"/></li>
        </ul>
      </fieldset>
    </g:uploadForm>

    <table>
      <thead>
        <g:sortableColumn class="col-date" property="date" title="${g.message(code: 'promptOverride.date.label')}"/>
        <g:sortableColumn class="col-menuGroup" property="menuGroup" title="${g.message(code: 'promptOverride.menuGroup.label')}"/>
        <g:sortableColumn class="col-prompt" property="optionsPrompt" title="${g.message(code: 'promptOverride.optionsPrompt.label')}"/>
        <th></th>
        <th></th>
      </thead>
      <tbody>
        <g:if test="${promptOverrideList.size() > 0}">
          <g:each in="${promptOverrideList}" var="promptOverride" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <td class="col-date"><joda:format value="${promptOverride.date}" pattern="MMMM d, yyyy"/></td>
              <td class="col-menuGroup">${fieldValue(bean: promptOverride.menuGroup, field: 'name')}</td>
              <td class="col-prompt">${fieldValue(bean: promptOverride, field: 'optionsPrompt')}</td>
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