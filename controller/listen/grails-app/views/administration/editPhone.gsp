<html>
  <head>
    <title><g:message code="page.administration.phones.editPhone.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="phones"/>
    <tooltip:resources/>
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
        
        select.prompt-select {
            width: 256px;
        }
        
        select.options-prompt {
            display: inline;
        }

        .acdEditHeading
        {
            width: 277px;
        }

        td, th
        {
            border-bottom: solid 1px #d3d3d3;
        }

        .acdEditValue
        {
            width: 256px;
        }
    </style>

  </head>
  <body>

    <g:form controller="administration" action="updateExtension" method="post">
        <input type="hidden" name="id" type="hidden" value="${fieldValue(bean: extension, field: 'id')}"/>
        <table>
          <caption><g:message code="page.administration.phones.editPhone.title"/></caption>
          <tbody>
            <tr>
                <td class="col-number"><g:message code="page.administration.phones.column.number"/></td>
                <td class="val-number"><g:textField name="number" value="${fieldValue(bean: extension, field: 'number')}" class="${listen.validationClass(bean: extension, field: 'number')}"/></td>
            </tr>
            <tr>
                <td class="col-owner"><g:message code="page.administration.phones.column.owner"/></td>
                <td class="col-owner"><listen:userSelectForOperator name="owner.id" value="${extension.owner.id}" class="${listen.validationClass(bean: extension, field: 'owner')}"/></td>
            </tr>
            <tr>
                <td class="col-userId"><g:message code="page.administration.phones.column.userId"/></td>
                <td class="val-userId"><g:textField name="phoneUserId" value="${fieldValue(bean: sipPhone, field: 'phoneUserId')}" class="${listen.validationClass(bean: sipPhone, field: 'phoneUserId')}"/></td>
              </tr>
            <tr>
                <td class="col-number"><g:message code="page.administration.phones.column.username"/></td>
                <td class="val-number"><g:textField name="username" value="${fieldValue(bean: sipPhone, field: 'username')}" class="${listen.validationClass(bean: sipPhone, field: 'username')}"/></td>
            </tr>
            <tr onmouseover="tooltip.show('${sipPhone?.password}');" onmouseout="tooltip.hide();">
                <td class="col-password"><g:message code="page.administration.phones.column.password"/></td>
                <td class="val-password"><g:passwordField name="password" value="${fieldValue(bean: sipPhone, field: 'password')}" class="${listen.validationClass(bean: sipPhone, field: 'password')}" placeholder="${g.message(code: 'page.administration.phones.password.placeholder')}" autocomplete="off"/></td>
            </tr>
            <tr>
                <td class="col-number"><g:message code="page.administration.phones.column.macAddress" default="Mac Address"/></td>
                <td class="val-number"><g:textField name="provisionerIdentifier" value="${fieldValue(bean: sipPhone, field: 'provisionerIdentifier')}" class="${listen.validationClass(bean: sipPhone, field: 'provisionerIdentifier')}"/></td>
            </tr>
            <tr>
                <td class="col-number"><g:message code="page.administration.phones.column.Template" default="Config Template"/></td>
                <td class="val-number"><g:select name="provisionerTemplate"
                                                 class="filterDropdown"
                                                 optionKey="id"
                                                 optionValue="name"
                                                 from="${templates}"
                                                 value="${sipPhone.provisionerTemplate ? sipPhone.provisionerTemplate.id : ''}"
                                                 noSelection="['':'']" /></td>
            </tr>
          </tbody>
        </table>

        %{--<h3>Custom Template Fields</h3>--}%
        %{--<table>--}%
            %{--<g:each in="${userFields}" var="field" status="i">--}%
                %{--<tr>--}%
                    %{--<td class="col-number">${field?.provisionerTemplateField?.name}</td>--}%
                    %{--<td class="val-number"><g:textField name="fields.${i}.fieldValue" value="${field.fieldValue}"/><g:hiddenField name="fields.${i}.id" value="${field.id}"/></td>--}%
                %{--</tr>--}%
            %{--</g:each>--}%
        %{--</table>--}%

        <h3>Information</h3>
        <table>
          <tbody>
            <tr>
                <td class="col-realname"><g:message code="page.administration.phones.column.realName"/></td>
                <td class="val-realname">${sipPhone?.realName}</td>
            </tr>
            <tr>
                <td class="col-datereg"><g:message code="page.administration.phones.column.dateRegistered"/></td>
                <td class="col-datereg" onmouseover="tooltip.show('CSeq:${sipPhone?.cseq}');" onmouseout="tooltip.hide();"><joda:format value="${sipPhone?.dateRegistered}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            </tr>
            <tr>
                <td class="col-provupdate"><g:message code="page.administration.phones.column.provisionerLastUpdated" default="Last Provisioner Update"/></td>
                <td class="col-provupdate"><joda:format value="${sipPhone?.provisionerLastUpdated}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            </tr>
            <tr>
                <td class="col-ip"><g:message code="page.administration.phones.column.ip"/></td>
                <td class="col-ip">${sipPhone?.ip}</td>
            </tr>
            <tr>
                <td class="col-useragent"><g:message code="page.administration.phones.column.userAgent"/></td>
                <td class="col-useragent">${sipPhone?.userAgent}</td>
            </tr>
            </tbody>
        </table>

        <div class="buttons">
            <g:submitButton name="updateExtension" style="margin-left: 40%" value="${g.message(code: 'default.button.update.label')}"/>
            <g:actionSubmit action="listPhones" value="${message(code: 'default.button.cancel.label', default: 'Cancel')}"/>
        </div>


    </g:form>

  </body>
</html>
