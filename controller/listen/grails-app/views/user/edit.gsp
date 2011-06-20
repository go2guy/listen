<html>
  <head>
    <title><g:message code="page.user.edit.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="users"/>
    <meta name="button" content="edit"/>
    <meta name="page-header" content="${g.message(code: 'page.user.edit.header')}"/>
  </head>
  <body>
    <g:form controller="user" action="update" method="post">
      <fieldset class="vertical">
        <g:hiddenField name="id" value="${user.id}"/>

        <label for="username"><g:message code="user.username.label"/></label>
        <g:textField name="username" value="${fieldValue(bean: user, field: 'username')}" maxlength="50"${user?.isActiveDirectory ? ' disabled="disabled" readonly="readonly" class="disabled"' : ''}/>

        <g:if test="${user?.isActiveDirectory}">
          <h3 class="active-directory-label">This is an Active Directory account.</h3>
        </g:if>
        <g:else>
          <label for="pass"><g:message code="user.new.pass.label"/></label>
          <g:passwordField name="pass"/>

          <label for="confirm"><g:message code="user.new.confirm.label"/></label>
          <g:passwordField name="confirm"/>
        </g:else>

        <label for="realName"><g:message code="user.realName.label"/></label>
        <g:textField name="realName" value="${fieldValue(bean: user, field: 'realName')}" maxlength="50"/>

        <label for="emailAddress"><g:message code="user.emailAddress.label"/></label>
        <g:textField name="emailAddress" value="${fieldValue(bean: user, field: 'emailAddress')}"/>

        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/></li>
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