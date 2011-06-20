<html>
  <head>
    <title><g:message code="page.administration.history.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="history"/>
    <style type="text/css">
table { margin-bottom: 10px; }
table tbody { font-size: 12px; }

.col-dateTime { width: 20%; }
.col-ani { width: 25%; }
.col-dnis { width: 25%; }
.col-duration { width: 15%; }
.col-result { width: 15%; }

.col-dateCreated { width: 15%; }
.col-byUser { width: 18%; }
.col-onUser { width: 18%; }
.col-description { width: 44%; }
.col-channel { width: 5%; }
    </style>
  </head>
  <body>
    <tmpl:/shared/history personalize="${false}"/>
  </body>
</html>