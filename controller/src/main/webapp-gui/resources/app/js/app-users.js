$(document).ready(function() {
    LISTEN.USERS = function() {
        return {
            UsersApplication: function() {
                LISTEN.trace('LISTEN.USERS.UsersApplication [construct]');
                var interval;
                var dynamicTable = new LISTEN.DynamicTable({
                    tableId: 'users-table',
                    templateId: 'user-row-template',
                    retrieveList: function(data) {
                        return data;
                    },
                    updateRowCallback: function(row, data) {
                        var usernameCell = row.find('.user-cell-username');
                        if(usernameCell.text() != data.username) {
                            usernameCell.text(data.username);
                            this.highlight(usernameCell);
                        }

                        var numberCell = row.find('.user-cell-number');
                        if(numberCell.text() != data.number) {
                            numberCell.text(data.number);
                            this.highlight(numberCell);
                        }

                        var lastLoginCell = row.find('.user-cell-lastLogin');
                        if(lastLoginCell.text() != data.lastLogin) {
                            lastLoginCell.text(data.lastLogin);
                            this.highlight(lastLoginCell);
                        }

                        var editButtonCell = row.find('.user-cell-editButton');
                        var editButtonHtml = '<button class="edit-button" title="Edit user" onclick="LISTEN.USERS.loadUser(' + data.id + ');return false;">Edit</button>';
                        if(editButtonCell.html() != editButtonHtml) {
                            editButtonCell.html(editButtonHtml);
                        }
                    }
                });

                var pollAndSet = function() {
                    LISTEN.trace('LISTEN.USERS.UsersApplication.pollAndSet');
                    $.ajax({
                        url: '/ajax/getUserList',
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                            dynamicTable.update(data);
                        }
                    });
                };

                this.load = function() {
                    LISTEN.trace('LISTEN.USERS.UsersApplication.load');
                    pollAndSet();
                    interval = setInterval(function() {
                        pollAndSet();
                    }, 1000);
                };

                this.unload = function() {
                    LISTEN.trace('LISTEN.USERS.UsersApplication.unload');
                    if(interval) {
                        clearInterval(interval);
                    }
                };
            },

            loadUser: function(id) {
                LISTEN.trace('LISTEN.USERS.loadUser ' + id);
                LISTEN.USERS.resetForm();
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
                    }
                });
            },

            resetForm: function() {
                LISTEN.trace('LISTEN.USERS.resetForm');
                LISTEN.USERS.clearError();
                $('#user-form')[0].reset();
                $('#user-form-cancel-button').hide();
                $('#user-form-edit-button').hide();
                $('#user-form-add-button').show();
            },

            addUser: function() {
                LISTEN.trace('LISTEN.USERS.addUser');
                LISTEN.USERS.disableButtons();
                SERVER.post({
                    url: '/ajax/addUser',
                    properties: {
                        username: $('#user-form-username').val(),
                        password: $('#user-form-password').val(),
                        confirmPassword: $('#user-form-confirmPassword').val(),
                        number: $('#user-form-number').val()
                    },
                    successCallback: function() {
                        LISTEN.USERS.resetForm();
                        LISTEN.notify('User added');
                        LISTEN.USERS.enableButtons();
                    },
                    errorCallback: function(message) {
                        LISTEN.USERS.showError(message);
                    }
                });
            },

            editUser: function() {
                LISTEN.trace('LISTEN.USERS.editUser');
                LISTEN.USERS.disableButtons();
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
                        LISTEN.USERS.resetForm();
                        LISTEN.notify('User updated');
                        LISTEN.USERS.enableButtons();
                    },
                    errorCallback: function(message) {
                        LISTEN.USERS.showError(message);
                    }
                });
            },

            clearError: function() {
                LISTEN.trace('LISTEN.USERS.clearError');
                $('#user-form .form-error-message').text('').hide();
            },

            showError: function(message) {
                LISTEN.trace('LISTEN.USERS.showError');
                $('#user-form .form-error-message').text(message).slideDown(100);
            },

            disableButtons: function() {
                LISTEN.trace('LISTEN.USERS.disableButtons');
                $('#user-form button').attr('readonly', 'readonly');
            },

            enableButtons: function() {
                LISTEN.trace('LISTEN.USERS.enableButtons');
                $('#user-form button').removeAttr('readonly');
            }
        }
    }();

    var app = new LISTEN.USERS.UsersApplication();
    LISTEN.registerApp(new LISTEN.Application('users', 'users-application', 'menu-users', app));
});