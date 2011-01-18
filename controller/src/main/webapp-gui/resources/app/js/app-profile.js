var interact = interact || {};
var Profile;
$(document).ready(function() {

    $('#sendEmail').click(function() {
        $('#sendEmailOptions').toggle();
    });

    $('#sendEmailRestrictTime').click(function() {
        if($('#sendEmailTimeRestrictions .period-selector').size() == 0) {
            Profile.addTimeRestriction($('#addEmailTimeRestriction'));
        }
        $('#sendEmailTimeRestrictions').toggle();
    });

    $('#addEmailTimeRestriction').click(function() {
        Profile.addTimeRestriction($('#addEmailTimeRestriction'));
    });

    $('#sendSms').click(function() {
        $('#sendSmsOptions').toggle();
    });

    $('#sendSmsRestrictTime').click(function() {
        if($('#sendSmsTimeRestrictions .period-selector').size() == 0) {
            Profile.addTimeRestriction($('#addSmsTimeRestriction'));
        }
        $('#sendSmsTimeRestrictions').toggle();
    });

    $('#addSmsTimeRestriction').click(function() {
        Profile.addTimeRestriction($('#addSmsTimeRestriction'));
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

    // initialize toggleboxes
    $('ul.togglebox').click(function() {
        var ul = $(this);
        if(!ul.hasClass('disabled')) {
            var next = $('li.active', ul).next();
            if(next.size() == 0) {
                next = $(':first-child', ul);
            }
            $('li', ul).removeClass('active');
            next.addClass('active');
        }
    }).children('.default').addClass('active');
    
    $('input.time').keyup(function() {
        var field = $(this);
        var result = parseTime(field.val());
        
        field.parent().next().toggleClass('disabled', result.isMilitary).attr('title', result.isMilitary ? 'You have specified a 24-hour time' : '');
        field.toggleClass('invalid', !result.isValid);
    }).blur(function() {
        var field = $(this);
        var value = field.val();
        var result = parseTime(value);
        if(result.isValid && (value.length == 1 || value.length == 2)) {
            field.val(value + ':00');
        } else if(result.isValid && value.indexOf(':') == -1) {
            var h = value.substr(0, value.length == 3 ? 1 : 2);
            var m = value.substr(value.length == 3 ? 1 : 2);
            field.val(h + ':' + m);
        }
    });
    // TODO validate that start < end
    // TODO if start is PM, default end to PM

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
                            $('#sendEmailRestrictTime').attr('checked', data.emailTimeRestrictions.length > 0);
                            $('#sendEmailTimeRestrictions').toggle(data.emailTimeRestrictions.length > 0);
                            var addEmailRestrictionButton = $('#addEmailTimeRestriction');
                            for(var i = 0, restriction; restriction = data.emailTimeRestrictions[i]; i++) {
                                Profile.addEmailTimeRestriction(addEmailRestrictionButton, restriction.from, restriction.to, restriction.days);
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
                            $('#sendSmsRestrictTime').attr('checked', data.smsTimeRestrictions.length > 0);
                            $('#sendSmsTimeRestrictions').toggle(data.smsTimeRestrictions.length > 0);
                            var addSmsRestrictionButton = $('#addSmsTimeRestriction');
                            for(var i = 0, restriction; restriction = data.smsTimeRestrictions[i]; i++) {
                                Profile.addTimeRestriction(addSmsRestrictionButton, restriction.from, restriction.to, restriction.days);
                            }
                            
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
                    $('.message-light', clone).attr('disabled', 'disabled');

                    $('.delete-button', clone).click(function(e) {
                        $(e.target).parent().remove();
                    })
                }

                $('.phone-number-category', clone).val(numberType);
                $('#phoneNumbersButtons').before(clone);
            },

            addTimeRestriction: function(before, from, to, days) {
                var clone = $('#period-selector-template').clone(true);
                clone.removeAttr('id');
                $('button', clone).click(function() {
                    $(this).parent().remove();
                });

                function setMeridiem(time, togglebox) { // time should have 'AM' or 'PM' appended, or nothing if military
                    if(time.indexOf('AM') > -1) {
                        $('.am', togglebox).addClass('active');
                        $('.pm', togglebox).removeClass('active');
                    } else if(time.indexOf('PM') > -1) {
                        $('.am', togglebox).removeClass('active');
                        $('.pm', togglebox).addClass('active');
                    } else { // assume military
                        togglebox.addClass('disabled');
                    }
                }

                function setTime(time, input) { // will remove AM/PM at the end of the time
                    if(time.indexOf('AM') > -1 || time.indexOf('PM') > -1) {
                        input.val(time.substr(0, time.indexOf('M') - 1));
                    } else {
                        input.val(time);
                    }
                }

                if(from) {
                    setTime(from, $('.from-time', clone));
                    setMeridiem(from, $('.from-meridiem', clone));
                }
                if(to) {
                    setTime(to, $('.to-time', clone));
                    setMeridiem(to, $('.to-meridiem', clone));
                }
                if(days) {
                    var all = ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'];
                    for(var i = 0, day; day = all[i]; i++) {
                        $('.' + day + ' .on', clone).toggleClass('active', days[day]);
                    }
                }
                before.before(clone);
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
                Server.post({
                    url: interact.listen.url('/ajax/mySetSubscriberVoicemailSettings'),
                    properties: {
                        voicemailPasscode: $('#voicemailPasscode').val(),
                        playbackOrder: $('#playbackOrder').val(),
                        transcribeVoicemail: $('#transcribeVoicemail').is(':checked'),
                        sendEmail: $('#sendEmail').is(':checked'),
                        sendEmailToAddress: $('#sendEmailUseCurrent').is(':checked') ? $('#emailAddress').val() : $('#sendEmailOtherAddress').val(),
                        sendEmailTimeRestrictions: Profile.buildTimeRestrictions('sendEmailTimeRestrictions'),
                        sendSms: $('#sendSms').is(':checked'),
                        sendSmsToAddress: $('#sendSmsNumber').val() + ($('#sendSmsNumberProvider') != 'N/A' ? '@' + $('#sendSmsNumberProvider').val() : ''),
                        sendSmsTimeRestrictions: Profile.buildTimeRestrictions('sendSmsTimeRestrictions'),
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
            
            buildTimeRestrictions: function(id) {
                var parent = $('#' + id);
                var restrictions = [];
                $('.period-selector', parent).each(function() {
                    var restriction = {};
                    restriction.from = $('.from-time', this).val() + ($('.from-meridiem', this).hasClass('disabled') ? '' : $('.from-meridiem .active', this).text());
                    restriction.to = $('.to-time', this).val() + ($('.to-meridiem', this).hasClass('disabled') ? '' : $('.to-meridiem .active', this).text());
                    
                    var days = ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'];
                    var group = $('.togglebox-group', this);
                    for(var i = 0, day; day = days[i]; i++) {
                        var item = $('.' + day, group);
                        restriction[day] = $('.active', item).hasClass('on');
                    }
                    restrictions.push(restriction);
                });
                return JSON.stringify(restrictions);
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

function parseTime(input) {
    var result = {
        isValid: false,
        isMilitary: false
    }; // TODO possible improvement: if military, return the meridiem and change the value of the togglebox
    var valid = /^[0-9]{1,2}:?[0-9]{2}$|^[0-9]{1,2}$/.test(input);
    if (!valid) {
        return result;
    }

    var numeric = input.replace(/:/g, '');
    var hours, minutes;
    if(numeric.length == 1 || numeric.length == 2) {
        hours = numeric;
        minutes = 0;
    } else {
        hours = parseInt(numeric.substr(0, numeric.length == 3 ? 1 : 2), 10);
        minutes = parseInt(numeric.substr(numeric == 3 ? 1 : 2), 10);
    }

    result.isValid = hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59;
    result.isMilitary = result.isValid && (hours == 0 || (hours >= 13 && hours <= 23));
    return result;
}