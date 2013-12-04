<html>
  <head>
    <title><g:message code="page.acd.status.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="acd"/>
    <meta name="button" content="status"/>
  </head>
  <body>

    <div id="status" class="panel">
      <h3>Status:</h3>
      <table>
        <tbody>
          <g:form controller="acd" action="updateStatus" method="post">
          <tr><td>${sec.authenticatedUser.status.toString()}</td></tr>
          <tr><td>Timer: 00:00:00</td></tr>
          <tr><td><g:submitButton name="update_status" value="${g.message(code: 'default.button.update.label')}"/></td></tr>
          </g:form>
        </tbody>
      </table>
    </div>

  </body>
</html>
