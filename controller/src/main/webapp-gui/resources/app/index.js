var currentConference;

$.ajaxSetup({
    error: function(xhr, textStatus, errorThrown) {
        if(xhr && xhr.status == 401) {
            window.location = '/logout';
        }
    }
});

var LISTEN, SERVER;

$(document).ready(function() {
    LISTEN = function() {

        function Application(windowId, menuId, position, content) {
            var windowId = windowId;
            var menuId = menuId;
            var position = position;
            this.content = content;

            var windowDiv = $('#' + windowId);
            var menuItem = $('#' + menuId);
            if(menuItem && windowDiv) {
                var application = this;
                menuItem.click(function() {
                    // use 'application' since 'this' will be in the function scope, not the parent object's scope
                    pub.switchApp(application);
                });
            }

            this.getPosition = function() {
                return position;
            };

            this.swapWith = function(other) {
                if(this === other) {
                    return;
                }
                this.hide(position < other.getPosition() ? 'left' : 'right', function() {
                    other.show(position < other.getPosition() ? 'right' : 'left');
                });
            };

            this.hide = function(direction, callback) {
                if(callback) {
                    windowDiv.effect('slide', { direction: direction ? direction : 'left', mode: 'hide' }, 250, callback);
                } else {
                    windowDiv.effect('slide', { direction: direction ? direction : 'left', mode: 'hide' }, 250);
                }
                if(this.content) {
                    this.content.unload();
                }
            };

            this.show = function(direction) {
                if(this.content) {
                    this.content.load();
                }
                windowDiv.effect('slide', { direction: direction ? direction : 'right', mode: 'show' }, 250);
            };
        };

        // FIXME conditionally load applications based on user permissions/licensing
        var appConference = new Application('conference-application', 'menu-conferencing', 1, new Conference());
        var appVoicemail = new Application('voicemail-application', 'menu-voicemail', 2);
        var appFindMe = new Application('findme-application', 'menu-findme', 3);
        var appAdministration = new Application('administration-application', 'menu-administration', 4); 

        var currentApplication;

        var pub = {
            switchApp: function(to) {
                if(currentApplication) {
                    currentApplication.swapWith(to);
                } else {
                    to.show();
                }
                currentApplication = to;
            },

            setContent: function(applicationName, content) {
                var app;
                switch(applicationName) {
                    case 'conference':
                        app = appConference;
                        break;
                }
                if(app) {
                    if(app.content) {
                        app.content.unload();
                    }
                    app.content = content;
                    app.content.load();
                }
            }
        };

        return pub;
    }();

    SERVER = {
        dropCaller: function(id) {
            $.ajax({
                type: 'POST',
                url: '/ajax/dropParticipant',
                data: { id: id },
                success: function(data) {
                    //noticeSuccess('Participant dropped');
                },
                error: function(req) {
                    //noticeError(req.responseText);
                }
            });
        },
    
        muteCaller: function(id) {
            $.ajax({
                type: 'POST',
                url: '/ajax/muteParticipant',
                data: { id: id },
                success: function(data) {
                    //noticeSuccess('Participant muted');
                },
                error: function(req) {
                    //noticeError(req.responseText);
                }
            });
        },
    
        outdial: function(number, conferenceId) {
            var errorDiv = $('#outdial-dialog .form-error-message');
            errorDiv.hide();
            errorDiv.text('');
    
            $.ajax({
                type: 'POST',
                url: '/ajax/outdial',
                data: { number: number,
                        conferenceId: conferenceId },
                success: function(data) {
                    $('#outdial-dialog').slideUp(200);
                    notify('Number ' + number + ' is being dialed');
                },
                error: function(req) {
                    errorDiv.text(req.responseText);
                    errorDiv.slideDown(200);
                    //notify('An error occurred dialing the number - please contact an Administrator.');
                }
            });
        },
    
        unmuteCaller: function(id) {
            $.ajax({
                type: 'POST',
                url: '/ajax/unmuteParticipant',
                data: { id: id },
                success: function(data) {
                    //noticeSuccess('Participant unmuted');
                },
                error: function(req) {
                    //noticeError(req.responseText);
                }
            });
        }
    };
});

function Conference(id) {
    var conferenceId;

    if(id) {
        conferenceId = id;
    } else {
        $.getJSON('/ajax/getConferenceInfo', function(data) {
            conferenceId = data.info.id;
        });
    }

    currentConference = this;

    var callers = new ConferenceCallerList();
    var history = new ConferenceHistoryList();
    var pins = new ConferencePinList();

    var interval;

    this.getConferenceId = function() {
        return conferenceId;
    }

    this.load = function(animate) {
        interval = setInterval(function() {
            $.ajax({
                url: '/ajax/getConferenceInfo?id=' + conferenceId,
                dataType: 'json',
                cache: false,
                success: function(data, textStatus, xhr) {
                    callers.update(data.participants.results);
                    history.update(data.history.results);
                    pins.update(data.pins.results);

                    var titleText = 'Conference ' + data.info.description;
                    var title = $('#conference-title');
                    if(title.text() != titleText) {
                        title.text(titleText);
                    }

                    var onMessage = titleText + ': Started';
                    var offMessage = titleText + ': Waiting for administrator';

                    if(data.info.isStarted) {
                        title.css('background-image', "url('resources/app/images/new/bullet_green_16x16.png')")
                        title.attr('title', onMessage);
                    } else {
                        title.css('background-image', "url('resources/app/images/new/bullet_red_16x16.png')")
                        title.attr('title', offMessage);
                    }
                }
            });
        }, 1000);
    };

    this.unload = function(animate) {
        var window = $('#conference-window');
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
            var muteHtml = '<button class="' + (data.isAdminMuted || data.isPassive ? 'un' : '') + 'mute-button' + (data.isPassive ? '-disabled' : '') + '" ' + (data.isPassive ? 'disabled="disabled" readonly="readonly" ': '') + 'onclick="' + (data.isAdminMuted ? 'SERVER.unmuteCaller(' + data.id + ');' : 'SERVER.muteCaller(' + data.id + ');return false;') + '"/>';
            var mute = li.find('.caller-mute-icon');
            if(mute.html() != muteHtml) {
                mute.html(muteHtml);
            }

            var dropHtml = '<button class="delete-button" onclick="SERVER.dropCaller(' + data.id + ');"/>';
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
                    var content = data.dateCreated + ' - ' + data.description;
                    if(li.text() != content) {
                        li.text(content);
                    }
                    found = true;
                    break;
                }
            }

            if(!found) {
                var clone = $('#history-row-template').clone();
                clone.attr('id', 'history-' + data.id);
                clone.find('.history-content').text(data.dateCreated + ' - ' + data.description);
                clone.css('opacity', 0);
                clone.addClass((list.length - i) % 2 == 0 ? 'odd' : 'even');
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

        var removeHtml = '<button class="delete-button" readonly="readonly" disabled="disabled"/>';
        li.find('.pin-remove').html(removeHtml);
    };

    this.update = function(list) {
        var pinCount = $('#conference-pin-count');
        // TODO move this out of the list functionality
        if(pinCount.text() != list.length) {
            pinCount.text(list.length);
        }

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

    $('#main-menu-handle').click(function() {
        $('#main-menu').animate({
            height: 'toggle'
        }, 500);
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