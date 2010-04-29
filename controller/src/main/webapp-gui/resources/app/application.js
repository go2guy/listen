YAHOO.namespace('listen');

YAHOO.listen.isAdmin = false;

YAHOO.widget.DataTable.MSG_LOADING = 'Loading...';
YAHOO.widget.DataTable.MSG_EMPTY = 'Empty';

function Conference(conferenceId) {

    $('#conferenceDialog').dialog({
        autoOpen: false,
        closeOnEscape: false,
        dialogClass: 'no-close',
        draggable: false,
        height: 700,
        position: [500, 50],
        resizable: false,
        width: 500
    })/*
    .parents('.ui-dialog').draggable({
        handle: '.ui-dialog-titlebar',
        containment: [0, 31, $(document).width(), $(document).height()]
    })*/;

    $.getJSON('/getConferenceInfo?id=' + conferenceId, function(data) {
        $('#conferenceDialog').dialog('option', 'title', 'Conference: ' + data.description);
    });

    YAHOO.widget.DataTable.Formatter.muteFormatter = function(liner, record, column, data) {
        liner.innerHTML = getMuteButtonHtml(record.getData('id'), record.getData('isAdminMuted'));
    };

    var participantColumns = [
        {key: 'number', label: '', className: 'conferenceParticipantTableNumber'},
        {key: 'isAdminMuted', label: '', formatter: 'muteFormatter', className: 'conferenceParticipantTableDrop'},
        {key: 'drop', label: '', className: 'conferenceParticipantTableDrop'}
    ];

    var participantDataSource = new YAHOO.util.DataSource('/getConferenceParticipants?id=' + conferenceId);
    participantDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
    participantDataSource.connXhrMode = 'queueRequests';
    participantDataSource.responseSchema = {
        resultsList: 'results',
        fields: [
            {key: 'id', parser: 'number'},
            'number',
            'isAdmin',
            'isMuted',
            'isAdminMuted'
        ]
    };

    var participantDataTable = new YAHOO.widget.DataTable('conferenceParticipantTable', participantColumns, participantDataSource);
    participantDataTable.subscribe('cellClickEvent', function(args) {
        var target = args.target;
        var column = participantDataTable.getColumn(target);

        switch(column.key) {

            case 'drop':
                var record = participantDataTable.getRecord(target)
                $.ajax({
                    type: 'POST',
                    url: '/dropParticipant',
                    data: { id: record.getData('id') },
                    success: function(data) {
                        noticeSuccess('Participant dropped');
                        //participantDataTable.deleteRow(target); // no delete, wait for poll
                    },
                    error: function(req) {
                        // TODO something here? 
                        //noticeError(req.responseText);
                    }
                });
                break;
        }
    });/* TODO get this to work
    participantDataTable.subscribe('cellUpdateEvent', function(record, column, oldData) {
        var td = participantDataTable.getTdEl({record: record, column: column});
        YAHOO.util.Dom.setStyle(td, 'backgroundColor', '#ffff00');
        var animation = new YAHOO.util.ColorAnim(td, {
            backgroundColor: {
                to: '#ffffff'
            }
        });
        animation.animate();
    });*/

    var historyColumns = [
        {key: 'dateCreated', label: '', className: 'conferenceHistoryTableDateCreated'},
        {key: 'user', label: '', className: 'conferenceHistoryTableUser'},
        {key: 'description', label: '', className: 'conferenceHistoryTableDescription'}
    ];

    var historyDataSource = new YAHOO.util.DataSource('/getConferenceHistory?id=' + conferenceId);
    historyDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
    historyDataSource.connXhrMode = 'queueRequests';
    historyDataSource.responseSchema = {
        resultsList: 'results',
        fields: [
            'dateCreated',
            'user',
            'description'
        ]
    };

    var historyDataTable = new YAHOO.widget.DataTable('conferenceHistoryTable', historyColumns, historyDataSource);

    this.show = function() {
        $('#conferenceDialog').dialog('open');
        this.startPolling();
    };

    this.hide = function() {
        this.stopPolling();
        $('#conferenceDialog').dialog('close');
    };

    this.startPolling = function() {
        var participantCallback = {
            success: participantDataTable.onDataReturnReplaceRows,
            failure: function() {
                // TODO something?
                //noticeError('Error initializing Conference DataTable');
            },
            scope: participantDataTable
        };

        participantDataSource.setInterval(1000, null, participantCallback);

        var historyCallback = {
            success: historyDataTable.onDataReturnReplaceRows,
            failure: function() {
                // TODO something?
            },
            scope: historyDataTable
        };
        
        historyDataSource.setInterval(1000, null, historyCallback);
    };

    this.stopPolling = function() {
        participantDataSource.clearAllIntervals();
        historyDataSource.clearAllIntervals();
    };
}

function ConferenceList() {

    var openConference;

    $('#conferenceListDialog').dialog({
        autoOpen: false,
        closeOnEscape: false,
        dialogClass: 'no-close',
        draggable: false,
        height: 250,
        position: [50, 50],
        resizable: false,
        title: 'Available Conferences',
        width: 400
    });
    
    YAHOO.widget.DataTable.Formatter.conferenceListDescriptionFormatter = function(liner, record, column, data) {
        liner.innerHTML = '<a href="#" onclick="YAHOO.listen.conferenceList.openConference(' + record.getData('id') + ');return false;">' + record.getData('description') + '</a>';
    };

    var columns = [
//        {key: 'id', label: 'Id', formatter: 'conferenceListFormatter', className: 'dataTableCell'},
        {key: 'description', label: 'Description', className: 'dataTableCell', formatter: 'conferenceListDescriptionFormatter'},
        {key: 'isStarted', label: 'Started?', className: 'dataTableCell'},
        {key: 'callers', label: 'Callers', className: 'dataTableCell'}
    ];

    var dataSource = new YAHOO.util.DataSource('/getConferenceList');
    dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
    dataSource.connXhrMode = 'queueRequests';
    dataSource.responseSchema = {
        resultsList: 'results',
        fields: [
            {key: 'id', parser: 'number'},
            'description',
            'isStarted'
            // TODO subscriber/user to whom conference belongs
        ]
    };

    var dataTable = new YAHOO.widget.DataTable('conferenceListTable', columns, dataSource);

    this.show = function() {
        this.startPolling();
        $('#conferenceListDialog').dialog('open');
    }

    this.hide = function() {
        $('#conferenceListDialog').dialog('close');
        this.stopPolling();
    }

    this.startPolling = function() {
        var callback = {
            success: dataTable.onDataReturnReplaceRows,
            failure: function() {
                // TODO something?
            },
            scope: dataTable
        };
        dataSource.setInterval(1000, null, callback);
    };

    this.stopPolling = function() {
        dataSource.clearAllIntervals();
    };

    this.openConference = function(id) {
        if(openConference) {
            openConference.hide();
        }
        openConference = new Conference(id);
        openConference.show();
    };

    this.hideOpenConferences = function() {
        if(openConference) {
            openConference.hide();
        }
        openConference = null;
    };
}

$(document).ready(function() {

    $.ajaxSetup({
        error: function(req, s, e) {
            if(req.status == 401) { // unauthorized
                logout();
            }
        }
    });

    $('#logoutButton').click(function(event) {
        logout();
    });

    $('#provisionAccountDialog').dialog({
        autoOpen: false,
        dialogClass: 'no-title',
        draggable: false,
        height: 350,
        position: [50, 350],
        resizable: false,
        width: 400
    });

    $('#provisionAccountForm').submit(function(event) {
        provisionAccount(event);
        return false;
    });

    showEverything();
});

function showEverything() {
    YAHOO.listen.isAdmin = true; // FIXME

    // open things
    $('#logoutButton').show();
    YAHOO.listen.conferenceList = new ConferenceList();
    YAHOO.listen.conferenceList.show();
    
    if(YAHOO.listen.isAdmin) {
        $('#provisionAccountDialog').dialog('open');
    }
}

function logout() {
    window.location = '/logout';
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

function provisionAccount(event) {
    var errorDiv = $('#provisionAccountForm .errors');
    errorDiv.hide();

    var provisionAccountNumber = $('#provisionAccountNumber');
    var provisionAccountPassword = $('#provisionAccountPassword');
    var provisionAccountUsername = $('#provisionAccountUsername'); 

    $.ajax({
        type: 'POST',
        url: event.target.action,
        data: { number: provisionAccountNumber.val(),
                password: provisionAccountPassword.val(),
                username: provisionAccountUsername.val() },
        success: function(data) {
            //$('#provisionAccountDialog').close();
            provisionAccountNumber.val('');
            provisionAccountPassword.val('');
            provisionAccountUsername.val('');
            noticeSuccess('Account Provisioned');
        },
        error: function(data, status) {
            errorDiv.html(data.responseText);
            errorDiv.slideDown(200);
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