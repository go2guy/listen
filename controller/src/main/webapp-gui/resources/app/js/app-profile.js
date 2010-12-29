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
                            var numbers = '';
                            for(var i = 0; i < data.accessNumbers.length; i++) {
                                numbers += data.accessNumbers[i].number;
                                if(i < data.accessNumbers.length - 1) {
                                    numbers += ',';
                                }
                            }
                            $('#profile-form-accessNumbers').text(numbers);
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
            }
        }
    }();

    new Profile.Application().load();
});