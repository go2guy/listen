<html>
  <head>
    <title><g:message code="page.fax.preparing.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="messages"/>
    <meta name="button" content="sendfax"/>
    <style type="text/css">
table.fax-information {
    border: 1px solid #6DACD6;
    clear: both;
}

table.fax-information td,
table.fax-information th {
    padding: 5px;
}

table.fax-information tbody th {
    font-weight: normal;
    text-align: left;
    width: 20%;
}

.highlighted {
    color: #FFFFFF;
}

.fax-status {
    border: 2px solid #176BA3;
    background: #E4F0FB url(${resource(dir: 'resources/app/images', file: 'spinner.gif')}) 10px 10px no-repeat;
    margin-top: 10px;
    padding: 10px;
    width: 377px;

    border-radius: 5px;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
}

.fax-status .status-message {
    font-family: Lucida Console, Courier New, monospace;
    font-size: 24px;
    height: 31px;
    line-height: 31px;
    margin-left: 41px;
    vertical-align: text-top;
}

.fax-status .num-attempts {
    clear: both;
    float: right;
    font-size: 14px;
}

#resend-form {
    clear: both;
    display: none;
    margin-top: 10px;
    padding: 5px 0;
    text-align: center;
}

.fax-error {
    background-color: #FFC7C7;
    border-color: #A81818;
    color: #000000;
}

.fax-error .fax-information {
    border-color: #A81818;
}

.fax-success {
    background-color: #D1FFC9;
    border-color: #437A3A;
    color: #000000;
}

.fax-success .fax-information {
    border-color: #437A3A;
}

    </style>
  </head>
  <body>
    <h3>Sending Fax</h3>

    <div class="fax-status">
      <div class="num-attempts">Attempts: <span class="attempts-value">0</span></div>
      <div class="status-message">${fax.status.encodeAsHTML()}</div>

      <div id="resend-form">
        <g:form controller="fax" action="resend" method="post">
          <g:hiddenField name="id" value="${fax.id}"/>
          <g:submitButton name="resend" value="Try Again"/>
        </g:form>
      </div>

      <div style="margin-top: 10px;"></div>
      <table class="fax-information">
        <tbody>
          <tr><th>To</th><td>${fax.dnis.encodeAsHTML()}</td></tr>
          <g:each in="${fax.sourceFiles}" var="file">
            <tr><th>File</th><td>${file.file.name.encodeAsHTML()}</td></tr>
          </g:each>
        </tbody>
      </table>
    </div>
    <script type="text/javascript">
$(document).ready(function() {
    var interval = setInterval(function() {
        $.ajax({
            url: '${createLink(action: 'status', params: [id: fax.id])}',
            dataType: 'json',
            cache: false,
            success: function(data) {
                var status = $('.status-message');
                if(status.text() != data.status) {
                    status.text(data.status);

                    if(data.status == 'Failed') {
                        $('.fax-status').addClass('fax-error').css('background-image', 'none');
                        $('.fax-status .status-message').css('margin-left', '10px');
                        $('#resend-form').show();
                        clearInterval(interval);
                    } else if(data.status.indexOf('Pages Sent') > -1) {
                        $('.fax-status').addClass('fax-success').css('background-image', 'none');
                        $('.fax-status .status-message').css('margin-left', '10px');
                        clearInterval(interval);
                    }
                }

                var attempts = $('.attempts-value');
                if(attempts.text() != data.attempts) {
                    attempts.text(data.attempts);
                }
            }
        });
    }, 1000);
});
    </script>
  </body>
</html>