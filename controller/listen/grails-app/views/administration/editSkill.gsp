<html>
  <head>
    <title><g:message code="page.administration.acd.skills.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="skills"/>
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
            width: 205px;
        }
        
        select.options-prompt {
            display: inline;
        }
    </style>
  </head>
  <body>
    <g:set var="prompts" scope="page"/>
    
    <g:form controller="administration" action="updateSkill" method="post">
        <input type="hidden" name="id" type="hidden" value="${skill.id}"/>
        <table>
          <caption><g:message code="page.administration.acd.editSkills.title"/>
          <tbody>
            <tr>
                <td><g:message code="page.administration.acd.skills.column.skillname"/></td>
            </tr>
            <tr class="add highlighted">
                <td><g:textField name="skillname" value="${fieldValue(bean: skill, field: 'skillname')}"/></td>
            </tr>

            <tr>
                <td><g:message code="page.administration.acd.skills.column.description"/></td>
            </tr>
            <tr class="add highlighted">
                <td><g:textField name="description" value="${fieldValue(bean: skill, field: 'description')}"/></td>
            </tr>

            <tr>
                <td><g:message code="page.administration.acd.skills.column.users"/></td>
            </tr>
            <tr class="add highlighted">
                <td><g:select id="userIds" name="userIds" multiple="multiple" optionKey="id" optionValue="realName" from="${orgUsers}" value="${skillUsers?.user}"/></td>
            </tr>

            <tr>
                <td><g:message code="page.administration.acd.skills.column.voiceMailUser"/></td>
            </tr>
            <tr class="add highlighted">
                <td><g:select id="vmUserId" name="vmUserId" optionKey="id" optionValue="realName" from="${skillUsers?.user}" value="${vmUser?.id}" noSelection="${['':'-- Choose Account --']}" /></td>
            </tr>
            
            <tr>
                <td><g:message code="page.administration.acd.skills.promptName.onHoldMsg"/></td>
            </tr>
            <tr class="add highlighted">
                <td><listen:acdPromptSelect class="prompt-select" name="onHoldMsg" value="${skill.onHoldMsg}"/></td>
            </tr>
            
            <tr>
                <td><g:message code="page.administration.acd.skills.promptName.onHoldMsgExtended"/></td>
            </tr>
            <tr class="add highlighted">
                <td><listen:acdPromptSelect class="prompt-select" name="onHoldMsgExtended" value="${skill.onHoldMsgExtended}"/></td>
            </tr>
        
            <tr>
                <td><g:message code="page.administration.acd.skills.promptName.onHoldMusic"/></td>
            </tr>
            <tr class="add highlighted">
                <td><listen:acdPromptSelect class="prompt-select" name="onHoldMusic" value="${skill.onHoldMusic}"/></td>
            </tr>
        
            <tr>
                <td><g:message code="page.administration.acd.skills.promptName.connectMsg"/></td>
            </tr>
            <tr class="add highlighted">
                <td><listen:acdPromptSelect class="prompt-select" name="connectMsg" value="${skill.connectMsg}"/></td>
            </tr>
            
          </tbody>
        </table>

        <div class="buttons">
            <g:submitButton name="update" value="${g.message(code: 'default.button.update.label')}"/>
            <g:actionSubmit action="skills" value="${message(code: 'default.button.cancel.label', default: 'Cancel')}"/>
        </div>
        
    </g:form>

    <div id="upload-prompt-dialog" title="Upload New Prompt" style="display: none;">
        <g:form enctype="multipart/form-data" controller="acd" action="uploadPrompt" target="upload-prompt-iframe" method="post">
          <fieldset>
            <label>
              Prompt File (.wav)
              <input type="file" name="uploadFile"/>
            </label>
            <span class="status"></span>
          </fieldset>
        </g:form>
        <iframe name="upload-prompt-iframe" id="upload-prompt-iframe" style="display: none;"></iframe>
    </div>
  
    <script type="text/javascript">

    $(document).ready(function() {
            
            $('#upload-prompt-dialog').dialog({
                autoOpen: false,
                draggable: false,
                modal: true,
                position: 'center',
                resizable: false,
                width: 400
            });
        
            $('.prompt-select').change(function(e) {
                var sel = $(e.target);
                if(sel.val() === '-- Upload New Prompt --') {
        
                    var dialog = $('#upload-prompt-dialog');
        
                    var upload = function() {
                        $('form', dialog).submit();
                    }
        
                    var form = $('form', dialog);
                    var input = $('input[type=file]', dialog);
        
                    var close = function(selectFirst) {
                        form.unbind(); // unbind, we'll re-bind when the option is selected again
                        input.unbind();
                        if(selectFirst === true) util.selectFirst(sel);
                        dialog.dialog('close');
        
                        // clear fields in the dialog
                        $('input', dialog).val('');
                        $('.status', dialog).text('');
                    }
        
                    var parseFilename = function(fullpath) {
                        // TODO this may need tweaking for non-windows platforms
                        return fullpath.replace(/^.*\\/, '');
                    }
        
                    var promptAlreadyExists = function(name) {
                        var found = false;
                        $('#menu-template .options-prompt option').each(function() {
                            if($(this).val() === name) {
                                found = true;
                            }
                        });
                        return found;
                    }
        
                    input.bind('change', function(e) {
                        var filename = parseFilename($(e.target).val());
                        if(promptAlreadyExists(filename)) {
                            $('.status', dialog).html('<pre style="word-wrap: break-word; white-space: pre-wrap;">Warning: there is already a prompt named "' + filename + '", it will be overwritten with this one.</pre>');
                        }
                    });
        
                    var buttons = { 'Upload': upload, 'Cancel': function() { close(true); } };
                    form.bind('submit', function() {
        
                        if($.trim($('input', dialog).val()) === '') {
                            $('.status', dialog).html('<pre style="word-wrap: break-word; white-space: pre-wrap;">Please choose a file</pre>');
                            return;
                        }
        
                        var filename = parseFilename($('input', dialog).val());
                        if(filename.indexOf('Upload New Prompt') >= 0) {
                            $('.status', dialog).html('<pre style="word-wrap: break-word; white-space: pre-wrap;">File name may not contain the text "Upload New Prompt"</pre>');
                            return;
                        }
        
                        if(filename.indexOf('No Prompt') >= 0) {
                            $('.status', dialog).html('<pre style="word-wrap: break-word; white-space: pre-wrap;">File name may not contain the text "No Prompt"</pre>');
                            return;
                        }
        
                        $('.status', dialog).text('Uploading...');
                        dialog.dialog('option', 'buttons', {}); // remove buttons while uploading
        
                        var iframe = $('#upload-prompt-iframe');
                        iframe.one('load', function() {
                            var contents = iframe.contents().find('body').html();
        
                            if(contents.indexOf('Success') >= 0) {
                                if(!promptAlreadyExists(filename)) {
                                    // add to all prompt select lists
                                    $('.prompt-select option:first-child').after('<option>' + filename + '</option>');
                                }
        
                                // select it in the active list
                                sel.val(filename);
                                close(false);
                            } else {
                                $('.status', dialog).html(contents);
                            }
        
                            dialog.dialog('option', 'buttons', buttons); // put the buttons back
                        });
                    });
        
                    dialog.dialog('option', 'buttons', buttons);
                    dialog.dialog('open');
                }
            });
        });
    
    </script>
  </body>
</html>