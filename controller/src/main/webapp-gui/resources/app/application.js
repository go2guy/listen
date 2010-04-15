YAHOO.namespace('listen');
var callerTable, conferencePoller;

function Conference() {

    // init jquery dialog
    $('#conferenceDialog').dialog({
        autoOpen: false,
        closeOnEscape: false,
        dialogClass: 'no-close',
        draggable: true,
        height: 300,
        position: [50, 50],
        resizable: true,
        title: 'My Conference',
        width: 400
    })/*
    .parents('.ui-dialog').draggable({
        handle: '.ui-dialog-titlebar',
        containment: [0, 31, $(document).width(), $(document).height()]
    })*/;

    $.getJSON('/getConferenceInfo', function(data) {
        $('#conferenceDialog').dialog('option', 'title', 'Conference ' + data.number);
    });

    YAHOO.widget.DataTable.Formatter.muteFormatter = function(liner, record, column, data) {
        liner.innerHTML = getMuteButtonHtml(record.getData('id'), record.getData('isAdminMuted') == "true");
    };

    var columns = [
        {key: 'number', label: '', className: 'conferenceParticipantTableNumber'},
        {key: 'isAdminMuted', label: '', formatter: 'muteFormatter', className: 'conferenceParticipantTableDrop'},
        {key: 'drop', label: '', className: 'conferenceParticipantTableDrop'}
    ];

    var dataSource = new YAHOO.util.DataSource('/getConferenceParticipants');
    dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
    dataSource.connXhrMode = 'queueRequests';
    dataSource.responseSchema = {
        resultsList: 'results',
        fields: [
            {key: 'id', parser: 'number'},
            'number',
            'isAdmin',
            'isMuted',
            'isAdminMuted',
            'isHolding'
        ]
    };

    var dataTable = new YAHOO.widget.DataTable('conferenceParticipantTable', columns, dataSource);
    dataTable.subscribe('cellClickEvent', function(args) {
        var target = args.target;
        var column = dataTable.getColumn(target);

        switch(column.key) {

            case 'drop':
                var record = dataTable.getRecord(target)
                $.ajax({
                    type: 'POST',
                    url: '/dropParticipant',
                    data: { id: record.getData('id') },
                    success: function(data) {
                        noticeSuccess('Participant dropped');
                        dataTable.deleteRow(target);
                    },
                    error: function(req) {
                        // TODO something here? 
                        //noticeError(req.responseText);
                    }
                });
                break;
        }
    });/* TODO get this to work
    dataTable.subscribe('cellUpdateEvent', function(record, column, oldData) {
        var td = dataTable.getTdEl({record: record, column: column});
        YAHOO.util.Dom.setStyle(td, 'backgroundColor', '#ffff00');
        var animation = new YAHOO.util.ColorAnim(td, {
            backgroundColor: {
                to: '#ffffff'
            }
        });
        animation.animate();
    });*/

    this.show = function() {
        $('#conferenceDialog').dialog('open');
    };

    this.hide = function() {
        $('#conferenceDialog').dialog('close');
    };

    this.startPolling = function() {
        var callback = {
            success: dataTable.onDataReturnInitializeTable,
            failure: function() {
                noticeError('Error initializing Conference DataTable');
            },
            scope: dataTable
        };

        dataSource.setInterval(1000, null, callback);
    };

    this.stopPolling = function() {
        dataSource.clearAllIntervals();
    };

}

$(document).ready(function() {

    $.ajaxSetup({
        error: function(req, s, e) {
            if(req.status == 401) { // unauthorized
                showLogin();

                var errorDiv = $('#loginForm .errors');
                errorDiv.html('You have been automatically logged out');
                errorDiv.show();
            }
        }
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

            YAHOO.listen.mainConference = new Conference();
            YAHOO.listen.mainConference.show();
            YAHOO.listen.mainConference.startPolling();
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
        error: function(req) {
            noticeError(req.responseText);
        }
    });
}

function showLogin() {
    if(YAHOO.listen.mainConference) {
        YAHOO.listen.mainConference.stopPolling();
        YAHOO.listen.mainConference.hide();
    }
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
            noticeSuccess('Participant muted');
        },
        error: function(req) {
            noticeError(req.responseText);
        }
    });
}

function unmuteParticipant(id) {
    $.ajax({
        type: 'POST',
        url: '/unmuteParticipant',
        data: { id: id },
        success: function(data) {
            noticeSuccess('Participant unmuted');
        },
        error: function(req) {
            noticeError(req.responseText);
        }
    });
}

function noticeSuccess(message, stay) {
    $.noticeAdd({
        text: message,
        type: 'notice-success',
        stayTime: 2000,
        stay: (stay ? true : false)
    });
}

function noticeError(message, stay) {
    $.noticeAdd({
        text: message,
        type: 'notice-error',
        stayTime: 2000,
        stay: (stay ? true : false)
    });
}