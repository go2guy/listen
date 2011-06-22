<g:if test="${list.size() > 0}">
  <table class="schedule-list">
    <caption>${caption}</caption>
    <thead>
      <th class="col-when">When</th>
      <th class="col-duration">Duration</th>
      <th class="col-subject">Subject</th>
      <th class="col-invited"># Invited</th>
      <th class="col-details"></th>
    </thead>
    <tbody>
    <g:each in="${list}" var="scheduledConference" status="i">
      <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
        <td class="col-when"><listen:prettytime date="${scheduledConference.startsAt().toDateTime()}"/></td>
        <td class="col-duration"><listen:prettyduration duration="${scheduledConference.duration()}"/></td>
        <td class="col-subject"><g:fieldValue bean="${scheduledConference}" field="emailSubject"/></td>
        <td class="col-invited">${scheduledConference?.invitedCount()?.encodeAsHTML()}</td>
        <td class="col-details"></td>
      </tr>
      <tr class="${i % 2 == 0 ? 'even' : 'odd'} schedule-details-row">
        <td colspan="5">
          <g:form controller="conferencing" action="cancel" method="post">
            <g:hiddenField name="id" value="${scheduledConference.id}"/>
            <g:submitButton name="cancel" value="${g.message(code: 'page.conferencing.scheduling.cancel.button.label')}"/>
          </g:form>
          <table class="schedule-details">
            <tr><th>Date</th><td><joda:format value="${scheduledConference.date}"/></td></tr>
            <tr><th>Time</th><td><joda:format value="${scheduledConference.starts}" pattern="h:mm a"/> to <joda:format value="${scheduledConference.ends}" pattern="h:mm a"/></td></tr>
            <tr><th>Memo</th><td><g:fieldValue bean="${scheduledConference}" field="emailBody"/></td></tr>
            <g:if test="${scheduledConference.activeCallers().size() > 0}">
              <tr><th>Active Callers</th><td>
                <ul>
                  <g:each in="${scheduledConference.activeCallers()}" var="email">
                    <li>${email.encodeAsHTML()}</li>
                  </g:each>
                </ul>
              </td></tr>
            </g:if>
            <g:if test="${scheduledConference.passiveCallers().size() > 0}">
              <tr><th>Passive Callers</th><td>
                <ul>
                  <g:each in="${scheduledConference.passiveCallers()}" var="email">
                    <li>${email.encodeAsHTML()}</li>
                  </g:each>
                </ul>
              </td></tr>
            </g:if>
          </table>
        </td>
      </tr>
    </g:each>
    </tbody>
  </table>
</g:if>