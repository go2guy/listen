$(document).ready(function() {
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