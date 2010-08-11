$(document).ready(function() {
    $('#subscriber-form-cancel-button').click(function() {
        Listen.Subscribers.resetForm();
        return false;
    });

    $('#subscriber-form-testEmail-button').click(function() {
        Listen.Subscribers.testEmailAddress();
        return false;
    });

    $('#subscriber-form-testSms-button').click(function() {
        Listen.Subscribers.testSmsAddress();
        return false;
    });

    $('#subscriber-form').submit(function() {
        if($('#subscriber-form-add-button').is(':visible')) {
            Listen.Subscribers.addSubscriber();
        } else {
            Listen.Subscribers.editSubscriber();
        }
        return false;
    });

    Listen.Subscribers = function() {
        return {
            Application: function() {
                Listen.trace('Listen.Subscribers.Application [construct]');
                var interval;
                var dynamicTable = new Listen.DynamicTable({
                    url: '/ajax/getSubscriberList',
                    tableId: 'subscribers-table',
                    templateId: 'subscriber-row-template',
                    retrieveList: function(data) {
                        return data.results;
                    },
                    paginationId: 'subscribers-pagination',
                    updateRowCallback: function(row, data, animate) {
                        Listen.setFieldContent(row.find('.subscriber-cell-username'), data.username, animate);

                        var numbers = '';
                        for(var i = 0; i < data.accessNumbers.length; i++) {
                            numbers += data.accessNumbers[i];
                            if(i < data.accessNumbers.length - 1) {
                                numbers += ', ';
                            }
                        }

                        Listen.setFieldContent(row.find('.subscriber-cell-accessNumbers'), numbers, animate);
                        Listen.setFieldContent(row.find('.subscriber-cell-lastLogin'), data.lastLogin, animate);
                        Listen.setFieldContent(row.find('.subscriber-cell-editButton'), '<button type="button" class="button-edit" title="Edit subscriber" onclick="Listen.Subscribers.loadSubscriber(' + data.id + ');">Edit</button>', false, true);
                        Listen.setFieldContent(row.find('.subscriber-cell-deleteButton'), '<button type="button" class="button-delete" title="Delete subscriber" onclick="Listen.Subscribers.confirmDeleteSubscriber(' + data.id + ');">Delete</button>', false, true);
                    }
                });

                this.load = function() {
                    Listen.trace('Listen.Subscribers.Application.load');
                    dynamicTable.pollAndSet(false);
                    interval = setInterval(function() {
                        dynamicTable.pollAndSet(true);
                    }, 1000);
                };

                this.unload = function() {
                    Listen.trace('Listen.Subscribers.Application.unload');
                    if(interval) {
                        clearInterval(interval);
                    }
                    Listen.Subscribers.resetForm();
                };
            },

            loadSubscriber: function(id) {
                Listen.trace('Listen.Subscribers.loadSubscriber ' + id);
                Listen.Subscribers.resetForm();
                var start = Listen.timestamp();
                $.ajax({
                    url: '/ajax/getSubscriber?id=' + id,
                    dataType: 'json',
                    cache: 'false',
                    success: function(data, textStatus, xhr) {
                        $('#subscriber-form-id').val(data.id);
                        $('#subscriber-form-username').val(data.username);

                        $('#subscriber-form-accountType').text(data.isActiveDirectory ? 'Active Directory' : 'Local');
                        if(data.isActiveDirectory) {
                            $('#subscriber-form-username').attr('readonly', true).addClass('disabled');
                            $('#subscriber-form-password').attr('readonly', true).addClass('disabled');
                            $('#subscriber-form-confirmPassword').attr('readonly', true).addClass('disabled');
                        } else {
                            $('#subscriber-form-username').removeAttr('readonly').removeClass('disabled');
                            $('#subscriber-form-password').removeAttr('readonly').removeClass('disabled');
                            $('#subscriber-form-confirmPassword').removeAttr('readonly').removeClass('disabled');
                        }

                        $('#subscriber-form-realName').val(data.realName);

                        var numbers = '';
                        for(var i = 0; i < data.accessNumbers.length; i++) {
                            numbers += data.accessNumbers[i];
                            if(i < data.accessNumbers.length - 1) {
                                numbers += ',';
                            }
                        }
                        
                        $('#subscriber-form-accessNumbers').val(numbers);
                        $('#subscriber-form-voicemailPin').val(data.voicemailPin);
                        $('#subscriber-form-emailAddress').val(data.emailAddress);
                        $('#subscriber-form-smsAddress').val(data.smsAddress);
                        
                        if(data.enableEmail) {                        
                            $('#subscriber-form-enableEmailNotification').attr('checked', true);
                        }
                        
                        if(data.enableSms) {                        
                            $('#subscriber-form-enableSmsNotification').attr('checked', true);
                        }
                        
                        if(data.enablePaging) {                        
                            $('#subscriber-form-paging').attr('checked', true);
                        }
                        $('#subscriber-form-voicemailPlaybackOrder').val(data.voicemailPlaybackOrder);

                        $('#subscriber-form-add-button').hide();
                        $('#subscriber-form-edit-button').show();
                        $('#subscriber-form-cancel-button').show();
                    },
                    complete: function(xhr, textStatus) {
                        var elapsed = Listen.timestamp() - start;
                        $('#latency').text(elapsed);
                    }
                });
                
            },

            resetForm: function() {
                Listen.trace('Listen.Subscribers.resetForm');
                Listen.Subscribers.clearError();
                $('#subscriber-form')[0].reset();
                $('#subscriber-form-cancel-button').hide();
                $('#subscriber-form-edit-button').hide();
                $('#subscriber-form-add-button').show();
                $('#subscriber-form-accountType').val('Local');
                $('#subscriber-form-username').removeAttr('readonly').removeClass('disabled');
                $('#subscriber-form-password').removeAttr('readonly').removeClass('disabled');
                $('#subscriber-form-confirmPassword').removeAttr('readonly').removeClass('disabled');
                $('#subscriber-form-accountType').text('Local');
            },

            addSubscriber: function() {
                Listen.trace('Listen.Subscribers.addSubscriber');
                Listen.Subscribers.disableButtons();
                Server.post({
                    url: '/ajax/addSubscriber',
                    properties: {
                        username: $('#subscriber-form-username').val(),
                        password: $('#subscriber-form-password').val(),
                        confirmPassword: $('#subscriber-form-confirmPassword').val(),
                        realName: $('#subscriber-form-realName').val(),
                        accessNumbers: $('#subscriber-form-accessNumbers').val(),
                        voicemailPin: $('#subscriber-form-voicemailPin').val(),
                        enableEmail: $('#subscriber-form-enableEmailNotification').is(":checked"),
                        enableSms: $('#subscriber-form-enableSmsNotification').is(":checked"),
                        emailAddress: $('#subscriber-form-emailAddress').val(),
                        smsAddress: $('#subscriber-form-smsAddress').val(),
                        enablePaging: $('#subscriber-form-paging').is(":checked"),
                        voicemailPlaybackOrder: $('#subscriber-form-voicemailPlaybackOrder').val()
                    },
                    successCallback: function() {
                        Listen.Subscribers.resetForm();
                        Listen.Subscribers.showSuccess('Subscriber added');
                        Listen.Subscribers.enableButtons();
                    },
                    errorCallback: function(message) {
                        Listen.Subscribers.showError(message);
                    }
                });
            },

            editSubscriber: function() {
                Listen.trace('Listen.Subscribers.editSubscriber');
                Listen.Subscribers.disableButtons();
                Server.post({
                    url: '/ajax/editSubscriber',
                    properties: {
                        id: $('#subscriber-form-id').val(),
                        username: $('#subscriber-form-username').val(),
                        password: $('#subscriber-form-password').val(),
                        confirmPassword: $('#subscriber-form-confirmPassword').val(),
                        realName: $('#subscriber-form-realName').val(),
                        accessNumbers: $('#subscriber-form-accessNumbers').val(),
                        voicemailPin: $('#subscriber-form-voicemailPin').val(),
                        enableEmail: $('#subscriber-form-enableEmailNotification').is(":checked"),
                        enableSms: $('#subscriber-form-enableSmsNotification').is(":checked"),
                        emailAddress: $('#subscriber-form-emailAddress').val(),
                        smsAddress: $('#subscriber-form-smsAddress').val(),
                        enablePaging: $('#subscriber-form-paging').is(":checked"),
                        voicemailPlaybackOrder: $('#subscriber-form-voicemailPlaybackOrder').val()
                    },
                    successCallback: function() {
                        Listen.Subscribers.resetForm();
                        Listen.Subscribers.showSuccess('Subscriber updated');
                        Listen.Subscribers.enableButtons();
                    },
                    errorCallback: function(message) {
                        Listen.Subscribers.showError(message);
                    }
                });
            },

            confirmDeleteSubscriber: function(id) {
                Listen.trace('Listen.Subscribers.confirmDeleteSubscriber');
                if(confirm('Are you sure?')) {
                    Listen.Subscribers.deleteSubscriber(id);
                }
            },

            deleteSubscriber: function(id) {
                Listen.trace('Listen.Subscribers.deleteSubscriber');
                Server.post({
                    url: '/ajax/deleteSubscriber',
                    properties: { id: id }
                });
            },

            clearError: function() {
                Listen.trace('Listen.Subscribers.clearError');
                $('#subscriber-form .form-error-message').text('').hide();
            },

            showError: function(message) {
                Listen.trace('Listen.Subscribers.showError');
                $('#subscriber-form .form-error-message').text(message).slideDown(100);
            },

            showSuccess: function(message) {
                Listen.trace('Listen.Subscribers.showSuccess');
                Listen.Subscribers.clearError();
                var elem = $('#subscriber-form .form-success-message');
                elem.text(message).slideDown(100);
                setTimeout(function() {
                    elem.slideUp(100);
                }, 2000);
            },

            disableButtons: function() {
                Listen.trace('Listen.Subscribers.disableButtons');
                $('#subscriber-form button').attr('readonly', 'readonly');
            },

            enableButtons: function() {
                Listen.trace('Listen.Subscribers.enableButtons');
                $('#subscriber-form button').removeAttr('readonly');
            },
            
            testEmailAddress: function() {
                Listen.trace('Listen.Subscribers.testEmailAddress');
                Listen.Subscribers.testAddress('email', $('#subscriber-form-emailAddress').val());
            },
            
            testSmsAddress: function() {
                Listen.trace('Listen.Subscribers.testSmsAddress');
                Listen.Subscribers.testAddress('sms', $('#subscriber-form-smsAddress').val());
            },
            
            testAddress: function(type, address) {
                Server.post({
                    url: '/ajax/testNotificationSettings',
                    properties: {
                        messageType: type,
                        address: address
                    },
                    successCallback: function() {
                        Listen.Subscribers.showSuccess("Test notification sent to " + address);
                    },
                    errorCallback: function(message) {
                        Listen.Subscribers.showError(message);
                    }
                });
            }
        }
    }();

    var app = new Listen.Subscribers.Application();
    Listen.registerApp(new Listen.Application('subscribers', 'subscribers-application', 'menu-subscribers', app));
});