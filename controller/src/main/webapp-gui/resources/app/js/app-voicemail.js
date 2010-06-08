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
            tr.removeClass('voicemail-read');
            tr.addClass('voicemail-unread');
        } else {
            tr.removeClass('voicemail-unread');
            tr.addClass('voicemail-read');
        }

        var readStatusCell = tr.find('.voicemail-cell-readStatus');
        var buttonHtml = '<button class="' + (data.isNew ? 'mark-read-button' : 'mark-unread-button') + '" onclick="' + (data.isNew ? 'SERVER.markVoicemailReadStatus(' + data.id + ', true);' : 'SERVER.markVoicemailReadStatus(' + data.id + ', false);return false;') + '" title="' + (data.isNew ? 'Mark as read' : 'Mark as unread') + '"></button>';
        if(readStatusCell.html() != buttonHtml) {
            readStatusCell.html(buttonHtml);
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
                var serverList = data.list.results;
                var ids = [];

                var countSpan = $('#voicemail-new-count');
                if(countSpan.text() != data.newCount) {
                    countSpan.text(data.newCount);
                }

                for(var i = serverList.length - 1; i >= 0; i--) {
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
                        //clone.addClass((serverList.length - i) % 2 == 0 ? 'odd' : 'even');
                        $('#voicemail-table tbody').prepend(clone);
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