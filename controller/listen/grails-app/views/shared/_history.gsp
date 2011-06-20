  <sec:ifNotGranted roles="ROLE_CUSTODIAN">
    <table>
      <caption>Call History</caption>
      <thead>
        <g:sortableColumn property="dateTime" title="Began" class="col-dateTime"/>
        <g:sortableColumn property="ani" title="Calling Party" class="col-ani"/>
        <g:sortableColumn property="dnis" title="Called Party" class="col-dnis"/>
        <g:sortableColumn property="duration" title="Duration" class="col-duration"/>
        <g:sortableColumn property="result" title="Call Result" class="col-result"/>
      </thead>
      <tbody>
        <g:each in="${callHistoryList}" var="callHistory" status="i">
          <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
            <td class="col-dateTime"><joda:format value="${callHistory.dateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <td class="col-ani"><listen:numberWithRealName number="${fieldValue(bean: callHistory, field: 'ani')}" user="${callHistory.fromUser}" personalize="${personalize}"/></td>
            <td class="col-dnis"><listen:numberWithRealName number="${fieldValue(bean: callHistory, field: 'dnis')}" user="${callHistory.toUser}" personalize="${personalize}"/></td>
            <td class="col-duration">${fieldValue(bean: callHistory, field: 'duration')}</td>
            <td class="col-result">${fieldValue(bean: callHistory, field: 'result')}</td>
          </tr>
        </g:each>
      </tbody>
    </table>
    <listen:paginateTotal total="${callHistoryTotal}" messagePrefix="paginate.total.callHistories"/>
    <div class="pagination">
      <g:paginate total="${callHistoryTotal}" maxsteps="10"/>
    </div>
  </sec:ifNotGranted>

    <table>
      <caption>Action History</caption>
      <thead>
        <g:sortableColumn property="dateCreated" title="Date" class="col-dateCreated"/>
        <g:sortableColumn property="byUser" title="User" class="col-byUser"/>
        <g:sortableColumn property="onUser" title="On User" class="col-onUser"/>
<!--        <g:sortableColumn property="action" title="Action" class="col-action"/>-->
        <g:sortableColumn property="description" title="Description" class="col-description"/>
        <g:sortableColumn property="channel" title="Channel" class="col-channel"/>
      </thead>
      <tbody>
        <g:each in="${actionHistoryList}" var="actionHistory" status="i">
          <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
            <td class="col-dateCreated"><joda:format value="${actionHistory.dateCreated}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <td class="col-byUser">${fieldValue(bean: actionHistory.byUser, field: 'realName')}</td>
            <td class="col-onUser">${fieldValue(bean: actionHistory.onUser, field: 'realName')}</td>
<!--            <td class="col-action">${fieldValue(bean: actionHistory, field: 'action')}</td>-->
            <td class="col-description">${fieldValue(bean: actionHistory, field: 'description')}</td>
            <td class="col-channel">${fieldValue(bean: actionHistory, field: 'channel')}</td>
          </tr>
        </g:each>
      </tbody>
    </table>
    <listen:paginateTotal total="${actionHistoryTotal}" messagePrefix="paginate.total.actionHistories"/>
    <div class="pagination">
      <g:paginate total="${actionHistoryTotal}" maxsteps="10"/>
    </div>
