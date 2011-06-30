<html>
  <head>
    <title><g:message code="page.custodianAdministration.outdialing.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="custodianAdministration"/>
    <meta name="button" content="outdialing"/>
    <style type="text/css">
table { margin-bottom: 10px; }

.add tr td.button { text-align: center; }

.pattern {
    padding-left: 5px;
    padding-right: 5px;
    width: 820px; /* 350px - 5px - 5px (paddings)  = 480px (extra space since no user list)*/
}
.button { width: 50px; }
    </style>
  </head>
  <body>
    <listen:infoSnippet summaryCode="page.custodianAdministration.outdialing.snippet.summary" contentCode="page.custodianAdministration.outdialing.snippet.content"/>

    <table class="add highlighted">
      <caption><g:message code="page.custodianAdministration.outdialing.add.caption"/></caption>
      <tbody>
        <tr>
          <g:form controller="custodianAdministration" action="addRestriction" method="post">
            <td class="pattern"><g:textField name="pattern" value="${fieldValue(bean: newRestriction, field: 'pattern')}" placeholder="${g.message(code: 'page.custodianAdministration.outdialing.add.pattern.placeholder')}" class="${listen.validationClass(bean: newRestriction, field: 'pattern')}"/></td>
            <td class="button" colspan="2"><g:submitButton name="add" value="${g.message(code: 'page.custodianAdministration.outdialing.add.addButton')}"/></td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <g:if test="${restrictions.size() > 0}">
      <table>
        <caption><g:message code="page.custodianAdministration.outdialing.list.caption"/></caption>
        <thead>
          <tr>
            <th class="pattern"><g:message code="page.custodianAdministration.outdialing.column.pattern"/></th>
            <th class="button"></th>
            <th class="button"></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${restrictions}" var="restriction" status="i">
            <tr class="<%= i % 2 == 0 ? 'even' : 'odd' %>">
              <g:form controller="custodianAdministration" action="updateRestriction" method="post">
                <td class="pattern"><g:textField name="pattern" value="${fieldValue(bean: restriction, field: 'pattern')}" class="${listen.validationClass(bean: restriction, field: 'pattern')}"/></td>
                <td class="button">
                  <g:hiddenField name="id" value="${restriction.id}"/>
                  <g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="custodianAdministration" action="deleteRestriction" method="post">
                <td class="button">
                  <g:hiddenField name="id" value="${restriction.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:if>
  </body>
</html>