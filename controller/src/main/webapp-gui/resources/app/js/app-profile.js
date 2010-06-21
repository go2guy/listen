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
    
                            $('#profile-form-edit-button').show();
                        }
                    });
                };
                
                this.unload = function() {
                    LISTEN.log('Unloading profile');
                    // no-op
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
                        confirmPassword: $('#profile-form-confirmPassword').val()
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
            }
        }
    }();

    var app = new LISTEN.PROFILE.ProfileApplication();
    LISTEN.registerApp(new LISTEN.Application('profile', 'profile-application', 'profile-button', app));
});