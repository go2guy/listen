$(document).ready(function() {
    LISTEN.PROFILE = function() {
        return {
            ProfileApplication: function() {
                LISTEN.trace('LISTEN.PROFILE.ProfileApplication [construct]');                
                this.load = function() {
                    LISTEN.log('Loading profile');
                    // no-op
                };
                
                this.unload = function() {
                    LISTEN.log('Unloading profile');
                    // no-op
                };
            },

            loadUser: function(id) {
                LISTEN.trace('LISTEN.PROFILE.loadUser ' + id);
                LISTEN.PROFILE.resetForm();
                $.ajax({
                    url: '/ajax/getUser?id=' + id,
                    dataType: 'json',
                    cache: 'false',
                    success: function(data, textStatus, xhr) {
                        $('#user-form-id').val(data.id);
                        $('#user-form-username').val(data.username);
                        $('#user-form-number').val(data.number);

                        $('#user-form-add-button').hide();
                        $('#user-form-edit-button').show();
                        $('#user-form-cancel-button').show();
                        
                        LISTEN.switchApp('profile');
                    }
                });
            },

            resetForm: function() {
                LISTEN.trace('LISTEN.PROFILE.resetForm');
                LISTEN.PROFILE.clearError();
                $('#profile-form')[0].reset();
                $('#user-form-cancel-button').hide();
                $('#user-form-edit-button').hide();
                $('#user-form-add-button').show();
            },

            addUser: function() {
                LISTEN.trace('LISTEN.PROFILE.addUser');
                LISTEN.PROFILE.disableButtons();
                SERVER.post({
                    url: '/ajax/addUser',
                    properties: {
                        username: $('#user-form-username').val(),
                        password: $('#user-form-password').val(),
                        confirmPassword: $('#user-form-confirmPassword').val(),
                        number: $('#user-form-number').val()
                    },
                    successCallback: function() {
                        LISTEN.PROFILE.resetForm();
                        LISTEN.PROFILE.showSuccess('User added');
                        LISTEN.PROFILE.enableButtons();
                    },
                    errorCallback: function(message) {
                        LISTEN.PROFILE.showError(message);
                    }
                });
            },

            editUser: function() {
                LISTEN.trace('LISTEN.PROFILE.editUser');
                LISTEN.PROFILE.disableButtons();
                SERVER.post({
                    url: '/ajax/editUser',
                    properties: {
                        id: $('#user-form-id').val(),
                        username: $('#user-form-username').val(),
                        password: $('#user-form-password').val(),
                        confirmPassword: $('#user-form-confirmPassword').val(),
                        number: $('#user-form-number').val()
                    },
                    successCallback: function() {
                        //LISTEN.PROFILE.resetForm();
                        LISTEN.PROFILE.showSuccess('User updated');
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
                $('#user-form button').attr('readonly', 'readonly');
            },

            enableButtons: function() {
                LISTEN.trace('LISTEN.PROFILE.enableButtons');
                $('#user-form button').removeAttr('readonly');
            }
        }
    }();

    var app = new LISTEN.PROFILE.ProfileApplication();
    LISTEN.registerApp(new LISTEN.Application('profile', 'profile-application', 'profileButton', app));
});