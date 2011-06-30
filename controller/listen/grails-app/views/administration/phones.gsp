<html>
  <head>
    <title><g:message code="page.administration.phones.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="phones"/>
    <style type="text/css">
table { margin-bottom: 10px; }

.add tbody td.button { text-align: center; }

.col-number input[type=text], .col-ip input[type=text] { width: 150px; }

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
        <th class="col-owner"><g:message code="page.administration.phones.column.owner"/></th>
        <th class="col-ip"><g:message code="page.administration.phones.column.ip"/></th>
        <th class="col-button"></th>
      </thead>
      <tbody>
        <tr>
          <g:form controller="administration" action="addExtension" method="post">
            <!-- TODO number needs focus on page load -->
            <td class="col-number"><g:textField name="number" value="${fieldValue(bean: newExtension, field: 'number')}" class="${listen.validationClass(bean: newExtension, field: 'number')}"/></td>
            <td class="col-owner"><g:select name="owner.id" from="${users}" optionKey="id" optionValue="realName" value="${newExtension?.owner?.id}" class="${listen.validationClass(bean: newExtension, field: 'owner')}"/></td>
            <td class="col-ip"><g:textField name="ip" value="${fieldValue(bean: newExtension, field: 'ip')}" class="${listen.validationClass(bean: newExtension, field: 'ip')}"/></td>
            <td class="col-button"><g:submitButton name="add" value="${g.message(code: 'page.administration.phones.add.button.addExtension')}"/></td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <table>
      <caption><g:message code="page.administration.phones.current.caption"/></caption>
      <thead>
        <g:sortableColumn property="number" title="${g.message(code: 'page.administration.phones.column.number')}" class="col-number"/>
        <g:sortableColumn property="owner" title="${g.message(code: 'page.administration.phones.column.owner')}" class="col-owner"/>
        <g:sortableColumn property="ip" title="${g.message(code: 'page.administration.phones.column.ip')}" class="col-ip"/>
        <th class="col-button"></th>
        <th class="col-button"></th>
      </thead>
      <tbody>
        <g:if test="${extensionList.size() > 0}">
          <g:each in="${extensionList}" var="extension" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <g:form controller="administration" action="updateExtension" method="post">
                <td class="col-number"><g:textField name="number" value="${fieldValue(bean: extension, field: 'number')}" class="${listen.validationClass(bean: extension, field: 'number')}"/></td>
                <td class="col-owner"><g:select name="owner.id" from="${users}" optionKey="id" optionValue="realName" value="${extension.owner.id}" class="${listen.validationClass(bean: nextension, field: 'owner')}"/></td>
                <td class="col-ip"><g:textField name="ip" value="${fieldValue(bean: extension, field: 'ip')}" class="${listen.validationClass(bean: extension, field: 'ip')}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${extension.id}"/>
                  <g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="administration" action="deleteExtension" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${extension.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </g:if>
        <g:else>
          <tr class="even"><td colspan="6"><g:message code="page.administration.phones.current.noExtensions"/></td></tr>
        </g:else>
      </tbody>
    </table>
    <g:if test="${extensionList.size() > 0}">
      <listen:paginateTotal total="${extensionTotal}" messagePrefix="paginate.total.extensions"/>
      <div class="pagination">
        <g:paginate total="${extensionTotal}" maxsteps="5"/>
      </div>
    </g:if>
    <script type="text/javascript">
$(document).ready(function() {
    $('#number').focus();
});
    </script>
  </body>
</html>