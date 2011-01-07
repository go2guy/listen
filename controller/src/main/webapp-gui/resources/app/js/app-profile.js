var interact = interact || {};
var Profile;
$(document).ready(function() {

    $('#sendEmail').click(function() {
        $('#sendEmailOptions').toggle();
    });

    $('#sendSms').click(function() {
        $('#sendSmsOptions').toggle();
    });

    $('#generalSettingsForm').submit(function() {
        Profile.saveGeneralSettings();
        return false;
    });
    
    $('#voicemailSettingsForm').submit(function() {
        Profile.saveVoicemailSettings();
        return false;
    });

    $('#phoneNumbersForm').submit(function() {
        Profile.savePhoneNumberSettings();
        return false;
    });

    $('#afterHoursForm').submit(function() {
        Profile.saveAfterHoursSettings();
        return false;
    });

    $('#addAnotherNumber').click(function() {
        Profile.addPhoneNumber();
    });
    
    $('#sendTestEmail').click(function() {
        Profile.testEmailAddress();
        return false;
    });

    $('#sendTestSms').click(function() {
        Profile.testSmsAddress();
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

                            // GENERAL TAB
                            $('#username').val(data.username);
                            $('#accountType').val(data.isActiveDirectory ? 'Active Directory' : 'Local');
                            $('#realName').val(data.realName);
                            $('#emailAddress').val(data.workEmailAddress);
                            
                            // VOICEMAIL TAB
                            
                            // initialize selects and other data dependent on the response
                            $('#sendEmailMyAddress').text(data.workEmailAddress ? data.workEmailAddress : 'Not Set');

                            // passcode
                            $('#voicemailPasscode').val(data.voicemailPin !== null ? data.voicemailPin : '');
                            
                            // voicemail playback order
                            $('#playbackOrder').val(data.voicemailPlaybackOrder);
                            
                            // email notifications
                            $('#sendEmail').attr('checked', data.enableEmail);
                            $('#sendEmailOptions').toggle(data.enableEmail);
                            if(!data.workEmailAddress) {
                                $('#sendEmailUseCurrent').attr('disabled', 'disabled').attr('readonly', 'readonly');
                            }
                            if(data.workEmailAddress && data.emailAddress === data.workEmailAddress) {
                                $('#sendEmailUseCurrent').attr('checked', 'checked');
                            } else {
                                $('#sendEmailUseAnother').attr('checked', 'checked');
                                $('#sendEmailOtherAddress').val(data.emailAddress);
                            }
                            
                            // sms notifications
                            $('#sendSms').attr('checked', data.enableSms);
                            $('#sendSmsOptions').toggle(data.enableSms);

                            var found = false;
                            if(data.smsAddress.indexOf('@') == -1) {
                                $('#sendSmsNumber').val(data.smsAddress);
                                $('#sendSmsNumberProvider').val('N/A');
                            } else {
                                var split = data.smsAddress.split('@');
                                $('#sendSmsNumber').val(split[0]);
                                $('#sendSmsNumberProvider').val(split[1]);
                            }
                            $('#keepSendingSms').attr('checked', data.enablePaging);
                            $('#transcribeVoicemail').attr('checked', data.enableTranscription);
                            
                            // PHONE NUMBERS TAB
                            
                            Profile.clearPhoneNumbers();
                            for(var i = 0; i < data.accessNumbers.length; i++) {
                                Profile.addPhoneNumber(data.accessNumbers[i].number, data.accessNumbers[i].messageLight, data.accessNumbers[i].numberType, data.accessNumbers[i].publicNumber);
                            }
                            
                            // ALTERNATE PAGER NUMBER TAB
                            
                            $('#pagerNumber').val(data.pagerNumber);
                            $('#pagePrefix').val(data.pagePrefix);
                            if(data.pagerAlternateNumber != '') {
                                var split = data.pagerAlternateNumber.split('@');
                                $('#alternatePagerNumber').val(split[0]);
                                $('#alternatePagerNumberProvider').val(split[1]);
                            }
                        }
                    });
                }
            },
            
            clearPhoneNumbers: function() {
                $('#phoneNumbersForm > fieldset').not(':last').remove();
            },
            
            addPhoneNumber: function(number, messageLight, numberType, publicNumber) {
                var clone = $('#phoneNumberTemplate').clone();
                clone.removeAttr('id');
                
                $('.phone-number', clone).val(number);
                $('.message-light', clone).attr('checked', messageLight);
                $('.public-number', clone).attr('checked', publicNumber);
                
                if(numberType == 'EXTENSION' || numberType == 'VOICEMAIL') {
                    clone.addClass('system');
                    $('input, select', clone).attr('disabled', 'disabled').attr('readonly', 'readonly').addClass('disabled');
                    $('button', clone).remove();
                    $('select', clone).append('<option value="VOICEMAIL">Voicemail</option>').append('<option value="EXTENSION">Office/Desk</option>');
                    
                    clone.append('<span class="annotation unmodifiable-number-annotation">This number is not modifiable</span>');
                } else {
                    $('.delete-button', clone).click(function(e) {
                        $(e.target).parent().remove();
                    })
                }

                $('.phone-number-category', clone).val(numberType);
                $('#phoneNumbersButtons').before(clone);
            },

            saveGeneralSettings: function() {
                interact.util.trace('Profile.saveGeneralSettings');
                Server.post({
                    url: interact.listen.url('/ajax/mySetSubscriberGeneralSettings'),
                    properties: {
                        password: $('#password').val(),
                        passwordConfirm: $('#passwordConfirm').val(),
                        realName: $('#realName').val(),
                        emailAddress: $('#emailAddress').val()
                    },
                    successCallback: function() {
                        interact.listen.notifySuccess('General settings updated');
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },
            
            saveVoicemailSettings: function() {
                interact.util.trace('Profile.saveVoicemailSettings');
                interact.util.trace("is sendEmailUseCurrent checked? " + ($('#sendEmailUseCurrent').is(':checked') ? 'yes' : 'no'));
                Server.post({
                    url: interact.listen.url('/ajax/mySetSubscriberVoicemailSettings'),
                    properties: {
                        voicemailPasscode: $('#voicemailPasscode').val(),
                        playbackOrder: $('#playbackOrder').val(),
                        transcribeVoicemail: $('#transcribeVoicemail').is(':checked'),
                        sendEmail: $('#sendEmail').is(':checked'),
                        sendEmailToAddress: $('#sendEmailUseCurrent').is(':checked') ? $('#emailAddress').val() : $('#sendEmailOtherAddress').val(),
                        sendSms: $('#sendSms').is(':checked'),
                        sendSmsToAddress: $('#sendSmsNumber').val() + ($('#sendSmsNumberProvider') != 'N/A' ? '@' + $('#sendSmsNumberProvider').val() : ''),
                        keepSendingSms: $('#keepSendingSms').is(':checked')
                    },
                    successCallback: function() {
                        interact.listen.notifySuccess('Voicemail settings updated');
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },
            
            savePhoneNumberSettings: function() {
                interact.util.trace('Profile.savePhoneNumberSettings');
                Server.post({
                    url: interact.listen.url('/ajax/mySetSubscriberPhoneNumberSettings'),
                    properties: {
                        numbers: JSON.stringify(Profile.buildPhoneNumberObject())
                    },
                    successCallback: function() {
                        interact.listen.notifySuccess('Phone number settings updated');
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },
            
            saveAfterHoursSettings: function() {
                interact.util.trace('Profile.saveAfterHoursSettings');

                Server.post({
                    url: interact.listen.url('/ajax/editPager'),
                    properties: {
                        alternateNumber: $('#alternatePagerNumber').val().replace(/[-\.]/g, ""),
                        alternateAddress: $('#alternatePagerNumberProvider').val(),
                        pagePrefix: $('#pagePrefix').val()
                    },
                    successCallback: function() {
                        interact.listen.notifySuccess('Alternate number updated');
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },
            
            buildPhoneNumberObject: function() {
                var numbers = [];
                $('#phoneNumbersForm > fieldset').not('.system').not('#phoneNumbersButtons').each(function(index, entry) {
                    numbers.push({
                        number: $('.phone-number', entry).val(),
                        //serviceProvider: $('.phone-number-service-provider', entry).val(),
                        category: $('.phone-number-category', entry).val(),
                        messageLight: $('.message-light', entry).is(':checked'),
                        publicNumber: $('.public-number', entry).is(':checked')
                    });
                });
                return numbers;
            },
            
            testEmailAddress: function() {
                interact.util.trace('Profile.testEmailAddress');
                Profile.testAddress('email', $('#sendEmailOtherAddress').val());
            },
            
            testSmsAddress: function() {
                interact.util.trace('Profile.testSmsAddress');
                var address = $('#sendSmsNumber').val();
                var select = $('#sendSmsNumberProvider').val();
                if(select != 'N/A') {
                    address += '@' + select;
                }

                Profile.testAddress('sms', address);
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
            }
        }
    }();

    new Profile.Application().load();
});