<%--
  Created by IntelliJ IDEA.
  User: cgeesey
  Date: 12/6/2016
  Time: 8:44 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title><g:message code="page.administration.templates.edit.title" default="Edit Provisioner Template"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="provisionerTemplates"/>
    <tooltip:resources/>
    <script type="application/javascript">
    $(document).ready(function() {
        var deleteField = function(e) {
            e.preventDefault();
            var fieldId = $(this).attr("data-id");
            switch($(this).text()) {
                case "Undo":
                    $("#field-"+fieldId).css("background-color", "transparent");
                    $("#field-"+fieldId).find("input[name='fields."+fieldId+".deleted']").val("false");
                    $("#field-"+fieldId).find("input").each(function() {
                        console.log(this);
                        $(this).removeAttr("readonly");
                    });
                    $(this).text("Delete");
                    break;
                default:
                    $("#field-"+fieldId).css("background-color", "red");
                    $("#field-"+fieldId).find("input[name='fields."+fieldId+".deleted']").val("true");
                    $("#field-"+fieldId).find("input").each(function() {
                        console.log(this);
                        $(this).attr("readonly", "readonly");
                    });
                    $(this).text("Undo");
                    break;
            }
        };

        $(".deleteField").click(deleteField);

        $(".addField").click(function(e) {
            e.preventDefault();
            var lastrow = $("#fieldrows tr:last");
            var nextid = 0;
            if (lastrow.length > 0) {
                nextid = parseInt(lastrow.attr("id").split("-")[1], 10) + 1;
            }
            var tr = $("<tr id=\"field-"+nextid+"\"></tr>");
            tr.append('<td><input type="text" name="fields.'+nextid+'.name" value=""/><input type="hidden" name="fields.'+nextid+'.deleted" value="false"/></td>');
            tr.append('<td><input type="text" name="fields.'+nextid+'.defaultValue" value=""/></td>');
            var a = $('<a href="#" data-id="'+nextid+'" class="deleteField">Delete</a>');
            a.click(deleteField);
            tr.append($("<td></td>").html(a));
            $("#fieldrows").append(tr)
        });
    });
    </script>
    <style type="text/css">
        .template-info {
            background-color: #EBEEF5;
            border-collapse:separate;
            border: 1px solid #6DACD6;
            border-spacing: 0em;
            padding: 5px;
            border-radius: 5px;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            font-size: .9em;
        }

        .textarea {
            width: 99%;
        }

        .buttons {
            margin-left: auto;
            margin-right: auto;
            text-align: center;
        }
    </style>
</head>

<body>

<h3>Edit ${template.name}</h3>
<g:form controller="administration" action="updateTemplate" id="${template.id}">
<table>
    <tr>
        <td><g:message code="page.administration.templates.column.name" default="Template Name"/>:</td>
        <td><g:textField name="name" value="${template.name}"/></td>
    </tr>
    <tr>
        <td style="vertical-align: text-top;"><g:message code="page.administration.templates.variables.label" default="Variables"/>:</td>
        <td>
            <table class="template-info">
                <tr><td>&lt;%=number%&gt;</td>          <td>Extension number</td></tr>
                <tr><td>&lt;%=userId%&gt;</td>          <td>"User ID" from extension</td></tr>
                <tr><td>&lt;%=username%&gt;</td>        <td>"Username" from extension</td></tr>
                <tr><td>&lt;%=password%&gt;</td>        <td>"Password" from extension</td></tr>
                <tr><td>&lt;%=organizationId%&gt;</td>  <td>Organization Id of Extension</td></tr>
                <tr><td>&lt;%=realName%&gt;</td>        <td>Real name of user assigned to the extension</td></tr>
                <tr><td>&lt;%=macAddress%&gt;</td>      <td>Mac Address assigned to this extension</td></tr>
            </table>
        </td>
    </tr>
    <tr>
        <td style="vertical-align: text-top;"><g:message code="page.administration.templates.column.template" default="Template"/>:</td>
        <td class="textarea-container"><g:textArea class="textarea" name="template" value="${template.template}" rows="30"/></td>
    </tr>
</table>

%{--<h3>Custom Fields</h3>--}%
%{--<table>--}%
    %{--<thead>--}%
        %{--<tr>--}%
            %{--<td>Name</td>--}%
            %{--<td>Default Value</td>--}%
        %{--</tr>--}%
    %{--</thead>--}%

    %{--<tbody id="fieldrows">--}%
    %{--<g:each in="${fields}" var="field" status="i">--}%
        %{--<tr id="field-${i}">--}%
            %{--<td><g:textField name="fields.${i}.name" value="${field.name}"/><g:hiddenField name="fields.${i}.id" value="${field.id}"/>--}%
                %{--<g:hiddenField name="fields.${i}.deleted" value="false"/></td>--}%
            %{--<td><g:textField name="fields.${i}.defaultValue" value="${field.defaultValue}"/></td>--}%
            %{--<td><a href="#" data-id="${i}" class="deleteField">Delete</a></td>--}%
        %{--</tr>--}%
    %{--</g:each>--}%
    %{--</tbody>--}%
    %{--<tr>--}%
        %{--<td></td>--}%
        %{--<td></td>--}%
        %{--<td><a href="#" class="addField">Add</a></td>--}%
    %{--</tr>--}%
%{--</table>--}%
<div class="buttons" style="margin-top: 10px;">
    <g:submitButton name="updateTemplate" value="${g.message(code: 'default.button.update.label')}"/>
    <g:actionSubmit action="provisionerTemplates" value="${message(code: 'default.button.cancel.label', default: 'Cancel')}"/>
</div>
</g:form>

</body>
</html>