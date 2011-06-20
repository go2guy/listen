<body>
  <head>
    <title>Listen</title>
    <link rel="shortcut icon" href="${resource(dir: 'resources/app/images', file: 'favicon.ico')}">
    <link rel="stylesheet" href="${resource(dir: 'resources/yui-2.8.0r4', file: 'reset-fonts.css')}">
    <link rel="stylesheet" href="${resource(dir: 'resources/app/css', file: 'login.css')}">
    <script type="text/javascript" src="${resource(dir: 'resources/jquery', file: 'jquery-1.4.2.min.js')}"></script>
  </head>
  <body>
    <div id="left">
<!--      <div id="logo"><img src="${request.contextPath}/logo/large" width="172" height="73"/></div>-->
      <div id="logo"><img src="${resource(dir: 'resources/app/images', file: 'listen_logo_172x73.png')}" width="172" height="73"/></div>
    </div>
    <div id="right">
      <div id="login">
        <form action="${postUrl}" method="post" id="loginForm">
          <fieldset>
            <g:if test="${flash.message}">
              <ul class="messages error"><li>${flash.message}</li></ul>
            </g:if>

            <label for="organization"><g:message code="page.login.auth.organization.label"/></label>
            <g:select name="organization" from="${organizations}" optionKey="id" optionValue="name" noSelection="['-1': 'N/A']"/>

            <label for="username"><g:message code="page.login.auth.username.label"/></label>
            <g:textField name="username" placeholder="${g.message(code: 'page.login.auth.username.label')}"/>

            <label for="j_password"><g:message code="page.login.auth.password.label"/></label>
            <g:passwordField name="j_password" placeholder="${g.message(code: 'page.login.auth.password.label')}"/>

            <g:hiddenField name="j_username" value=""/>
            <g:submitButton name="loginButton" value="${g.message(code: 'page.login.auth.button.login')}" class="button"/>
          </fieldset>
        </form>
      </div>
    </div>

    <script type="text/javascript">
(function(){
	document.forms['loginForm'].elements['organization'].focus();
    $('#loginForm').submit(function(e) {
        var org = $('#organization').val();
        var username = $('#username').val();
        if(parseInt(org, 10) < 0) {
            $('#j_username').val(username);
        } else {
            $('#j_username').val(org + ':' + username);
        }
        return true; // continue form submission
    });
})();
    </script>
  </body>
</html>
