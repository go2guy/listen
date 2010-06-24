$(document).ready(function() {
    LISTEN.PROFILE = function() {
        return {
            ProfileApplication: function() {
                LISTEN.trace('LISTEN.PROFILE.ProfileApplication [construct]');                
                
                this.load = function() {
                    LISTEN.log('Loading profile');
                    $.ajax({
                        url: '/ajax/getSubscriber',
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                            $('#profile-form-id').val(data.id);
                            $('#profile-form-username').val(data.username);
                            var numbers = '';
                            for(var i = 0; i < data.accessNumbers.length; i++) {
                                numbers += data.accessNumbers[i];
                                if(i < data.accessNumbers.length - 1) {
                                    numbers += ',';
                                }
                            }
                            $('#profile-form-accessNumbers').text(numbers);
                            $('#profile-form-voicemailPin').val(data.voicemailPin);
                            $('#profile-form-emailAddress').val(data.emailAddress);
                            $('#profile-form-smsAddress').val(data.smsAddress);
                            
                            if(data.enableEmail) {                        
                                $('#profile-form-enableEmailNotification').attr('checked', true);
                            }
                            
                            if(data.enableSms) {                        
                                $('#profile-form-enableSmsNotification').attr('checked', true);
                            }
    
                            $('#profile-form-edit-button').show();
                        }
                    });
                };
                
                this.unload = function() {
                    LISTEN.log('Unloading profile');
                    LISTEN.PROFILE.clearError();
                };
            },
            
            editSubscriber: function() {
                LISTEN.trace('LISTEN.PROFILE.editSubscriber');
                LISTEN.PROFILE.disableButtons();
                SERVER.post({
                    url: '/ajax/editSubscriber',
                    properties: {
                        id: $('#profile-form-id').val(),
                        username: $('#profile-form-username').val(),
                        password: $('#profile-form-password').val(),
                        confirmPassword: $('#profile-form-confirmPassword').val(),
                        voicemailPin: $('#profile-form-voicemailPin').val(),
                        enableEmail: $('#profile-form-enableEmailNotification').is(":checked"),
                        enableSms: $('#profile-form-enableSmsNotification').is(":checked"),
                        emailAddress: $('#profile-form-emailAddress').val(),
                        smsAddress: $('#profile-form-smsAddress').val()
                    },
                    successCallback: function() {
                        LISTEN.PROFILE.showSuccess('Profile updated');
                        LISTEN.PROFILE.enableButtons();
                    },
                    errorCallback: function(message) {
                        LISTEN.PROFILE.showError(message);
                    }
                });
            },

            clearError: function() {
                LISTEN.trace('LISTEN.PROFILE.clearError');
                $('#profile-form .form-error-message').text('').hide();
            },

            showError: function(message) {
                LISTEN.trace('LISTEN.PROFILE.showError');
                $('#profile-form .form-error-message').text(message).slideDown(100);
            },

            showSuccess: function(message) {
                LISTEN.trace('LISTEN.PROFILE.showSuccess');
                var elem = $('#profile-form .form-success-message');
                elem.text(message).slideDown(100);
                setTimeout(function() {
                    elem.slideUp(100);
                }, 2000);
            },

            disableButtons: function() {
                LISTEN.trace('LISTEN.PROFILE.disableButtons');
                $('#profile-form button').attr('readonly', 'readonly');
            },

            enableButtons: function() {
                LISTEN.trace('LISTEN.PROFILE.enableButtons');
                $('#profile-form button').removeAttr('readonly');
            },
            
            testEmailAddress: function() {
                LISTEN.trace('LISTEN.PROFILE.testEmailAddress');
                alert("you entered " + $('#profile-form-emailAddress').val()); 
            },
            
            testSmsAddress: function() {
                LISTEN.trace('LISTEN.PROFILE.testSmsAddress');
                alert("you entered " + $('#profile-form-smsAddress').val()); 
            }
        }
    }();

    var app = new LISTEN.PROFILE.ProfileApplication();
    LISTEN.registerApp(new LISTEN.Application('profile', 'profile-application', 'profile-button', app));
});