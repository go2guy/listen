$(document).ready(function() {

    $('#profile-form').submit(function() {
        Listen.Profile.editSubscriber();
        return false;
    });

    $('#profile-form-testEmail-button').click(function() {
        Listen.Profile.testEmailAddress();
        return false;
    });

    $('#profile-form-testSms-button').click(function() {
        Listen.Profile.testSmsAddress();
        return false;
    });

    $('#pager-form').submit(function() {
        Listen.Profile.editPagerInfo();
        return false;
    });

    Listen.Profile = function() {
        return {
            Application: function() {
                Listen.trace('Listen.Profile.Application [construct]');                
                
                this.load = function() {
                    Listen.trace('Loading profile');
                    $.ajax({
                        url: '/ajax/getSubscriber',
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
                
                this.unload = function() {
                    Listen.trace('Unloading profile');
                    Listen.Profile.clearError();
                    $('#pager-form .form-error-message').text('').hide();
                };
            },
            
            editSubscriber: function() {
                Listen.trace('Listen.Profile.editSubscriber');
                Listen.Profile.disableButtons();
                Server.post({
                    url: '/ajax/editSubscriber',
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
                        voicemailPlaybackOrder: $('#profile-form-voicemailPlaybackOrder').val()
                    },
                    successCallback: function() {
                        Listen.Profile.showSuccess('Profile updated');
                        Listen.Profile.enableButtons();
                    },
                    errorCallback: function(message) {
                        Listen.Profile.showError(message);
                    }
                });
            },
            
            editPagerInfo: function() {
                Listen.trace('Listen.Profile.editPagerInfo');
                $('#pager-form .form-error-message').text('').hide();
                
                $('#pager-form-alternate-number').val($('#pager-form-alternate-number').val().replace(/[-\.]/g, ""));
                
                $('#pager-form button').attr('readonly', 'readonly');
                Server.post({
                    url: '/ajax/editPager',
                    properties: {
                        alternateNumber: $('#pager-form-alternate-number').val(),
                        alternateAddress: $('#pager-form-alternate-address').val(),
                        pagePrefix: $('#pager-form-page-prefix').val()
                    },
                    successCallback: function() {
                        var elem = $('#pager-form .form-success-message');
                        elem.text('Alternate number updated').slideDown(100);
                        setTimeout(function() {
                            elem.slideUp(100);
                        }, 2000);
                        
                        $('#pager-form button').removeAttr('readonly');
                    },
                    errorCallback: function(message) {
                        $('#pager-form .form-error-message').text(message).slideDown(100);
                    }
                });
            },

            clearError: function() {
                Listen.trace('Listen.Profile.clearError');
                $('#profile-form .form-error-message').text('').hide();
            },

            showError: function(message) {
                Listen.trace('Listen.Profile.showError');
                $('#profile-form .form-error-message').text(message).slideDown(100);
            },

            showSuccess: function(message) {
                Listen.trace('Listen.Profile.showSuccess');
                Listen.Profile.clearError();
                var elem = $('#profile-form .form-success-message');
                elem.text(message).slideDown(100);
                setTimeout(function() {
                    elem.slideUp(100);
                }, 2000);
            },

            disableButtons: function() {
                Listen.trace('Listen.Profile.disableButtons');
                $('#profile-form button').attr('readonly', 'readonly');
            },

            enableButtons: function() {
                Listen.trace('Listen.Profile.enableButtons');
                $('#profile-form button').removeAttr('readonly');
            },
            
            testEmailAddress: function() {
                Listen.trace('Listen.Profile.testEmailAddress');
                Listen.Profile.testAddress('email', $('#profile-form-emailAddress').val());
            },
            
            testSmsAddress: function() {
                Listen.trace('Listen.Profile.testSmsAddress');
                Listen.Profile.testAddress('sms', $('#profile-form-smsAddress').val());
            },
            
            testAddress: function(type, address) {
                Server.post({
                    url: '/ajax/testNotificationSettings',
                    properties: {
                        messageType: type,
                        address: address
                    },
                    successCallback: function() {
                        Listen.Profile.showSuccess("Test notification sent to " + address);
                    },
                    errorCallback: function(message) {
                        Listen.Profile.showError(message);
                    }
                });
            }
        }
    }();

    var app = new Listen.Profile.Application();
    Listen.registerApp(new Listen.Application('profile', 'profile-application', 'profile-button', app));
});