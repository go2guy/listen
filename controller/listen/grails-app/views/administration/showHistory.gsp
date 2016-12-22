<%--
  Created by IntelliJ IDEA.
  User: bjohnston
  Date: 12/15/2016
  Time: 1:42 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title><g:message code="callHistory.detail.label"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="callHistory"/>
    <tooltip:resources/>

    <style type="text/css">
        table { margin-bottom: 10px; }


    </style>

</head>

<body>

<h3><g:message code="callHistory.detail.label"/></h3>


    <table id="twoColumns">
    <tbody>
        <tbody>
            <tr>
                <th><g:message code="callHistory.startDate.label"/></th>
                <td><joda:format value="${callHistoryInstance.dateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            </tr>
            <tr>
                <th><g:message code="callHistory.ani.label"/></th>
                <td>${callHistoryInstance.ani}</td>
            </tr>
            <tr>
                <th><g:message code="callHistory.dnis.label"/></th>
                <td>${callHistoryInstance.dnis}</td>
            </tr>
            <tr>
                <th><g:message code="callHistory.duration.label"/></th>
                <td><listen:formatduration duration="${callHistoryInstance.duration}" millis="true"/></td>
            </tr>
            <tr>
                <th><g:message code="callHistory.organization.label"/></th>
                <td>${callHistoryInstance.organization.name}</td>
            </tr>
            <tr>
                <th><g:message code="callHistory.fromUser.label"/></th>
                <td><listen:numberWithRealName number="${fieldValue(bean: callHistoryInstance, field: 'ani')}"
                                               user="${callHistoryInstance.fromUser}" personalize="false"/></td>
            </tr>
            <tr>
                <th><g:message code="callHistory.toUser.label"/></th>
                <td><listen:numberWithRealName number="${fieldValue(bean: callHistoryInstance, field: 'dnis')}"
                                               user="${callHistoryInstance.toUser}" personalize="false"/></td>
            </tr>
            <tr>
                <th><g:message code="callHistory.sessionId.label"/></th>
                <td>${callHistoryInstance.sessionId}</td>
            </tr>
            <tr>
                <th><g:message code="callHistory.callResult.label"/></th>
                <td>${callHistoryInstance.result}</td>
            </tr>

        </tbody>
    </table>

    <g:if test="${acdCallHistoryInstance}">

                <g:each in="${acdCallHistoryInstance}" status="i" var="acdCallHistory">
                    <table id="twoColumns">
                    <tbody>
                        <h3><g:message code="acdCallHistory.detail.label" args="[i+1]"/></h3>
                        <tr>
                            <th><g:message code="acdCallHistory.skill.label"/></th>
                            <td>${acdCallHistory.skill.skillname}</td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.enqueueTime.label"/></th>
                            <td><joda:format value="${acdCallHistory.enqueueTime}"/></td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.dequeueTime.label"/></th>
                            <td><joda:format value="${acdCallHistory.dequeueTime}"/></td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.callStatus.label"/></th>
                            <td>${acdCallHistory.callStatus}</td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.user.label"/></th>
                            <td>${acdCallHistory.user}</td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.callStart.label"/></th>
                            <td><joda:format value="${acdCallHistory.callStart}"/></td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.callEnd.label"/></th>
                            <td><joda:format value="${acdCallHistory.callEnd}"/></td>
                        </tr>
                    </tbody>
                    </table>
                </g:each>

    </g:if>


</body>
</html>
