<html>
  <head>
    <title><g:message code="page.user.create.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="users"/>
    <meta name="button" content="create"/>
    <meta name="page-header" content="${g.message(code: 'page.user.create.header')}"/>
  </head>
  <body>
    <g:form controller="user" action="save" method="post">
      <fieldset class="vertical">
        <h3>User Details</h3>

        <label for="username"><g:message code="user.username.label"/></label>
        <g:textField name="username" value="${fieldValue(bean: user, field: 'username')}" maxlength="50" class="${listen.validationClass(bean: user, field: 'username')}"/>

        <label for="pass"><g:message code="user.pass.label"/></label>
        <g:passwordField name="pass" class="${listen.validationClass(bean: user, field: 'pass')}"/>

        <label for="confirm"><g:message code="user.confirm.label"/></label>
        <g:passwordField name="confirm" class="${listen.validationClass(bean: user, field: 'confirm')}"/>

        <label for="realName"><g:message code="user.realName.label"/></label>
        <g:textField name="realName" value="${fieldValue(bean: user, field: 'realName')}" maxlength="50" class="${listen.validationClass(bean: user, field: 'realName')}"/>

        <label for="emailAddress"><g:message code="user.emailAddress.label"/></label>
        <g:textField name="emailAddress" value="${fieldValue(bean: user, field: 'emailAddress')}" class="${listen.validationClass(bean: user, field: 'emailAddress')}"/>

        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="${g.message(code: 'default.button.create.label')}"/></li>
        </ul>
      </fieldset>
    </g:form>
    <script type="text/javascript">
$(document).ready(function() {
    $('#username').focus();
});
    </script>
  </body>
</html>