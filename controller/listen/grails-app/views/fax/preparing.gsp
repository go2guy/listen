<html>
  <head>
    <title><g:message code="page.fax.preparing.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="messages"/>
    <meta name="button" content="sendfax"/>
    <style type="text/css">

#ready {
    border: 2px solid #176BA3;
    padding: 10px;

    border-radius: 10px;
}

h4 {
    margin: 0 0 10px 0;
    padding: 0;
}

ol {
    margin: 0 0 10px 40px;
    padding: 0;
}

    </style>
  </head>
  <body>
    <div id="ready">
      <h4>Your fax is ready to be sent.</h4>
      <div id="pageCount"></div>
      <g:form action="send" method="post">
        <g:hiddenField name="id" value="${fax.id}"/>
        <g:submitButton name="send" value="Send Fax"/>
      </g:form>
    </div>
  </body>
</html>