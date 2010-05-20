$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('administration', 'administration-application', 'menu-administration', 4));

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

    $('#add-dnis-mapping').click(function() {
        //var clone = $('#dnis-mapping tbody tr:first').next().clone(true);
        var node = $('<tr><td>Number</td><td><input type="text"/></td><td>maps to</td><td><select><option>Voicemail</option><option>Conferencing</option></select></td><td><button class="delete-button"></button></td></tr>');
        $('.delete-button', node).click(function() {
            $(this).parent().parent().remove();
        });
        $('#dnis-mapping tbody tr:last').before(node);
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
