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

.col-pattern,
.col-target,
.col-restriction {
    width: 45%;
}
.col-button { width: 5%; }
    </style>
  </head>
  <body>

    <g:form controller="administration" action="updateSkill" method="post">
        <input type="hidden" name="id" type="hidden" value="${skill.id}"/>
        <table>
          <caption><g:message code="page.administration.acd.editSkills.title"/>
          <tbody>
            <tr>
                <td><g:message code="page.administration.acd.skills.column.skillname"/></td>
            </tr>
            <tr class="add highlighted">
                <td><g:textField name="skillname" value="${fieldValue(bean: skill, field: 'skillname')}"/></td>
            </tr>

            <tr>
                <td><g:message code="page.administration.acd.skills.column.description"/></td>
            </tr>
            <tr class="add highlighted">
                <td><g:textField name="description" value="${fieldValue(bean: skill, field: 'description')}"/></td>
            </tr>

            <tr>
                <td><g:message code="page.administration.acd.skills.column.users"/></td>
            </tr>
            <tr class="add highlighted">
                <td><g:select id="userIds" name="userIds" multiple="multiple" optionKey="id" optionValue="realName" from="${orgUsers}" value="${skillUsers.user}"/></td>
            </tr>
          </tbody>
        </table>

        <div class="buttons">
            <g:submitButton name="update" value="${g.message(code: 'default.button.update.label')}"/>
            <g:actionSubmit action="skills" value="${message(code: 'default.button.cancel.label', default: 'Cancel')}"/>
        </div>
        
    </g:form>
    

      
  </body>
</html>