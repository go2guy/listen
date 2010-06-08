$(document).ready(function() {
    var administration = new Administration();

    LISTEN.registerApp(new LISTEN.Application('sysconfig', 'sysconfig-application', 'menu-sysconfig', 4, administration));

    function Administration() {
        this.load = function() {
            $.ajax({
                url: '/ajax/getProperties',
                dataType: 'json',
                cache: false,
                success: function(data, textStatus, xhr) {
                    $('#smtp-server').val(data['com.interact.listen.mail.smtpHost']);
                    $('#smtp-username').val(data['com.interact.listen.mail.smtpUsername']);
                    $('#smtp-password').val(data['com.interact.listen.mail.smtpPassword']);
                    $('#from-address').val(data['com.interact.listen.mail.fromAddress']);

                    clearAllDnisRows();
                    var dnis = data['com.interact.listen.dnisMapping'];
                    var mappings = dnis.split(';');
                    for(var i = 0; i < mappings.length; i++) {
                        var mapping = mappings[i].split(':');
                        addDnisRow(mapping[0], mapping[1]);
                    }
                }
            });
        };

        this.unload = function() {
            // no-op
        };
    };

    $('#provisionAccountDialog').dialog({
        autoOpen: false,
        draggable: false,
        height: 600,
        modal: true,
        position: 'center',
        resizable: false,
        title: 'Create New Account',
        width: 600
    });

    $('#provisionAccountForm').submit(function(event) {
        provisionAccount(event);
        return false;
    });

    $('#create-new-account-button').click(function(event) {
        $('#provisionAccountDialog').dialog('open');
    });

    function clearAllDnisRows() {
        $('#dnis-mapping-form tbody tr').not(':last').remove();
    };

    function addDnisRow(number, destination) {
        var n = (number ? number : '');
        var d = (destination ? destination : '');
        var html = '<tr><td>Number</td><td><input type="text" value="' + n + '"/></td>';
        html += '<td>maps to</td><td><select>';
        html += '<option value="conferencing"' + (d == 'conferencing' ? ' selected="selected"' : '') + '>Conferencing</option>';
        html += '<option value="mailbox"' + (d == 'mailbox' ? ' selected="selected"' : '') + '>Mailbox</option>';
        html += '<option value="voicemail"' + (d == 'voicemail' ? ' selected="selected"' : '') + '>Voicemail</option>';
        html += '</select></td>';
        html += '<td><button class="delete-button" title="Remove this DNIS mapping"></button></td></tr>';
        var node = $(html);
        $('.delete-button', node).click(function() {
            $(this).parent().parent().remove();
        });
        $('#dnis-mapping-form tbody tr:last').before(node);
    }

    $('#add-dnis-mapping').click(function() {
        addDnisRow();
        return false;
    });

    $('#mail-form').submit(function() {
        $.ajax({
            type: 'POST',
            url: '/ajax/setProperties',
            data: { 'com.interact.listen.mail.smtpHost': $('#smtp-server').val(),
                    'com.interact.listen.mail.smtpUsername': $('#smtp-username').val(),
                    'com.interact.listen.mail.smtpPassword': $('#smtp-password').val(),
                    'com.interact.listen.mail.fromAddress': $('#from-address').val() },
            success: function(data) {
                administration.load();
                notify('Mail settings updated');
            }
        });
        return false;
    });

    $('#dnis-mapping-form').submit(function() {
        var value = '';
        var rows = $('#dnis-mapping-form tr');
        var num = 0;
        for(var i = 0; i < rows.length - 1; i++) {
            var number = $('input:text', rows[i]).val();
            if(number.length == 0) {
                continue;
            }
            var destination = $('select', rows[i]).val();
            value += number + ':' + destination + ';';
            num++;
        }
        if(num > 0 && value.length > 0) {
            value = value.substring(0, value.length - 1); // remove last semicolon
        }

        $.ajax({
            type: 'POST',
            url: '/ajax/setProperties',
            data: { 'com.interact.listen.dnisMapping': value },
            success: function(data) {
                administration.load();
                notify('DNIS mappings updated');
            }
        });

        return false;
    });
});

function provisionAccount(event) {
    var div = $('#scheduleConferenceDialog .form-error-message');
    div.hide();
    div.text('');

    var provisionAccountNumber = $('#provisionAccountNumber');
    var provisionAccountPassword = $('#provisionAccountPassword');
    var provisionAccountPasswordConfirm = $('#provisionAccountPasswordConfirm');
    var provisionAccountUsername = $('#provisionAccountUsername'); 

    $.ajax({
        type: 'POST',
        url: event.target.action,
        data: { confirmPassword: provisionAccountPasswordConfirm.val(),
                number: provisionAccountNumber.val(),
                password: provisionAccountPassword.val(),
                username: provisionAccountUsername.val() },
        success: function(data) {
            $('#provisionAccountDialog').dialog('close');
            provisionAccountNumber.val('');
            provisionAccountPassword.val('');
            provisionAccountPasswordConfirm.val('');
            provisionAccountUsername.val('');
            notify('Account provisioned');
        },
        error: function(data, status) {
            var div = $('#provisionAccountDialog .form-error-message');
            div.text(data.responseText);
            div.slideDown(200);
        }
    });
}
