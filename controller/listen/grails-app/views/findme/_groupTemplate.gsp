<li class="group"<g:if test="${id}"> id="${id}"</g:if>>
  <h4 class="group-header">Dial the following number</h4>
  <ul class="destinations">
    <g:each in="${group}" var="findMeNumber">
      <g:render template="destinationTemplate" model="${[findMeNumber: findMeNumber]}"/>
    </g:each>
  </ul>
  <button type="button" class="delete-group">Delete Group</button>
  <button type="button" class="add-destination">Add Number</button>
</li>