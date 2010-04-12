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

    showLogin();
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
                                   getMuteButtonHtml(result.id, result.isMuted || result.isAdminMuted),
                                   '<a href="#" onclick="dropParticipant(' + result.id + ');return false;" alt="Drop" title="Drop"><img src="resources/app/images/user-denied.png"/></a>'])
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
    $('#username').focus();
}

function getMuteButtonHtml(id, isMuted) {
    var html = '<a href="#" ';
    if(isMuted) {
        html += 'onclick="unmuteParticipant(' + id + ');return false;"';
    } else {
        html += 'onclick="muteParticipant(' + id + ');return false;"';
    }
    html += '>';
    html += '<img src="resources/app/images/' + (isMuted ? 'speaker-muted.png' : 'speaker.png') + '" id="muteButton' + id + '" width="16" height="16"/>';
    html += '</a>';
    return html;
}

function muteParticipant(id) {
    $.ajax({
        type: 'POST',
        url: '/muteParticipant',
        data: { id: id },
        success: function(data) {
            // TODO anything? the table will refresh itself
        },
        error: function(req) {
            // TODO error somewhere on the screen
            alert('ERROR muting participant: ' + req.status);
        }
    });
}

function unmuteParticipant(id) {
    $.ajax({
        type: 'POST',
        url: '/unmuteParticipant',
        data: { id: id },
        success: function(data) {
            // TODO anything? the table will refresh itself
        },
        error: function(req) {
            // TODO error somewhere on the screen
            alert('ERROR unmuting participant: ' + req.status);
        }
    });
}

function dropParticipant(id) {
    $.ajax({
        type: 'POST',
        url: '/dropParticipant',
        data: { id: id },
        success: function(data) {
            // TODO feedback that the drop was successful
        },
        error: function(req) {
            // TODO error somewhere on the screen
            alert('ERROR dropping participant: ' + req.status);
        }
    });
}