<li<g:if test="${id}"> id="${id}"</g:if> class="destination ${findMeNumber?.isEnabled ? 'enabled' : 'disabled'}">
  <div class="buttons">
    <button type="button" class="toggle-destination">${findMeNumber?.isEnabled ? 'Disable' : 'Enable'}</button>
    <button type="button" class="delete-destination">Delete</button>
  </div>
  <div class="inputs">
    <input type="text" class="number ${listen.validationClass(bean: findMeNumber, field: 'number')}" value="${findMeNumber?.number}"/>
    for
    <input type="text" class="seconds ${listen.validationClass(bean: findMeNumber, field: 'dialDuration')}" value="${findMeNumber?.dialDuration}"/>
    seconds
    <g:set var="forwardedTo" value="${findMeNumber?.forwardedTo()}"/>
    <listen:ifCannotDial number="${forwardedTo ?: findMeNumber?.number}">
      <span class="blocked-number error" title="You are not allowed to dial ${forwardedTo ?: fieldValue(bean: findMeNumber, field: 'number')}">Blocked</span>
    </listen:ifCannotDial>
    <g:if test="${forwardedTo}">
      <span class="forwarded-to info">Forwarded to ${forwardedTo.encodeAsHTML()}</span>
    </g:if>
  </div>
</li>
