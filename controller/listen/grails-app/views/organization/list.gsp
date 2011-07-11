<html>
  <head>
    <title><g:message code="page.organization.list.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="organization"/>
    <meta name="button" content="list"/>
    <style type="text/css">
.pagination {
    margin-bottom: 10px;
}

table td {
    padding: 2px 2px;
}

table td.button-cell {
    width: 5%;
}

fieldset.vertical {
    background: #F0F0F0;
    padding: 1px 10px 10px 10px;
}
    </style>
  </head>
  <body>
    <g:if test="${organizationList.size() > 0}">
      <table>
        <caption><g:message code="page.organization.list.header"/></caption>
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

      <listen:infoSnippet summaryCode="page.organization.list.snippet.summary" contentCode="page.organization.list.snippet.content"/>

      <g:form controller="organization" action="setSingleOrganization">
        <fieldset class="vertical">
          <label for="id" class="inline-label"><g:message code="page.organization.list.single.label"/></label>
          <g:select from="${organizationList}" name="id" optionKey="id" optionValue="name" noSelection="${['-1': g.message(code: 'page.organization.list.single.noSelection')]}" value="${fieldValue(bean: singleOrganization, field: 'id')}"/>

          <ul class="form-buttons">
            <li><g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/>
          </ul>
        </fieldset>
      </g:form>

    </g:if>
    <g:else>
      <g:message code="page.organization.list.noOrganizations"/>
    </g:else>
  </body>
</html>