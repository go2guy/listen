var interact = interact || {};
$(document).ready(function() {
    var application = new SystemConfigurationApplication();
    application.load();

    function SystemConfigurationApplication() {
        this.load = function() {
            interact.util.trace('Loading system configuration');
            var start = interact.util.timestamp();
            $.ajax({
                url: interact.listen.url('/ajax/getProperties'),
                dataType: 'json',
                cache: false,
                success: function(data, textStatus, xhr) {
                    $('#smtp-server').val(data['com.interact.listen.mail.smtpHost']);
                    $('#smtp-username').val(data['com.interact.listen.mail.smtpUsername']);
                    $('#smtp-password').val(data['com.interact.listen.mail.smtpPassword']);
                    $('#from-address').val(data['com.interact.listen.mail.fromAddress']);

                    clearAllDnisRows();
                    var dnis = data['com.interact.listen.dnisMapping'];
                    var mappings = dnis.split(';');
                    for(var i = 0; i < mappings.length; i++) {
                        var mapping = mappings[i].split(':');
                        addDnisRow(mapping[0], mapping[1]);
                    }

                    $('#conferencing-configuration-pinLength').val(data['com.interact.listen.conferencing.pinLength']);
                    clearAllConferenceBridgeRows();
                    var bridgeNumbers = data['com.interact.listen.conferenceBridges'];
                    var mappings = bridgeNumbers.split(';');
                    if(mappings != "") {
                    	for(var i = 0; i < mappings.length; i++) {
	                        var mapping = mappings[i].split(':');
                        	addConferenceBridgeRow(mapping[0], mapping[1]);
                    	}
                    }
                    
                    var mailboxSelect = $('#direct-voicemail-access-number-select');
                    mailboxSelect.empty();
                    var mailboxNumbers = getMailboxNumbers();
                    var selectedDirectVoicemailNumber = data['com.interact.listen.directVoicemailNumber'];
                    populateSelectList(mailboxSelect, mailboxNumbers, selectedDirectVoicemailNumber);
                    
                    $('#alerts-configuration-realizeUrl').val(data['com.interact.listen.realizeUrl']);
                    $('#alerts-configuration-realizeAlertName').val(data['com.interact.listen.realizeAlertName']);

                    $('#sysconfig-authentication-activeDirectoryEnabled').attr('checked', data['com.interact.listen.activeDirectory.enabled'] == "true" ? true : false);
                    $('#sysconfig-authentication-activeDirectoryServer').val(data['com.interact.listen.activeDirectory.server']);
                    $('#sysconfig-authentication-activeDirectoryDomain').val(data['com.interact.listen.activeDirectory.domain']);
                    toggleActiveDirectoryFields();

                    $('#sysconfig-c2dm-enabled').attr('checked', data['com.interact.listen.google.c2dm.enabled'] == "true" ? true : false);
                    $('#sysconfig-google-account').val(data['com.interact.listen.google.username']);
                    $('#sysconfig-google-password').val('');
                    $('#sysconfig-google-authtoken').val(data['com.interact.listen.google.authToken']);
                    toggleAndroidFields();

                    // SPOT systems
                    var spotSystemTable = $('#sysconfig-spot-systems');
                    $('tr', spotSystemTable).remove(); // delete all rows

                    var spotSystems = data['com.interact.listen.spotSystems'].split(',');
                    if(spotSystems.length > 0) {
                        for(var i = 0; i < spotSystems.length; i++) {
                          if(spotSystems[i] != '') {
                            var row = $('<tr><td>' + spotSystems[i] + '</td><td><button type="button" class="icon-delete" title="Unregister this SPOT System" onclick="deleteSpotSystem(\'' + spotSystems[i] + '\');return false;">');
                            $('tbody', spotSystemTable).append(row);
                          }
                        }
                    }
                },
                complete: function(xhr, textStatus) {
                    var elapsed = interact.util.timestamp() - start;
                    $('#latency').text(elapsed);
                }
            });
        };
    };

    function clearAllDnisRows() {
        $('#dnis-mapping-form tbody tr').not(':last').remove();
    };

    function addDnisRow(number, destination) {
        var n = (number ? number : '');
        var d = (destination ? destination : '');

        var clone = $('#dnis-row-template').clone();
        $('.dnis-mapping-number', clone).val(n);

        $('#dnis-mapping-form tbody tr:last').before(clone);
        if(d != 'voicemail' && d != 'mailbox' && d != 'conferencing' && d != 'directVoicemail') { // assume custom
            $('select option[value=\'custom\']', clone).attr('selected', 'selected');
            $('.dnis-mapping-custom-destination', clone).val(d).show();
        } else {
            $('select option[value=\'' + d + '\']', clone).attr('selected', 'selected');
        }
        $('.icon-delete', clone).click(function() {
            $(this).parent().parent().remove();
        });
        $('select', clone).change(function() {
            var input = $('.dnis-mapping-custom-destination', $(this).parent().parent());
            if($(this).val() == 'custom') {
                input.show();
            } else {
                input.hide().val('');
            }
        });
    }

    $('#add-dnis-mapping').click(function() {
        addDnisRow();
        return false;
    });

    $('#notifications-form').submit(function() {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/setProperties'),
            data: { 'com.interact.listen.mail.smtpHost': $('#smtp-server').val(),
                    'com.interact.listen.mail.smtpUsername': $('#smtp-username').val(),
                    'com.interact.listen.mail.smtpPassword': $('#smtp-password').val(),
                    'com.interact.listen.mail.fromAddress': $('#from-address').val(),
                    'com.interact.listen.directVoicemailNumber': $('#direct-voicemail-access-number-select').val() },
            success: function(data) {
                application.load();
                interact.listen.notifySuccess('Notification settings updated');
            },
            error: function(xhr) {
                interact.listen.notifyError(xhr.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
        return false;
    });

    $('#dnis-mapping-form').submit(function() {
        var value = '';
        var rows = $('#dnis-mapping-form tr');
        var num = 0;
        for(var i = 0; i < rows.length - 1; i++) {
            var entry = '';
            var number = $('.dnis-mapping-number', rows[i]).val();
            if(number.length == 0) {
                continue;
            }
            var destination = $('select', rows[i]).val();
            entry += number + ':';
            if(destination != 'voicemail' && destination != 'mailbox' && destination != 'conferencing' && destination != 'directVoicemail') {
                var customVal = $('.dnis-mapping-custom-destination', rows[i]).val();
                if(customVal == '') {
                    continue;
                }
                entry += customVal;
            } else {
                entry += destination;
            }
            entry += ';';
            value += entry;
            num++;
        }
        if(num > 0 && value.length > 0) {
            value = value.substring(0, value.length - 1); // remove last semicolon
        }

        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/setProperties'),
            data: { 'com.interact.listen.dnisMapping': value },
            success: function(data) {
                application.load();
                interact.listen.notifySuccess('Phone numbers updated');
            },
            error: function(xhr) {
                interact.listen.notifyError(xhr.responseText);
            },
            complete: function(xhr, textStatus) {
            	var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
                updateSelectLists();
            }
        });

        return false;
    });
    
    function updateSelectLists() {
    	var conferenceNumbers = getConferenceNumbers();
    	var mailboxNumbers = getMailboxNumbers();
    	
    	$('#conferencing-configuration-form select').each(function() {
    		var selected = $('option:selected', this).val();
    		$(this).empty();
    		populateSelectList($(this), conferenceNumbers, selected);
    	});
    	
		var mailboxSelect = $('#direct-voicemail-access-number-select');
		var selected = $('option:selected', mailboxSelect).val();
		mailboxSelect.empty();
		populateSelectList(mailboxSelect, mailboxNumbers, selected);
    }
    
    function getConferenceNumbers() {
    	var conferenceNumbers = [];
    	$('#dnis-mapping-form table tbody tr').each(function() {
    		if($('.dnis-row-select', $(this)).val() == 'conferencing')
    		{
    			conferenceNumbers.push($('input', $(this)).val());
    		}
    	});
    	
    	return conferenceNumbers;
    }
    
    function getMailboxNumbers() {
    	var mailboxNumbers = [];
    	$('#dnis-mapping-form table tbody tr').each(function() {
    		if($('.dnis-row-select', $(this)).val() == 'mailbox')
    		{
    			mailboxNumbers.push($('input', $(this)).val());
    		}
    	});
    	
    	return mailboxNumbers;
    }
    
    function populateSelectList(selectList, listOfItems, selectedItem) {
    	$.each(listOfItems, function(index, value) {   
     		$(selectList).
          		append($("<option></option>").
          		attr("value",value).
          		text(value));
		});
		
		if($.inArray(selectedItem, listOfItems) != -1)
        {
			selectList.val(selectedItem);
		}
    }
    
    function clearAllConferenceBridgeRows() {
        $('#conferencing-bridge-numbers tr').not(':last').remove();
    };
    
    function addConferenceBridgeRow(number, label) {
    	var conferenceNumbers = getConferenceNumbers();
    	var n = (number ? number : '');
        var l = (label ? label : '');

        var clone = $('#conference-bridge-row-template').clone();
        clone.removeAttr("id");
        $('.conference-bridge-number-label', clone).val(l);
        $.each(conferenceNumbers, function(index, value) {
        	$('select', clone).
          		append($("<option></option>").
          		attr("value",value).
          		text(value)); 
		});

        $('#conferencing-configuration-form tbody tr:last').before(clone);
        $("select option[value='" + n + "']", clone).attr('selected', 'selected');
        
        $('.icon-delete', clone).click(function() {
            $(this).parent().parent().remove();
        });
    }
    
    $('#add-conference-bridge-number').click(function() {
        addConferenceBridgeRow();
        return false;
    });

    $('#conferencing-configuration-form').submit(function() {
        var value = '';
        var rows = $('#conferencing-bridge-numbers tr');
        var num = 0;
        for(var i = 0; i < rows.length - 1; i++) {
        	var entry = '';
            var number = $('select', rows[i]).val();
            if(number.length == 0) {
                continue;
            }
            var label = $('.conference-bridge-number-label', rows[i]).val();
            entry += number + ':' + label + ';';
            value += entry;
            num++;
        }
        if(num > 0 && value.length > 0) {
            value = value.substring(0, value.length - 1); // remove last semicolon
        }
        
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/setProperties'),
            data: { 'com.interact.listen.conferencing.pinLength': $('#conferencing-configuration-pinLength').val(),
                    'com.interact.listen.conferenceBridges': value },
            success: function(data) {
                application.load();
                interact.listen.notifySuccess('Conferencing settings updated');
            },
            error: function(xhr) {
                interact.listen.notifyError(xhr.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
        return false;
    });

    $('#alerts-configuration-form').submit(function() {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/setProperties'),
            data: { 'com.interact.listen.realizeUrl': $('#alerts-configuration-realizeUrl').val(),
                    'com.interact.listen.realizeAlertName': $('#alerts-configuration-realizeAlertName').val() },
            success: function(data) {
                application.load();
                interact.listen.notifySuccess('Alert settings updated');
            },
            error: function(xhr) {
                interact.listen.notifyError(xhr.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
        return false;
    });

    $('#sysconfig-authentication-form').submit(function() {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/setProperties'),
            data: { 'com.interact.listen.activeDirectory.enabled': $('#sysconfig-authentication-activeDirectoryEnabled').attr('checked'),
                    'com.interact.listen.activeDirectory.server': $('#sysconfig-authentication-activeDirectoryServer').val(),
                    'com.interact.listen.activeDirectory.domain': $('#sysconfig-authentication-activeDirectoryDomain').val() },
            success: function(data) {
                application.load();
                interact.listen.notifySuccess('Authentication settings updated');
            },
            error: function(xhr) {
                interact.listen.notifyError(xhr.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
        return false;
    });
    
    $('#sysconfig-google-form').submit(function() {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/setGoogleAuth'),
            data: { 'com.interact.listen.google.c2dm.enabled': $('#sysconfig-c2dm-enabled').attr('checked'),
                    'com.interact.listen.google.username': $('#sysconfig-google-account').val(),
                    'com.interact.listen.google.password': $('#sysconfig-google-password').val(),
                    'com.interact.listen.google.authToken': $('#sysconfig-google-authtoken').val() },
            success: function(data) {
                application.load();
                interact.listen.notifySuccess('Google account settings updated');
            },
            error: function(xhr) {
                interact.listen.notifyError(xhr.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
        return false;
    });

    function toggleActiveDirectoryFields() {
        var elem = $('#sysconfig-authentication-activeDirectoryEnabled');
        if(elem.is(':checked')) {
            $('#sysconfig-authentication-activeDirectoryServer').removeAttr('readonly').removeClass('disabled');
            $('#sysconfig-authentication-activeDirectoryDomain').removeAttr('readonly').removeClass('disabled');
        } else {
            $('#sysconfig-authentication-activeDirectoryServer').attr('readonly', true).addClass('disabled');
            $('#sysconfig-authentication-activeDirectoryDomain').attr('readonly', true).addClass('disabled');
        }
    }

    $('#sysconfig-authentication-activeDirectoryEnabled').click(toggleActiveDirectoryFields);

    function toggleAndroidFields() {
        var elem = $('#sysconfig-c2dm-enabled');
        if(elem.is(':checked')) {
            $('#sysconfig-google-account').removeAttr('readonly').removeClass('disabled');
            $('#sysconfig-google-password').removeAttr('readonly').removeClass('disabled');
            $('#sysconfig-google-authtoken').removeAttr('readonly').removeClass('disabled');
        } else {
            $('#sysconfig-google-account').attr('readonly', true).addClass('disabled');
            $('#sysconfig-google-password').attr('readonly', true).addClass('disabled');
            $('#sysconfig-google-authtoken').attr('readonly', true).addClass('disabled');
        }
    }

    $('#sysconfig-c2dm-enabled').click(toggleAndroidFields);

});

// TODO remove this from the global scope. ideally, the system configuration app should be structured like the others
function deleteSpotSystem(system) {
    var rows = $('#sysconfig-spot-systems tbody tr');
    var systems = '';

    for(var i = 0; i < rows.length; i++) {
        var s = $('td:first-child', rows[i]).text();
        if(s === system) {
            $(rows[i]).remove();
            continue;
        }
        if(systems != '') {
            systems += ',';
        }
        systems += s;
    }

    Server.post({
        url: interact.listen.url('/ajax/setProperties'),
        properties: {
            'com.interact.listen.spotSystems': systems
        }
    });
}