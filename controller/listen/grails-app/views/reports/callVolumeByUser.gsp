<html>
  <head>
    <title><g:message code="page.reports.callVolumeByUser.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="reports"/>
    <style type="text/css">
table.report {
    border-collapse: collapse;
}

table.report thead,
table.report tbody,
table.report tfoot {
    font: normal 11px Courier New, monospace;
}

table.report thead {
    position: relative;
    top: expression(this.offsetParent.scrollTop);
}

table.report td,
table.report th {
    border: 1px solid #E4A634;
    padding: 2px 4px;
}

table.report thead th {
    color: #574D3E;
    text-align: center;
}

table.report tbody tr:hover,
table.report tbody tr:hover td {
    background-color: #FFBC40;
}

table.report .col-call-count,
table.report .col-call-duration {
    text-align: right;
}

table.report .col-name { width: 16%; }
table.report .col-number { width: 16%; }
table.report .col-call-count { width: 4%; }
table.report .col-call-duration { width: 7%; }

table.report .col-call-count.total { width: 4%; }
table.report .col-call-duration.total { width: 7%; }

table.report .subtotal {
    background-color: #FAEDD5;
}

table.report .total {
    background-color: #F2DCB3;
}

table.report .supertotal {
    background-color: #FFBC40;
}


form {
    margin: 5px 0;
}

form,
form input[type=text],
form input[type=submit] {
    font-size: 12px;
}

form input[type=text],
form input[type=submit] {
    display: inline;
    margin-left: 5px;
}

form input[type=text] {
    width: 150px;
}

form input[type=submit] {
    border-color: #CCCCCC !important; // FIXME
    border-radius: 0;
    -moz-border-radius: 0;
    -webkit-border-radius: 0;
}

ul.download-options {
    float: right;
    font: bold 11px Courier New, monospace;
}

ul.download-options li {
    display: inline;
}
    </style>
  </head>
  <body>
    <h3>Call Volumes By User</h3>

        <ul class="download-options">
          <li>Download:</li>
          <li><g:link controller="reports" action="callVolumeByUser.xls">Excel</g:link></li>
        </ul>
        <g:form controller="reports" action="callVolumeByUser" method="get">
          <label for="start">From</label><g:textField name="start" value="${params.start}"/>
          <label for="end">To</label><g:textField name="end" value="${params.end}"/>
          <input type="submit" class="button" value="Apply"/>
        </g:form>

    <table class="report">
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
      <tfoot>
        <tr class="total">
          <td colspan="2">Total</td>
          <td class="col-call-count"><listen:reportCount value="${totals.outbound.internal.count}"/></td>
          <td class="col-call-duration"><listen:reportDuration duration="${totals.outbound.internal.duration}"/></td>
          <td class="col-call-count"><listen:reportCount value="${totals.outbound.external.count}"/></td>
          <td class="col-call-duration"><listen:reportDuration duration="${totals.outbound.external.duration}"/></td>
          <td class="col-call-count"><listen:reportCount value="${totals.outbound.total.count}"/></td>
          <td class="col-call-duration"><listen:reportDuration duration="${totals.outbound.total.duration}"/></td>

          <td class="col-call-count"><listen:reportCount value="${totals.inbound.internal.count}"/></td>
          <td class="col-call-duration"><listen:reportDuration duration="${totals.inbound.internal.duration}"/></td>
          <td class="col-call-count"><listen:reportCount value="${totals.inbound.external.count}"/></td>
          <td class="col-call-duration"><listen:reportDuration duration="${totals.inbound.external.duration}"/></td>
          <td class="col-call-count"><listen:reportCount value="${totals.inbound.total.count}"/></td>
          <td class="col-call-duration"><listen:reportDuration duration="${totals.inbound.total.duration}"/></td>

          <td class="col-call-count supertotal"><listen:reportCount value="${totals.total.count}"/></td>
          <td class="col-call-duration supertotal"><listen:reportDuration duration="${totals.total.duration}"/></td>
        </tr>
      </tfoot>
      <tbody>
        <g:each in="${calls}" var="user">
          <g:if test="${user.value.numbers.size() > 0}">
            <g:each in="${user.value.numbers}" var="ani" status="i">
              <tr>
                <g:if test="${i == 0}">
                  <td class="col-name" rowspan="${user.value.numbers.size()}"><listen:truncate value="${user.value.name}" length="18"/></td>
                </g:if>
                <td class="col-number"><listen:truncate value="${ani.key}" length="18"/></td>
 
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
