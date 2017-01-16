<html>
  <head>
    <title><g:message code="page.organization.routing.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="organization"/>
    <meta name="button" content="routing"/>
    <style type="text/css">
table { margin-bottom: 10px; }
.add td.col-button { text-align: center; }

.col-pattern {
    padding-left: 5px;
    padding-right: 5px;
    width: 250px; /* 260px - 5px - 5px (paddings) */
}
.col-organization { width: 260px; }
.col-destination { width: 260px; }
.col-button { width: 50px; }
    </style>
  </head>
  <body>
    <listen:infoSnippet summaryCode="page.organization.routing.snippet.summary" contentCode="page.organization.routing.snippet.content"/>

    <table class="add">
      <caption><g:message code="page.organization.routing.add.caption"/></caption>
      <tbody>
        <tr>
          <g:form controller="organization" action="addRoute" method="post">
            <td class="col-pattern"><g:textField name="pattern" value="${fieldValue(bean: newRoute, field: 'pattern')}" placeholder="${g.message(code: 'page.organization.routing.add.pattern.placeholder')}" class="${listen.validationClass(bean: newRoute, field: 'pattern')}"/></td>
            <td class="col-organization"><listen:organizationSelect name="organization.id" value="${newRoute?.organization?.id}" class="organization-select add-organization-select"/></td>
            <td class="col-destination"><listen:applicationSelect name="destination" value="${newRoute?.destination}"/></td>
            <td class="col-button" colspan="2"><g:submitButton name="add" value="${g.message(code: 'page.organization.routing.add.addButton')}"/></td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <g:if test="${routes.size() > 0}">
      <table>
        <caption><g:message code="page.organization.routing.list.caption"/></caption>
        <thead>
          <tr>
            <th class="col-pattern"><g:message code="page.organization.routing.column.pattern"/></th>
            <th class="col-organization"><g:message code="page.organization.routing.column.organization"/></th>
            <th class="col-destination"><g:message code="page.organization.routing.column.destination"/></th>
            <th class="col-button"></th>
            <th class="col-button"></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${routes}" var="route" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <g:form controller="organization" action="updateRoute" method="post">
                <td class="col-pattern"><g:textField name="pattern" value="${fieldValue(bean: route, field: 'pattern')}" class="${listen.validationClass(bean: route, field: 'pattern')}"/></td>
                <td class="col-organization"><listen:organizationSelect name="organization.id" value="${route.organization?.id}" class="organization-select"/></td>
                <td class="col-destination"><listen:applicationSelect name="destination" value="${route.destination}" organization="${route.organization}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${route.id}"/>
                  <g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="organization" action="deleteRoute" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${route.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </tbody>
      </table>
      <g:if test="${routes.size() > 0}">
        <listen:paginateTotal total="${routesTotal}" messagePrefix="paginate.total.routes"/>
        <div class="pagination">
          <g:paginate total="${routesTotal}" maxSteps="5"/>
        </div>
      </g:if>
    </g:if>
    <script type="text/javascript">
$(document).ready(function() {
    // if the organization changes, the application select should only show applications available to that organization
    (function() {
        function update(orgSelect, data) {
            var appSelect = $('.application-select', orgSelect.closest('tr'));

            var newOptions = data[orgSelect.val()];
            var selectedApp = appSelect.val();

            $('option', appSelect).remove();
            for(var i = 0; i < newOptions.length; i++) {
                appSelect.append('<option>' + newOptions[i] + '</option>');
            }
            appSelect.val(selectedApp);
        };

        $.get('${createLink(controller: 'organization', action: 'allowedApplications')}', function(data) {
            update($('.add-organization-select'), data);
            $('.organization-select').change(function(e) {
                var orgSelect = $(e.target);
                update(orgSelect, data);
            });
        });
    })();
});
    </script>
  </body>
</html>