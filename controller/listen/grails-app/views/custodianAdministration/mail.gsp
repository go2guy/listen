<html>
  <head>
    <title><g:message code="page.custodianAdministration.mail.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="custodianAdministration"/>
    <meta name="button" content="mail"/>
  </head>
  <body>
    <fieldset class="vertical">
      <g:form controller="custodianAdministration" action="saveMail" method="post">
        <h3>Mail Server Settings</h3>

        <label for="host">Host</label>
        <g:textField name="host" value="${g.fieldValue(bean: mail, field: 'host')}" class="${listen.validationClass(bean: mail, field: 'host')}"/>

        <label for="username">Username</label>
        <g:textField name="username" value="${g.fieldValue(bean: mail, field: 'username')}" class="${listen.validationClass(bean: mail, field: 'username')}"/>

        <label for="password">Password</label>
        <g:passwordField name="password" value="${g.fieldValue(bean: mail, field: 'password')}" class="${listen.validationClass(bean: mail, field: 'password')}"/>

        <label for="defaultFrom">Default From Address</label>
        <g:textField name="defaultFrom" value="${g.fieldValue(bean: mail, field: 'defaultFrom')}" class="${listen.validationClass(bean: mail, field: 'defaultFrom')}"/>

        <ul class="form-buttons">
          <li><g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/></li>
        </ul>
      </g:form>
    </fieldset>
  </body>
</html>