$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('voicemail', 'voicemail-application', 'menu-voicemail', new Voicemail()));
});

function Voicemail() {
    var interval;

    var dynamicTable = new LISTEN.DynamicTable({
        tableId: 'voicemail-table',
        templateId: 'voicemail-row-template',
        retrieveList: function(data) {
            return data.list.results;
        },
        countContainer: 'voicemail-new-count',
        retrieveCount: function(data) {
            return data.newCount;
        },
        reverse: true,
        updateRowCallback: function(row, data, setId) {
            if(setId) {
                row.attr('id', 'voicemail-table-row-' + data.id);
            }

            if(data.isNew) {
                row.removeClass('voicemail-read');
                row.addClass('voicemail-unread');
            } else {
                row.removeClass('voicemail-unread');
                row.addClass('voicemail-read');
            }

            var readStatusCell = row.find('.voicemail-cell-readStatus');
            var buttonHtml = '<button class="' + (data.isNew ? 'mark-read-button' : 'mark-unread-button') + '" onclick="' + (data.isNew ? 'SERVER.markVoicemailReadStatus(' + data.id + ', true);' : 'SERVER.markVoicemailReadStatus(' + data.id + ', false);return false;') + '" title="' + (data.isNew ? 'Mark as read' : 'Mark as unread') + '"></button>';
            if(readStatusCell.html() != buttonHtml) {
                readStatusCell.html(buttonHtml);
            }

            var fromColumn = row.find('.voicemail-cell-from');
            if(fromColumn.text() != data.leftBy) {
                fromColumn.text(data.leftBy);
            }

            var dateColumn = row.find('.voicemail-cell-received');
            if(dateColumn.text() != data.dateCreated) {
                dateColumn.text(data.dateCreated);
            }

            var downloadColumn = row.find('.voicemail-cell-download');
            var downloadHtml = '<a href="/ajax/downloadVoicemail?id=' + data.id + '">Download</a>';
            if(downloadColumn.html() != downloadHtml) {
                downloadColumn.html(downloadHtml);
            }
        }
    });

    var pollAndSet = function(withAnimation) {
        $.ajax({
            url: '/ajax/getVoicemailList',
            dataType: 'json',
            cache: false,
            success: function(data, textStatus, xhr) {
                dynamicTable.update(data, withAnimation)
            }
        });
    };

    this.load = function() {
        LISTEN.log('Loading voicemail');
        pollAndSet(false);
        interval = setInterval(function() {
            pollAndSet(true);
        }, 1000);
    };

    this.unload = function() {
        LISTEN.log('Unloading voicemail');
        if(interval) {
            clearInterval(interval);
        }
        //$('#voicemail-table tbody').find('tr').remove();
    };
}