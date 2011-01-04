var interact = interact || {};
var Profile;
$(document).ready(function() {

    $('#profile-form').submit(function() {
        Profile.editSubscriber();
        return false;
    });

    $('#profile-form-testEmail-button').click(function() {
        Profile.testEmailAddress();
        return false;
    });

    $('#profile-form-testSms-button').click(function() {
        Profile.testSmsAddress();
        return false;
    });

    $('#profile-form-addAccessNumber').click(function() {
        Profile.addAccessNumberRow();
    });

    $('#pager-form').submit(function() {
        Profile.editPagerInfo();
        return false;
    });
    

    Profile = function() {
        return {
            Application: function() {
                interact.util.trace('Profile.Application [construct]');                
                
                this.load = function() {
                    interact.util.trace('Loading profile');
                    $.ajax({
                        url: interact.listen.url('/ajax/getSubscriber'),
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                            $('#profile-form-id').val(data.id);
                            $('#profile-form-username').val(data.username);
                            $('#profile-form-accountType').text(data.isActiveDirectory ? 'Active Directory' : 'Local');
                            $('#profile-form-realName').val(data.realName);
                            $('#profile-form-workEmailAddress').val(data.workEmailAddress);

                            Profile.clearAllAccessNumberRows();
                            for(var i = 0; i < data.accessNumbers.length; i++) {
                                Profile.addAccessNumberRow(data.accessNumbers[i].number, data.accessNumbers[i].messageLight, data.accessNumbers[i].numberType, data.accessNumbers[i].publicNumber);
                            }
                            
                            if(data.voicemailPin !== null) {
                                $('#profile-form-voicemailPin').val(data.voicemailPin);
                            }
                            $('#profile-form-emailAddress').val(data.emailAddress);
                            $('#profile-form-smsAddress').val(data.smsAddress);
                            
                            if(data.enableEmail) {                        
                                $('#profile-form-enableEmailNotification').attr('checked', true);
                            }
                            
                            if(data.enableSms) {                        
                                $('#profile-form-enableSmsNotification').attr('checked', true);
                            }
                            
                            if(data.enablePaging) {                        
                                $('#profile-form-paging').attr('checked', true);
                            }
                            if(data.enableTranscription) {                        
                                $('#profile-form-transcription').attr('checked', true);
                            }
                            $('#profile-form-voicemailPlaybackOrder').val(data.voicemailPlaybackOrder);
                            $('#profile-form-edit-button').show();

                            $('#pager-form-number').text(data.pagerNumber);
                            
                            if(data.pagerAlternateNumber != '')
                            {
                                var fullAlternateNumber = data.pagerAlternateNumber.split("@");
                                var alternateNumber = fullAlternateNumber[0];
                                var alternateAddress = fullAlternateNumber[1];
                                
                                $('#pager-form-alternate-number').val(alternateNumber);
                                $('#pager-form-alternate-address').val(alternateAddress);
                            }
                            
                            $('#pager-form-page-prefix').val(data.pagePrefix);
                        }
                    });
                };
            },
            
            editSubscriber: function() {
                interact.util.trace('Profile.editSubscriber');
                Profile.disableButtons();
                Server.post({
                    url: interact.listen.url('/ajax/editSubscriber'),
                    properties: {
                        id: $('#profile-form-id').val(),
                        username: $('#profile-form-username').val(),
                        password: $('#profile-form-password').val(),
                        confirmPassword: $('#profile-form-confirmPassword').val(),
                        realName: $('#profile-form-realName').val(),
                        workEmailAddress: $('#profile-form-workEmailAddress').val(),
                        accessNumbers: Profile.buildAccessNumberString(),
                        voicemailPin: $('#profile-form-voicemailPin').val(),
                        enableEmail: $('#profile-form-enableEmailNotification').is(":checked"),
                        enableSms: $('#profile-form-enableSmsNotification').is(":checked"),
                        emailAddress: $('#profile-form-emailAddress').val(),
                        smsAddress: $('#profile-form-smsAddress').val(),
                        enablePaging: $('#profile-form-paging').is(":checked"),
                        enableTranscription: $('#profile-form-transcription').is(":checked"),
                        voicemailPlaybackOrder: $('#profile-form-voicemailPlaybackOrder').val()
                    },
                    successCallback: function() {
                        interact.listen.notifySuccess('Profile updated');
                        Profile.enableButtons();
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },
            
            editPagerInfo: function() {
                interact.util.trace('Profile.editPagerInfo');
                
                $('#pager-form-alternate-number').val($('#pager-form-alternate-number').val().replace(/[-\.]/g, ""));
                
                $('#pager-form button').attr('readonly', 'readonly');
                Server.post({
                    url: interact.listen.url('/ajax/editPager'),
                    properties: {
                        alternateNumber: $('#pager-form-alternate-number').val(),
                        alternateAddress: $('#pager-form-alternate-address').val(),
                        pagePrefix: $('#pager-form-page-prefix').val()
                    },
                    successCallback: function() {
                        interact.listen.notifySuccess('Alternate number updated');
                        $('#pager-form button').removeAttr('readonly');
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },

            disableButtons: function() {
                interact.util.trace('Profile.disableButtons');
                $('#profile-form button').attr('readonly', 'readonly');
            },

            enableButtons: function() {
                interact.util.trace('Profile.enableButtons');
                $('#profile-form button').removeAttr('readonly');
            },
            
            testEmailAddress: function() {
                interact.util.trace('Profile.testEmailAddress');
                Profile.testAddress('email', $('#profile-form-emailAddress').val());
            },
            
            testSmsAddress: function() {
                interact.util.trace('Profile.testSmsAddress');
                Profile.testAddress('sms', $('#profile-form-smsAddress').val());
            },
            
            testAddress: function(type, address) {
                Server.post({
                    url: interact.listen.url('/ajax/testNotificationSettings'),
                    properties: {
                        messageType: type,
                        address: address
                    },
                    successCallback: function() {
                        interact.listen.notifySuccess('Test notification sent to ' + address);
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },
            
            clearAllAccessNumberRows: function() {
                $('#profile-form-accessNumbersTable tbody tr').not(':last').remove();
            },

            addAccessNumberRow: function(number, messageLight, numberType, publicNumber) {
                var clone = $('#profile-accessNumber-row-template').clone();
                clone.removeAttr('id');
                $('.accessNumber-row-number', clone).val(number);
                if(messageLight) {
                    $('.accessNumber-row-messageLight', clone).attr('checked', 'checked');
                } else {
                    $('.accessNumber-row-messageLight', clone).removeAttr('checked');
                }
                $('.accessNumber-row-numberType', clone).val(numberType);
                if(publicNumber) {
                    $('.accessNumber-row-publicNumber', clone).attr('checked', 'checked');
                } else {
                    $('.accessNumber-row-publicNumber', clone).removeAttr('checked');
                }
                $('.icon-delete', clone).click(function() {
                    $(this).parent().parent().remove();
                });
                if(numberType == 'VOICEMAIL' || numberType == 'EXTENSION') {
                	$('.accessNumber-row-number', clone).attr('disabled', 'disabled');
                	$('.accessNumber-row-numberType', clone).attr('disabled', 'disabled');
                	$('.accessNumber-row-publicNumber', clone).attr('disabled', 'disabled');
                	$('.icon-delete', clone).attr('disabled', 'disabled');
                }
                $('#profile-form-accessNumbersTable tbody tr:last').before(clone);
            },

            buildAccessNumberString: function() {
                var value = '';
                var rows = $('#profile-form-accessNumbersTable tr');
                for(var i = 0; i < rows.length - 1; i++) {
                    var number = $('.accessNumber-row-number', rows[i]).val();
                    if(number.length == 0) {
                        continue;
                    }
                    var messageLight = $('.accessNumber-row-messageLight', rows[i]).is(':checked');
                    var numberType = $('.accessNumber-row-numberType', rows[i]).val();
                    var publicNumber = $('.accessNumber-row-publicNumber', rows[i]).is(':checked');
                    value += number + ':' + messageLight + ':' + numberType + ':' + publicNumber + ';';
                }
                if(value.length > 0) {
                    value = value.substring(0, value.length - 1); // remove last semicolon
                }
                return value;
            }

        }
    }();

    new Profile.Application().load();
});