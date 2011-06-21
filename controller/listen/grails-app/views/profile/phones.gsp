<html>
  <head>
    <title><g:message code="page.profile.phones.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="profile"/>
    <meta name="button" content="phones"/>
    <style type="text/css">
table { margin-bottom: 10px; }

#system-phones .col-number { width: 20%; }
#system-phones .col-type { width: 30%; }
#system-phones .col-button { width: 5%; }

.user-phones .col-number { width: 20%; }
.user-phones .col-type { width: 30%; }
.user-phones .col-public { width: 45%; }
.user-phones .col-button { width: 5%; }

.col-forward input[type=text] { width: 150px; }
.col-number input[type=text] { width: 150px; }

td.col-public {
    padding-left: 20px;
}

.add .col-button { width: 60px; }
.col-button { text-align: center; }
    </style>
  </head>
  <body>
    <g:if test="${systemPhoneNumberList.size() > 0}">
      <table id="system-phones">
        <caption><g:message code="page.profile.phones.caption.system"/></caption>
        <thead>
          <th class="col-number"><g:message code="phoneNumber.number.label"/></th>
          <th class="col-type"><g:message code="phoneNumber.type.label"/></th>
          <th class="col-public"><g:message code="phoneNumber.isPublic.label"/></th>
          <th class="col-forward"><g:message code="phoneNumber.forwardedTo.label"/></th>
          <th class="col-button"></th>
        </thead>
        <tbody>
          <g:each in="${systemPhoneNumberList}" var="phoneNumber" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <g:form controller="profile" action="updateSystemPhoneNumber" method="post">
                <td class="col-number">${fieldValue(bean: phoneNumber, field: 'number')}</td>
                <td class="col-type">${fieldValue(bean: phoneNumber, field: 'type')}</td>
                <td class="col-public"><listen:checkMark value="true"/></td>
                <td class="col-forward">
                  <g:if test="${phoneNumber.type == com.interact.listen.PhoneNumberType.EXTENSION}">
                    <g:textField name="forwardedTo" value="${fieldValue(bean: phoneNumber, field: 'forwardedTo')}"/>
                  </g:if>
                </td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${phoneNumber.id}"/>
                  <g:submitButton name="save" value="Save"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:if>

    <table class="user-phones">
      <caption><g:message code="page.profile.phones.caption.personal"/></caption>
      <tbody>
        <tr class="add highlighted">
          <g:form controller="profile" action="addUserPhoneNumber" method="post">
            <td class="col-number"><g:textField name="number" value="${fieldValue(bean: newPhoneNumber, field: 'number')}"/></td>
            <td class="col-type"><g:select name="type" from="${com.interact.listen.PhoneNumberType.userTypes()}" optionKey="key" value="${newPhoneNumber?.type?.name()}"/></td>
            <td class="col-public"><g:checkBox name="isPublic" value="${newPhoneNumber?.isPublic}"/></td>
            <td class="col-button" colspan="2">
              <g:submitButton name="add" value="${g.message(code: 'default.add.label', args: [g.message(code: 'phoneNumber.label')])}"/>
            </td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <table class="user-phones">
      <thead>
        <th class="col-number"><g:message code="phoneNumber.number.label"/></th>
        <th class="col-type"><g:message code="phoneNumber.type.label"/></th>
        <th class="col-public"><g:message code="phoneNumber.isPublic.label"/></th>
        <th class="col-button"></th>
        <th class="col-button"></th>
      </thead>
      <tbody>
        <g:if test="${userPhoneNumberList.size() > 0}">
          <g:each in="${userPhoneNumberList}" var="phoneNumber" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <g:form controller="profile" action="updateUserPhoneNumber" method="post">
                <td class="col-number"><g:textField name="number" value="${fieldValue(bean: phoneNumber, field: 'number')}"/></td>
                <td class="col-type"><g:select name="type" from="${com.interact.listen.PhoneNumberType.userTypes()}" optionKey="key" value="${phoneNumber.type.name()}"/></td>
                <td class="col-public"><g:checkBox name="isPublic" value="${phoneNumber.isPublic}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${phoneNumber.id}"/>
                  <g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="profile" action="deleteUserPhoneNumber" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${phoneNumber.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </g:if>
        <g:else>
          <tr class="even"><td colspan="5"><g:message code="page.profile.phones.noPersonalPhones"/></td></tr>
        </g:else>
      </tbody>
    </table>
  </body>
</html>