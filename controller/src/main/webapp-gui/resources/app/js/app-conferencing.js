var currentConference;

$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('conferencing', 'conferencing-application', 'menu-conferencing', new Conference()));

    // schedule

    $('#scheduleConferenceForm').submit(function(event) { return false; });

    $('#scheduleConferenceDialog .button-schedule').click(function(event) {
        scheduleConference(event);
        return false;
    });

    $('#scheduleConferenceDate').datepicker();

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
        return false;
    });

    $('#outdial-submit').click(function() {
        SERVER.outdial($('#outdial-number').val(), currentConference.getConferenceId()); // FIXME referencing a global here is gross
        return false;
    });
});

function Conference(id) {
    var conferenceId;

    if(LISTEN.isDefined(id)) {
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
        url: '/ajax/getConferenceParticipants?id=' + conferenceId,
        tableId: 'conference-caller-table',
        templateId: 'caller-row-template',
        retrieveList: function(data) {
            return data.results;
        },
        paginationId: 'conference-caller-pagination',
        countContainer: 'conference-caller-count',
        retrieveCount: function(data) {
            return data.results.length;
        },
        updateRowCallback: function(row, data, animate) {
            if(data.isAdmin && !row.hasClass('caller-row-admin')) {
                row.addClass('caller-row-admin');
            } else if(!data.isAdmin && row.hasClass('caller-row-admin')) {
                row.removeClass('caller-row-admin');
            }

            if(!data.isAdmin && !data.isPassive && !row.hasClass('caller-row-active')) {
                row.addClass('caller-row-active');
            } else if(data.isAdmin || data.isPassive) {
                row.removeClass('caller-row-active');
            }

            if(data.isPassive && !row.hasClass('caller-row-passive')) {
                row.addClass('caller-row-passive');
            } else if(!data.isPassive && row.hasClass('caller-row-passive')) {
                row.removeClass('caller-row-passive');
            }

            var numberCell = row.find('.caller-cell-number');
            var numberContent = data.number + (data.isAdmin ? ' *' : '');
            numberCell.attr('title', numberContent + ': ' + (data.isAdmin ? 'Admin' : (data.isPassive ? 'Passive' : 'Active')));
            LISTEN.setFieldContent(numberCell, numberContent, animate);

            // mute/unmute icons
            if(!data.isAdmin && !data.isPassive) {
                LISTEN.setFieldContent(row.find('.caller-cell-muteIcon'), '<button class="icon-' + (data.isAdminMuted ? 'un' : '') + 'mute' + '" ' + 'onclick="' + (data.isAdminMuted ? 'SERVER.unmuteCaller(' + data.id + ');' : 'SERVER.muteCaller(' + data.id + ');return false;') + '" title="' + ((data.isAdminMuted ? 'Unmute' : 'Mute') + ' ' + data.number) + '"></button>', false, true);
            } else {
                LISTEN.setFieldContent(row.find('.caller-cell-muteIcon'), '', false);
            }

            // drop button
            if(data.isAdmin) {
                LISTEN.setFieldContent(row.find('.caller-cell-dropIcon'), '', false);
            } else {
                LISTEN.setFieldContent(row.find('.caller-cell-dropIcon'), '<button class="icon-delete" onclick="SERVER.dropCaller(' + data.id + ');" title="Drop ' + data.number + ' from the conference"/>', false, true);
            }
        }
    });

    var historyTable = new LISTEN.DynamicTable({
        tableId: 'conference-history-table',
        templateId: 'conferencehistory-row-template',
        retrieveList: function(data) {
            return data;
        },
        alternateRowColors: true,
        reverse: true,
        updateRowCallback: function(row, data, animate) {
            LISTEN.setFieldContent(row.find('.conferencehistory-cell-date'), data.dateCreated, animate);
            LISTEN.setFieldContent(row.find('.conferencehistory-cell-description'), data.description, animate);
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
        updateRowCallback: function(row, data, animate) {
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

            LISTEN.setFieldContent(row.find('.pin-cell-number'), data.number, animate);
            LISTEN.setFieldContent(row.find('.pin-cell-type'), data.type, animate);
            LISTEN.setFieldContent(row.find('.pin-cell-removeIcon'), '<button class="button-delete" readonly="readonly" disabled="disabled"></button>', false, true);
        }
    });

    var recordingTable = new LISTEN.DynamicTable({
        tableId: 'conference-recording-table',
        templateId: 'recording-row-template',
        retrieveList: function(data) {
            return data;
        },
        reverse: true,
        alternateRowColors: true,
        updateRowCallback: function(row, data, animate) {
            LISTEN.setFieldContent(row.find('.recording-cell-dateCreated'), data.dateCreated, animate);

            if(data.duration && data.duration != '') {
                var d = Math.floor(parseInt(data.duration) / 1000);
                var durationText = (d < 60 ? '0' : (Math.floor(d / 60))) + ":" + (d % 60 < 10 ? '0' : '') + (d % 60);
                LISTEN.setFieldContent(row.find('.recording-cell-duration'), durationText, animate);
            }

            LISTEN.setFieldContent(row.find('.recording-cell-fileSize'), (Math.floor((parseInt(data.fileSize) / 1024) * 100) / 100) + "KB", animate);
            LISTEN.setFieldContent(row.find('.recording-cell-download'), '<a href="/ajax/getConferenceRecording?id=' + data.id + '">Download</a>', false, true);
        }
    });

    var pollAndSet = function(animate) {
        callerTable.setUrl('/ajax/getConferenceParticipants?id=' + conferenceId);
        callerTable.pollAndSet(animate);
        var start = LISTEN.timestamp();
        $.ajax({
            url: '/ajax/getConferenceInfo?id=' + conferenceId,
            dataType: 'json',
            cache: false,
            success: function(data, textStatus, xhr) {
                historyTable.update(data.history.results, animate);
                pinTable.update(data.pins.results, animate);
                recordingTable.update(data.recordings.results, animate);

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

                var recordHtml = '<button class="button-' + (data.info.isRecording ? 'stop' : 'record') + '" onclick="' + (data.info.isRecording ? 'SERVER.stopRecording(' + conferenceId + ');return false;' : 'SERVER.startRecording(' + conferenceId + ');return false;') + '" title="' + (data.info.isRecording ? 'Stop' : 'Start') + ' recording this conference">' + (data.info.isRecording ? 'Stop' : 'Record') + '</button>';
                if(recordButton.html() != recordHtml) {
                    recordButton.html(recordHtml);
                }
            },
            complete: function(xhr, textStatus) {
                var elapsed = LISTEN.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    };

    this.getConferenceId = function() {
        return conferenceId;
    }

    this.load = function() {
        LISTEN.log('Loading conferencing, conference id = [' + conferenceId + ']');
        if(LISTEN.isDefined(conferenceId)) {
            pollAndSet(false);
            $('#conferencing-application .conference-notloaded').hide();
            $('#conferencing-application .conference-content').show();
            interval = setInterval(function() {
                pollAndSet(true);
            }, 1000);
        } else {
            $('#conferencing-application .conference-content').hide();
            $('#conferencing-application .conference-notloaded').show();
        }
    };

    this.unload = function() {
        LISTEN.log('Unloading conferencing');
        $('#conferencing-application .conference-content').hide();
        $('#conferencing-application .conference-notloaded').show();
        if(interval) {
            clearInterval(interval);
        }
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
    var scheduleConferenceEndTimeHour = $('#scheduleConferenceEndTimeHour');
    var scheduleConferenceEndTimeMinute = $('#scheduleConferenceEndTimeMinute');
    var scheduleConferenceEndTimeAmPm = $('#scheduleConferenceEndTimeAmPm');
    var scheduleConferenceSubject = $('#scheduleConferenceSubject');
    var scheduleConferenceDescription = $('#scheduleConferenceDescription');
    var scheduleConferenceActiveParticipants = $('#scheduleConferenceActiveParticipants');
    var scheduleConferencePassiveParticipants = $('#scheduleConferencePassiveParticipants'); 

    var start = LISTEN.timestamp();
    $.ajax({
        type: 'POST',
        url: '/ajax/scheduleConference',
        data: { date: scheduleConferenceDate.val(),
                hour: scheduleConferenceTimeHour.val(),
                minute: scheduleConferenceTimeMinute.val(),
                amPm: scheduleConferenceTimeAmPm.val(),
                endHour: scheduleConferenceEndTimeHour.val(),
                endMinute: scheduleConferenceEndTimeMinute.val(),
                endAmPm: scheduleConferenceEndTimeAmPm.val(),
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
            scheduleConferenceTimeEndHour.val('1');
            scheduleConferenceTimeEndMinute.val('15');
            scheduleConferenceTimeEndAmPm.val('AM');
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
        },
        complete: function(xhr, textStatus) {
            var elapsed = LISTEN.timestamp() - start;
            $('#latency').text(elapsed);
        }
    });
}