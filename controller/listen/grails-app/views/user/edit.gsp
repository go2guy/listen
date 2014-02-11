<%@ page import="com.interact.listen.acd.*" %>
<!doctype html>
<html>
  <head>
    <title><g:message code="page.user.edit.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="users"/>
    <meta name="button" content="edit"/>
    <meta name="page-header" content="${g.message(code: 'page.user.edit.header')}"/>
    <style type="text/css">

        .skillsDiv { padding-top: 20px; }

        .priorityDropdown { width: 40px; }

        .priorityDropdown .hidden{ display: none; }

    </style>
  </head>
  <body>
    <g:if test="${!user.enabled}">
      <ul class="messages info">
        <li><g:message code="page.user.edit.disabled"/></li>
      </ul>
    </g:if>

    <g:form controller="user" action="update" method="post" autocomplete="off">
      <fieldset class="vertical">
        <g:hiddenField name="id" value="${user.id}"/>

        <label for="username"><g:message code="user.username.label"/></label>

        <g:if test="${user?.isActiveDirectory}">
            <g:textField name="username" value="${fieldValue(bean: user, field: 'username')}" maxlength="50" disabled="disabled" readonly="readonly" class="disabled"/>
            <h3 class="active-directory-label">This is an Active Directory account.</h3>
        </g:if>
        <g:else>
            <g:textField name="username" value="${fieldValue(bean: user, field: 'username')}" maxlength="50"/>
            <label for="pass"><g:message code="user.new.pass.label"/></label>
            <g:passwordField name="pass" class="${listen.validationClass(bean: user, field: 'pass')}"/>

             <label for="confirm"><g:message code="user.new.confirm.label"/></label>
             <g:passwordField name="confirm" class="${listen.validationClass(bean: user, field: 'confirm')}"/>
        </g:else>

        <label for="realName"><g:message code="user.realName.label"/></label>
        <g:textField name="realName" value="${fieldValue(bean: user, field: 'realName')}" maxlength="50" class="${listen.validationClass(bean: user, field: 'realName')}"/>

        <label for="emailAddress"><g:message code="user.emailAddress.label"/></label>
        <g:textField name="emailAddress" value="${fieldValue(bean: user, field: 'emailAddress')}" class="${listen.validationClass(bean: user, field: 'emailAddress')}"/>

        <g:if test="${userSkills}">
          <div id="acdSkillsDiv" class="skillsDiv">
          %{--<label for="skills"><g:message code="user.skills.label"/></label>--}%
              <table style="width: 305px;">
                <thead>
                  <th></th>
                  <th>ACD Skill</th>
                  <th>Priority</th>
                </thead>
                <tbody>
                  <g:each in="${userSkills}" var="skill">
                    %{--<g:hiddenField name="skillId" value="${skill.id}"/>--}%
                    <tr>
                        <td><g:checkBox name="selected${skill.id}" value="${skill.selected ? "true" : "false"}"
                                        checked="${skill.selected}"
                                        onchange="togglePriority(this, ${skill.id})"/></td>

                        <td>${skill.skillname}</td>
%{--                        <td><g:textField id="priority${skill.id}" name="priority${skill.id}"
                                         value="${skill.selected && skill.priority != "0" ? skill.priority : ''}"
                                         maxlength="3" disabled="${!skill.selected}" style="width: 30px;"/></td>--}%
                         <td><g:select id="priority${skill.id}" name="priority${skill.id}"
                                       class="priorityDropdown"
                                       value="${skill.selected && skill.priority != "0" ? skill.priority : ''}"
                                       noSelection="${['6':'']}"
                                       from="${1..5}"
                                       disabled="${!skill.selected}">
                         </g:select></td>
                    </tr>
                  </g:each>
                </tbody>
              </table>
            </div>
        </g:if>
            
        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/></li>
        </ul>
        
      </fieldset>
    </g:form>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#username').focus();
        });

        function togglePriority(element, skill_id) {
            if(element.checked)
            {
                $("#" + "priority" + skill_id)[0].disabled = false;
                element.value = "true";
            }
            else
            {
                $("#" + "priority" + skill_id)[0].disabled = true;
                element.value = false;
            }
        };
    </script>
  </body>
</html>
