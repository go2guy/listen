<tr class="time-restriction-row"<g:if test="${index != null}"> data-index="${index}"</g:if><g:if test="${id != null}"> id="${id}"</g:if>>
  <td class="time-cell"><listen:timePicker name="${prefix}${index != null ? '[' + index + ']' : ''}.startTime" value="${restriction?.startTime}"/></td>
  <td class="time-cell"><listen:timePicker name="${prefix}${index != null ? '[' + index + ']' : ''}.endTime" value="${restriction?.endTime}"/></td>
  <td class="day-cell"><g:checkBox name="${prefix}${index != null ? '[' + index + ']' : ''}.monday" value="${restriction?.monday}"/></td>
  <td class="day-cell"><g:checkBox name="${prefix}${index != null ? '[' + index + ']' : ''}.tuesday" value="${restriction?.tuesday}"/></td>
  <td class="day-cell"><g:checkBox name="${prefix}${index != null ? '[' + index + ']' : ''}.wednesday" value="${restriction?.wednesday}"/></td>
  <td class="day-cell"><g:checkBox name="${prefix}${index != null ? '[' + index + ']' : ''}.thursday" value="${restriction?.thursday}"/></td>
  <td class="day-cell"><g:checkBox name="${prefix}${index != null ? '[' + index + ']' : ''}.friday" value="${restriction?.friday}"/></td>
  <td class="day-cell"><g:checkBox name="${prefix}${index != null ? '[' + index + ']' : ''}.saturday" value="${restriction?.saturday}"/></td>
  <td class="day-cell"><g:checkBox name="${prefix}${index != null ? '[' + index + ']' : ''}.sunday" value="${restriction?.sunday}"/></td>
  <td><button type="button" class="delete-restriction"><g:message code="default.button.delete.label"/></button></td>
</tr>