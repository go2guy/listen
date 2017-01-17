<html>
    <head>
        <title><g:message code="page.acd.agentStatus.title"/></title>
        <meta name="layout" content="main"/>
        <meta name="tab" content="acd"/>
        <meta name="button" content="agentStatus"/>

        <style type="text/css">
            .skillTable {
                margin-bottom: 20px;
            }

            .agentColumn {
                width: 180px;
            }

            .statusColumn {
                width: 150px;
                text-align: center;
            }

            .priorityColumn {
                text-align: center;
                width: 30px;
                /*padding-right: 30px;*/
            }

            .statusModifiedColumn {
                width: 180px;
            }

            .lastCallColumn {
                width: 180px;
            }

            .currentCallColumn {
                width: 180px;
            }
        </style>
    </head>
    <body>
        <div id="agentStatus">
            <g:each in="${skills}" var="skill">
                <table id="${skill.skill}" class="fixed skillTable" cellspacing="0" cellpadding="0">
                    <caption>${skill.skill}:</caption>
                    <thead>
                        <tr>
                            <th class="agentColumn" id="agent_column">Agent</th>
                            <th class="priorityColumn" id="priority_column">Priority</th>
                            <th class="statusColumn" id="status_column">Status</th>
                            <th class="statusModifiedColumn" id="status_modified_column">Status Changed</th>
                            <th class="lastCallColumn" id="last_call_column">Last Call Completed</th>
                            <th class="currentCallColumn" id="current_call_column">Current Call Start</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:set var="row_count" value="${0}"/>
                        <g:each in="${skill.agents}" var="agent">
                            <tr class="${++row_count % 2 == 0 ? 'even' : 'odd'}">
                                <td class="agentColumn">${agent.agent}</td>
                                <td class="priorityColumn">${agent.priority}</td>
                                %{--<td class="statusColumn">${agent.status}</td>--}%
                                <td class="statusColumn">
                                    <div id="agent-status-toggle">
                                        <g:form controller="acd" action="toggleAgentStatus" method="post">
                                            <g:hiddenField name="agentId" value="${agent.agentId}"/>
                                            <g:submitButton id="statusButton" name="toggle_status"
                                                   value="${agent.status}"
                                                   class="statusButton ${agent.status}"/>
                                        </g:form>
                                    </div>
                                </td>
                                <td class="statusModifiedColumn">
                                    ${agent.statusModified ? agent.statusModified.toString("MM'/'dd'/'yyyy' 'HH':'mm':'ss") : ""}
                                    %{--<listen:prettytime date="${agent.statusModified}"/>--}%
                                </td>
                                <td class="lastCallColumn">
                                    %{--<listen:prettytime date="${agent.lastCall}"/>--}%
                                    ${agent.lastCall ? agent.lastCall.toString("MM'/'dd'/'yyyy' 'HH':'mm':'ss"): ""}
                                </td>
                                <td class="currentCallColumn">
                                    ${agent.agentCallStart ? agent.agentCallStart.toString("MM'/'dd'/'yyyy' 'HH':'mm':'ss") : ""}
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </g:each>
        </div>

        <script type="text/javascript">


            setTimeout(function(){
                window.location.reload(1);
            }, 5000);
        </script>
    </body>
</html>