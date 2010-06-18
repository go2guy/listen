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

            loadSubscriber: function(id) {
                LISTEN.trace('LISTEN.PROFILE.loadSubscriber ' + id);
                LISTEN.PROFILE.resetForm();
                $.ajax({
                    url: '/ajax/getSubscriber?id=' + id,
                    dataType: 'json',
                    cache: 'false',
                    success: function(data, textStatus, xhr) {
                        $('#subscriber-form-id').val(data.id);
                        $('#subscriber-form-username').val(data.username);
                        $('#subscriber-form-number').val(data.number);

                        $('#subscriber-form-add-button').hide();
                        $('#subscriber-form-edit-button').show();
                        $('#subscriber-form-cancel-button').show();
                        
                        LISTEN.switchApp('profile');
                    }
                });
            },

            resetForm: function() {
                LISTEN.trace('LISTEN.PROFILE.resetForm');
                LISTEN.PROFILE.clearError();
                $('#profile-form')[0].reset();
                $('#subscriber-form-cancel-button').hide();
                $('#subscriber-form-edit-button').hide();
                $('#subscriber-form-add-button').show();
            },

            addSubscriber: function() {
                LISTEN.trace('LISTEN.PROFILE.addSubscriber');
                LISTEN.PROFILE.disableButtons();
                SERVER.post({
                    url: '/ajax/addSubscriber',
                    properties: {
                        username: $('#subscriber-form-username').val(),
                        password: $('#subscriber-form-password').val(),
                        confirmPassword: $('#subscriber-form-confirmPassword').val(),
                        number: $('#subscriber-form-number').val()
                    },
                    successCallback: function() {
                        LISTEN.PROFILE.resetForm();
                        LISTEN.PROFILE.showSuccess('Subscriber added');
                        LISTEN.PROFILE.enableButtons();
                    },
                    errorCallback: function(message) {
                        LISTEN.PROFILE.showError(message);
                    }
                });
            },

            editSubscriber: function() {
                LISTEN.trace('LISTEN.PROFILE.editSubscriber');
                LISTEN.PROFILE.disableButtons();
                SERVER.post({
                    url: '/ajax/editSubscriber',
                    properties: {
                        id: $('#subscriber-form-id').val(),
                        username: $('#subscriber-form-username').val(),
                        password: $('#subscriber-form-password').val(),
                        confirmPassword: $('#subscriber-form-confirmPassword').val(),
                        number: $('#subscriber-form-number').val()
                    },
                    successCallback: function() {
                        //LISTEN.PROFILE.resetForm();
                        LISTEN.PROFILE.showSuccess('Subscriber updated');
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
                $('#subscriber-form button').attr('readonly', 'readonly');
            },

            enableButtons: function() {
                LISTEN.trace('LISTEN.PROFILE.enableButtons');
                $('#subscriber-form button').removeAttr('readonly');
            }
        }
    }();

    var app = new LISTEN.PROFILE.ProfileApplication();
    LISTEN.registerApp(new LISTEN.Application('profile', 'profile-application', 'profileButton', app));
});