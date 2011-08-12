<html>
  <head>
    <title><g:message code="page.user.list.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="users"/>
    <meta name="button" content="list"/>
    <meta name="page-header" content="${g.message(code: 'page.user.list.header')}"/>
    <style type="text/css">
.col-username { width: 20%; }
.col-realName { width: 26%; }
.col-enabled { width: 10%; }
.col-isActiveDirectory { width: 16%; }
.col-lastLogin { width: 18%; }
.col-button { width: 5%; }

td.col-enabled,
td.col-isActiveDirectory {
    padding-left: 20px;
}

tr.disabled {
    color: #999999;
}
    </style>
  </head>
  <body>
    <table>
      <thead>
        <tr>
          <g:sortableColumn property="username" title="${g.message(code: 'user.username.label')}" class="col-username"/>
          <g:sortableColumn property="realName" title="${g.message(code: 'user.realName.label')}" class="col-realName"/>
          <g:sortableColumn property="enabled" title="${g.message(code: 'user.enabled.label')}" class="col-enabled"/>
          <listen:ifLicensed feature="ACTIVE_DIRECTORY">
            <g:sortableColumn property="isActiveDirectory" title="${g.message(code: 'user.isActiveDirectory.label')}" class="col-activeDirectory"/>
          </listen:ifLicensed>
          <g:sortableColumn property="lastLogin" title="${g.message(code: 'user.lastLogin.label')}" class="col-lastLogin"/>
          <th class="col-button"></th>
          <th class="col-button"></th>
        </tr>
      </thead>
      <tbody>
        <g:each in="${userList}" var="user" status="i">
          <tr class="${i % 2 == 0 ? 'even' : 'odd'}${!user.enabled ? ' disabled' : ''}">
            <td class="col-username">${fieldValue(bean: user, field: 'username')}</td>
            <td class="col-realName">${fieldValue(bean: user, field: 'realName')}</td>
            <td class="col-enabled"><listen:checkMark value="${user.enabled}"/></td>
            <listen:ifLicensed feature="ACTIVE_DIRECTORY">
              <td class="col-isActiveDirectory"><listen:checkMark value="${user.isActiveDirectory}"/></td>
            </listen:ifLicensed>
            <td class="col-lastLogin">
              <g:if test="${user.lastLogin}">
                <span title="${joda.format(value: user.lastLogin, style: 'LL')}"><listen:prettytime date="${user.lastLogin}"/></span>
              </g:if>
              <g:else>
                Never
              </g:else>
            </td>
            <td class="col-button">
              <g:form controller="user" action="edit" method="get">
                <g:hiddenField name="id" value="${user.id}"/>
                <g:submitButton name="edit" value="${g.message(code: 'default.button.edit.label')}"/>
              </g:form>
            </td>
            <td class="col-button">
              <g:if test="${user.enabled}">
                <g:form controller="user" action="disable" method="post">
                  <g:hiddenField name="id" value="${user.id}"/>
                  <input type="submit" name="disable" value="${g.message(code: 'default.button.disable.label')}"<g:if test="${sec.loggedInUserInfo(field: 'id') as Long == user.id}">readonly="readonly" class="disabled" disabled="disabled" title="You cannot disable your own account"</g:if>/>
                </g:form>
              </g:if>
              <g:else>
                <g:form controller="user" action="enable" method="post">
                  <g:hiddenField name="id" value="${user.id}"/>
                  <input type="submit" name="enable" value="${g.message(code: 'default.button.enable.label')}"/>
                </g:form>
              </g:else>
            </td>
          </tr>
        </g:each>
      </tbody>
    </table>
    <g:if test="${userListTotal > 0}">
      <listen:paginateTotal total="${userListTotal}" messagePrefix="paginate.total.users"/>
      <div class="pagination">
        <g:paginate total="${userListTotal}" maxSteps="5"/>
      </div>
    </g:if>
  </body>
</html>