var callerTable, conferencePoller;

$(document).ready(function() {

    $.ajaxSetup({
        error: function(req, s, e) {
            if(req.status == 401) { // unauthorized
                showLogin(); 
            }
            // TODO put a message at the bottom of the screen in a semi-opaque red box
        }
    });

    $('#conferenceCallerDialog').dialog({
        autoOpen: false,
        dialogClass: 'no-close',
        closeOnEscape: false,
        draggable: true,
        resizable: true,
        height: 300,
        width: 400,
        position: [50, 50]
    })/*
    .parents('.ui-dialog').draggable({
        handle: '.ui-dialog-titlebar',
        containment: [0, 31, $(document).width(), $(document).height()]
    })*/;

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

    $('#logoutButton').click(function(event) {
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
            callerTable.fnAddData([(result.isAdmin ? '<b>' : '') + result.number + (result.isAdmin ? '</b>' : ''),
                                   (result.isHolding ? 'Holding' : ''),
                                   getMuteButtonHtml(result.isMuted),
                                   '<a href="#" onclick="return false;" alt="Kick" title="Kick"><img src="resources/app/images/user-denied.png"/></a>'])
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
            $('#logoutButton').show();
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
        success: showLogin,
        error: function(data, status) {
            alert('ERROR: ' + status);
        }
    });
}

function showLogin() {
    stopPollingParticipants();
    $('#conferenceCallerDialog').dialog('close');
    $('#logoutButton').hide();
    $('#loginDialog').dialog('open');
}

function getMuteButtonHtml(isMuted) {
    var html = '<a href="#" onclick="return false;">';
    html += '<img src="resources/app/images/' + (isMuted ? 'speaker-muted.png' : 'speaker.png') + '"/>';
    html += '</a>';
    return html;
}