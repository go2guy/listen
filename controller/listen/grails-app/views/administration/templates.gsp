<%--
  Created by IntelliJ IDEA.
  User: cgeesey
  Date: 12/6/2016
  Time: 8:07 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title><g:message code="page.administration.templates.title" default="Provisioner Templates"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="provisionerTemplates"/>
    <tooltip:resources/>
    <style type="text/css">
        table { margin-bottom: 10px; }

        .add tbody td.button { text-align: center; }

        .col-name input[type=text], .col-name, .col-ip input[type=text] { width: 250px; }
        .col-button { width: 50px; }
        .add .col-button {
            text-align: center;
            width: 110px;
        }
        td.col-reg {
            padding-left: 30px;
        }

        tbody .col-light { text-align: center; }
    .highlighted {
        width: 40%;
    }
    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            var dialog = $("#deleteDialog").dialog({
                autoOpen: false,
                draggable: false,
                modal: true,
                position: 'center',
                resizable: false,
                width: 400,
                buttons: {
                    "Delete": function() {
                        console.log("Delete Selected");
                        window.location = "${createLink(action: 'deleteTemplate', controller: 'administration')}/"+$(this).data("templateId");
                    },
                    "Cancel": function() {
                        dialog.dialog("close");
                    }
                }
            });

            $(".deleteButton").click(function(e) {
                e.preventDefault();
                dialog
                        .data("templateId", $(this).attr("data-id"))
                        .dialog("open");
            })
        });
    </script>
</head>

<body>

<h3><g:message code="page.administration.templates.add.caption" default="Add Template"/></h3>
<table class="add highlighted">
    <thead>
    <th class="col-name"><g:message code="page.administration.templates.column.name" default="Name"/></th>
    <th class="col-button"></th>
    </thead>
    <tbody>
    <tr>
        <g:form controller="administration" action="addTemplate" method="post" autocomplete="off">
            <td class="col-name"><g:textField name="name" value="${fieldValue(bean: newExtension, field: 'number')}" placeholder="${g.message(code: 'page.administration.templates.name.placeholder', default: 'Name...')}" autofocus="focus"/></td>

            <td class="col-button"><g:submitButton name="add" value="${g.message(code: 'page.administration.templates.add.button.addTemplate', default: "Add Template")}"/></td>
        </g:form>
    </tr>
    </tbody>
</table>

<h3><g:message code="page.administration.templates.list.caption" default="Templates"/></h3>
<table>
    <thead>
    <th class="col-name"><g:message code="page.administration.templates.column.name" default="Name"/></th>
    </thead>
    <tbody>
    <g:each in="${templates}" var="template">
        <tr>
            <td class="col-name"><g:link action="editTemplate" controller="administration" id="${template.id}">${template.name}</g:link></td>
            <td class="col-name" style="float: right;"><button class="deleteButton" data-id="${template.id}"><g:message code="page.administration.templates.delete.button" default="Delete"/></button></td>
        </tr>
    </g:each>
    </tbody>
</table>

<div id="deleteDialog" title="Are You Sure?" style="display: none;">
    <g:message code="page.administration.templates.deletedialog.confirm" default="Are you sure you want to delete this template?"/>
</div>

</body>
</html>