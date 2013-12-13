<html>
  <head>
    <title><g:message code="page.administration.acd.skills.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="skills"/>
    <style type="text/css">
table { margin-bottom: 10px; }

tr.add td.col-button {
    text-align: center;
    width: 10%;
}

tr.disabled {
    color: #999999;
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

    <table>
      <caption><g:message code="page.administration.acd.skills.caption"/>
      <tbody>
        <tr class="add highlighted">
          <g:form controller="administration" action="addSkill" method="post">
            <td class="col-skillname"><g:textField name="skillname" value="${fieldValue(bean: newSkill, field: 'skillname')}" placeholder="${g.message(code: 'page.administration.acd.skills.new.skillname.placeholder')}" class="${listen.validationClass(bean: newSkill, field: 'skillname')}"/></td>
            <td class="col-description"><g:textField name="description" value="${fieldValue(bean: newSkill, field: 'description')}" placeholder="${g.message(code: 'page.administration.acd.skills.new.description.placeholder')}" class="${listen.validationClass(bean: newSkill, field: 'description')}"/></td>
            <td class="col-button" colspan="2"><g:submitButton name="add" value="${g.message(code: 'page.administration.acd.skills.button.addSkill')}"/></td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <g:if test="${skills.size() > 0}">
      <table>
        <thead>
          <tr>
            <th class="col-skillname"><g:message code="page.administration.acd.skills.column.skillname"/></th>
            <th class="col-userCount"><g:message code="page.administration.acd.skills.column.userCount"/></th>
            <th class="col-menuCount"><g:message code="page.administration.acd.skills.column.menuCount"/></th>
            <th class="col-button"></th>
            <th class="col-button"></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${skills}" var="skill" status="i">
            <tr class="<%= i % 2 == 0 ? 'even' : 'odd' %>">
              <g:form controller="administration" action="editSkill" method="post">
                <td class="col-skillname">${skill?.skillname}</td>
                <td class="col-usercount">${skill?.userCount}</td>
                <td class="col-menucount">${skill?.menuCount}</td>

                <td class="col-button">
                  <g:hiddenField name="id" value="${skill.id}"/>
                  <g:submitButton name="edit" value="${g.message(code: 'default.button.edit.label')}"/>
                </td>
              </g:form>
              <g:form controller="administration" action="deleteSkill" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${skill.id}"/>
                  <g:if test="${skill.menuCount == 0}">
                      <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                  </g:if>
                  <g:else>
                      <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}" readonly"readonly" class="disabled" disabled="disabled" title="Cannot delete skill that is currently associated with a menu"/>
                  </g:else>
                </td>
              </g:form>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:if>
      
  </body>
</html>