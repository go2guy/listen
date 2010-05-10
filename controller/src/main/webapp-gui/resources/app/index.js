var currentConference;

$.ajaxSetup({
    error: function(xhr, textStatus, errorThrown) {
        if(xhr && xhr.status == 401) {
            window.location = '/logout';
        }
    }
});

var server = {
    muteCaller: function(id) {
        $.ajax({
            type: 'POST',
            url: '/muteParticipant',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Participant muted');
            },
            error: function(req) {
                //noticeError(req.responseText);
            }
        });
    },

    unmuteCaller: function(id) {
        $.ajax({
            type: 'POST',
            url: '/unmuteParticipant',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Participant unmuted');
            },
            error: function(req) {
                //noticeError(req.responseText);
            }
        });
    },

    dropCaller: function(id) {
        $.ajax({
            type: 'POST',
            url: '/dropParticipant',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Participant dropped');
            },
            error: function(req) {
                //noticeError(req.responseText);
            }
        });
    }
}

function Conference(id) {
    var conferenceId = id;

    var callers = new ConferenceCallerList();
    var history = new ConferenceHistoryList();
    var pins = new ConferencePinList();

    var interval;

    this.show = function(animate) {
        interval = setInterval(function() {
            $.ajax({
                url: '/getConferenceInfo?id=' + conferenceId,
                dataType: 'json',
                cache: false,
                success: function(data, textStatus, xhr) {
                    callers.update(data.participants.results);
                    history.update(data.history.results);
                    pins.update(data.pins.results);
    
                    var title = $('#conference-title');
                    if(title.text() != 'Conference ' + data.info.description) {
                        title.text('Conference ' + data.info.description);
                    }
    
                    // conference status icon and message
                    var icon = $('#conference-status-icon');
                    var message = $('#conference-status-message');
    
                    var onMessage = 'Started';
                    var offMessage = 'Waiting for administrator';
    
                    if(data.info.isStarted) {
                        icon.css('background-image', "url('resources/app/images/new/bullet_green_16x16.png')")
                        if(message.text() != onMessage) {
                            message.text(onMessage);
                        }
                    } else {
                        icon.css('background-image', "url('resources/app/images/new/bullet_red_16x16.png')")
                        if(message.text() != offMessage) {
                            message.text(offMessage);
                        }
                    }
                }
            });
        }, 1000);

        setTimeout(function() {
            var window = $('#conference-window');
            if(animate) {
                window.css('opacity', 0);
                window.css('display', 'block');
                window.animate({ opacity: 1 }, 1000);
            } else {
                window.css('display', 'block');
            }
        }, 1000);
    };

    this.hide = function(animate) {
        var window = $('#conference-window');
        if(animate) {
            window.animate({ opacity: 0 }, 1000, function() {
                window.css('display', 'none');
            });
        } else {
            window.css('display', 'none');
        }
        $('#caller-list').find('li').remove();
        $('#pin-list').find('li').remove();
        $('#history-list').find('li').remove();

        if(interval) {
            clearInterval(interval);
        }
    };
}

function ConferenceCallerList() {
    function updateMarkup(li, data, setId) {
    
        // row properties
        if(setId) {
            li.attr('id', 'caller-' + data.id);
        }

        if(data.isAdmin && !li.hasClass('caller-row-admin')) {
            li.addClass('caller-row-admin');
        } else if(!data.isAdmin && li.hasClass('caller-row-admin')) {
            li.removeClass('caller-row-admin');
        }

        if(data.isPassive && !li.hasClass('caller-row-passive')) {
            li.addClass('caller-row-passive');
        } else if(!data.isPassive && li.hasClass('caller-row-passive')) {
            li.removeClass('caller-row-passive');
        }

        // number
        var number = li.find('.caller-number');
        if(number.text() != data.number) {
            number.text(data.number);
        }

        // mute & drop
        if(data.isAdmin) {
            var mute = li.find('.caller-mute-icon');
            if(mute.text() != '') {
                li.find('.caller-mute-icon').text('');
            }
            var drop = li.find('.caller-drop-icon');
            if(drop.text() != '') {
                li.find('.caller-drop-icon').text('');
            }
        } else {
            var muteHtml = '<button class="' + (data.isAdminMuted || data.isPassive ? 'un' : '') + 'mute-button' + (data.isPassive ? '-disabled' : '') + '" ' + (data.isPassive ? 'disabled="disabled" readonly="readonly" ': '') + 'onclick="' + (data.isAdminMuted ? 'server.unmuteCaller(' + data.id + ');' : 'server.muteCaller(' + data.id + ');return false;') + '"></button>';
            var mute = li.find('.caller-mute-icon');
            if(mute.html() != muteHtml) {
                mute.html(muteHtml);
            }

            var dropHtml = '<button class="delete-button" onclick="server.dropCaller(' + data.id + ');"></button>';
            var drop = li.find('.caller-drop-icon');
            if(drop.html() != dropHtml) {
                drop.html(dropHtml);
            }
        }
    }

    /**
     * Updates the list with the provided data. Provided data should be an array of caller objects.
     */
    this.update = function(list) {
        var callerCount = $('#conference-caller-count');
        // TODO move this out of the list functionality
        if(callerCount.text() != list.length) {
            callerCount.text(list.length);
        }
        //$('#conference-caller-count').text(list.length);

        var callers = $('#caller-list').find('.caller-row');
        var ids = [];
        for(var i = 0; i < list.length; i++) {
            var found = false;
            var data = list[i];
            for(var j = 0; j < callers.length; j++) {
                var li = $(callers[j]);
                if(li.attr('id') == 'caller-' + data.id) {
                    updateMarkup(li, data, false);
                    found = true;
                    break;
                }
            }

            if(!found) {
                var clone = $('#caller-row-template').clone();
                updateMarkup(clone, data, true);
                clone.css('opacity', 0);
                $('#caller-list').append(clone);
                clone.animate({ opacity: 1 }, 1000);
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
    this.update = function(list) {
        var histories = $('#history-list').find('.history-row');
        var ids = [];

        for(var i = list.length - 1; i >= 0; i--) {
            var found = false;
            var data = list[i];
            for(var j = 0; j < histories.length; j++) {
                var li = $(histories[j]);
                if(li.attr('id') == 'history-' + data.id) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                var clone = $('#history-row-template').clone();
                clone.attr('id', 'history-' + data.id);
                clone.find('.history-content').html(data.dateCreated + ' - ' + data.description);
                clone.css('opacity', 0);
                clone.addClass(i % 2 == 1 ? 'odd' : 'even');
                $('#history-list:first-child').prepend(clone);
                clone.animate({ opacity: 1 }, 1000);
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

function ConferencePinList() {
    function updateMarkup(li, data, setId) {
        // row properties
        if(setId) {
            li.attr('id', 'pin-' + data.id);
        }

        if(data.type == 'ADMIN') {
            li.addClass('pin-row-admin');
        } else {
            li.removeClass('pin-row-admin');
        }

        if(data.type == 'PASSIVE') {
            li.addClass('pin-row-passive');
        } else {
            li.removeClass('pin-row-passive');
        }

        li.find('.pin-number').html(data.number);
        li.find('.pin-type').html(data.type);

        var removeHtml = '<button class="delete-button" readonly="readonly" disabled="disabled"></button>';
        li.find('.pin-remove').html(removeHtml);
    };

    this.update = function(list) {
        var pinCount = $('#conference-pin-count');
        // TODO move this out of the list functionality
        if(pinCount.text() != list.length) {
            pinCount.text(list.length);
        }
        //$('#conference-pin-count').text(list.length);

        var pins = $('#pin-list').find('.pin-row');
        var ids = [];

        for(var i = list.length - 1; i >= 0; i--) {
            var found = false;
            var data = list[i];
            for(var j = 0; j < pins.length; j++) {
                var li = $(pins[j]);
                if(li.attr('id') == 'pin-' + data.id) {
                    updateMarkup(li, data, false);
                    found = true;
                    break;
                }
            }

            if(!found) {
                var clone = $('#pin-row-template').clone();
                clone.attr('id', 'pin-' + data.id);
                updateMarkup(clone, data, true);
                clone.css('opacity', 0);
                $('#pin-list').append(clone);
                clone.animate({ opacity: 1 }, 1000);
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

$(document).ready(function() {
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

    $.getJSON('/getConferenceInfo', function(data) {
        currentConference = new Conference(data.info.id);
        currentConference.show(true);
    });
});

function scheduleConference(event) {
    var div = $('#scheduleConferenceDialog .form-error-message');
    div.hide();
    div.text('');

    var scheduleConferenceDate = $('#scheduleConferenceDate');
    var scheduleConferenceTimeHour = $('#scheduleConferenceTimeHour');
    var scheduleConferenceTimeMinute = $('#scheduleConferenceTimeMinute');
    var scheduleConferenceTimeAmPm = $('#scheduleConferenceTimeAmPm');
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
                description: scheduleConferenceDescription.val(),
                activeParticipants: scheduleConferenceActiveParticipants.val(),
                passiveParticipants: scheduleConferencePassiveParticipants.val() },
        success: function(data) {
            $('#scheduleConferenceDialog').dialog('close');
            scheduleConferenceDate.val('');
            scheduleConferenceTimeHour.val('1');
            scheduleConferenceTimeMinute.val('00');
            scheduleConferenceTimeAmPm.val('AM');
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

function notify(message, isError, stay) {
    var div = $('#notification');
    if(isError === true) {
        div.addClass('error');
    } else {
        div.removeClass('error');
    }
    $('#notification').text(message);
    $('#notification').slideDown(200);

    if(stay !== true) {
        setTimeout(function() {
            $('#notification').slideUp(200);
        }, 3000);
    }
}