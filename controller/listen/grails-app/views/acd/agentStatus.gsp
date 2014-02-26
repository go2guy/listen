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
                width: 120px;
            }

            .priorityColumn {
                text-align: center;
                width: 40px;
                padding-right: 30px;
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
                                <td class="statusColumn">${agent.status}</td>
                                <td class="statusModifiedColumn">
                                    ${agent.statusModified ? agent.statusModified.toString("MM'/'dd'/'yyyy' 'HH':'mm':'ss") : ""}
                                    %{--<listen:prettytime date="${agent.statusModified}"/>--}%
                                </td>
                                <td class="lastCallColumn">
                                    %{--<listen:prettytime date="${agent.lastCall}"/>--}%
                                    ${agent.lastCall ? agent.lastCall.toString("MM'/'dd'/'yyyy' 'HH':'mm':'ss"): ""}
                                </td>
                                <td class="currentCallColumn">
                                    ${agent.callStart ? agent.callStart.toString("MM'/'dd'/'yyyy' 'HH':'mm':'ss") : ""}
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </g:each>
        </div>
    </body>
</html>