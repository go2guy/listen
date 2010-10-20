$(document).ready(function() {
    var displayingFullTranscription = [];
    Listen.Voicemail = function() {
        return {
            Application: function() {
                var interval;

                var dynamicTable = new Listen.DynamicTable({
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

                        var statusButton = '<button class="icon-' + (data.isNew ? 'unread' : 'read') + '" onclick="' + (data.isNew ? 'Server.markVoicemailReadStatus(' + data.id + ', true);' : 'Server.markVoicemailReadStatus(' + data.id + ', false);return false;') + '" title="' + (data.isNew ? 'Mark as old' : 'Mark as new') + '"></button>';
                        Listen.setFieldContent(row.find('.voicemail-cell-from'), '<div>' + statusButton + '</div><div>' + data.leftBy + '</div>', false, true);
                        Listen.setFieldContent(row.find('.voicemail-cell-received'), data.dateCreated, animate);
                        Listen.setFieldContent(row.find('.voicemail-cell-play'), data.duration, animate);

                        var downloadAction = '<a href="' + Listen.url('/ajax/downloadVoicemail?id=' + data.id) + '">Download</a>';
                        var deleteAction = '<a href="#" onclick="Listen.Voicemail.confirmDeleteVoicemail(' + data.id + ');return false;" title="Delete this voicemail">Delete</a>';
                        Listen.setFieldContent(row.find('.voicemail-cell-actions'), '<div>' + deleteAction + '</div><div>' + downloadAction + '</div>', false, true);

                        var transcriptionField = row.find('.voicemail-cell-transcription');
                        if(data.transcription != null && data.transcription.length > 0) {
                            var more = '<a href="#" onclick="Listen.Voicemail.toggleTranscription(' + data.id + ', \'full\');return false;" title="Show the entire transcription.">show&nbsp;&raquo;</a>';
                            var less = '<a href="#" onclick="Listen.Voicemail.toggleTranscription(' + data.id + ', \'abbr\');return false;" title="Hide the transcription.">&laquo;&nbsp;hide</a>';

                            var abbrField = row.find('.voicemail-cell-transcription-abbr');
                            var fullField = row.find('.voicemail-cell-transcription-full');
                            Listen.setFieldContent(abbrField, data.transcription.substring(0, 75) + '...&nbsp;' + more, false, true);
                            Listen.setFieldContent(fullField, data.transcription + '&nbsp;' + less, false, true);

                            var contains = false;
                            for(var i = 0; i < displayingFullTranscription.length; i++) {
                                if(displayingFullTranscription[i] == data.id) {
                                    contains = true;
                                    break;
                                }
                            }

                            if(contains) {
                                Listen.setFieldContent(transcriptionField, fullField.html(), false, true);
                            } else {
                                Listen.setFieldContent(transcriptionField, abbrField.html(), false, true);
                            }
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

            toggleTranscription: function(id, action) {
                Listen.trace('Listen.Voicemail.toggleTranscription');
                var row = $('#voicemail-table-row-' + id);
                var transcription = row.find('.voicemail-cell-transcription');
                var abbr = row.find('.voicemail-cell-transcription-abbr');
                var full = row.find('.voicemail-cell-transcription-full');
                if(action == 'full') {
                    displayingFullTranscription.push(id);
                    Listen.setFieldContent(transcription, full.html(), false, true);
                } else {
                    var newArr = [];
                    for(var i = 0; i < displayingFullTranscription.length; i++) {
                        if(displayingFullTranscription[i] != id) {
                            newArr.push(displayingFullTranscription[i]);
                        }
                    }
                    displayingFullTranscription = newArr;
                    Listen.setFieldContent(transcription, abbr.html(), false, true);
                }
            }
        }
    }();

    Listen.registerApp(new Listen.Application('voicemail', 'voicemail-application', 'menu-voicemail', new Listen.Voicemail.Application()));
});