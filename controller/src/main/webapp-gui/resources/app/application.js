var callerTable, conferencePoller;

$(document).ready(function() {

    $('#conferenceCallerDialog').dialog({
        autoOpen: false,
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
        $.ajax({
            type: 'POST',
            url: event.target.action,
            data: { username: $('#username').val(),
                    password: $('#password').val() },
            success: function(data) {
                $('#loginDialog').dialog('close');
                $('#conferenceCallerDialog').dialog('open');
                startPollingParticipants();
            },
            error: function(data, status) {
                alert('ERROR: ' + status);
            }
        });

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
            callerTable.fnAddData([data.results[i].number, data.results[i].isAdmin, data.results[i].isHolding, data.results[i].isMuted])
        }
    })
}

function logout() {
    $.ajax({
        type: 'POST',
        url: '/logout',
        success: function(data) {
            stopPollingParticipants();
            $('#conferenceCallerDialog').dialog('close');
            $('#loginDialog').dialog('open');
        },
        error: function(data, status) {
            alert('ERROR: ' + status);
        }
    });
}