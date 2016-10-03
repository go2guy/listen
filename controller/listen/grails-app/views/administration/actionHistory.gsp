<%--
  Created by IntelliJ IDEA.
  User: cgeesey
  Date: 9/30/2016
  Time: 3:25 PM
--%>

<%@ page import="com.interact.listen.User" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title><g:message code="page.administration.history.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="actionHistory"/>
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
            $(".datepicker").datepicker({
            });

            $("#startDate").mask("MM/dd/yyyy", {placeholder: "MM/dd/yyyy"});
            $("#endDate").mask("MM/dd/yyyy", {placeholder: "MM/dd/yyyy"});

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
            document.form.startDate.value = '${params.startDate}';
            document.form.endDate.value = '${params.endDate}';
            document.form.campaign.selectedIndex = 0;
            document.form.phoneNumber.value = '';
            document.form.timeZone.selectedIndex = 0;
            document.form.callDisposition.selectedIndex = 0;
            document.form.milestone.value = '';
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

<g:form action="actionHistory" name="form">
    <div class="form">
        <table>
            <tbody>
            <tr>
                <th><label for="startDate"><g:message
                        code="actionHistory.startDate.label"/></label></th>
                <td><input id="startDate" name="startDate" value="${params.startDate}"
                           class="datepicker"/>
                <th><label for="endDate"><g:message code="actionHistory.endDate.label"/></label>
                </th>
                <td><input id="endDate" name="endDate" value="${params.endDate}"
                           class="datepicker"/>
            </tr>

            <tr>
                <th><label for="user"><g:message
                        code="actionHistory.users.label"/></label></th>
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
<table>
    <caption><g:message code="actionHistory.label"/><span style="float: right;"><g:link action="exportActionHistoryToCSV"
                                                                  params="${params}">${message(code: 'actionHistory.exportCSV.label')}</g:link></span></caption>

    <thead>
    <g:sortableColumn property="dateCreated" title="Date" class="col-dateCreated"/>
    <g:sortableColumn property="byUser" title="User" class="col-byUser"/>
    <g:sortableColumn property="description" title="Description" class="col-description"/>
    <g:sortableColumn property="channel" title="Channel" class="col-channel"/>
    </thead>
    <tbody>
    <g:each in="${actionHistoryList}" var="actionHistory" status="i">
        <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
            <td class="col-dateCreated"><joda:format value="${actionHistory.dateCreated}"
                                                     pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <td class="col-byUser">
                ${fieldValue(bean: actionHistory.byUser, field: 'realName')}
                <g:if test="${actionHistory.byUser && actionHistory.onUser && actionHistory.byUser != actionHistory.onUser}">
                    &nbsp;&#9656;&nbsp;${fieldValue(bean: actionHistory.onUser, field: 'realName')}
                </g:if>
            </td>
            <td class="col-description">${fieldValue(bean: actionHistory, field: 'description')}</td>
            <td class="col-channel">${fieldValue(bean: actionHistory, field: 'channel')}</td>
        </tr>
    </g:each>
    </tbody>
</table>
<listen:paginateTotal total="${actionHistoryTotal}" messagePrefix="paginate.total.actionHistories"/>
<div class="pagination">
    <g:paginate total="${actionHistoryTotal}" maxsteps="10"/>
</div>
</body>
</html>