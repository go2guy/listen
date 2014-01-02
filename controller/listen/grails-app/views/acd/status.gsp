<html>
  <head>
    <title><g:message code="page.acd.status.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="acd"/>
    <meta name="button" content="status"/>
    
    <style type="text/css">
        td.col-button {
            text-align: left;
            width: 10%;
        }
    </style>
    
  </head>
  <body>

      <h3>${g.message(code: 'page.acd.status.title')}</h3>
          
      <table>
        <tbody>
          <g:form controller="acd" action="toggleStatus" method="post">
              <tr>
                  <td class="col-button">
                      <g:set var="titleMessage" value="" />
                      <g:submitButton name="toggle_status" value="${status}" class="${status}"
                                      title="${g.message(code: 'page.acd.status.button.' + status)}"
                                      disabled="${statusDisabled}" />
                  </td>
              </tr>
          </g:form>
          <g:form controller="acd" action="updateNumber" method="post">
              <tr><td><h3>Contact Number:</h3></td></tr>
              <tr><td><g:select name="contactNumber" from="${phoneNumbers}" value="${contactNumber}"/></tr></td>
              <tr><td><g:submitButton name="update_status" value="${g.message(code: 'default.button.update.label')}"/>
              </td></tr>
          </g:form>
        </tbody>
      </table>

  </body>
</html>
