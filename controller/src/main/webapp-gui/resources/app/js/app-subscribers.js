$(document).ready(function() {
    LISTEN.SUBSCRIBERS = function() {
        return {
            SubscribersApplication: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.SubscribersApplication [construct]');
                var interval;
                var dynamicTable = new LISTEN.DynamicTable({
                    tableId: 'subscribers-table',
                    templateId: 'subscriber-row-template',
                    retrieveList: function(data) {
                        return data;
                    },
                    updateRowCallback: function(row, data) {
                        var usernameCell = row.find('.subscriber-cell-username');
                        if(usernameCell.text() != data.username) {
                            usernameCell.text(data.username);
                            this.highlight(usernameCell);
                        }

                        var numbers = '';
                        for(var i = 0; i < data.accessNumbers.length; i++) {
                            numbers += data.accessNumbers[i];
                            if(i < data.accessNumbers.length - 1) {
                                numbers += ',';
                            }
                        }

                        var accessNumbersCell = row.find('.subscriber-cell-accessNumbers');
                        if(accessNumbersCell.text() != numbers) {
                            accessNumbersCell.text(numbers);
                            this.highlight(accessNumbersCell);
                        }

                        var lastLoginCell = row.find('.subscriber-cell-lastLogin');
                        if(lastLoginCell.text() != data.lastLogin) {
                            lastLoginCell.text(data.lastLogin);
                            this.highlight(lastLoginCell);
                        }

                        var editButtonCell = row.find('.subscriber-cell-editButton');
                        var editButtonHtml = '<button class="edit-button" title="Edit subscriber" onclick="LISTEN.SUBSCRIBERS.loadSubscriber(' + data.id + ');return false;">Edit</button>';
                        if(editButtonCell.html() != editButtonHtml) {
                            editButtonCell.html(editButtonHtml);
                        }
                    }
                });

                var pollAndSet = function() {
                    LISTEN.trace('LISTEN.SUBSCRIBERS.SubscribersApplication.pollAndSet');
                    $.ajax({
                        url: '/ajax/getSubscriberList',
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                            dynamicTable.update(data);
                        }
                    });
                };

                this.load = function() {
                    LISTEN.trace('LISTEN.SUBSCRIBERS.SubscribersApplication.load');
                    pollAndSet();
                    interval = setInterval(function() {
                        pollAndSet();
                    }, 1000);
                };

                this.unload = function() {
                    LISTEN.trace('LISTEN.SUBSCRIBERS.SubscribersApplication.unload');
                    if(interval) {
                        clearInterval(interval);
                    }
                };
            },

            loadSubscriber: function(id) {
                LISTEN.trace('LISTEN.SUBSCRIBERS.loadSubscriber ' + id);
                LISTEN.SUBSCRIBERS.resetForm();
                $.ajax({
                    url: '/ajax/getSubscriber?id=' + id,
                    dataType: 'json',
                    cache: 'false',
                    success: function(data, textStatus, xhr) {
                        $('#subscriber-form-id').val(data.id);
                        $('#subscriber-form-username').val(data.username);
                        
                        var numbers = '';
                        for(var i = 0; i < data.accessNumbers.length; i++) {
                            numbers += data.accessNumbers[i];
                            if(i < data.accessNumbers.length - 1) {
                                numbers += ',';
                            }
                        }
                        
                        $('#subscriber-form-accessNumbers').val(numbers);

                        $('#subscriber-form-add-button').hide();
                        $('#subscriber-form-edit-button').show();
                        $('#subscriber-form-cancel-button').show();
                    }
                });
                
            },

            resetForm: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.resetForm');
                LISTEN.SUBSCRIBERS.clearError();
                $('#subscriber-form')[0].reset();
                $('#subscriber-form-cancel-button').hide();
                $('#subscriber-form-edit-button').hide();
                $('#subscriber-form-add-button').show();
            },

            addSubscriber: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.addSubscriber');
                LISTEN.SUBSCRIBERS.disableButtons();
                SERVER.post({
                    url: '/ajax/addSubscriber',
                    properties: {
                        username: $('#subscriber-form-username').val(),
                        password: $('#subscriber-form-password').val(),
                        confirmPassword: $('#subscriber-form-confirmPassword').val()
                    },
                    successCallback: function() {
                        LISTEN.SUBSCRIBERS.resetForm();
                        LISTEN.SUBSCRIBERS.showSuccess('Subscriber added');
                        LISTEN.SUBSCRIBERS.enableButtons();
                    },
                    errorCallback: function(message) {
                        LISTEN.SUBSCRIBERS.showError(message);
                    }
                });
            },

            editSubscriber: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.editSubscriber');
                LISTEN.SUBSCRIBERS.disableButtons();
                SERVER.post({
                    url: '/ajax/editSubscriber',
                    properties: {
                        id: $('#subscriber-form-id').val(),
                        username: $('#subscriber-form-username').val(),
                        password: $('#subscriber-form-password').val(),
                        confirmPassword: $('#subscriber-form-confirmPassword').val(),
                        accessNumbers: $('#subscriber-form-accessNumbers').val()
                    },
                    successCallback: function() {
                        LISTEN.SUBSCRIBERS.resetForm();
                        LISTEN.SUBSCRIBERS.showSuccess('Subscriber updated');
                        LISTEN.SUBSCRIBERS.enableButtons();
                    },
                    errorCallback: function(message) {
                        LISTEN.SUBSCRIBERS.showError(message);
                    }
                });
            },

            clearError: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.clearError');
                $('#subscriber-form .form-error-message').text('').hide();
            },

            showError: function(message) {
                LISTEN.trace('LISTEN.SUBSCRIBERS.showError');
                $('#subscriber-form .form-error-message').text(message).slideDown(100);
            },

            showSuccess: function(message) {
                LISTEN.trace('LISTEN.SUBSCRIBERS.showSuccess');
                var elem = $('#subscriber-form .form-success-message');
                elem.text(message).slideDown(100);
                setTimeout(function() {
                    elem.slideUp(100);
                }, 2000);
            },

            disableButtons: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.disableButtons');
                $('#subscriber-form button').attr('readonly', 'readonly');
            },

            enableButtons: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.enableButtons');
                $('#subscriber-form button').removeAttr('readonly');
            }
        }
    }();

    var app = new LISTEN.SUBSCRIBERS.SubscribersApplication();
    LISTEN.registerApp(new LISTEN.Application('subscribers', 'subscribers-application', 'menu-subscribers', app));
});