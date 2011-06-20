<tr<g:if test="${type != 'keypress'}"> class="default"</g:if><g:if test="${className}"> class="${className}"</g:if>>
  <td>
    <g:if test="${type == 'keypress'}">
      <input type="text" class="keypress" value="${action?.keysPressed}" placeholder="e.g. 1 or #"/>
    </g:if>
    <g:if test="${type == 'default'}">Other Input</g:if>
    <g:if test="${type == 'timeout'}">Timeout (5s)</g:if>
  </td>
  <td><listen:promptSelect class="prompt-select" value="${action?.promptBefore}"/></td>
  <td><listen:actionSelect action="${action}"/></td>
  <td>
    <listen:attendantApplicationSelect action="${action}"/>
    <listen:dialNumberInput action="${action}"/>
    <listen:menuSelect group="${group}" action="${action}"/>
  </td>
  <td class="cell-delete-action">
    <g:if test="${type == 'keypress'}">
      <button type="button" class="delete-action">Delete</button>
    </g:if>
  </td>
</tr>