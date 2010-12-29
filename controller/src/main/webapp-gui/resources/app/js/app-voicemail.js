var interact = interact || {};
var Voicemail;
$(document).ready(function() {
    $('#bubble-new').click(function() {
        Voicemail.updateBubbleNew();
    });

    $('#voicemail-header .received a').click(function() {
        var indicator = $('#voicemail-header .received span');
        var ascending = !indicator.hasClass('sort-descending');

        indicator.removeClass(ascending ? 'sort-ascending' : 'sort-descending');
        indicator.addClass(ascending ? 'sort-descending' : 'sort-ascending');

        $('#voicemail-header .from span').removeClass('sort-ascending').removeClass('sort-descending');

        Voicemail.sort('received', ascending);
    });

    $('#voicemail-header .from a').click(function() {
        var indicator = $('#voicemail-header .from span');
        var ascending = !indicator.hasClass('sort-descending');

        indicator.removeClass(ascending ? 'sort-ascending' : 'sort-descending');
        indicator.addClass(ascending ? 'sort-descending' : 'sort-ascending');

        $('#voicemail-header .received span').removeClass('sort-ascending').removeClass('sort-descending');

        Voicemail.sort('from', ascending);
    });

    Voicemail = function() {
        var dynamicTable, interval;
        return {
            Application: function() {
                dynamicTable = new interact.util.DynamicTable({
                    url: interact.listen.url('/ajax/getVoicemailList'),
                    tableId: 'voicemail-list',
                    templateId: 'voicemail-template',
                    retrieveList: function(data) {
                        return data.results;
                    },
                    countContainer: 'voicemail-new-count', // FIXME the count container needs to have the voicemail count even if they're not looking at the voicemail application
                    retrieveCount: function(data) {
                        return data.newCount;
                    },
                    reverse: true,
                    isList: true,
                    paginationId: 'pagination',
                    updateRowCallback: function(row, data, animate) {
                        if(data.isNew) {
                            row.removeClass('old-voicemail');
                            row.addClass('new-voicemail');
                        } else {
                            row.removeClass('new-voicemail');
                            row.addClass('old-voicemail');
                        }

                        // status button
                        var statusButton = row.find('.icon-unread, .icon-read');
                        statusButton.unbind('click').removeClass().addClass(data.isNew ? 'icon-unread' : 'icon-read');
                        statusButton.attr('title', data.isNew ? 'Mark as old' : 'Mark as new');
                        if(data.isNew) {
                            statusButton.click(function() {
                                Voicemail.markVoicemailNew(data.id);
                                return false;
                            });
                        } else {
                            statusButton.click(function() {
                                Voicemail.markVoicemailOld(data.id);
                                return false;
                            });
                        }

                        interact.util.setFieldContent(row.find('.from'), '<div>' + data.leftBy + '</div>', false, true);
                        interact.util.setFieldContent(row.find('.received'), data.dateCreated, animate);
                        
                        var playAction = '<a href="#" onclick="Voicemail.playVoicemail(' + data.id + ', \'' + data.uri + '\');return false;">Play</a>';
                        var playerCell = $('#voicemail-list-row-' + data.id + ' .play');
                        
                        if(playerCell.html() == null) {
                            interact.util.trace('setting player cell contents to link');
                        	interact.util.setFieldContent(row.find('.play'), '<div>' + data.duration + '</div><div class="playLink" id="playLink' + data.id + '">' + playAction + '</div>', false, true);
                        }

                        // download, delete buttons
                        var deleteButton = row.find('.icon-delete');
                        deleteButton.unbind('click').click(function() {
                            Voicemail.confirmDeleteVoicemail(data.id);
                            return false;
                        });
                        
                        var downloadButton = row.find('.icon-view');
                        downloadButton.unbind('click').click(function() {
                            var url = interact.listen.url('/ajax/downloadVoicemail?id=' + data.id);
                            window.location = url;
                            return false;
                        });
                        
                        var transcriptionField = row.find('.transcription-bubble');
                        if(data.transcription != null && data.transcription.length > 0) {
                            interact.util.setFieldContent(transcriptionField, data.transcription, false, true);
                            transcriptionField.css('display', 'block');
                        } else {
                            transcriptionField.css('display', 'none');
                        }
                    },
                    updateFinishedCallback: function(data) {
                        if(data.results.length == 0) {
                            $('#voicemail-header').hide();
                            $('#pagination').hide();
                        } else {
                            $('#voicemail-header').show();
                            $('#pagination').show();
                        }
                    }
                });

                // TODO don't really need a separate "load()" anymore, do we?
                this.load = function() {
                    interact.util.trace('Loading voicemail');
                    dynamicTable.pollAndSet(false);
                    interval = setInterval(function() {
                        dynamicTable.pollAndSet(true);
                    }, 1000);
                };

//                this.unload = function() {
//                    interact.util.trace('Unloading voicemail');
//                    if(interval) {
//                        clearInterval(interval);
//                    }
//                };
            },

            confirmDeleteVoicemail: function(id) {
                interact.util.trace('Voicemail.confirmDeleteVoicemail');
                if(confirm('Are you sure?')) {
                    Voicemail.deleteVoicemail(id);
                }
            },

            deleteVoicemail: function(id) {
                interact.util.trace('Voicemail.deleteVoicemail');
                Server.post({
                    url: interact.listen.url('/ajax/deleteVoicemail'),
                    properties: { id: id }
                });
            },

            markVoicemailNew: function(id) {
                Server.post({
                    url: interact.listen.url('/ajax/markVoicemailReadStatus'),
                    properties: { id: id, readStatus: true},
                    successCallback: function() {
                        Voicemail.updateBubbleNew();
                    }
                });
            },
            
            markVoicemailOld: function(id) {
                Server.post({
                    url: interact.listen.url('/ajax/markVoicemailReadStatus'),
                    properties: {id: id, readStatus: false },
                    successCallback: function() {
                        Voicemail.updateBubbleNew();
                    }
                });
            },
            
            playVoicemail: function(id, uri) {
            	interact.util.trace('Voicemail.playVoicemail');
            	$('.playerDiv').remove();
            	$('.playLink').show();
            	var playerHtml = '<div class="playerDiv"><object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" width="165" height="37" id="player' + id + '" align=""><param name=movie value="./resources/audio/niftyplayer.swf?file=' + uri + '&as=1"><param name=quality value=high><param name=bgcolor value=#FFFFFF><embed src="./resources/audio/niftyplayer.swf?file=' + uri + '&as=1" quality=high bgcolor=#FFFFFF width="165" height="37" id="player' + id + '" name="player' + id + '" align="" type="application/x-shockwave-flash" swLiveConnect="true" pluginspage="http://www.macromedia.com/go/getflashplayer"></embed></object></div>';
            	$('#voicemail-list-row-' + id + ' .play').append(playerHtml);
            	$('#playLink' + id).hide()
			},

            updateBubbleNew: function() {
                // stop polling (ugly)

                if(interval) {
                    clearInterval(interval);
                }

                dynamicTable.clear();

                var bubble = $('#bubble-new').is(':checked');
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
    
    new Voicemail.Application().load();
});