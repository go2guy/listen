$(document).ready(function() {

    Listen.Voicemail = function() {
        return {
            Application: function() {
                var interval;
                var dynamicTable = new Listen.DynamicTable({
                    url: '/ajax/getVoicemailList',
                    tableId: 'voicemail-table',
                    templateId: 'voicemail-row-template',
                    retrieveList: function(data) {
                        return data.results;
                    },
                    countContainer: 'voicemail-new-count',
                    retrieveCount: function(data) {
                        return data.newCount;
                    },
                    reverse: true,
                    paginationId: 'voicemail-pagination',
                    updateRowCallback: function(row, data, animate) {
                        if(data.isNew) {
                            row.removeClass('voicemail-read');
                            row.addClass('voicemail-unread');
                        } else {
                            row.removeClass('voicemail-unread');
                            row.addClass('voicemail-read');
                        }

                        Listen.setFieldContent(row.find('.voicemail-cell-readStatus'), '<button class="icon-' + (data.isNew ? 'unread' : 'read') + '" onclick="' + (data.isNew ? 'SERVER.markVoicemailReadStatus(' + data.id + ', true);' : 'SERVER.markVoicemailReadStatus(' + data.id + ', false);return false;') + '" title="' + (data.isNew ? 'Mark as old' : 'Mark as new') + '"></button>', false, true);
                        Listen.setFieldContent(row.find('.voicemail-cell-from'), data.leftBy, animate);
                        Listen.setFieldContent(row.find('.voicemail-cell-received'), data.dateCreated, animate);
                        Listen.setFieldContent(row.find('.voicemail-cell-duration'), data.duration, animate);
                        Listen.setFieldContent(row.find('.voicemail-cell-download'), '<a href="/ajax/downloadVoicemail?id=' + data.id + '">Download</a>', false, true);
                    }
                });

                this.load = function() {
                    Listen.trace('Loading voicemail');
                    dynamicTable.pollAndSet(false);
                    interval = setInterval(function() {
                        dynamicTable.pollAndSet(true);
                    }, 1000);
                };

                this.unload = function() {
                    Listen.trace('Unloading voicemail');
                    if(interval) {
                        clearInterval(interval);
                    }
                };
            }
        }
    }();

    Listen.registerApp(new Listen.Application('voicemail', 'voicemail-application', 'menu-voicemail', new Listen.Voicemail.Application()));
});