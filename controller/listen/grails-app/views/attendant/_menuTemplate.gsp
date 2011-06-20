<li class="menu"<g:if test="${id}"> id="${id}"</g:if>>
  <button type="button" class="delete-menu">Delete</button>
  <button type="button" class="menu-visibility-toggle">Show/Hide</button>

  <label>
    <span class="label-text">Menu Label</span>
    <input type="text" class="menu-label" value="${menu?.name?.encodeAsHTML()}" placeholder="Enter a friendly name..."/>
  </label>

  <div class="hideable">
    <label>
      <span class="label-text">Options Prompt</span>
      <listen:promptSelect class="prompt-select options-prompt" value="${menu?.optionsPrompt}"/>
    </label>

    <table>
      <thead>
        <tr>
          <th class="cell-on-keypress">On Keypress</th>
          <th class="cell-play-prompt">Play Prompt</th>
          <th class="cell-perform-action">Perform Action</th>
          <th class="cell-action-options"></th>
          <th class="cell-delete-action">
            <button type="button" class="add-action prominent">Add Action</button>
          </th>
        </tr>
      </thead>
      <tbody>
        <g:each in="${menu?.keypressActions?.sort { it.keysPressed }}" var="action">
          <g:render template="actionTemplate" model="[group: menu?.menuGroup, action: action, type: 'keypress']"/>
        </g:each>
        <g:render template="actionTemplate" model="[group: menu?.menuGroup, action: menu?.defaultAction, type: 'default']"/>
        <g:render template="actionTemplate" model="[group: menu?.menuGroup, action: menu?.timeoutAction, type: 'timeout']"/>
      </tbody>
    </table>
  </div>
</li>