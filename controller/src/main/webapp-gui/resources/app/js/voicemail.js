$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('voicemail', 'voicemail-application', 'menu-voicemail', 2, new Voicemail()));
});

function Voicemail() {
    var interval;

    function updateMarkup(tr, data, setId) {
        if(setId) {
            tr.attr('id', 'voicemail-' + data.id);
        }

        if(data.isNew) {
            tr.addClass('voicemail-new');
        } else {
            tr.removeClass('voicemail-new');
        }

        var fromColumn = tr.find('.voicemail-cell-from');
        if(fromColumn.text() != data.leftBy) {
            fromColumn.text(data.leftBy);
        }

        var dateColumn = tr.find('.voicemail-cell-received');
        if(dateColumn.text() != data.dateCreated) {
            dateColumn.text(data.dateCreated);
        }

        var downloadColumn = tr.find('.voicemail-cell-download');
        var downloadHtml = '<a href="/ajax/downloadVoicemail?id=' + data.id + '">Download</a>';
        if(downloadColumn.html() != downloadHtml) {
            downloadColumn.html(downloadHtml);
        }
    };

    var pollAndSet = function() {
        $.ajax({
            url: '/ajax/getVoicemailList',
            dataType: 'json',
            cache: 'false',
            success: function(data, textStatus, xhr) {
                var tableRows = $('#voicemail-table tbody').find('tr');
                var serverList = data.results;
                var ids = [];

                for(var i = 0; i < serverList.length; i++) {
                    var found = false;
                    var serverItem = serverList[i];
                    for(var j = 0; j < tableRows.length; j++) {
                        var tableRow = $(tableRows[j]);
                        if(tableRow.attr('id') == 'voicemail-' + serverItem.id) {
                            updateMarkup(tableRow, serverItem, false);
                            found = true;
                            break;
                        }
                    }

                    if(!found) {
                        var clone = $('#voicemail-row-template').clone();
                        updateMarkup(clone, serverItem, true);
                        clone.css('opacity', 0);
                        var tbody = $('#voicemail-table tbody').prepend(clone);
                        clone.animate({ opacity: 1 }, 1000);
                    }

                    ids.push('voicemail-' + serverItem.id);
                }

                // remove table rows that no longer exist on the server
                for(var i = 0; i < tableRows.length; i++) {
                    var found = false;
                    var row = $(tableRows[i]);
                    for(var j = 0; j < ids.length; j++) {
                        if(row.attr('id') == ids[j]) {
                            found = true;
                            break;
                        }
                    }

                    if(!found) {
                        row.animate({ opacity: 0 }, 1000, function() {
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
    };

    this.unload = function() {
        if(interval) {
            clearInterval(interval);
        }
        $('#voicemail-table tbody').find('tr').remove();
    };
}