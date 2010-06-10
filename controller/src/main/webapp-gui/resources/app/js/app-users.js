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
                        }

                        var numberCell = row.find('.user-cell-number');
                        if(numberCell.text() != data.number) {
                            numberCell.text(data.number);
                        }

                        var lastLoginCell = row.find('.user-cell-lastLogin');
                        if(lastLoginCell.text() != data.lastLogin) {
                            lastLoginCell.text(data.lastLogin);
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
                LISTEN.USERS.clearErrors();
                $('#user-form')[0].reset();
                $('#user-form-cancel-button').hide();
                $('#user-form-edit-button').hide();
                $('#user-form-add-button').show();
            },

            addUser: function() {
                LISTEN.trace('LISTEN.USERS.addUser');
                SERVER.addUser({
                    properties: {
                        username: $('#user-form-username').val(),
                        password: $('#user-form-password').val(),
                        confirmPassword: $('#user-form-confirmPassword').val(),
                        number: $('#user-form-number').val()
                    },
                    successCallback: function() {
                        LISTEN.USERS.resetForm();
                    },
                    errorCallback: function(message) {
                        var error = $('#user-form .form-error-message');
                        error.text(message);
                        error.slideDown(100);
                    }
                });
            },

            editUser: function() {
                LISTEN.trace('LISTEN.USERS.editUser');
                SERVER.editUser($('#user-form-id').val(),
                                $('#user-form-username').val(),
                                $('#user-form-password').val(),
                                $('#user-form-confirmPassword').val(),
                                $('#user-form-number').val());
            },

            clearErrors: function() {
                LISTEN.trace('LISTEN.USERS.clearErrors');
                $('#user-form .form-error-message').text('').hide();
            }
        }
    }();

    var app = new LISTEN.USERS.UsersApplication();
    LISTEN.registerApp(new LISTEN.Application('users', 'users-application', 'menu-users', app));
});