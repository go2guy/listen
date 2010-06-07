$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('conference-list', 'conference-list-application', 'menu-conference-list', 5, new ConferenceList()));
});

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

    var interval;

    var pollAndSet = function() {
        $.ajax({
            url: '/ajax/getConferenceList',
            dataType: 'json',
            cache: false,
            success: function(data, textStatus, xhr) {
                var tableRows = $('#conference-list-table tbody').find('tr');
                var serverList = data.results;
                var ids = [];

                for(var i = serverList.length - 1; i >= 0; i--) {
                    var found = false;
                    var serverItem = serverList[i];
                    for(var j = 0; j < tableRows.length; j++) {
                        var tr = $(tableRows[j]);
                        if(tr.attr('id') == 'conference-' + serverItem.id) {
                            updateMarkup(tr, serverItem, false);
                            found = true;
                            break;
                        }
                    }

                    if(!found) {
                        var html = '<tr id="conference-' + serverItem.id + '">';
                        html += '<td class="conference-description"></td>';
                        html += '<td class="conference-status"></td>';
        //                html += '<td class="conference-callers"></td>';
        //                html += '<td class="conference-duration"></td>';
                        html += '<td class="conference-view"></td>';
                        html += '</tr>';
        
                        var clone = $(html);
                        updateMarkup(clone, serverItem, true);
                        clone.css('opacity', 0);
                        $('#conference-list-table tbody').append(clone);
                        clone.animate({ opacity: 1 }, 1000);
                    }
        
                    ids.push('conference-' + serverItem.id);
                }
        
                for(var i = 0; i < tableRows.length; i++) {
                    var found = false;
                    var conference = $(tableRows[i]);
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
            }
        });
    };
    
    this.load = function() {
        pollAndSet();
        interval = setInterval(function() {
            pollAndSet();
        }, 1000);
        //$('#conference-list-window').show();
    };
    
    this.unload = function() {
        if(interval) {
            clearInterval(interval);
        }
    };
}

function viewConference(id) {
    var conference = new Conference(id);
    LISTEN.switchApp('conferencing', conference);
}