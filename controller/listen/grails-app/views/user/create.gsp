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
        <g:textField name="username" value="${fieldValue(bean: user, field: 'username')}" maxlength="50"/>

        <label for="pass"><g:message code="user.pass.label"/></label>
        <g:passwordField name="pass"/>

        <label for="confirm"><g:message code="user.confirm.label"/></label>
        <g:passwordField name="confirm"/>

        <label for="realName"><g:message code="user.realName.label"/></label>
        <g:textField name="realName" value="${fieldValue(bean: user, field: 'realName')}" maxlength="50"/>

        <label for="emailAddress"><g:message code="user.emailAddress.label"/></label>
        <g:textField name="emailAddress" value="${fieldValue(bean: user, field: 'emailAddress')}"/>

        <h3>Office Phone (Optional)</h3>
        <label for="extension"><g:message code="phoneNumber.number.label"/></label>
        <g:textField name="extension" value="${params.extension?.encodeAsHTML()}"/>

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