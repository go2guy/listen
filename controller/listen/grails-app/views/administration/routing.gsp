<html>
<head>
    <title><g:message code="page.administration.routing.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="routing"/>
    <style type="text/css">
    table { margin-bottom: 10px; }
    thead {
        display: block;
        /*max-height: 20em;*/
    }
    tbody{
        display: block;
        max-height: 20em;
        overflow: scroll;
    }

    tr.add td.col-button {
        text-align: center;
        width: 14%;
    }

    .col-destination { width: 275px; }
    .col-label, .col-label input {
        width: 100px;
    }
    #internal .col-label, #internal .col-label input {
        padding-right: 100px;
    }
    #external .col-label, #external .col-label input {
        padding-right: 120px;
    }
    #directInwardDial .col-label, #directInwardDial .col-label input {
        padding-right: 180px;
    }
    #directMessageNumber .col-label, #directMessageNumber .col-label input {
        padding-right: 180px;
    }

    .col-pattern, .col-pattern input, .col-pattern select {
        padding-left: 5px;
        padding-right: 5px;
        width: 200px;
    }
    .col-pattern select {
        width: 210px;
    }

    .col-destination select, .col-owner select {
        width: 275px;
    }

    #external .col-button { width: 70px;}
    /*
    #internal .col-pattern input { width: 250px; }
    */
    #internal .col-button button, #external .col-button button,
    #directInwardDial .col-button {
        width: 7%;
    }
    #directMessageNumber .col-button {
        width: 7%;
    }
    </style>
</head>
<body>
<g:if test="${external.size() > 0}">
    <table id="external">
        <caption><g:message code="page.administration.routing.external.caption"/></caption>
        <thead>
        <tr><span>
            <th class="col-pattern"><g:message code="page.administration.routing.external.column.pattern"/></th>
            <th class="col-destination"><g:message code="page.administration.routing.external.column.destination"/></th>
            <th class="col-label"></th>
            <th class="col-button"></th>
        </span>
        </tr>
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
    <tbody id="internal">
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
    <caption><g:message code="page.administration.routing.directInwardDial.caption"/></caption>
    <table id="directInwardDial">
    <tbody>
    <tr class="add highlighted">
        <g:form controller="administration" action="addDirectInwardDialNumber" method="post">
            <td class="col-pattern"><g:select name="number" optionKey="pattern" optionValue="pattern" from="${externalDIDs}" noSelection="${['':'-- Choose Number --']}" class="${listen.validationClass(bean: newDirectInwardDialNumber, field: 'number')}"/></td>
            <td class="col-owner"><listen:userSelectForOperator name="owner.id" value="${newDirectInwardDialNumber?.owner?.id}" noSelection="${['':'-- Choose Owner --']}" class="${listen.validationClass(bean: newDirectInwardDialNumber, field: 'owner')}"/></td>
            <td class="col-button"><g:submitButton name="add" value="${g.message(code: 'page.administration.routing.directInwardDial.add.addButton')}"/>
        </g:form>
    </tr>
    </tbody>
</table>

<g:if test="${directInwardDialNumbers.size() > 0}">
    <table id="directInwardDial">
        <thead>
        <th class="col-pattern"><g:message code="page.administration.routing.directInwardDial.column.number"/></th>
        <th class="col-owner"><g:message code="page.administration.routing.directInwardDial.column.owner"/></th>
        <th class="col-label"></th>
        <th class="col-button"></th>
        <th class="col-button"></th>
        </thead>
        <tbody>
        <g:each in="${directInwardDialNumbers}" var="directInwardDialNumber" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
                <g:form controller="administration" action="updateDirectInwardDialNumber" method="post">
                    <td class="col-pattern"><g:textField name="number" value="${fieldValue(bean: directInwardDialNumber, field: 'number')}" disabled="true" class="${listen.validationClass(bean: directInwardDialNumber, field: 'number')}"/></td>
                    <td class="col-owner"><listen:userSelectForOperator name="owner.id" value="${directInwardDialNumber.owner.id}" class="${listen.validationClass(bean: directInwardDialNumber, field: 'owner')}"/></td>
                    <th class="col-label"></th>
                    <td class="col-button">
                        <g:hiddenField name="id" value="${directInwardDialNumber.id}"/>
                        <g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/>
                    </td>
                </g:form>
                <g:form controller="administration" action="deleteDirectInwardDialNumber" method="post">
                    <td class="col-button">
                        <g:hiddenField name="id" value="${directInwardDialNumber.id}"/>
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
    <table id="directMessageNumber">
    <tbody>
    <tr class="add highlighted">
        <g:form controller="administration" action="addDirectMessageNumber" method="post">
            <td class="col-pattern"><g:select name="number" optionKey="pattern" optionValue="pattern" from="${externalDMs}" noSelection="${['':'-- Choose Number --']}" class="${listen.validationClass(bean: newDirectMessageNumber, field: 'number')}"/> </td>
            <td class="col-owner"><listen:userSelectForOperator name="owner.id" value="${newDirectMessageNumber?.owner?.id}" noSelection="${['':'-- Choose Owner --']}" class="${listen.validationClass(bean: newDirectMessageNumber, field: 'owner')}"/></td>
            <td class="col-button"><g:submitButton name="add" value="${g.message(code: 'page.administration.routing.direct.add.addButton')}"/>
        </g:form>
    </tr>
    </tbody>
</table>

<g:if test="${directMessageNumbers.size() > 0}">
    <table id="directMessageNumber">
        <thead>
        <th class="col-pattern"><g:message code="page.administration.routing.direct.column.number"/></th>
        <th class="col-owner"><g:message code="page.administration.routing.direct.column.owner"/></th>
        <th class="col-label"></th>
        <th class="col-button"></th>
        <th class="col-button"></th>
        </thead>
        <tbody>
        <g:each in="${directMessageNumbers}" var="directMessageNumber" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
                <g:form controller="administration" action="updateDirectMessageNumber" method="post">
                    <td class="col-pattern"><g:textField name="number" value="${fieldValue(bean: directMessageNumber, field: 'number')}"  disabled="true" class="${listen.validationClass(bean: directMessageNumber, field: 'number')}"/></td>
                    <td class="col-owner"><listen:userSelectForOperator name="owner.id" value="${directMessageNumber.owner.id}" class="${listen.validationClass(bean: directMessageNumber, field: 'owner')}"/></td>
                    <th class="col-label"></th>
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