var currentConference;

$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('conferencing', 'conferencing-application', 'menu-conferencing', 1, new Conference()));

    $('#scheduleConferenceDialog').dialog({
        autoOpen: false,
        draggable: false,
        height: 600,
        modal: true,
        position: 'center',
        resizable: false,
        title: 'Schedule Conference',
        width: 600
    });

    $('#scheduleConferenceForm').submit(function(event) {
        scheduleConference(event);
        return false;
    });

    $('#schedule-button').click(function(event) {
        $('#scheduleConferenceDialog').dialog('open');
    });
    $("#scheduleConferenceDate").datepicker();

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
        conferenceId = id;
    } else {
        var app = this;
        $.getJSON('/ajax/getConferenceInfo', function(data) {
            conferenceId = data.info.id;
            // if conferencing is the first loaded application, the LISTEN object will try and load it;
            // however, this ajax response might be returned AFTER the LISTEN object loads this application,
            // which means that this.conferenceId will not be set and the load() function will not actually
            // poll for the conference.
            // therefore, if the current LISTEN application is this one (conferencing), we need to force it
            // to re-load, since it now has the conferenceId
            if(LISTEN.getCurrentApplication().name == 'conferencing') {
                app.load();
            }
        });
    }

    currentConference = this;

    var callers = new ConferenceCallerList();
    var history = new ConferenceHistoryList();
    var pins = new ConferencePinList();
    var recordings = new ConferenceRecordingsList();

    var interval;

    var pollAndSet = function(withAnimation) {
        $.ajax({
            url: '/ajax/getConferenceInfo?id=' + conferenceId,
            dataType: 'json',
            cache: false,
            success: function(data, textStatus, xhr) {
                callers.update(data.participants.results, withAnimation);
                history.update(data.history.results, withAnimation);
                pins.update(data.pins.results, withAnimation);
                recordings.update(data.recordings.results, withAnimation);

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
        if(conferenceId) {
            pollAndSet(false);
            interval = setInterval(function() {
                pollAndSet(true);
            }, 1000);
        }
    };

    this.unload = function() {
        if(interval) {
            clearInterval(interval);
        }
        $('#conference-window').hide();
        $('#conference-caller-table tbody').find('tr').remove();
        $('#conference-pin-table tbody').find('tr').remove();
        $('#conference-history-table tbody').find('tr').remove();
        $('#conference-recording-table tbody').find('tr').remove();
    };
}

function ConferenceCallerList() {
    function updateMarkup(tr, data, setId) {
    
        // row properties
        if(setId) {
            tr.attr('id', 'caller-' + data.id);
        }

        if(data.isAdmin && !tr.hasClass('caller-row-admin')) {
            tr.addClass('caller-row-admin');
        } else if(!data.isAdmin && tr.hasClass('caller-row-admin')) {
            tr.removeClass('caller-row-admin');
        }

        if(data.isPassive && !tr.hasClass('caller-row-passive')) {
            tr.addClass('caller-row-passive');
        } else if(!data.isPassive && tr.hasClass('caller-row-passive')) {
            tr.removeClass('caller-row-passive');
        }

        // number
        var numberCell = tr.find('.caller-cell-number');
        if(numberCell.text() != data.number) {
            numberCell.text(data.number);
        }

        // mute & drop
        // TODO can we get rid of this isAdmin block?
        if(data.isAdmin) {
            var muteIconCell = tr.find('.caller-cell-muteIcon');
            if(muteIconCell.text() != '') {
                muteIconCell.text('');
            }
            var dropIconCell = tr.find('.caller-cell-dropIcon');
            if(dropIconCell.text() != '') {
                dropIconCell.text('');
            }
        } else {
            var muteHtml = '<button class="' + (data.isAdminMuted || data.isPassive ? 'un' : '') + 'mute-button' + (data.isPassive ? '-disabled' : '') + '" ' + (data.isPassive ? 'disabled="disabled" readonly="readonly" ': '') + 'onclick="' + (data.isAdminMuted ? 'SERVER.unmuteCaller(' + data.id + ');' : 'SERVER.muteCaller(' + data.id + ');return false;') + '" title="' + (data.isPassive ? 'Cannot unmute ' + data.number + ' (passive caller)' : ((data.isAdminMuted ? 'Unmute' : 'Mute') + ' ' + data.number)) + '"></button>';
            var muteIconCell = tr.find('.caller-cell-muteIcon');
            if(muteIconCell.html() != muteHtml) {
                muteIconCell.html(muteHtml);
            }

            var dropHtml = '<button class="delete-button" onclick="SERVER.dropCaller(' + data.id + ');" title="Drop ' + data.number + ' from the conference"/>';
            var dropIconCell = tr.find('.caller-cell-dropIcon');
            if(dropIconCell.html() != dropHtml) {
                dropIconCell.html(dropHtml);
            }
        }
    }

    /**
     * Updates the list with the provided data. Provided data should be an array of caller objects.
     */
    this.update = function(list, withAnimation) {
        var callerCount = $('#conference-caller-count');
        // TODO move this out of the list functionality
        if(callerCount.text() != list.length) {
            callerCount.text(list.length);
        }

        var callers = $('#conference-caller-table tbody').find('tr');
        var ids = [];
        for(var i = 0; i < list.length; i++) {
            var found = false;
            var data = list[i];
            for(var j = 0; j < callers.length; j++) {
                var tr = $(callers[j]);
                if(tr.attr('id') == 'caller-' + data.id) {
                    updateMarkup(tr, data, false);
                    found = true;
                    break;
                }
            }

            if(!found) {
                var clone = $('#caller-row-template').clone();
                updateMarkup(clone, data, true);
                clone.css('opacity', 0);
                $('#conference-caller-table tbody').append(clone);
                clone.animate({ opacity: 1 }, (withAnimation === true ? 1000 : 0));
            }

            ids.push('caller-' + data.id);
        }

        for(var i = 0; i < callers.length; i++) {
            var found = false;
            var caller = $(callers[i]);
            for(var j = 0; j < ids.length; j++) {
                if(caller.attr('id') == ids[j]) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                caller.animate({
                    opacity: 0
                }, 1000, function() {
                    $(this).remove();
                });
            }
        }
    }
}

function ConferenceHistoryList() {
    function updateMarkup(tr, data, setId) {
        if(setId) {
            tr.attr('id', 'history-' + data.id);
        }

        var dateCell = tr.find('.history-cell-date');
        if(dateCell.text() != data.dateCreated) {
            dateCell.text(data.dateCreated);
        }

        var descriptionCell = tr.find('.history-cell-description');
        if(descriptionCell.text() != data.description) {
            descriptionCell.text(data.description);
        }
    }

    this.update = function(list, withAnimation) {
        var histories = $('#conference-history-table tbody').find('tr');
        var ids = [];

        for(var i = list.length - 1; i >= 0; i--) {
            var found = false;
            var data = list[i];
            for(var j = 0; j < histories.length; j++) {
                var tr = $(histories[j]);
                if(tr.attr('id') == 'history-' + data.id) {
                    updateMarkup(tr, data, false);
                    found = true;
                    break;
                }
            }

            if(!found) {
                var clone = $('#history-row-template').clone();
                updateMarkup(clone, data, true);
                clone.css('opacity', 0);
                clone.addClass((list.length - i) % 2 == 0 ? 'odd' : 'even');
                $('#conference-history-table tbody').prepend(clone);
                clone.animate({ opacity: 1 }, (withAnimation === true ? 1000 : 0));
            }

            ids.push('history-' + data.id);
        }

        for(var i = 0; i < histories.length; i++) {
            var found = false;
            var history = $(histories[i]);
            for(var j = 0; j < ids.length; j++) {
                if(history.attr('id') == ids[j]) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                history.animate({
                    opacity: 0
                }, 1000, function() {
                    $(this).remove();
                });
            }
        }
    };
}

function ConferenceRecordingsList() {
    function updateMarkup(tr, data, setId) {
        if(setId) {
            tr.attr('id', 'recording-' + data.id);
        }
        
        var description = data.dateCreated + ' - ' + '<a href="/ajax/getConferenceRecording?id=' + data.id + '">' + data.description + '</a>' + ' [' + data.fileSize + ']';
        var descriptionCell = tr.find('.recording-cell-description');
        if(descriptionCell.html() != description) {
            descriptionCell.html(description);
        }
    }

    this.update = function(list, withAnimation) {
        var recordings = $('#conference-recording-table tbody').find('tr');
        var ids = [];

        for(var i = list.length - 1; i >= 0; i--) {
            var found = false;
            var data = list[i];
            for(var j = 0; j < recordings.length; j++) {
                var tr = $(recordings[j]);
                if(tr.attr('id') == 'recording-' + data.id) {
                    updateMarkup(tr, data, false);
                    found = true;
                    break;
                }
            }

            if(!found) {
                var clone = $('#recording-row-template').clone();
                updateMarkup(clone, data, true);
                clone.css('opacity', 0);
                clone.addClass(i % 2 == 0 ? 'odd' : 'even');
                $('#conference-recording-table tbody').append(clone);
                clone.animate({ opacity: 1 }, (withAnimation === true ? 1000 : 0));
            }

            ids.push('recording-' + data.id);
        }

        for(var i = 0; i < recordings.length; i++) {
            var found = false;
            var recording = $(recordings[i]);
            for(var j = 0; j < ids.length; j++) {
                if(recording.attr('id') == ids[j]) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                recording.animate({
                    opacity: 0
                }, 1000, function() {
                    $(this).remove();
                });
            }
        }
    };
}

function ConferencePinList() {
    function updateMarkup(tr, data, setId) {
        // row properties
        if(setId) {
            tr.attr('id', 'pin-' + data.id);
        }

        if(data.type == 'ADMIN') {
            tr.addClass('pin-row-admin');
        } else {
            tr.removeClass('pin-row-admin');
        }

        if(data.type == 'PASSIVE') {
            tr.addClass('pin-row-passive');
        } else {
            tr.removeClass('pin-row-passive');
        }

        tr.find('.pin-cell-number').html(data.number);
        tr.find('.pin-cell-type').html(data.type);

        var removeHtml = '<button class="delete-button" readonly="readonly" disabled="disabled"></button>';
        tr.find('.pin-cell-removeIcon').html(removeHtml);
    };

    this.update = function(list, withAnimation) {
        var pinCount = $('#conference-pin-count');
        // TODO move this out of the list functionality
        if(pinCount.text() != list.length) {
            pinCount.text(list.length);
        }

        var pins = $('#conference-pin-table tbody').find('tr');
        var ids = [];

        for(var i = list.length - 1; i >= 0; i--) {
            var found = false;
            var data = list[i];
            for(var j = 0; j < pins.length; j++) {
                var tr = $(pins[j]);
                if(tr.attr('id') == 'pin-' + data.id) {
                    updateMarkup(tr, data, false);
                    found = true;
                    break;
                }
            }

            if(!found) {
                var clone = $('#pin-row-template').clone();
                clone.attr('id', 'pin-' + data.id);
                updateMarkup(clone, data, true);
                clone.css('opacity', 0);
                $('#conference-pin-table tbody').append(clone);
                clone.animate({ opacity: 1 }, (withAnimation === true ? 1000 : 0));
            }

            ids.push('pin-' + data.id);
        }

        for(var i = 0; i < pins.length; i++) {
            var found = false;
            var pin = $(pins[i]);
            for(var j = 0; j < ids.length; j++) {
                if(pin.attr('id') == ids[j]) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                pin.animate({
                    opacity: 0
                }, 1000, function() {
                    $(this).remove();
                });
            }
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
    var scheduleConferenceSubject = $('#scheduleConferenceSubject');
    var scheduleConferenceDescription = $('#scheduleConferenceDescription');
    var scheduleConferenceActiveParticipants = $('#scheduleConferenceActiveParticipants');
    var scheduleConferencePassiveParticipants = $('#scheduleConferencePassiveParticipants'); 

    $.ajax({
        type: 'POST',
        url: event.target.action,
        data: { date: scheduleConferenceDate.val(),
                hour: scheduleConferenceTimeHour.val(),
                minute: scheduleConferenceTimeMinute.val(),
                amPm: scheduleConferenceTimeAmPm.val(),
                subject: scheduleConferenceSubject.val(),
                description: scheduleConferenceDescription.val(),
                activeParticipants: scheduleConferenceActiveParticipants.val(),
                passiveParticipants: scheduleConferencePassiveParticipants.val() },
        success: function(data) {
            $('#scheduleConferenceDialog').dialog('close');
            scheduleConferenceDate.val('');
            scheduleConferenceTimeHour.val('1');
            scheduleConferenceTimeMinute.val('00');
            scheduleConferenceTimeAmPm.val('AM');
            scheduleConferenceSubject.val('');
            scheduleConferenceDescription.val('');
            scheduleConferenceActiveParticipants.val('');
            scheduleConferencePassiveParticipants.val('');
            notify('Emails have been sent to the provided addresses');
        },
        error: function(data, status) {
            var div = $('#scheduleConferenceDialog .form-error-message');
            div.text(data.responseText);
            div.slideDown(200);
        }
    });
}