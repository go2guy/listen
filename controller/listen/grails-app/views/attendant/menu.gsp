<html>
  <head>
    <title>Attendant - Menu</title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="attendant"/>
    <meta name="button" content="menu"/>
    <style type="text/css">

ul#configurations {
    display: inline-block;
    margin-bottom: 10px;

    *display: inline; /* IE7 */
    zoom: 1; /* IE7 */
}

ul#configurations * {
    font-family: Century Gothic, Arial, sans-serif;
    font-size: 13px;
}

ul#configurations li {
    display: inline-block;
    margin-right: 5px;

    *display: inline; /* IE7 */
    zoom: 1; /* IE7 */
}

ul#configurations li a {
    background-color: inherit;
    border: 1px solid #80b5ed;
    color: #054B7A; /* swatch:text */
    display: inline-block;
    text-decoration: none;
    padding: 3px 10px;

    border-radius: 0;
    -moz-border-radius: 0;
    -webkit-border-radius: 0;

    *display: inline; /* IE7 */
    zoom: 1; /* IE7 */
}

ul#configurations li a:hover,
ul#configurations li a.current {
    background-color: #e4f0fb;
    border-color: #054B7A;
}

.group-configuration {
    background-color: #EBEEF5;
    border: 1px solid #80b5ed;
}

.default-message {
    display: block;
    font-size: 14px;
    font-style: italic;
    margin: 10px;
}

.delete-group {
    float: right;
    margin: 5px 5px 0 0;
}

#attendant-menu-action-template {
    display: none;
}

ul.all-menus {
    display: block;
}

ul.all-menus > li {
    background-color: #inherit;
    border: 1px solid #80b5ed;
    display: block;
    margin: 10px 0;
    padding: 5px;
}

hr {
    border-color: black;
    border-style: solid;
    border-width: 0 0 1px 0;
    margin: 10px 0;
}

label {
    display: block;
    padding: 5px;
}

button {
    margin-top: 1px;
}

button.prominent {
    font-weight: bold;
}

input[type=text],
input[type=file],
select {
    display: block;
    margin-top: 5px;
    width: 180px;
}

#upload-prompt-dialog input {
    display: block;
    width: 300px;
}

select.prompt-select {
    width: 205px;
}

select.action-select {
    width: 255px;
}

input[type=text].keypress {
    width: 90px;
}

input[type=text].number-to-dial,
select.application-select,
select.menu-select {
    width: 200px;
}

input.menu-label {
    display: inline;
    margin-top: 0;
}

select.greeting-prompt,
select.options-prompt {
    display: inline;
}

span.label-text {
    display: inline-block;
    width: 140px;

    *display: inline; /* IE7 */
    zoom: 1; /* IE7 */
}

.hideable {
    margin-top: 20px;
}

table {
    border-collapse: separate;
    border-spacing: 0 2px;
    border-top: 1px solid #e4f0fb;
    margin-top: 10px;
}

button.menu-visibility-toggle,
button.delete-menu {
    display: block;
    float: right;
}

.page-buttons {
    background-color: #EBEEF5;
    margin: 5px auto;
    padding: 5px 0;
    text-align: center;
}

.cell-on-keypress    { width: 95px; }
.cell-play-prompt    { width: 210px; }
.cell-perform-action { width: 260px; }
.cell-action-options { width: 205px; }
.cell-delete-action  {
    text-align: right;
    width: 118px;
}

#attendant-error-messages {
    border: 1px dashed #ED5555;
    background-color: #FAC5C5;
    display: none;
    margin-top: 5px;
    padding: 3px;
}

.templates {
    display: none;
}

.time-restriction-placeholder {
    font-size: 13px;
    font-style: italic;
    font-weight: normal;
    margin: 0 0 5px 0;
}

.time-restriction {
    border-collapse: separate;
    border-spacing: 0px 2px;
    display: block;
    font-size: 12px;
    padding: 2px 5px;
}

.time-restriction .time-cell {
    padding-left: 8px;
    padding-right: 8px;
}

.time-restriction .day-cell {
    text-align: center;
}

.time-restriction select {
    display: inline;
    margin-top: 0;
    width: 60px;
}

.addTimeRestriction {
    margin: 0 0 5px 10px;
}
    </style>
  </head>
  <body>
    <listen:infoSnippet summaryCode="page.attendant.menu.snippet.summary" contentCode="page.attendant.menu.snippet.content"/>

    <h3>Configurations</h3>
    <ul id="configurations">
      <g:each in="${groups}" var="group" status="i">
        <li><a href="#"<g:if test="${i == 0}"> class="current"</g:if>>${fieldValue(bean: group, field: 'name')}</a></li>
      </g:each>
    </ul>
    <button type="button" id="add-group">Add Configuration</button>

    <ul id="attendant-error-messages"></ul>

    <ul id="all-groups">
      <g:each in="${groups}" var="group" status="i">
        <g:render template="groupTemplate" model="[group: group, hidden: i != 0]"/>
      </g:each>
    </ul>

    <div class="page-buttons">
      <button type="button" class="add-menu">Add New Menu</button>
      <button type="button" class="save">Save</button>
    </div>

    <ul class="templates">
      <g:render template="menuTemplate" model="[id: 'menu-template', menu: null]"/>
      <g:render template="groupTemplate" model="[id: 'group-template', group: null]"/>
    </ul>

    <table class="templates">
      <tbody>
        <g:set var="template" value="${[startTime: new org.joda.time.LocalTime(8, 0), endTime: new org.joda.time.LocalTime(17, 0), monday: false, tuesday: false, wednesday: false, thursday: false, friday: false, saturday: false, sunday: false]}"/>
        <g:render template="/shared/timeRestrictionRow" model="${[id: 'timeRestrictionTemplate', prefix: 'restrictions', restriction: template]}"/>
      </tbody>
    </table>
    
    <div id="upload-prompt-dialog" title="Upload New Prompt" style="display: none;">
      <g:form enctype="multipart/form-data" controller="attendant" action="uploadPrompt" target="upload-prompt-iframe" method="post">
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

    <g:form controller="attendant" action="save" method="post" name="menu-save-form">
      <g:hiddenField name="groups"/>
    </g:form>

    <script type="text/javascript">
$(document).ready(function() {

    var attendant = {

        addKeypressAction: function(table) {
            var group = $(table).closest('.menu-group');
            var clone = $('.action-row-template', group).clone(true).removeClass('action-row-template');
            $('.application-select, .number-to-dial, .menu-select', clone).hide();
            $('.action-select', clone).val('Dial What They Pressed');
            $('tr.default:first', table).before(clone);
        },

        addMenu: function(group) {
            var clone = $('#menu-template').clone(true).removeAttr('id');
            $('.application-select, .number-to-dial, .menu-select', clone).hide();
            var table = clone.find('table');
            attendant.addKeypressAction(table);

            $('.all-menus', group).append(clone);
            $('.menu-label', clone).focus();
            $('.entry-menu-label', group).show();
        },

        deleteMenu: function(menu) {
            var label = $('.menu-label', menu).val();
            var group = menu.closest('.menu-group');
            menu.remove();

            if($('.menu', group).length == 0) {
                $('.entry-menu-label', group).hide();
            }

            attendant.rebuildMenuSelects(group);
        },
        
        rebuildMenuSelects: function(group) {
            var available = [];
            $('.all-menus .menu-label', group).each(function() {
                var input = $(this);
                if($.trim(input.val()).length !== 0) {
                    available.push(input.val());
                }
            });
            $('.menu-select, .entry-menu-select', group).each(function() {
                var sel = $(this);
                var current = sel.val();
                
                var hasNoSelectOption = $('option[value="-- Select A Menu --"]', sel).length > 0;
                var hasCreateNewOption = $('option[value="Create New Menu..."]', sel).length > 0;

                $('option', sel).remove();
                if(hasNoSelectOption) {
                    sel.append('<option>-- Select A Menu --</option>');
                }
                for(var i = 0, length = available.length; i < length; i++) {
                    sel.append('<option>' + available[i] + '</option>');
                }
                if(hasCreateNewOption) {
                    sel.append('<option>Create New Menu...</option>');
                }
                if(attendant.arrayContains(current, available)) {
                    sel.val(current);
                } else {
                    util.selectFirst(sel);
                }
            });
        },

        arrayContains: function(needle, haystack) {
            for(var i = 0, length = haystack.length; i < length; i++) {
                if(haystack[i] === needle) return true;
            }
            return false;
        },
        
        // TODO should validation be per-configuration, or overall?
        validate: function() {
            if($('.all-menus > li').size() == 0) {
                attendant.notifyErrors('There must be at least one menu.');
                return false;
            }

            var valid = true;
            function checkEmpty(className) {
                $('.' + className).each(function() {
                    var el = $(this);
                    if(el.is(':visible') && $.trim(el.val()).length === 0) {
                        valid = false;
                        el.addClass('field-error');
                    }
                });
            }
    
            checkEmpty('menu-label');
            checkEmpty('keypress');
            checkEmpty('number-to-dial');

            $('.all-menus .menu-select').each(function() {
                var el = $(this);
                if(el.is(':visible') && el.val() === '-- Select A Menu --') {
                    valid = false;
                    el.addClass('field-error');
                }
            });
            
            $('.all-menus .options-prompt').each(function() {
                var el = $(this);
                if(el.val() === '-- No Prompt --') {
                    valid = false;
                    el.addClass('field-error');
                }
            });

            if(!valid) {
                attendant.notifyErrors('Some of the fields below are missing values.');
            }
            return valid;
        },
        
        buildJson: function() {
            var groups = [];
            $('#all-groups > li').each(function() {
                var groupLi = $(this);
                var group = {
                    name: $('.group-name', groupLi).val(),
                    isDefault: $('.is-default', groupLi).val() == 'true',
                    restrictions: []
                };

                $('table.time-restriction tbody tr.time-restriction-row', groupLi).each(function() {
                    var restriction = {
                        startTime: {
                            h: $('td:eq(0) select:eq(0)', this).val(),
                            m: $('td:eq(0) select:eq(1)', this).val()
                        },
                        endTime: {
                            h: $('td:eq(1) select:eq(0)', this).val(),
                            m: $('td:eq(1) select:eq(1)', this).val()
                        },
                        monday: $('td:eq(2) input[type=checkbox]', this).is(':checked'),
                        tuesday: $('td:eq(3) input[type=checkbox]', this).is(':checked'),
                        wednesday: $('td:eq(4) input[type=checkbox]', this).is(':checked'),
                        thursday: $('td:eq(5) input[type=checkbox]', this).is(':checked'),
                        friday: $('td:eq(6) input[type=checkbox]', this).is(':checked'),
                        saturday: $('td:eq(7) input[type=checkbox]', this).is(':checked'),
                        sunday: $('td:eq(8) input[type=checkbox]', this).is(':checked')
                    };
                    group.restrictions.push(restriction);
                });

                var menus = [];
                $('.all-menus > li', groupLi).each(function() {
                    var li = $(this);
                
                    var optionsPrompt = $('.options-prompt', li).val();
                    var label = $('.menu-label', li).val();
                    var menu = {
                        label: label,
                        entryMenu: $('.entry-menu-select', groupLi).val() == label,
                        optionsPrompt: (optionsPrompt === '-- No Prompt --' ? '' : optionsPrompt),
                        actions: []
                    };

                    $('table tbody tr', li).each(function() {
                        var row = $(this);
                        var directive = $('.action-select', row).val();
                        var prompt = $('.prompt-select', row).val();
                        var action = {
                            keypress: $('.keypress', row).val(),
                            promptBefore: (prompt === '-- No Prompt --' ? '' : prompt), 
                            directive: directive
                        };

                        switch(directive) {
                            case 'Dial A Number...':
                                action.argument = $('.number-to-dial', row).val();
                                break;

                            case 'Go To A Menu...':
                                action.argument = $('.menu-select', row).val();
                                break;

                            case 'Launch An Application...':
                                action.argument = $('.application-select', row).val();
                                break;
                        }
                    
                        if($.trim($('td:first-child', row).text()) === 'Other Input') {
                            menu.defaultAction = action;
                        } else if($.trim($('td:first-child', row).text()) === 'Timeout (5s)') {
                            menu.timeoutAction = action;
                        } else {
                            menu.actions.push(action);
                        }
                    });
                    menus.push(menu);
                });
                group.menus = menus;
                groups.push(group);
            });
            return groups;
        },
        
        clearAllErrors: function() {
            $('.field-error').removeClass('field-error');
            $('.messages').remove();
        },

        notifyErrors: function(messages) {
            var m = (messages instanceof Array ? messages : [messages]);
            if(m.length > 0) {
                var list = '<ul class="messages error">'
                for(var i = 0, message; message = m[i++];) {
                    list += '<li>' + message + '</li>';
                }
                list += '</ul>';
                $('#content-area').prepend(list);
            }
        },

        addGroup: function() {
            var clone = $('#group-template').clone(true).removeAttr('id');
            $('.group-name', clone).val('New Configuration');

            var tab = $('<li><a href="#">New Configuration</a></li>');
            $('a', tab).click(function(e) {
                attendant.showGroup(e.target);
            });

            attendant.addMenu(clone);

            $('ul#configurations').append(tab);
            $('#all-groups').append(clone);

            attendant.showGroup($('a', tab));
            $('.group-name', clone).focus();
        },

        showGroup: function(link) {
            var index = $('ul#configurations > li a').index(link);

            $('ul#configurations > li a').each(function(i) {
                $(this).toggleClass('current', index == i);
            });
            $('ul#all-groups > li').each(function(i) {
                if(index == i) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
        },

        deleteGroup: function(group) {
            var index = $('#all-groups').index(group);
            var tab = $('ul#configurations li').get(index);

            group.remove();
            $(tab).remove();

            attendant.showGroup($('ul#configurations li:first a'));
        }
    };

    $('.menu-visibility-toggle').click(function(e) {
        $('.hideable', $(e.target).parent()).slideToggle();
        return false;
    });
    
    $('.delete-action').click(function(e) {
        $(e.target).closest('tr').remove();
        return false;
    });
    
    $('.add-action').click(function(e) {
        var table = $(e.target).closest('table');
        attendant.addKeypressAction(table);
        return false;
    });
    
    $('.add-menu').click(function(e) {
        var group = $('#all-groups > li:visible');
        attendant.addMenu(group);
        return false;
    });
    
    $('.delete-menu').click(function(e) {
        if(confirm('Are you sure?')) {
            var menu = $(e.target).closest('.menu');
            attendant.deleteMenu(menu);
        }
        return false;
    });

    $('.save').click(function() {
        attendant.clearAllErrors();

        if(!attendant.validate()) {
            return false;
        }

        var groups = attendant.buildJson();
        var form = $('#menu-save-form');
        $('input', form).val(JSON.stringify(groups));
        form.submit();
        return false;
    });
    
    $('input[type=text],select').change(function() {
        $('.save').addClass('prominent');
    });

    $('.menu-label').change(function(e) {
        attendant.rebuildMenuSelects($(e.target).closest('.menu-group'));
    });

    function renameGroup(e) {
        var group = $(e.target).closest('.menu-group');
        var name = $('.group-name', group).val();
        if($.trim(name) == '') {
            name = 'Unnamed';
        }
        var index = $('#all-groups > li').index(group);
        $($('ul#configurations a').get(index)).text(name);
    }
    $('.group-name').change(renameGroup).keyup(renameGroup);

    $('#add-group').click(function() {
        attendant.addGroup();
        return false;
    });

    $('ul#configurations > li').click(function(e) {
        attendant.showGroup(e.target);
        return false;
    });

    $('.delete-group').click(function(e) {
        if(confirm('Are you sure?')) {
            var group = $(e.target).closest('.menu-group');
            attendant.deleteGroup(group);
        }
        return false;
    });

    $('.menu-select').change(function(e) {
        var sel = $(e.target);
        if(sel.val() == 'Create New Menu...') {
            attendant.addMenu(sel.closest('.menu-group'));
            util.selectFirst(sel);
            //sel.val($('option:first', sel).val());
        }
    });

    $('.action-select').change(function(e) {
        var sel = $(e.target);
        var optionsCell = sel.parent('td').next('td');
        $('.application-select, .number-to-dial, .menu-select', optionsCell).hide();
        switch(sel.val()) {
            case 'Dial A Number...':
                var field = $('.number-to-dial', optionsCell);
                field.show();
                break;
            case 'Go To A Menu...':
                $('.menu-select', optionsCell).show();
                break;
            case 'Launch An Application...':
                $('.application-select', optionsCell).show();
                break;
            default:
                // show nothing
        }
    });
    
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
        if(sel.val() === 'Upload New Prompt...') {

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
            //$('input', dialog).focus();
        }
    });
    
    $('.addTimeRestriction').click(function(e) {
        var group = $(e.target).closest('.menu-group');
        var table = $('table.time-restriction', group);
        var clone = $('#timeRestrictionTemplate').clone(true).removeAttr('id');
        $('tbody', table).append(clone);
        table.show();
        $('.default-message', group).text('This configuration applies to the following times/days:');
    });

    $('.time-restriction-row button').click(function() {
        var button = $(this);
        var table = button.closest('table.time-restriction');
        if($('tbody tr', table).length == 1) {
            $('.default-message', table.closest('.menu-group')).text('This configuration can be restricted to certain times/days.');
            table.hide();
        }
        $(this).closest('tr').remove();
    });
});
    </script>
  </body>
</html>