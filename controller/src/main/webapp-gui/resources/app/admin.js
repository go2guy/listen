$(document).ready(function() {
    $('#provisionAccountDialog').dialog({
        autoOpen: false,
        draggable: false,
        height: 600,
        modal: true,
        position: 'center',
        resizable: false,
        title: 'Create New Account',
        width: 600
    });

    $('#provisionAccountForm').submit(function(event) {
        provisionAccount(event);
        return false;
    });

    $('#create-new-account-button').click(function(event) {
        $('#provisionAccountDialog').dialog('open');
    });
/*
    $('#conference-list .panel-title').click(function(event) {
        var content = $('#conference-list .panel-content')
        if(content.css('display') == 'none') {
            content.slideDown(200);
        } else {
            content.slideUp(200);
        }
    });
*/
    var list = new ConferenceList();
    var interval = setInterval(function() {
        $.getJSON('/getConferenceList', function(data) {
            list.update(data.results);
        });
    }, 1000);
});

function provisionAccount(event) {
    var div = $('#scheduleConferenceDialog .form-error-message');
    div.hide();
    div.text('');

    var provisionAccountNumber = $('#provisionAccountNumber');
    var provisionAccountPassword = $('#provisionAccountPassword');
    var provisionAccountPasswordConfirm = $('#provisionAccountPasswordConfirm');
    var provisionAccountUsername = $('#provisionAccountUsername'); 

    $.ajax({
        type: 'POST',
        url: event.target.action,
        data: { confirmPassword: provisionAccountPasswordConfirm.val(),
                number: provisionAccountNumber.val(),
                password: provisionAccountPassword.val(),
                username: provisionAccountUsername.val() },
        success: function(data) {
            $('#provisionAccountDialog').dialog('close');
            provisionAccountNumber.val('');
            provisionAccountPassword.val('');
            provisionAccountUsername.val('');
            notify('Account provisioned');
        },
        error: function(data, status) {
            var div = $('#provisionAccountDialog .form-error-message');
            div.text(data.responseText);
            div.slideDown(200);
        }
    });
}

function ConferenceList() {
    function updateMarkup(tr, data, setId) {
        // row properties
        if(setId) {
            tr.attr('id', 'conference-' + data.id);
        }

        tr.find('.conference-description').html(data.description);
        tr.find('.conference-status').html(data.isStarted ? 'Started' : 'Not Started');
//        tr.find('.conference-callers').html(data.callers);
//        tr.find('.conference-duration').html(data.duration);

        var viewHtml = '<button class="view-button" onclick="viewConference(' + data.id + ');">View</button>';
        var td = tr.find('.conference-view');
        if(td.html() != viewHtml) {
            td.html(viewHtml);
        }
    };

    this.update = function(list) {
        var conferences = $('#conference-list table tbody').find('tr');
        var ids = [];

        for(var i = list.length - 1; i >= 0; i--) {
            var found = false;
            var data = list[i];
            for(var j = 0; j < conferences.length; j++) {
                var tr = $(conferences[j]);
                if(tr.attr('id') == 'conference-' + data.id) {
                    updateMarkup(tr, data, false);
                    found = true;
                    break;
                }
            }

            if(!found) {
                var html = '<tr id="conference-' + data.id + '">';
                html += '<td class="conference-description"></td>';
                html += '<td class="conference-status"></td>';
//                html += '<td class="conference-callers"></td>';
//                html += '<td class="conference-duration"></td>';
                html += '<td class="conference-view"></td>';
                html += '</tr>';

                var clone = $(html);
                updateMarkup(clone, data, true);
                clone.css('opacity', 0);
                $('#conference-list table tbody').append(clone);
                clone.animate({ opacity: 1 }, 1000);
            }

            ids.push('conference-' + data.id);
        }

        for(var i = 0; i < conferences.length; i++) {
            var found = false;
            var conference = $(conferences[i]);
            for(var j = 0; j < ids.length; j++) {
                if(conference.attr('id') == ids[j]) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                conference.animate({
                    opacity: 0
                }, 1000, function() {
                    $(this).remove();
                });
            }
        }
    };
}

function viewConference(id) {
    //$('#conference-list .panel-content').slideUp(200);
    if(currentConference) {
        currentConference.hide(false);
    }
    currentConference = new Conference(id);
    currentConference.show(false);
}