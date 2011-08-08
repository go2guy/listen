<html>
  <head>
    <title><g:message code="page.administration.reports.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="reports"/>
    <style type="text/css">
table thead,
table tbody {
    border-collapse: collapse;
    font: normal 11px Courier New, monospace;
}

td,
th {
    border: 1px solid #CACACA;
    padding: 2px 4px;
}

thead th {
    text-align: center;
}

.col-call-count,
.col-call-duration {
    text-align: right;
}

.col-call-count { width: 4%; }
.col-call-duration { width: 6%; }
.col-name { width: 15%; }
.col-number { width: 12%; }

.col-call-duration.total { width: 7%; }

.subtotal {
    background-color: #F0F0F0
}

.total {
    background-color: #E0E0E0;
}

caption {
    border-width: 0;
}

caption form {
    margin-top: 5px;
}

caption form,
caption form input[type=text],
caption form input[type=submit] {
    font-size: 12px;
}

caption form input[type=text],
caption form input[type=submit] {
    display: inline;
    margin-left: 5px;
}

caption form input[type=text] {
    width: 150px;
}

caption form input[type=submit] {
    border-color: #CCCCCC !important; // FIXME
    border-radius: 0;
    -moz-border-radius: 0;
    -webkit-border-radius: 0;
}
    </style>
  </head>
  <body>
    <table>
      <caption>
        Call Volumes By User
        <g:form action="reports" method="get">
          <label for="start">From</label><g:textField name="start" value="${params.start}"/>
          <label for="end">To</label><g:textField name="end" value="${params.end}"/>
          <input type="submit" class="button" value="Apply"/>
        </g:form>
      </caption>
      <thead>
        <tr>
          <th rowspan="2">Name</th>
          <th rowspan="2">Number</th>
          <th colspan="6">Outbound</th>
          <th colspan="6">Inbound</th>
          <th rowspan="2" colspan="2" class="total">Total</th>
        </tr>
        <tr>
          <th colspan="2">Internal</th>
          <th colspan="2">External</th>
          <th colspan="2" class="subtotal">Total</th>
          <th colspan="2">Internal</th>
          <th colspan="2">External</th>
          <th colspan="2" class="subtotal">Total</th>
        </tr>
      </thead>
      <tbody>
        <g:each in="${calls}" var="user">
          <g:if test="${user.value.numbers.size() > 0}">
            <g:each in="${user.value.numbers}" var="ani" status="i">
              <tr>
                <g:if test="${i == 0}">
                  <td class="col-name" rowspan="${user.value.numbers.size()}">${user.value.name}</td>
                </g:if>
                <td class="col-number">${ani.key}</td>
 
                <td class="col-call-count"><listen:reportCount value="${ani.value.outbound.internal.count}"/></td>
                <td class="col-call-duration"><listen:reportDuration duration="${ani.value.outbound.internal.duration}"/></td>
                <td class="col-call-count"><listen:reportCount value="${ani.value.outbound.external.count}"/></td>
                <td class="col-call-duration"><listen:reportDuration duration="${ani.value.outbound.external.duration}"/></td>
                <td class="col-call-count subtotal"><listen:reportCount value="${ani.value.outbound.total.count}"/></td>
                <td class="col-call-duration subtotal"><listen:reportDuration duration="${ani.value.outbound.total.duration}"/></td>

                <td class="col-call-count"><listen:reportCount value="${ani.value.inbound.internal.count}"/></td>
                <td class="col-call-duration"><listen:reportDuration duration="${ani.value.inbound.internal.duration}"/></td>
                <td class="col-call-count"><listen:reportCount value="${ani.value.inbound.external.count}"/></td>
                <td class="col-call-duration"><listen:reportDuration duration="${ani.value.inbound.external.duration}"/></td>
                <td class="col-call-count subtotal"><listen:reportCount value="${ani.value.inbound.total.count}"/></td>
                <td class="col-call-duration subtotal"><listen:reportDuration duration="${ani.value.inbound.total.duration}"/></td>

                <td class="col-call-count total"><listen:reportCount value="${ani.value.total.count}"/></td>
                <td class="col-call-duration total"><listen:reportDuration duration="${ani.value.total.duration}"/></td>
              </tr>
            </g:each>
          </g:if>
        </g:each>
      </tbody>
    </table>
    <script type"text/javascript">
$(document).ready(function() {
    $('#start').datepicker({ dateFormat: 'yy-mm-dd', showAnim: 'fadeIn' });
    $('#end').datepicker({ dateFormat: 'yy-mm-dd', showAnim: 'fadeIn' });
});

    </script>
  </body>
</html>
