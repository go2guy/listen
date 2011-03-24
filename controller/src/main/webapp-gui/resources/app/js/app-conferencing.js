var interact = interact || {};

var currentConference;
var displayingScheduledConferenceData = [];

$(document).ready(function() {
    if(interact.util.isDefined(CONFERENCE_ID)) {
        new Conference(CONFERENCE_ID).load();
    } else {
        new Conference().load();
    }

    // schedule

    $('#scheduleConferenceForm').submit(function(event) {
        scheduleConference(event);
        return false;
    });

    $('#scheduleConferenceDate').datepicker();

    // outdial

    $('#outdial-show').click(function() {
        $('#outdial-dialog').slideDown(200);
        $('#outdial-number').focus();
    });

    $('#outdial-number').change(function(e) {
        interact.listen.checkBlacklist(e.target);
    });

    $('#outdial-form').submit(function() { return false });

    $('#outdial-cancel').click(function() {
        $('#outdial-dialog').slideUp(200);
        return false;
    });

    $('#outdial-submit').click(function(data) {
        if(data.interrupt) {                        
             $('#outdial-number').attr('checked', true);
        }
        $('#outdial-dialog').slideUp(200);
        Server.outdial($('#outdial-number').val(), currentConference.getConferenceId(), $('#outdial-interrupt').val()); // FIXME referencing a global here is gross
        $('#outdial-number').val('');
        return false;
    });
});

function Conference(id) {
    var conferenceId;

    if(interact.util.isDefined(id)) {
        interact.util.debug('Constructing conference, id provided [' + id + ']')
        conferenceId = id;
    } else {
        interact.util.debug('Constructing conference without id; asking server');
        $.getJSON(interact.listen.url('/ajax/getConferenceInfo'), interact.util.bind(this, function(data) {
            conferenceId = data.info.id;
            interact.util.debug('Got id [' + conferenceId + '] from server');
            this.load();
        }));
    }

    currentConference = this;
    var interval;

    var callerTable = new interact.util.DynamicTable({
        url: interact.listen.url('/ajax/getConferenceParticipants?id=' + conferenceId),
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
            interact.util.setFieldContent(numberCell, numberContent, animate);

            // mute/unmute icons
            if(!data.isAdmin && !data.isPassive) {
                interact.util.setFieldContent(row.find('.caller-cell-muteIcon'), '<button class="icon-' + (data.isAdminMuted ? 'un' : '') + 'mute' + '" ' + 'onclick="' + (data.isAdminMuted ? 'Server.unmuteCaller(' + data.id + ');' : 'Server.muteCaller(' + data.id + ');return false;') + '" title="' + ((data.isAdminMuted ? 'Unmute' : 'Mute') + ' ' + data.number) + '"></button>', false, true);
            } else {
                interact.util.setFieldContent(row.find('.caller-cell-muteIcon'), '', false);
            }

            // drop button
            if(data.isAdmin) {
                interact.util.setFieldContent(row.find('.caller-cell-dropIcon'), '', false);
            } else {
                interact.util.setFieldContent(row.find('.caller-cell-dropIcon'), '<button class="icon-delete" onclick="confirmDropCaller(' + data.id + ');" title="Drop ' + data.number + ' from the conference"/>', false, true);
            }
        }
    });

    var historyTable = new interact.util.DynamicTable({
        tableId: 'conference-history-table',
        templateId: 'conferencehistory-row-template',
        retrieveList: function(data) {
            return data;
        },
        alternateRowColors: true,
        reverse: true,
        updateRowCallback: function(row, data, animate) {
            interact.util.setFieldContent(row.find('.conferencehistory-cell-date'), data.dateCreated, animate);
            interact.util.setFieldContent(row.find('.conferencehistory-cell-description'), data.description, animate);
        }
    });

    var pinTable = new interact.util.DynamicTable({
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

            interact.util.setFieldContent(row.find('.pin-cell-number'), data.number, animate);
            interact.util.setFieldContent(row.find('.pin-cell-type'), data.type, animate);
            interact.util.setFieldContent(row.find('.pin-cell-removeIcon'), '<button class="button-delete" readonly="readonly" disabled="disabled"></button>', false, true);
        }
    });

    var recordingTable = new interact.util.DynamicTable({
        url: interact.listen.url('/ajax/getConferenceRecordingList?id=' + conferenceId),
        tableId: 'conference-recording-table',
        templateId: 'recording-row-template',
        retrieveList: function(data) {
            return data.results;
        },
        reverse: true,
        alternateRowColors: true,
        paginationId: 'conference-recording-pagination',
        updateRowCallback: function(row, data, animate) {
            interact.util.setFieldContent(row.find('.recording-cell-dateCreated'), data.dateCreated, animate);

            if(data.duration && data.duration != '') {
                var d = Math.floor(parseInt(data.duration) / 1000);
                var durationText = (d < 60 ? '0' : (Math.floor(d / 60))) + ":" + (d % 60 < 10 ? '0' : '') + (d % 60);
                interact.util.setFieldContent(row.find('.recording-cell-duration'), durationText, animate);
            }

            interact.util.setFieldContent(row.find('.recording-cell-fileSize'), (Math.floor((parseInt(data.fileSize) / 1024) * 100) / 100) + "KB", animate);
            interact.util.setFieldContent(row.find('.recording-cell-download'), '<a href="' + interact.listen.url('/ajax/getConferenceRecording?id=' + data.id) + '">Download</a>', false, true);
        }
    });

    var scheduledConferenceTable = new interact.util.DynamicTable({
        url: interact.listen.url('/ajax/getScheduledConferenceList?historic=false&id=' + conferenceId),
        tableId: 'scheduled-conference-table',
        templateId: 'scheduled-conference-row-template',
        retrieveList: function(data) {
            return data.results;
        },
        isList: true,
        reverse: true,
        paginationId: 'scheduled-conference-pagination',
        initialMax: 5,
        updateRowCallback: function(row, data, animate) {
            if(data.isFuture === true) {
                row.removeClass('past');
                row.addClass('future');
            } else {
                row.removeClass('future');
                row.addClass('past');
            }
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-when'), data.startDate + ' to ' + data.endDate.split(' ')[1], animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-topic'), data.topic, animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-callers'), data.callers, animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-notes'), 'Notes: ' + data.notes, animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-activeCallers'), 'Active Callers: ' + data.activeCallers, animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-passiveCallers'), 'Passive Callers: ' + data.passiveCallers, animate);

            var more = '<a href="#" onclick="toggleScheduledConferenceData(' + data.id + ', \'more\');return false;" title="More information">show&nbsp;&raquo;</a>';
            var less = '<a href="#" onclick="toggleScheduledConferenceData(' + data.id + ', \'less\');return false;" title="Hide information">&laquo;&nbsp;hide</a>';
            var contains = false;
            for(var i = 0; i < displayingScheduledConferenceData.length; i++) {
                if(displayingScheduledConferenceData[i] === data.id) {
                    contains = true;
                    break;
                }
            }
            var field = row.find('.scheduled-conference-cell-view');
            if(contains) {
                interact.util.setFieldContent(field, less, false, true);
                row.find('.scheduled-conference-cell-notes').css('display', 'block');
                row.find('.scheduled-conference-cell-activeCallers').css('display', 'block');
                row.find('.scheduled-conference-cell-passiveCallers').css('display', 'block');
            } else {
                interact.util.setFieldContent(field, more, false, true);
                row.find('.scheduled-conference-cell-notes').css('display', 'none');
                row.find('.scheduled-conference-cell-activeCallers').css('display', 'none');
                row.find('.scheduled-conference-cell-passiveCallers').css('display', 'none');
            }
        }
    });

    var historicScheduledConferenceTable = new interact.util.DynamicTable({
        url: interact.listen.url('/ajax/getScheduledConferenceList?historic=true&id=' + conferenceId),
        tableId: 'historic-scheduled-conference-table',
        templateId: 'scheduled-conference-row-template',
        retrieveList: function(data) {
            return data.results;
        },
        isList: true,
        reverse: true,
        paginationId: 'historic-scheduled-conference-pagination',
        initialMax: 5,
        updateRowCallback: function(row, data, animate) {
            if(data.isFuture === true) {
                row.removeClass('past');
                row.addClass('future');
            } else {
                row.removeClass('future');
                row.addClass('past');
            }
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-when'), data.startDate + ' until ' + data.endDate, animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-topic'), data.topic, animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-callers'), data.callers, animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-notes'), 'Notes: ' + data.notes, animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-activeCallers'), 'Active Callers: ' + data.activeCallers, animate);
            interact.util.setFieldContent(row.find('.scheduled-conference-cell-passiveCallers'), 'Passive Callers: ' + data.passiveCallers, animate);

            var more = '<a href="#" onclick="toggleScheduledConferenceData(' + data.id + ', \'more\');return false;" title="More information">show&nbsp;&raquo;</a>';
            var less = '<a href="#" onclick="toggleScheduledConferenceData(' + data.id + ', \'less\');return false;" title="Hide information">&laquo;&nbsp;hide</a>';
            var contains = false;
            for(var i = 0; i < displayingScheduledConferenceData.length; i++) {
                if(displayingScheduledConferenceData[i] === data.id) {
                    contains = true;
                    break;
                }
            }
            var field = row.find('.scheduled-conference-cell-view');
            if(contains) {
                interact.util.setFieldContent(field, less, false, true);
                row.find('.scheduled-conference-cell-notes').css('display', 'block');
                row.find('.scheduled-conference-cell-activeCallers').css('display', 'block');
                row.find('.scheduled-conference-cell-passiveCallers').css('display', 'block');
            } else {
                interact.util.setFieldContent(field, more, false, true);
                row.find('.scheduled-conference-cell-notes').css('display', 'none');
                row.find('.scheduled-conference-cell-activeCallers').css('display', 'none');
                row.find('.scheduled-conference-cell-passiveCallers').css('display', 'none');
            }
        }
    });

    var pollAndSet = function(animate) {
        callerTable.setUrl(interact.listen.url('/ajax/getConferenceParticipants?id=' + conferenceId));
        callerTable.pollAndSet(animate);

        recordingTable.setUrl(interact.listen.url('/ajax/getConferenceRecordingList?id=' + conferenceId));
        recordingTable.pollAndSet(animate);

        scheduledConferenceTable.setUrl(interact.listen.url('/ajax/getScheduledConferenceList?historic=false&id=' + conferenceId));
        scheduledConferenceTable.pollAndSet(animate);

        historicScheduledConferenceTable.setUrl(interact.listen.url('/ajax/getScheduledConferenceList?historic=true&id=' + conferenceId));
        historicScheduledConferenceTable.pollAndSet(animate);

        var start = interact.util.timestamp();
        $.ajax({
            url: interact.listen.url('/ajax/getConferenceInfo?id=' + conferenceId),
            dataType: 'json',
            cache: false,
            success: function(data, textStatus, xhr) {
                historyTable.update(data.history.results, animate);
                pinTable.update(data.pins.results, animate);

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

                var recordHtml = '<button class="button-' + (data.info.isRecording ? 'stop' : 'record') + '" onclick="' + (data.info.isRecording ? 'Server.stopRecording(' + conferenceId + ');return false;' : 'Server.startRecording(' + conferenceId + ');return false;') + '" title="' + (data.info.isRecording ? 'Stop Recording' : 'Start') + ' recording this conference">' + (data.info.isRecording ? 'Stop Recording' : 'Record') + '</button>';
                if(recordButton.html() != recordHtml) {
                    recordButton.html(recordHtml);
                }
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    };

    this.getConferenceId = function() {
        return conferenceId;
    }

    this.load = function() {
        interact.util.trace('Loading conferencing, conference id = [' + conferenceId + ']');
        if(interact.util.isDefined(conferenceId)) {
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
}

function scheduleConference(event) {
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

    var start = interact.util.timestamp();
    $.ajax({
        type: 'POST',
        url: interact.listen.url('/ajax/scheduleConference'),
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
            scheduleConferenceDate.val('');
            scheduleConferenceTimeHour.val('1');
            scheduleConferenceTimeMinute.val('00');
            scheduleConferenceTimeAmPm.val('AM');
            scheduleConferenceEndTimeHour.val('1');
            scheduleConferenceEndTimeMinute.val('15');
            scheduleConferenceEndTimeAmPm.val('AM');
            scheduleConferenceSubject.val('');
            scheduleConferenceDescription.val('');
            scheduleConferenceActiveParticipants.val('');
            scheduleConferencePassiveParticipants.val('');
            interact.listen.notifySuccess('Emails have been sent to the provided addresses');
        },
        error: function(data, status) {
            interact.listen.notifyError(data.responseText);
        },
        complete: function(xhr, textStatus) {
            var elapsed = interact.util.timestamp() - start;
            $('#latency').text(elapsed);
        }
    });
}

function confirmDropCaller(id) {
    if(confirm('Are you sure?')) {
        Server.dropCaller(id);
    }
}

function toggleScheduledConferenceData(id, action) {
    interact.util.trace('toggleScheduledConferenceData');
    var row = $('#scheduled-conference-table-row-' + id);
    if(action == 'more') {
        displayingScheduledConferenceData.push(id);
        var less = '<a href="#" onclick="toggleScheduledConferenceData(' + id + ', \'less\');return false;" title="Hide information">&laquo;&nbsp;hide</a>';
        interact.util.setFieldContent(row.find('.scheduled-conference-cell-view'), less, false, true);
        row.find('.scheduled-conference-cell-notes').css('display', 'block');
        row.find('.scheduled-conference-cell-activeCallers').css('display', 'block');
        row.find('.scheduled-conference-cell-passiveCallers').css('display', 'block');
    } else if(action === 'less') {
        var newArr = [];
        for(var i = 0; i < displayingScheduledConferenceData.length; i++) {
            if(displayingScheduledConferenceData[i] !== id) {
                newArr.push(displayingScheduledConferenceData[i]);
            }
        }
        displayingScheduledConferenceData = newArr;
        row.find('.scheduled-conference-cell-notes').css('display', 'none');
        row.find('.scheduled-conference-cell-activeCallers').css('display', 'none');
        row.find('.scheduled-conference-cell-passiveCallers').css('display', 'none');
        var more = '<a href="#" onclick="toggleScheduledConferenceData(' + id + ', \'more\');return false;" title="More information">show&nbsp;&raquo;</a>';
        interact.util.setFieldContent(row.find('.scheduled-conference-cell-view'), more, false, true);
    }
}