<li<g:if test="${id}"> id="${id}"</g:if> class="destination ${findMeNumber?.isEnabled ? 'enabled' : 'disabled'}">
  <div class="buttons">
    <button type="button" class="toggle-destination">${findMeNumber?.isEnabled ? 'Disable' : 'Enable'}</button>
    <button type="button" class="delete-destination">Delete</button>
  </div>
  <div class="inputs">
    <input type="text" class="number" value="${findMeNumber?.number}"/>
    for
    <input type="text" class="seconds" value="${findMeNumber?.dialDuration}"/>
    seconds
    <g:set var="forwardedTo" value="${findMeNumber?.forwardedTo()}"/>
    <g:if test="${forwardedTo}">
      <span class="forwarded-to">( <b>${findMeNumber?.number}</b> is forwarded to <b>${forwardedTo.encodeAsHTML()}</b> )</span>
    </g:if>
  </div>
</li>
