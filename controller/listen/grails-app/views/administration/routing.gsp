<html>
  <head>
    <title><g:message code="page.administration.routing.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="routing"/>
    <style type="text/css">
table { margin-bottom: 10px; }

tr.add td.col-button {
    text-align: center;
    width: 14%;
}

.col-pattern {
    padding-left: 5px;
    padding-right: 5px;
    width: 26%;
}
.col-destination { width: 34%; }
.col-label { width: 26%; }

.col-label input,
.col-pattern input {
    width: 200px;
}

#external .col-button { width: 14%; }
#internal .col-button,
#direct .col-button {
    width: 7%;
}
    </style>
  </head>
  <body>
    <g:if test="${external.size() > 0}">
      <table id="external">
        <caption><g:message code="page.administration.routing.external.caption"/></caption>
        <thead>
          <th class="col-pattern"><g:message code="page.administration.routing.external.column.pattern"/></th>
          <th class="col-destination"><g:message code="page.administration.routing.external.column.destination"/></th>
          <th class="col-label"></th>
          <th class="col-button"></th>
        </thead>
        <tbody>
          <g:each in="${external}" var="route" status="i">
            <tr class="<%= i % 2 == 0 ? 'even' : 'odd' %>">
              <g:form controller="administration" action="updateExternalRoute" method="post">
                <td class="col-pattern">${route?.pattern.encodeAsHTML()}</td>
                <td class="col-destination"><listen:applicationSelect name="destination" value="${route.destination}"/></td>
                <td class="col-label"><g:textField name="label" value="${fieldValue(bean: route, field: 'label')}" placeholder="${g.message(code: 'page.administration.routing.label.placeholder')}" class="${listen.validationClass(bean: route, field: 'label')}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${route.id}"/>
                  <g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:if>

    <table>
      <caption><g:message code="page.administration.routing.internal.caption"/></caption>
      <tbody>
        <tr class="add highlighted">
          <g:form controller="administration" action="addInternalRoute" method="post">
            <td class="col-pattern"><g:textField name="pattern" value="${fieldValue(bean: newRoute, field: 'pattern')}" placeholder="${g.message(code: 'page.administration.routing.internal.add.pattern.placeholder')}" class="${listen.validationClass(bean: newRoute, field: 'pattern')}"/></td>
            <td class="col-destination"><listen:applicationSelect name="destination" value="${newRoute?.destination}" class="${listen.validationClass(bean: newRoute, field: 'destination')}"/></td>
            <td class="col-label"><g:textField name="label" value="${fieldValue(bean: newRoute, field: 'label')}" placeholder="${g.message(code: 'page.administration.routing.label.placeholder')}" class="${listen.validationClass(bean: newRoute, field: 'label')}"/></td>
            <td class="col-button"><g:submitButton name="add" value="${g.message(code: 'page.administration.routing.internal.add.addButton')}"/></td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <g:if test="${internal.size() > 0}">
      <table id="internal">
        <thead>
          <th class="col-pattern"><g:message code="page.administration.routing.internal.column.pattern"/></th>
          <th class="col-destination"><g:message code="page.administration.routing.internal.column.destination"/></th>
          <th class="col-label"></th>
          <th class="col-button"></th>
          <th class="col-button"></th>
        </thead>
        <tbody>
          <g:each in="${internal}" var="route" status="i">
            <tr class="<%= i % 2 == 0 ? 'even' : 'odd' %>">
              <g:form controller="administration" action="updateInternalRoute" method="post">
                <td class="col-pattern"><g:textField name="pattern" value="${fieldValue(bean: route, field: 'pattern')}" class="${listen.validationClass(bean: route, field: 'pattern')}"/></td>
                <td class="col-destination"><listen:applicationSelect name="destination" value="${route.destination}"/></td>
                <td class="col-label"><g:textField name="label" value="${fieldValue(bean: route, field: 'label')}" placeholder="${g.message(code: 'page.administration.routing.label.placeholder')}" class="${listen.validationClass(bean: route, field: 'label')}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${route.id}"/>
                  <g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="administration" action="deleteInternalRoute" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${route.id}"/>
                  <g:submitButton name="submit" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:if>

    <table>
      <caption><g:message code="page.administration.routing.direct.caption"/></caption>
      <tbody>
        <tr class="add highlighted">
          <g:form controller="administration" action="addDirectMessageNumber" method="post">
            <td class="col-pattern"><g:textField name="number" value="${fieldValue(bean: newDirectMessageNumber, field: 'number')}" placeholder="${g.message(code: 'page.administration.routing.direct.add.number.placeholder')}" class="${listen.validationClass(bean: newDirectMessageNumber, field: 'number')}"/></td>
            <td class="col-owner"><g:select name="owner.id" from="${users}" optionKey="id" optionValue="realName" value="${newDirectMessageNumber?.owner?.id}" class="${listen.validationClass(bean: newDirectMessageNumber, field: 'owner')}"/></td>
            <td class="col-button"><g:submitButton name="add" value="${g.message(code: 'page.administration.routing.direct.add.addButton')}"/>
          </g:form>
        </tr>
      </tbody>
    </table>

    <g:if test="${directMessageNumbers.size() > 0}">
      <table id="direct">
        <thead>
          <th class="col-pattern"><g:message code="page.administration.routing.direct.column.number"/></th>
          <th class="col-owner"><g:message code="page.administration.routing.direct.column.owner"/></th>
          <th class="col-button"></th>
          <th class="col-button"></th>
        </thead>
        <tbody>
          <g:each in="${directMessageNumbers}" var="directMessageNumber" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <g:form controller="administration" action="updateDirectMessageNumber" method="post">
                <td class="col-pattern"><g:textField name="number" value="${fieldValue(bean: directMessageNumber, field: 'number')}" class="${listen.validationClass(bean: directMessageNumber, field: 'number')}"/></td>
                <td class="col-owner"><g:select name="owner.id" from="${users}" optionKey="id" optionValue="realName" value="${directMessageNumber.owner.id}" class="${listen.validationClass(bean: directMessageNumber, field: 'owner')}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${directMessageNumber.id}"/>
                  <g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="administration" action="deleteDirectMessageNumber" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${directMessageNumber.id}"/>
                  <g:submitButton name="submit" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:if>

    <script type="text/javascript">
var routing = {
    toggleLabelVisibility: function(row) {
        var pattern = $('input[name="pattern"]', row).val() || $('.col-pattern', row).text();
        var destination = $('select[name="destination"]', row).val();
        if(pattern.indexOf('*') == -1 && destination == 'Conferencing') {
            $('input[name="label"]', row).css('visibility', 'visible');
        } else {
            $('input[name="label"]', row).css('visibility', 'hidden');
        }
    }
};

$(document).ready(function() {
    $('table tbody tr').each(function() {
        var row = this;
        routing.toggleLabelVisibility(row);
        $('input[name="pattern"]', row).keyup(function(e) {
            util.typewatch(function() {
                routing.toggleLabelVisibility(row);
            }, 250);
        });
        $('select[name="destination"]', row).change(function(e) {
            routing.toggleLabelVisibility(row);
        });
    });

    // any invisible labels should have their values cleared so that they
    // don't fail validation if the pattern has changed to a wildcard
    $('form').submit(function(e) {
        $('input[name="label"]').each(function() {
            var input = $(this);
            if(input.css('visibility') === 'hidden') {
                input.val('');
            }
        });
        return true;
    });
});
    </script>
  </body>
</html>