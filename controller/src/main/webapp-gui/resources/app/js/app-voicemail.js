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
        updateRowCallback: function(row, data) {
            if(data.isNew) {
                row.removeClass('voicemail-read');
                row.addClass('voicemail-unread');
            } else {
                row.removeClass('voicemail-unread');
                row.addClass('voicemail-read');
            }

            LISTEN.setFieldContent(row.find('.voicemail-cell-readStatus'), '<button class="icon-' + (data.isNew ? 'unread' : 'read') + '" onclick="' + (data.isNew ? 'SERVER.markVoicemailReadStatus(' + data.id + ', true);' : 'SERVER.markVoicemailReadStatus(' + data.id + ', false);return false;') + '" title="' + (data.isNew ? 'Mark as read' : 'Mark as unread') + '"></button>', false, true);
            LISTEN.setFieldContent(row.find('.voicemail-cell-from'), data.leftBy);
            LISTEN.setFieldContent(row.find('.voicemail-cell-received'), data.dateCreated);
            LISTEN.setFieldContent(row.find('.voicemail-cell-download'), '<a href="/ajax/downloadVoicemail?id=' + data.id + '">Download</a>', false, true);
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
    };
}