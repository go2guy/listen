<html>
  <head>
    <title><g:message code="page.profile.afterHours.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="profile"/>
    <meta name="button" content="afterHours"/>
    <meta name="page-header" content="${g.message(code: 'page.profile.afterHours.header')}"/>
    <style type="text/css">
fieldset.vertical label {
    display: inline-block;
}

fieldset.vertical input[type="text"],
fieldset.vertical select {
    display: block;
}
    </style>
  </head>
  <body>
    <g:if test="${afterHoursConfiguration?.phoneNumber}">
      <span>The after hours phone number is <b>${afterHoursConfiguration?.phoneNumber?.number?.encodeAsHTML()}</b></span>

      <g:form controller="profile" action="saveAfterHours">
        <fieldset class="vertical">
          <label for="alternateNumber">
            <g:message code="afterHoursConfiguration.alternateNumber.label"/>
            <g:textField name="alternateNumber" value="${afterHoursConfiguration?.alternateNumberComponents()?.number?.encodeAsHTML()}"/>
          </label>

          <label for="provider">
            Mobile Provider
            <listen:mobileProviderSelect name="provider" value="${afterHoursConfiguration?.alternateNumberComponents()?.provider}"/>
          </label>

          <ul class="form-buttons">
            <li><g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/></li>
          </ul>
        </fieldset>
      </g:form>
    </g:if>
    <g:else>
      The administrator has not configured the After Hours phone number; the After Hours service is disabled.
    </g:else>
  </body>
</html>