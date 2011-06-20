<html>
  <head>
    <title><g:message code="page.organization.list.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="organization"/>
    <meta name="button" content="list"/>
    <meta name="page-header" content="${g.message(code: 'page.organization.list.header')}"/>
    <style type="text/css">
table td {
    padding: 2px 2px;
}

table td.button-cell {
    width: 5%;
}
    </style>
  </head>
  <body>
    <g:if test="${organizationList.size() > 0}">
      <table>
        <thead>
          <g:sortableColumn property="name" title="${g.message(code: 'organization.name.label')}"/>
          <th></th>
          <th></th>
        </thead>
        <tbody>
          <g:each in="${organizationList}" var="organization" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <td>${fieldValue(bean: organization, field: 'name')}</td>
              <td class="button-cell">
                <g:form controller="organization" action="edit" method="get">
                  <g:hiddenField name="id" value="${organization.id}"/>
                  <g:submitButton name="edit" value="${g.message(code: 'default.button.edit.label')}"/>
                </g:form>
              </td>
              <td class="button-cell">
                <g:form controller="organization" action="delete" method="post">
                  <g:hiddenField name="id" value="${organization.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </g:form>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>
      <g:if test="${organizationList.size() > 0}">
        <listen:paginateTotal total="${organizationListTotal}" messagePrefix="paginate.total.organizations"/>
        <div class="pagination">
          <g:paginate total="${organizationListTotal}" maxSteps="5"/>
        </div>
      </g:if>
    </g:if>
    <g:else>
      <g:message code="page.organization.list.noOrganizations"/>
    </g:else>
  </body>
</html>