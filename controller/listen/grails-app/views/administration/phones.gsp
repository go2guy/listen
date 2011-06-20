<html>
  <head>
    <title><g:message code="page.administration.phones.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="phones"/>
    <style type="text/css">
table { margin-bottom: 10px; }

.add tbody td.button { text-align: center; }

.col-number input[type=text] { width: 150px; }

.col-button { width: 50px; }
.add .col-button {
    text-align: center;
    width: 110px;
}

tbody .col-light { text-align: center; }
    </style>
  </head>
  <body>
    <table class="add highlighted">
      <caption><g:message code="page.administration.phones.add.caption"/></caption>
      <thead>
        <th class="col-number"><g:message code="page.administration.phones.column.number"/></th>
        <th class="col-type"><g:message code="page.administration.phones.column.type"/></th>
        <th class="col-owner"><g:message code="page.administration.phones.column.owner"/></th>
        <th class="col-button"></th>
      </thead>
      <tbody>
        <tr>
          <g:form controller="administration" action="addPhoneNumber" method="post">
            <!-- TODO number needs focus on page load -->
            <td class="col-number"><g:textField name="number" value="${fieldValue(bean: newPhoneNumber, field: 'number')}"/></td>
            <td class="col-type"><g:select name="type" from="${com.interact.listen.PhoneNumberType.values()}" optionKey="key" value="${newPhoneNumber?.type?.name()}"/></td>
            <td class="col-owner"><g:select name="owner.id" from="${users}" optionKey="id" optionValue="realName" value="${newPhoneNumber?.owner?.id}"/></td>
            <td class="col-button"><g:submitButton name="add" value="${g.message(code: 'page.administration.phones.add.button.addPhone')}"/></td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <table>
      <caption><g:message code="page.administration.phones.current.caption"/></caption>
      <thead>
        <g:sortableColumn property="number" title="${g.message(code: 'page.administration.phones.column.number')}" class="col-number"/>
        <g:sortableColumn property="type" title="${g.message(code: 'page.administration.phones.column.type')}" class="col-type"/>
        <g:sortableColumn property="owner" title="${g.message(code: 'page.administration.phones.column.owner')}" class="col-owner"/>
        <th class="col-button"></th>
        <th class="col-button"></th>
      </thead>
      <tbody>
        <g:if test="${phoneNumberList.size() > 0}">
          <g:each in="${phoneNumberList}" var="phoneNumber" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <g:form controller="administration" action="updatePhoneNumber" method="post">
                <td class="col-number"><g:textField name="number" value="${fieldValue(bean: phoneNumber, field: 'number')}"/></td>
                <td class="col-type"><g:select name="type" from="${com.interact.listen.PhoneNumberType.values()}" optionKey="key" value="${phoneNumber.type.name()}"/></td>
                <td class="col-owner"><g:select name="owner.id" from="${users}" optionKey="id" optionValue="realName" value="${phoneNumber.owner.id}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${phoneNumber.id}"/>
                  <g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="administration" action="deletePhoneNumber" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${phoneNumber.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </g:if>
        <g:else>
          <tr class="even"><td colspan="6"><g:message code="page.administration.phones.current.noPhones"/></td></tr>
        </g:else>
      </tbody>
    </table>
    <g:if test="${phoneNumberList.size() > 0}">
      <listen:paginateTotal total="${phoneNumberTotal}" messagePrefix="paginate.total.phones"/>
      <div class="pagination">
        <g:paginate total="${phoneNumberTotal}" maxsteps="5"/>
      </div>
    </g:if>
    <script type="text/javascript">
$(document).ready(function() {
    $('#number').focus();
});
    </script>
  </body>
</html>