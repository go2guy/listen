<html>
  <head>
    <title><g:message code="page.reports.list.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="reports"/>
    <style type="text/css">
ul#available-reports li {
    display: inline;
}

ul#available-reports li img {
    border: 2px solid #E4A634;
    border-radius: 5px;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
    box-shadow: 0 5px 5px #CCC;
    -moz-box-shadow: 0 5px 5px #CCC;
    -webkit-box-shadow: 0 5px 5px #CCC;
}

ul#available-reports li img:hover {
    opacity: .7;
}
    </style>
  </head>
  <body>
    <h3>Available Reports</h3>
    <ul id="available-reports">
      <li><g:link controller="reports" action="callVolumeByUser" alt="Call Volume By User"><img src="${resource(dir: 'resources/app/images', file: 'report-callVolumeByUser.png')}" width="300" height="218"/></g:link></li>
    </ul>
  </body>
</html>