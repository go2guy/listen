<html>
  <head>
    <title><g:message code="page.administration.outdialing.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="outdialing"/>
    <style type="text/css">
table { margin-bottom: 10px; }

tr.add td.col-button {
    text-align: center;
    width: 10%;
}

.col-pattern,
.col-target,
.col-restriction {
    width: 45%;
}
.col-button { width: 5%; }
    </style>
  </head>
  <body>

    <g:if test="${globalRestrictions.size() > 0}">
      <table>
        <caption><g:message code="page.administration.outdialing.global.caption"/></caption>
        <tbody>
          <g:each in="${globalRestrictions}" var="globalRestriction" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <td class="col-pattern">${fieldValue(bean: globalRestriction, field: 'pattern')}</td>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:if>

    <table>
      <caption><g:message code="page.administration.outdialing.restrictions.caption"/>
      <tbody>
        <tr class="add highlighted">
          <g:form controller="administration" action="addRestriction" method="post">
            <td class="col-pattern"><g:textField name="pattern" value="${fieldValue(bean: newRestriction, field: 'pattern')}" placeholder="${g.message(code: 'page.administration.outdialing.restrictions.new.pattern.placeholder')}" class="${listen.validationClass(bean: newRestriction, field: 'pattern')}"/></td>
            <td class="col-target"><listen:userSelectForOperator name="target" value="${newRestriction ? newRestriction.target?.id : ''}" noSelection="['': 'Everyone']"/></td>
            <td class="col-button" colspan="2"><g:submitButton name="add" value="${g.message(code: 'page.administration.outdialing.restrictions.button.addRestriction')}"/></td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <g:if test="${restrictions.size() > 0}">
      <table>
        <thead>
          <tr>
            <th class="col-pattern"><g:message code="page.administration.outdialing.restrictions.column.pattern"/></th>
            <th class="col-target"><g:message code="page.administration.outdialing.restrictions.column.target"/></th>
            <th class="col-button"></th>
            <th class="col-button"></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${restrictions}" var="restriction" status="i">
            <tr class="<%= i % 2 == 0 ? 'even' : 'odd' %>">
              <g:form controller="administration" action="updateRestriction" method="post">
                <td class="col-pattern"><g:textField name="pattern" value="${fieldValue(bean: restriction, field: 'pattern')}" placeholder="${g.message(code: 'page.administration.outdialing.restrictions.new.pattern.placeholder')}" class="${listen.validationClass(bean: restriction, field: 'pattern')}"/></td>
                <td class="col-target"><listen:userSelectForOperator name="target" value="${restriction.target?.id}" noSelection="${['': g.message(code: 'page.administration.outdialing.restrictions.targetNoSelectionValue')]}" class="${listen.validationClass(bean: restriction, field: 'target')}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${restriction.id}"/>
                  <g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="administration" action="deleteRestriction" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${restriction.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:if>

    <g:if test="${everyoneRestrictions.size() > 0}">
      <table>
        <caption><g:message code="page.administration.outdialing.exceptions.caption"/></caption>
        <tbody>
          <tr class="add highlighted">
            <g:form controller="administration" action="addException" method="post">
              <td class="col-target"><listen:userSelectForOperator name="target.id" value="${newException?.target?.id}" class="${listen.validationClass(bean: newException, field: 'target')}"/></td>
              <td class="col-restriction"><g:select name="restriction.id" from="${everyoneRestrictions}" optionKey="id" optionValue="pattern" value="${newException?.restriction?.id}" class="${listen.validationClass(bean: newException, field: 'restriction')}"/></td>
              <td class="col-button" colspan="2">
              <g:submitButton name="add" value="${g.message(code: 'page.administration.outdialing.exceptions.button.addException')}"/>
              </td>
            </g:form>
          </tr>
        </tbody>
      </table>

      <g:if test="${exceptions.size() > 0}">
        <table>
          <thead>
            <tr>
              <th class="col-target"><g:message code="page.administration.outdialing.exceptions.column.target"/></th>
              <th class="col-restriction"><g:message code="page.administration.outdialing.exceptions.column.restriction"/></th>
              <th class="col-button"></th>
              <th class="col-button"></th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${exceptions}" var="exception" status="i">
              <tr class="<%= i % 2 == 0 ? 'even' : 'odd' %>">
                <g:form controller="administration" action="updateException" method="post">
                  <td class="col-target"><listen:userSelectForOperator name="target.id" value="${exception.target.id}" class="${listen.validationClass(bean: exception, field: 'target')}"/></td>
                  <td class="col-restriction"><g:select name="restriction.id" from="${everyoneRestrictions}" optionKey="id" optionValue="pattern" value="${exception.restriction.id}" class="${listen.validationClass(bean: exception, field: 'restriction')}"/></td>
                  <td class="col-button">
                    <g:hiddenField name="id" value="${exception.id}"/>
                    <g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/>
                  </td>
                </g:form>
                <g:form controller="administration" action="deleteException" method="post">
                  <td class="col-button">
                    <g:hiddenField name="id" value="${exception.id}"/>
                    <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                  </td>
                </g:form>
              </tr>
            </g:each>
          </tbody>
        </table>
      </g:if>
    </g:if>
  </body>
</html>