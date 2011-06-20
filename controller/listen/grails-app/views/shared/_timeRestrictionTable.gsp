<table class="time-restriction"<g:if test="${!visible}"> style="display: none;"</g:if>>
  <thead>
    <tr>
      <td class="time-cell"><g:message code="timeRestriction.header.fromTime"/></td>
      <td class="time-cell"><g:message code="timeRestriction.header.toTime"/></td>
      <td class="day-cell"><g:message code="timeRestriction.header.monday"/></td>
      <td class="day-cell"><g:message code="timeRestriction.header.tuesday"/></td>
      <td class="day-cell"><g:message code="timeRestriction.header.wednesday"/></td>
      <td class="day-cell"><g:message code="timeRestriction.header.thursday"/></td>
      <td class="day-cell"><g:message code="timeRestriction.header.friday"/></td>
      <td class="day-cell"><g:message code="timeRestriction.header.saturday"/></td>
      <td class="day-cell"><g:message code="timeRestriction.header.sunday"/></td>
      <td></td>
    </tr>
  </thead>
  <tbody>
    <g:each in="${restrictions}" var="restriction" status="i">
      <g:render template="/shared/timeRestrictionRow" model="${[index: i, restriction: restriction, prefix: prefix]}"/>
    </g:each>
  </tbody>
</table>