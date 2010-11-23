$(document).ready(function() {

    $('#voicemail-bubble-new').click(function() {
        Listen.Voicemail.updateBubbleNew();
    });

    $('#voicemail-header .voicemail-cell-received a').click(function() {
        var indicator = $('#voicemail-header .voicemail-cell-received span');
        var ascending = !indicator.hasClass('sort-descending');

        indicator.removeClass(ascending ? 'sort-ascending' : 'sort-descending');
        indicator.addClass(ascending ? 'sort-descending' : 'sort-ascending');

        $('#voicemail-header .voicemail-cell-from span').removeClass('sort-ascending').removeClass('sort-descending');

        Listen.Voicemail.sort('received', ascending);
    });

    $('#voicemail-header .voicemail-cell-from a').click(function() {
        var indicator = $('#voicemail-header .voicemail-cell-from span');
        var ascending = !indicator.hasClass('sort-descending');

        indicator.removeClass(ascending ? 'sort-ascending' : 'sort-descending');
        indicator.addClass(ascending ? 'sort-descending' : 'sort-ascending');

        $('#voicemail-header .voicemail-cell-received span').removeClass('sort-ascending').removeClass('sort-descending');

        Listen.Voicemail.sort('from', ascending);
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
                        
                        var playAction = '<a href="#" onclick="Listen.Voicemail.playVoicemail(' + data.id + ', \'' + data.uri + '\');return false;">Play</a>';
                        var playerCell = $('#voicemail-table-row-' + data.id + ' .voicemail-cell-play');
                        
                        if(playerCell.html() == null)
                        {
                        	Listen.setFieldContent(row.find('.voicemail-cell-play'), '<div>' + data.duration + '</div><div class="playLink" id="playLink' + data.id + '">' + playAction + '</div>', false, true);
                        }

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
            
            playVoicemail: function(id, uri) {
            	Listen.trace('Listen.Voicemail.playVoicemail');
            	$('.playerDiv').remove();
            	$('.playLink').show();
            	var playerHtml = '<div class="playerDiv"><object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" width="165" height="37" id="player' + id + '" align=""><param name=movie value="./resources/audio/niftyplayer.swf?file=' + uri + '&as=1"><param name=quality value=high><param name=bgcolor value=#FFFFFF><embed src="./resources/audio/niftyplayer.swf?file=' + uri + '&as=1" quality=high bgcolor=#FFFFFF width="165" height="37" id="player' + id + '" name="player' + id + '" align="" type="application/x-shockwave-flash" swLiveConnect="true" pluginspage="http://www.macromedia.com/go/getflashplayer"></embed></object></div>';
            	$('#voicemail-table-row-' + id + ' .voicemail-cell-play').append(playerHtml);
            	$('#playLink' + id).hide()
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