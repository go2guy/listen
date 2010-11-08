$(document).ready(function() {

    $('#voicemail-bubble-new').click(function() {
        Listen.Voicemail.updateBubbleNew();
    });

    $('#voicemail-header .voicemail-cell-received a').click(function() {
        var indicator = $('#voicemail-header .voicemail-cell-received span');
        var ascending = !indicator.hasClass('sort-descending');

        indicator.removeClass(ascending ? 'sort-ascending' : 'sort-descending');
        indicator.addClass(ascending ? 'sort-descending' : 'sort-ascending');

        Listen.Voicemail.sort('received', ascending);
    });

    Listen.Voicemail = function() {
        var dynamicTable, interval;
        return {
            Application: function() {
                dynamicTable = new Listen.DynamicTable({
                    url: Listen.url('/ajax/getVoicemailList'),
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
                    isList: true,
                    paginationId: 'voicemail-pagination',
                    updateRowCallback: function(row, data, animate) {
                        if(data.isNew) {
                            row.removeClass('voicemail-read');
                            row.addClass('voicemail-unread');
                        } else {
                            row.removeClass('voicemail-unread');
                            row.addClass('voicemail-read');
                        }

                        var statusButton = '<button class="icon-' + (data.isNew ? 'unread' : 'read') + '" onclick="' + (data.isNew ? 'Listen.Voicemail.markVoicemailNew(' + data.id + ');' : 'Listen.Voicemail.markVoicemailOld(' + data.id + ');return false;') + '" title="' + (data.isNew ? 'Mark as old' : 'Mark as new') + '"></button>';
                        Listen.setFieldContent(row.find('.voicemail-cell-from'), '<div>' + statusButton + '</div><div>' + data.leftBy + '</div>', false, true);
                        Listen.setFieldContent(row.find('.voicemail-cell-received'), data.dateCreated, animate);
                        Listen.setFieldContent(row.find('.voicemail-cell-play'), data.duration, animate);

                        var downloadAction = '<a href="' + Listen.url('/ajax/downloadVoicemail?id=' + data.id) + '">Download</a>';
                        var deleteAction = '<a href="#" onclick="Listen.Voicemail.confirmDeleteVoicemail(' + data.id + ');return false;" title="Delete this voicemail">Delete</a>';
                        Listen.setFieldContent(row.find('.voicemail-cell-actions'), '<div>' + deleteAction + '</div><div>' + downloadAction + '</div>', false, true);

                        var transcriptionField = row.find('.voicemail-cell-transcription');
                        if(data.transcription != null && data.transcription.length > 0) {
                            Listen.setFieldContent(transcriptionField, data.transcription, false, true);
                            transcriptionField.css('display', 'block');
                        } else {
                            transcriptionField.css('display', 'none');
                        }
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
                    url: Listen.url('/ajax/deleteVoicemail'),
                    properties: { id: id }
                });
            },

            markVoicemailNew: function(id) {
                Server.post({
                    url: Listen.url('/ajax/markVoicemailReadStatus'),
                    properties: { id: id, readStatus: true},
                    successCallback: function() {
                        Listen.Voicemail.updateBubbleNew();
                    }
                });
            },
            
            markVoicemailOld: function(id) {
                Server.post({
                    url: Listen.url('/ajax/markVoicemailReadStatus'),
                    properties: {id: id, readStatus: false },
                    successCallback: function() {
                        Listen.Voicemail.updateBubbleNew();
                    }
                });
            },

            updateBubbleNew: function() {
                // stop polling (ugly)

                if(interval) {
                    clearInterval(interval);
                }

                dynamicTable.clear();

                var bubble = $('#voicemail-bubble-new').is(':checked');
                dynamicTable.setUrl('/ajax/getVoicemailList?bubbleNew=' + (bubble ? 'true' : 'false'));

                // start polling (ugly)

                dynamicTable.pollAndSet(false);
                interval = setInterval(function() {
                    dynamicTable.pollAndSet(true);
                }, 1000);
            },
            
            sort: function(field, ascending) {
                if(interval) {
                    clearInterval(interval);
                }
                
                dynamicTable.clear();
                
                dynamicTable.setQueryParameter('sort', field);
                dynamicTable.setQueryParameter('order', ascending ? 'ascending' : 'descending');
                dynamicTable.setReverse(!ascending);

                dynamicTable.pollAndSet(false);
                interval = setInterval(function() {
                    dynamicTable.pollAndSet(true);
                }, 1000);
            }
        }
    }();

    Listen.registerApp(new Listen.Application('voicemail', 'voicemail-application', 'menu-voicemail', new Listen.Voicemail.Application()));
});