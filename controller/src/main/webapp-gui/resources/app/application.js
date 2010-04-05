var callerTable, conferencePoller;

$(document).ready(function() {

    $('#conferenceCallerDialog').dialog({
        autoOpen: false,
        dialogClass: 'no-close',
        closeOnEscape: false,
        draggable: true,
        resizable: true,
        height: 300,
        width: 400,
        position: [50, 50]
    });

    callerTable = $('#callerTable').dataTable({
        bPaginate: false,
        bFilter: false,
        bInfo: false
    });

    $('#loginDialog').dialog({
        modal: true,
        dialogClass: 'no-title',
        autoOpen: false,
        draggable: false,
        resizable: false,
        bigframe: true,
        height: 250,
        width: 400,
        position: 'center'
    });

    $('#loginForm').submit(function(event) {
        login(event);
        return false;
    });

    $('#logout').click(function(event) {
        logout();
    });

    $('#loginDialog').dialog('open');
});

function startPollingParticipants() {
    getConferenceParticipants();
    conferencePoller = setInterval(function() {
        getConferenceParticipants();
    }, 1000);
}

function stopPollingParticipants() {
    clearTimeout(conferencePoller);
}

function getConferenceParticipants() {
    $.getJSON('/getConferenceParticipants', function(data) {
        callerTable.fnClearTable();
        for(var i = 0; i < data.count; i++) {
            var result = data.results[i];
            callerTable.fnAddData([result.number, result.isAdmin, result.isHolding, result.isMuted])
        }
    })
}

function login(event) {
    var usernameField = $('#username');
    var passwordField = $('#password');
    var errorDiv = $('#loginForm .errors');
    errorDiv.hide();

    $.ajax({
        type: 'POST',
        url: event.target.action,
        data: { username: usernameField.val(),
                password: passwordField.val() },
        success: function(data) {
            $('#loginDialog').dialog('close');
            usernameField.val('');
            passwordField.val('');
            $('#logout').show();
            $('#conferenceCallerDialog').dialog('open');
            startPollingParticipants();
        },
        error: function(data, status) {
            passwordField.val('');
            errorDiv.html(data.responseText);
            errorDiv.slideDown(200);
        }
    });
}

function logout() {
    $.ajax({
        type: 'POST',
        url: '/logout',
        success: function(data) {
            stopPollingParticipants();
            $('#conferenceCallerDialog').dialog('close');
            $('#logout').hide();
            $('#loginDialog').dialog('open');
        },
        error: function(data, status) {
            alert('ERROR: ' + status);
        }
    });
}
