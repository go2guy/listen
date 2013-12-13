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
          <tr><td><g:select name="status" from="${optionNames}" value="${status}"/></tr></td>
          <tr><td><h3>Contact Number:</h3></td></tr>
          <tr><td><g:select name="contactNumber" from="${phoneNumbers}" value="${contactNumber}"/></tr></td>
          <tr><td><g:submitButton name="update_status" value="${g.message(code: 'default.button.update.label')}"/></td></tr>
          </g:form>
        </tbody>
      </table>
    </div>

  </body>
</html>
