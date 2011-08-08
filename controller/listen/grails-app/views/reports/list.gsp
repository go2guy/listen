<html>
  <head>
    <title><g:message code="page.reports.list.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="reports"/>
    <style type="text/css">
ul#available-reports {
    list-style-type: disc;
    margin: 10px 20px;
}
    </style>
  </head>
  <body>
    <h3>Available Reports</h3>
    <ul id="available-reports">
      <li><g:link controller="reports" action="callVolumesByUser">Call Volumes By User</g:link></li>
    </ul>
  </body>
</html>