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
          <caption><g:message code="page.administration.phones.editPhone.title"/>
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
                <td class="col-number"><g:message code="page.administration.phones.column.username"/></td>
                <td class="val-number"><g:textField name="username" value="${fieldValue(bean: sipPhone, field: 'username')}" class="${listen.validationClass(bean: sipPhone, field: 'username')}"/></td>
            </tr>
            <tr onmouseover="tooltip.show('${sipPhone?.password}');" onmouseout="tooltip.hide();">
                <td class="col-password"><g:message code="page.administration.phones.column.password"/></td>
                <td class="val-password"><g:passwordField name="password" value="${fieldValue(bean: sipPhone, field: 'password')}" class="${listen.validationClass(bean: sipPhone, field: 'password')}" placeholder="${g.message(code: 'page.administration.phones.password.placeholder')}" autocomplete="off"/></td>
            </tr>
            <tr>
                <td class="col-password"><g:message code="page.administration.phones.column.passwordConfirm"/></td>
                <td class="val-password" onmouseover="tooltip.show('${sipPhone?.passwordConfirm}');" onmouseout="tooltip.hide();"><g:passwordField name="passwordConfirm" value="${fieldValue(bean: sipPhone, field: 'passwordConfirm')}" class="${listen.validationClass(bean: sipPhone, field: 'passwordConfirm')}" placeholder="${g.message(code: 'page.administration.phones.passwordConfirm.placeholder')}" autocomplete="off"/></td>
            </tr>
            <tr>
                <td class="col-realname"><g:message code="page.administration.phones.column.realName"/></td>
                <td class="val-realname">${sipPhone?.realName}</td>
            </tr>
            <tr>
                <td class="col-reg"><g:message code="page.administration.phones.column.registered"/></td>
                <g:if test="${sipPhone?.registered == true}">
                    <td class="col-reg">Registered</td>
                </g:if>
                <g:else>
                    <td class="col-reg">Not Registered</td>
                </g:else>
            </tr>
            <tr>
                <td class="col-datereg"><g:message code="page.administration.phones.column.dateRegistered"/></td>
                <td class="col-datereg" onmouseover="tooltip.show('CSeq:${sipPhone?.cseq}');" onmouseout="tooltip.hide();"><joda:format value="${sipPhone?.dateRegistered}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            </tr>
            <tr>
                <td class="col-ip"><g:message code="page.administration.phones.column.ip"/></td>
                <td class="col-ip">${sipPhone?.ip}</td>
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
