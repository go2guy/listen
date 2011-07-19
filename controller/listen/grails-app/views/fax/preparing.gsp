<html>
  <head>
    <title><g:message code="page.fax.preparing.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="messages"/>
    <meta name="button" content="sendfax"/>
    <style type="text/css">

#preparing,
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

#progress-bar {
    float: right;
}

#progress-status {
    font-size: 13px;
    padding-left: 15px;
}

#pageCount {
    margin-bottom: 5px;
}

.be-patient {
    font-size: 13px;
    font-style: italic;
}

<g:if test="${!fax.merged}">
#ready {
    display: none;
}
</g:if>

    </style>
  </head>
  <body>

    <g:if test="${!fax.merged}">
      <div id="preparing">
        <g:set var="size" value="${fax.toMerge.size()}"/>
        <div id="progress-bar">
          <img src="${resource(dir: 'resources/app/images', file: 'progress.gif')}" alt="Preparing..."/>
          <div id="progress-status">Reticulating splines...</div>
        </div>
        <h4>Please wait while we <g:if test="${size > 1}">merge and </g:if>convert the following file<g:if test="${size > 1}">s</g:if>:</h4>
        <ol>
         <g:each in="${fax.toMerge}" var="${file}" status="i">
           <li>${file.file.name.encodeAsHTML()}</li>
         </g:each>
        </ol>
        <span class="be-patient">This may take a couple of minutes, depending on the size of the files.</span>
      </div>
    </g:if>

    <div id="ready">
      <h4>Your fax is ready to be sent.</h4>
      <div id="pageCount"></div>
      <g:form action="send" method="post">
        <g:hiddenField name="id" value="${fax.id}"/>
        <g:submitButton name="send" value="Send Fax"/>
      </g:form>
    </div>

    <g:if test="${!fax.merged}">
      <script type="text/javascript">
$(document).ready(function() {
    var interval = setInterval(function() {
        $.ajax({
            url: '${createLink(action: 'prepareStatus', params: [id: fax.id])}',
            dataType: 'json',
            cache: false,
            success: function(data) {
                if(data.ready === true) {
                    clearInterval(interval);
                    $('#pageCount').text(data.pages + ' Page' + (data.pages != 1 ? 's' : ''));
                    $('#preparing').fadeOut(function() {
                        $('#ready').fadeIn();
                    });
                } else {
                    $('#progress-status').text(data.status + '...');
                }
            }
        });
    }, 2500);
});
      </script>
    </g:if>
  </body>
</html>