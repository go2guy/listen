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

.col-name { width: 55%; }
.col-enabled { width: 35%; }
.col-button { width: 5%; }

td.col-enabled { padding-left: 20px; }

fieldset.vertical {
    background: #F0F0F0;
    padding: 1px 10px 10px 10px;
}

tr.disabled {
    color: #999999;
}
    </style>
  </head>
  <body>
    <g:if test="${organizationList.size() > 0}">
      <table>
        <caption><g:message code="page.organization.list.header"/></caption>
        <thead>
          <g:sortableColumn property="name" title="${g.message(code: 'organization.name.label')}" class="col-name"/>
          <g:sortableColumn property="enabled" title="${g.message(code: 'organization.enabled.label')}" class="col-enabled"/>
          <th class="col-button"></th>
          <th class="col-button"></th>
        </thead>
        <tbody>
          <g:each in="${organizationList}" var="organization" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}${!organization.enabled ? ' disabled' : ''}">
              <td class="col-name">${fieldValue(bean: organization, field: 'name')}</td>
              <td class="col-enabled"><listen:checkMark value="${organization.enabled}"/></td>
              <td class="col-button">
                <g:form controller="organization" action="edit" method="get">
                  <g:hiddenField name="id" value="${organization.id}"/>
                  <g:submitButton name="edit" value="${g.message(code: 'default.button.edit.label')}"/>
                </g:form>
              </td>
              <td class="col-button">
                <g:if test="${organization.enabled}">
                  <g:form controller="organization" action="disable" method="post">
                    <g:hiddenField name="id" value="${organization.id}"/>
                    <g:submitButton name="disable" value="${g.message(code: 'default.button.disable.label')}"/>
                  </g:form>
                </g:if>
                <g:else>
                  <g:form controller="organization" action="enable" method="post">
                    <g:hiddenField name="id" value="${organization.id}"/>
                    <g:submitButton name="enable" value="${g.message(code: 'default.button.enable.label')}"/>
                  </g:form>
                </g:else>
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