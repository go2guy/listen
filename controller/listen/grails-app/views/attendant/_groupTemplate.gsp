<li class="menu-group"<g:if test="${id}"> id="${id}"</g:if><g:if test="${hidden}"> style="display: none;"</g:if>>
  <input type="hidden" class="is-default" value="${group?.isDefault}"/>
  <input type="hidden" class="group-id" value="${group?.id}"/>
  <div class="group-configuration">
    <g:if test="${!group?.isDefault}">
      <button type="button" class="delete-group">Delete Configuration</button>
    </g:if>

    <label>
      Configuration Name
      <input type="text" class="group-name" value="${group?.name?.encodeAsHTML()}"/>
    </label>

    <label class="entry-menu-label"<g:if test="${!group || group.menus.size() == 0}"> style="display: none;"</g:if>>
      Which menu (below) do callers hear first?
      <listen:entryMenuSelect group="${group}"/>
    </label>

    <g:if test="${group?.isDefault}">
      <span class="default-message">This is the default configuration. If no other configuration is found for a specific time, this one will be used.</span>
    </g:if>
    <g:else>
      <span class="default-message">This configuration applies to the following times/days:</span>
      <g:render template="/shared/timeRestrictionTable" model="${[visible: group?.restrictions?.size() > 0, prefix: 'restrictions', restrictions: group?.restrictions]}"/>
      <button type="button" class="addTimeRestriction">Add Restriction</button>
    </g:else>
  </div>

  <ul class="all-menus">
    <g:each in="${group?.menusInDisplayOrder()}" var="menu">
      <g:render template="menuTemplate" model="[menu: menu]"/>
    </g:each>
  </ul>

  <table class="templates">
    <tbody>
      <g:render template="actionTemplate" model="[className: 'action-row-template', type: 'keypress', group: group, action: null]"/>
    </tbody>
  </table>
</li>