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

                        Listen.setFieldContent(row.find('.voicemail-cell-readStatus'), '<button class="icon-' + (data.isNew ? 'unread' : 'read') + '" onclick="' + (data.isNew ? 'Server.markVoicemailReadStatus(' + data.id + ', true);' : 'Server.markVoicemailReadStatus(' + data.id + ', false);return false;') + '" title="' + (data.isNew ? 'Mark as old' : 'Mark as new') + '"></button>', false, true);
                        Listen.setFieldContent(row.find('.voicemail-cell-from'), data.leftBy, animate);
                        Listen.setFieldContent(row.find('.voicemail-cell-received'), data.dateCreated, animate);
                        Listen.setFieldContent(row.find('.voicemail-cell-duration'), data.duration, animate);
                        Listen.setFieldContent(row.find('.voicemail-cell-download'), '<a href="/ajax/downloadVoicemail?id=' + data.id + '">Download</a>', false, true);
                        Listen.setFieldContent(row.find('.voicemail-cell-deleteButton'), '<button type="button" class="icon-delete" onclick="Listen.Voicemail.confirmDeleteVoicemail(' + data.id + ');" title="Delete this voicemail"></button>', false, true);
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
            },

            confirmDeleteVoicemail: function(id) {
                Listen.trace('Listen.Voicemail.confirmDeleteVoicemail');
                if(confirm('Are you sure?')) {
                    Listen.Voicemail.deleteVoicemail(id);
                }
            },

            deleteVoicemail: function(id) {
                Listen.trace('Listen.Voicemail.deleteVoicemail');
                Server.post({
                    url: '/ajax/deleteVoicemail',
                    properties: { id: id }
                });
            }
        }
    }();

    Listen.registerApp(new Listen.Application('voicemail', 'voicemail-application', 'menu-voicemail', new Listen.Voicemail.Application()));
});