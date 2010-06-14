var currentConference;

$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('conferencing', 'conferencing-application', 'menu-conferencing', new Conference()));

    // schedule

    $('#schedule-show').click(function(event) {
        $('#scheduleConferenceDialog').slideDown(200);
    });

    $('#scheduleConferenceForm').submit(function(event) { return false; });

    $('#scheduleConferenceDialog .cancel-button').click(function(event) {
        $('#scheduleConferenceDialog').slideUp(200);
        $('#scheduleConferenceDialog .form-error-message').hide().text('');
    });

    $('#scheduleConferenceDialog .schedule-button').click(function(event) {
        scheduleConference(event);
    });

    $("#scheduleConferenceDate").datepicker();

    // outdial

    $('#outdial-show').click(function() {
        $('#outdial-dialog').slideDown(200);
        $('#outdial-number').focus();
    });

    $('#outdial-form').submit(function() { return false });

    $('#outdial-cancel').click(function() {
        $('#outdial-dialog').slideUp(200);
        $('#outdial-dialog .form-error-message').hide();
        $('#outdial-dialog .form-error-message').text('');
    });

    $('#outdial-submit').click(function() {
        SERVER.outdial($('#outdial-number').val(), currentConference.getConferenceId()); // FIXME referencing a global here is gross
    });
});

function Conference(id) {
    var conferenceId;

    if(id) {
        LISTEN.log('Constructing conference, id provided [' + id + ']')
        conferenceId = id;
    } else {
        LISTEN.log('Constructing conference without id; asking server');
        $.getJSON('/ajax/getConferenceInfo', LISTEN.bind(this, function(data) {
            conferenceId = data.info.id;
            LISTEN.log('Got id [' + conferenceId + '] from server');
            // if conferencing is the first loaded application, the LISTEN object will try and load it;
            // however, this ajax response might be returned AFTER the LISTEN object loads this application,
            // which means that this.conferenceId will not be set and the load() function will not actually
            // poll for the conference.
            // therefore, if the current LISTEN application is this one (conferencing), we need to force it
            // to re-load, since it now has the conferenceId
            if(LISTEN.getCurrentApplication().name == 'conferencing') {
                LISTEN.log("Current application is 'conferencing', forcing load()");
                this.load();
            }
        }));
    }

    currentConference = this;
    var interval;

    var callerTable = new LISTEN.DynamicTable({
        tableId: 'conference-caller-table',
        templateId: 'caller-row-template',
        retrieveList: function(data) {
            return data;
        },
        countContainer: 'conference-caller-count',
        retrieveCount: function(data) {
            return data.length;
        },
        updateRowCallback: function(row, data) {
            if(data.isAdmin && !row.hasClass('caller-row-admin')) {
                row.addClass('caller-row-admin');
            } else if(!data.isAdmin && row.hasClass('caller-row-admin')) {
                row.removeClass('caller-row-admin');
            }

            if(data.isPassive && !row.hasClass('caller-row-passive')) {
                row.addClass('caller-row-passive');
            } else if(!data.isPassive && row.hasClass('caller-row-passive')) {
                row.removeClass('caller-row-passive');
            }

            var numberCell = row.find('.caller-cell-number');
            if(numberCell.text() != data.number) {
                numberCell.text(data.number);
            }

            if(data.isAdmin) {
                var muteIconCell = row.find('.caller-cell-muteIcon');
                if(muteIconCell.text() != '') {
                    muteIconCell.text('');
                }
                var dropIconCell = row.find('.caller-cell-dropIcon');
                if(dropIconCell.text() != '') {
                    dropIconCell.text('');
                }
            } else {
                var muteHtml = '<button class="' + (data.isAdminMuted || data.isPassive ? 'un' : '') + 'mute-button' + (data.isPassive ? '-disabled' : '') + '" ' + (data.isPassive ? 'disabled="disabled" readonly="readonly" ': '') + 'onclick="' + (data.isAdminMuted ? 'SERVER.unmuteCaller(' + data.id + ');' : 'SERVER.muteCaller(' + data.id + ');return false;') + '" title="' + (data.isPassive ? 'Cannot unmute ' + data.number + ' (passive caller)' : ((data.isAdminMuted ? 'Unmute' : 'Mute') + ' ' + data.number)) + '"></button>';
                var muteIconCell = row.find('.caller-cell-muteIcon');
                if(muteIconCell.html() != muteHtml) {
                    muteIconCell.html(muteHtml);
                }
    
                var dropHtml = '<button class="delete-button" onclick="SERVER.dropCaller(' + data.id + ');" title="Drop ' + data.number + ' from the conference"/>';
                var dropIconCell = row.find('.caller-cell-dropIcon');
                if(dropIconCell.html() != dropHtml) {
                    dropIconCell.html(dropHtml);
                }
            }
        }
    });

    var historyTable = new LISTEN.DynamicTable({
        tableId: 'conference-history-table',
        templateId: 'history-row-template',
        retrieveList: function(data) {
            return data;
        },
        alternateRowColors: true,
        reverse: true,
        updateRowCallback: function(row, data) {
            var dateCell = row.find('.history-cell-date');
            if(dateCell.text() != data.dateCreated) {
                dateCell.text(data.dateCreated);
            }
    
            var descriptionCell = row.find('.history-cell-description');
            if(descriptionCell.text() != data.description) {
                descriptionCell.text(data.description);
            }
        }
    });

    var pinTable = new LISTEN.DynamicTable({
        tableId: 'conference-pin-table',
        templateId: 'pin-row-template',
        retrieveList: function(data) {
            return data;
        },
        countContainer: 'conference-pin-count',
        retrieveCount: function(data) {
            return data.length;
        },
        updateRowCallback: function(row, data) {
            if(data.type == 'ADMIN') {
                row.addClass('pin-row-admin');
            } else {
                row.removeClass('pin-row-admin');
            }

            if(data.type == 'PASSIVE') {
                row.addClass('pin-row-passive');
            } else {
                row.removeClass('pin-row-passive');
            }

            row.find('.pin-cell-number').html(data.number);
            row.find('.pin-cell-type').html(data.type);

            var removeHtml = '<button class="delete-button" readonly="readonly" disabled="disabled"></button>';
            row.find('.pin-cell-removeIcon').html(removeHtml);
        }
    });

    var recordingTable = new LISTEN.DynamicTable({
        tableId: 'conference-recording-table',
        templateId: 'recording-row-template',
        retrieveList: function(data) {
            return data;
        },
        alternateRowColors: true,
        updateRowCallback: function(row, data) {
            var dateCreatedCell = row.find('.recording-cell-dateCreated');
            if(dateCreatedCell.text() != data.dateCreated) {
                dateCreatedCell.text(data.dateCreated);
                this.highlight(dateCreatedCell);
            }

            if(data.duration && data.duration != '') {
                var durationCell = row.find('.recording-cell-duration');
                var d = parseInt(data.duration);
                var durationText = (d < 60 ? '0' : (Math.floor(d / 60))) + ":" + (d % 60 < 10 ? '0' : '') + (d % 60);
                if(durationCell.text() != durationText) {
                    durationCell.text(durationText);
                    this.highlight(durationCell);
                }
            }

            var fileSizeCell = row.find('.recording-cell-fileSize');
            var fileSizeText = (Math.floor((parseInt(data.fileSize) / 1024) * 100) / 100) + "KB";
            if(fileSizeCell.text() != fileSizeText) {
                fileSizeCell.text(fileSizeText);
                this.highlight(fileSizeCell);
            }

            var downloadCell = row.find('.recording-cell-download');
            var downloadHtml = '<a href="/ajax/getConferenceRecording?id=' + data.id + '">Download</a>';
            if(downloadCell.html() != downloadHtml) {
                downloadCell.html(downloadHtml);
            }
        }
    });

    var pollAndSet = function(withAnimation) {
        $.ajax({
            url: '/ajax/getConferenceInfo?id=' + conferenceId,
            dataType: 'json',
            cache: false,
            success: function(data, textStatus, xhr) {
                callerTable.update(data.participants.results, withAnimation);
                historyTable.update(data.history.results, withAnimation);
                pinTable.update(data.pins.results, withAnimation);
                recordingTable.update(data.recordings.results, withAnimation);

                var infoDescription = $('#conference-info-description');
                if(infoDescription.text() != data.info.description) {
                    infoDescription.text(data.info.description);
                }

                var infoStatus = $('#conference-info-status');
                var onMessage = 'Started';
                var offMessage = 'Waiting for administrator';
                var recordButton = $("#record-button-div");

                if(data.info.isStarted) {
                    infoStatus.text(onMessage);
                    infoStatus.removeClass('conference-not-started');
                    infoStatus.addClass('conference-started');
                    recordButton.show();
                    $('#outdial-show').show();
                } else {
                    infoStatus.text(offMessage);
                    infoStatus.removeClass('conference-started');
                    infoStatus.addClass('conference-not-started');
                    recordButton.hide();
                    $('#outdial-show').hide();
                }

                var recordHtml = '<button class="' + (data.info.isRecording ? 'stop' : 'record') + '-button"' + 'onclick="' + (data.info.isRecording ? 'SERVER.stopRecording(' + conferenceId + ');return false;' : 'SERVER.startRecording(' + conferenceId + ');return false;') + '" title="' + (data.info.isRecording ? 'Stop' : 'Start') + ' recording this conference">' + (data.info.isRecording ? 'Stop' : 'Record') + '</button>';                        
                if(recordButton.html() != recordHtml) {
                    recordButton.html(recordHtml);
                }
            }
        });
    };

    this.getConferenceId = function() {
        return conferenceId;
    }

    this.load = function() {
        LISTEN.log('Loading conferencing');
        if(conferenceId) {
            pollAndSet(false);
            interval = setInterval(function() {
                pollAndSet(true);
            }, 1000);
        }
    };

    this.unload = function() {
        LISTEN.log('Unloading conferencing');
        if(interval) {
            clearInterval(interval);
        }
//        $('#conference-window').hide();
//        $('#conference-caller-table tbody').find('tr').remove();
//        $('#conference-pin-table tbody').find('tr').remove();
//        $('#conference-history-table tbody').find('tr').remove();
//        $('#conference-recording-table tbody').find('tr').remove();
    };
}

function scheduleConference(event) {
    var div = $('#scheduleConferenceDialog .form-error-message');
    div.hide();
    div.text('');

    var scheduleConferenceDate = $('#scheduleConferenceDate');
    var scheduleConferenceTimeHour = $('#scheduleConferenceTimeHour');
    var scheduleConferenceTimeMinute = $('#scheduleConferenceTimeMinute');
    var scheduleConferenceTimeAmPm = $('#scheduleConferenceTimeAmPm');
    var scheduleConferenceSubject = $('#scheduleConferenceSubject');
    var scheduleConferenceDescription = $('#scheduleConferenceDescription');
    var scheduleConferenceActiveParticipants = $('#scheduleConferenceActiveParticipants');
    var scheduleConferencePassiveParticipants = $('#scheduleConferencePassiveParticipants'); 

    $.ajax({
        type: 'POST',
        url: '/ajax/scheduleConference',
        data: { date: scheduleConferenceDate.val(),
                hour: scheduleConferenceTimeHour.val(),
                minute: scheduleConferenceTimeMinute.val(),
                amPm: scheduleConferenceTimeAmPm.val(),
                subject: scheduleConferenceSubject.val(),
                description: scheduleConferenceDescription.val(),
                activeParticipants: scheduleConferenceActiveParticipants.val(),
                passiveParticipants: scheduleConferencePassiveParticipants.val() },
        success: function(data) {
            $('#scheduleConferenceDialog').slideUp(200);
            scheduleConferenceDate.val('');
            scheduleConferenceTimeHour.val('1');
            scheduleConferenceTimeMinute.val('00');
            scheduleConferenceTimeAmPm.val('AM');
            scheduleConferenceSubject.val('');
            scheduleConferenceDescription.val('');
            scheduleConferenceActiveParticipants.val('');
            scheduleConferencePassiveParticipants.val('');
            LISTEN.notify('Emails have been sent to the provided addresses');
        },
        error: function(data, status) {
            var div = $('#scheduleConferenceDialog .form-error-message');
            div.text(data.responseText);
            div.slideDown(200);
        }
    });
}