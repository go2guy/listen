<html>
  <head>
    <title><g:message code="page.administration.android.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="android"/>
    <meta name="page-header" content="${g.message(code: 'page.administration.android.header')}"/>
    <style type="text/css">
textarea {
    display: block;
    height: 80px;
    width: 550px;
}

fieldset.vertical .auth-option,
.or-separator {
    display: inline-block;
    margin-right: 10px;
}

    </style>
  </head>
  <body>
    <g:form controller="administration" action="saveAndroid">
      <fieldset class="vertical">
        <label for="isEnabled">
          <g:checkBox name="isEnabled" value="${googleAuthConfiguration?.isEnabled}"/>
          <g:message code="googleAuthConfiguration.isEnabled.label"/>
        </label>

        <div id="auth-settings">
          <label for="authUser"><g:message code="googleAuthConfiguration.authUser.label"/></label>
          <g:textField name="authUser" value="${fieldValue(bean: googleAuthConfiguration, field: 'authUser')}" class="${listen.validationClass(bean: googleAuthConfiguration, field: 'authUser')}"/>

          <label for="authPass" class="auth-option">
            <g:message code="googleAuthConfiguration.authPass.label"/>
            <g:passwordField name="authPass" value="${fieldValue(bean: googleAuthConfiguration, field: 'authPass')}" class="${listen.validationClass(bean: googleAuthConfiguration, field: 'authPass')}"/>
          </label>

          <div class="or-separator">-or-</div>

          <label for="authToken" class="auth-option">
            <g:message code="googleAuthConfiguration.authToken.label"/>
            <g:textArea name="authToken" value="${fieldValue(bean: googleAuthConfiguration, field: 'authToken')}" class="${listen.validationClass(bean: googleAuthConfiguration, field: 'authToken')}"/>
          </label>
        </div>

        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/></li>
        </ul>
      </fieldset>
    </g:form>
    <script type="text/javascript">
$(document).ready(function() {
    function toggleAuthSettings() {
        $('#auth-settings').toggle($('#isEnabled').is(':checked'));
    }
    toggleAuthSettings();
    $('#isEnabled').click(toggleAuthSettings);
});
    </script>
  </body>
</html>