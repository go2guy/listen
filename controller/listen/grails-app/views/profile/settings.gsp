<html>
  <head>
    <title><g:message code="page.profile.settings.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="profile"/>
    <meta name="button" content="settings"/>
    <style type="text/css">
fieldset {
    display: block;
    float: left;
    width: 300px;
}

.info-snippet {
    float: left;
    margin-top: 10px;
    width: 350px;
}
    </style>
  </head>
  <body>
    <h3><g:message code="page.profile.settings.header"/></h3>
    <g:form controller="profile" action="saveSettings" method="post">
      <fieldset class="vertical">
        <label for="username"><g:message code="user.username.label"/></label>
        <g:textField name="username" disabled="disabled" readonly="readonly" value="${fieldValue(bean: user, field: 'username')}"/>

        <g:if test="${!user.isActiveDirectory}">
          <label for="pass"><g:message code="user.new.pass.label"/></label>
          <g:passwordField name="pass" class="${listen.validationClass(bean: user, field: 'pass')}"/>

          <label for="confirm"><g:message code="user.new.confirm.label"/></label>
          <g:passwordField name="confirm" class="${listen.validationClass(bean: user, field: 'confirm')}"/>
        </g:if>

        <label for="realName"><g:message code="user.realName.label"/></label>
        <g:textField name="realName" value="${fieldValue(bean: user, field: 'realName')}" class="${listen.validationClass(bean: user, field: 'realName')}"/>

        <label for="emailAddress"><g:message code="user.emailAddress.label"/></label>
        <g:textField name="emailAddress" value="${fieldValue(bean: user, field: 'emailAddress')}" class="${listen.validationClass(bean: user, field: 'emailAddress')}"/>

        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/></li>
        </ul>
      </fieldset>
    </g:form>

    <g:if test="${user.isActiveDirectory}">
      <listen:infoSnippet summaryCode="page.profile.settings.snippet.summary.ad" contentCode="page.profile.settings.snippet.content.ad"/>
    </g:if>
  </body>
</html>