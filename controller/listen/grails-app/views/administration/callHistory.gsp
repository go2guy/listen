<%@ page import="com.interact.listen.User" %>
<html>
<head>
    <title><g:message code="page.administration.history.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="callHistory"/>
    <style type="text/css">
    table {
        margin-bottom: 10px;
    }

    table tbody {
        font-size: 12px;
    }

    .col-dateTime {
        width: 20%;
    }

    .col-ani {
        width: 25%;
    }

    .col-dnis {
        width: 25%;
    }

    .col-duration {
        width: 10%;
    }

    .col-result {
        width: 20%;
    }

    .col-dateCreated {
        width: 15%;
    }

    .col-byUser {
        width: 36%;
    }

    .col-description {
        width: 44%;
    }

    .col-channel {
        text-align: center;
        width: 5%;
    }

    tbody .col-duration {
        padding-right: 20px;
        text-align: right;
    }

    th {
        font-weight: normal;
        vertical-align: text-top;
        font-size: 16px;
        text-align: left;
    }
    </style>
    <script type="text/javascript">
        $(function () {
            ////////////////////////////////////////////////////////////////////////////
            // DatePicker Logic
            ////////////////////////////////////////////////////////////////////////////
            $("#startDate").datepicker({
                onSelect: function () {
                    var startDate = $(this).datepicker('getDate');

                    $("#endDate").datepicker("option", "minDate", startDate);
                }
            });

            $("#endDate").datepicker({
            });

            ////////////////////////////////////////////////////////////////////////////
            // Misc Logic
            ////////////////////////////////////////////////////////////////////////////
            var width = $(this).width();
            if (width > 400) {
                $('#campaign').width(400);
            } else {
                $('#campaign').width(width);
            }
        });

        function resetForm() {
            document.form.startDate.value = '';
            document.form.endDate.value = '';
            document.form.user.selectedIndex = 0;
            document.form.caller.value = '';
            document.form.callee.value = '';
            document.form.callResult.value = '';
        }

        function numbersonly(myfield, e, dec) {
            var key;
            var keychar;

            if (window.event)
                key = window.event.keyCode;
            else if (e)
                key = e.which;
            else
                return true;
            keychar = String.fromCharCode(key);

            // control keys
            if ((key == null) || (key == 0) || (key == 8) ||
                    (key == 9) || (key == 13) || (key == 27))
                return true;

            // numbers
            else if ((("*%0123456789").indexOf(keychar) > -1))
                return true;

            // decimal point jump
            else if (dec && (keychar == ".")) {
                myfield.form.elements[dec].focus();
                return false;
            }
            else
                return false;
        }
    </script>
</head>
<body>
<g:if test="${flash.errorMessage}">
    <script type="text/javascript">
        listen.showErrorMessage('${flash.errorMessage}')
    </script>
</g:if>

  <g:form action="callHistory" name="form">
      <div class="form">
          <table>
              <tbody>
              <tr>
                  <th><label for="startDate"><g:message
                          code="callHistory.startDate.label"/></label></th>
                  <td><input id="startDate" name="startDate" value="${params.startDate}"/></td>
                  <th><label for="endDate"><g:message code="callHistory.endDate.label"/></label>
                  </th>
                  <td><input id="endDate" name="endDate" value="${params.endDate}"/></td>
              </tr>

              <tr>
                  <th><label for="caller"><g:message
                          code="callHistory.caller.label"/></label></th>
                  <td><g:textField name="caller" maxlength="14" onKeyPress="return numbersonly(this, event)"
                                   value="${params.caller}"/></td>

                  <th><label for="callee"><g:message code="callHistory.callee.label"/></label>
                  </th>
                  <td><g:textField name="callee" maxlength="14" onKeyPress="return numbersonly(this, event)"
                                   value="${params.callee}"/></td>
              </tr>

              <tr>
                  <th><label for="user"><g:message
                          code="callHistory.users.label"/></label></th>
                  <td>
                      <g:select
                              id="user"
                              name="user"
                              multiple="multiple"
                              optionKey="id"
                              from="${users}"
                              noSelection="['': '-All-']"
                              value="${selectedUsers ? selectedUsers.collect {it.id} : ''}"
                              optionValue="realName"/></td>
                  <th><label for="callResult"><g:message code="callHistory.callResult.label"/></label>
                  </th>
                  <td style="vertical-align: top;"><g:textField name="callResult" id="callResult" maxlength="64" value="${params.callResult}"/></td>
              </tr>
              </tbody>
          </table>

          <div class="buttons">
              <input type="submit" value="Search" name="searchButton" id="searchButton"/>
              <input type="button" value="Reset" onclick="resetForm()" id="resetButton" name="resetButton"/>
          </div>
      </div>
  </g:form>
<br/>

<sec:ifNotGranted roles="ROLE_CUSTODIAN">
    <table>
        <caption><g:message code="callHistory.label"/><span style="float: right;"><g:link action="exportCallHistoryToCSV"
                                                                                          params="${params}">${message(code: 'callHistory.exportCSV.label')}</g:link></span></caption>
        <thead>
        <g:sortableColumn property="dateTime" title="Began" class="col-dateTime"/>
        <g:sortableColumn property="ani" title="Calling Party" class="col-ani"/>
        <g:sortableColumn property="dnis" title="Called Party" class="col-dnis"/>
        <g:sortableColumn property="duration" title="Duration" class="col-duration"/>
        <g:sortableColumn property="result" title="Call Result" class="col-result"/>
        </thead>
        <tbody>
        <g:each in="${callHistoryList}" var="callHistory" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
                <td class="col-dateTime"><joda:format value="${callHistory.dateTime}"
                                                      pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td class="col-ani"><listen:numberWithRealName number="${fieldValue(bean: callHistory, field: 'ani')}"
                                                               user="${callHistory.fromUser}" personalize="false"/></td>
                <td class="col-dnis"><listen:numberWithRealName number="${fieldValue(bean: callHistory, field: 'dnis')}"
                                                                user="${callHistory.toUser}" personalize="false"/></td>
                <td class="col-duration"><listen:formatduration duration="${callHistory.duration}" millis="true"/></td>
                <td class="col-result">${fieldValue(bean: callHistory, field: 'result')}</td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <listen:paginateTotal total="${callHistoryTotal}" messagePrefix="paginate.total.callHistories"/>
    <div class="pagination">
        <g:paginate total="${callHistoryTotal}" maxsteps="10" params="${params}" controller="administration" action="callHistory"/>
    </div>
</sec:ifNotGranted>
</body>
</html>