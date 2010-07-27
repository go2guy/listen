$(document).ready(function() {
    $('#subscriber-form-cancel-button').click(function() {
        LISTEN.SUBSCRIBERS.resetForm();
        return false;
    });

    $('#subscriber-form-testEmail-button').click(function() {
        LISTEN.SUBCRIBERS.testEmailAddress();
        return false;
    });

    $('#subscriber-form-testSms-button').click(function() {
        LISTEN.SUBSCRIBERS.testSmsAddress();
        return false;
    });

    $('#subscriber-form').submit(function() {
        if($('#subscriber-form-add-button').is(':visible')) {
            LISTEN.SUBSCRIBERS.addSubscriber();
        } else {
            LISTEN.SUBSCRIBERS.editSubscriber();
        }
        return false;
    });

    LISTEN.SUBSCRIBERS = function() {
        return {
            SubscribersApplication: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.SubscribersApplication [construct]');
                var interval;
                var dynamicTable = new LISTEN.DynamicTable({
                    url: '/ajax/getSubscriberList',
                    tableId: 'subscribers-table',
                    templateId: 'subscriber-row-template',
                    retrieveList: function(data) {
                        return data.results;
                    },
                    paginationId: 'subscribers-pagination',
                    updateRowCallback: function(row, data, animate) {
                        LISTEN.setFieldContent(row.find('.subscriber-cell-username'), data.username, animate);

                        var numbers = '';
                        for(var i = 0; i < data.accessNumbers.length; i++) {
                            numbers += data.accessNumbers[i];
                            if(i < data.accessNumbers.length - 1) {
                                numbers += ', ';
                            }
                        }

                        LISTEN.setFieldContent(row.find('.subscriber-cell-accessNumbers'), numbers, animate);
                        LISTEN.setFieldContent(row.find('.subscriber-cell-lastLogin'), data.lastLogin, animate);
                        LISTEN.setFieldContent(row.find('.subscriber-cell-editButton'), '<button class="button-edit" title="Edit subscriber" onclick="LISTEN.SUBSCRIBERS.loadSubscriber(' + data.id + ');return false;">Edit</button>', false, true);
                    }
                });

                this.load = function() {
                    LISTEN.trace('LISTEN.SUBSCRIBERS.SubscribersApplication.load');
                    dynamicTable.pollAndSet(false);
                    interval = setInterval(function() {
                        dynamicTable.pollAndSet(true);
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
                var start = LISTEN.timestamp();
                $.ajax({
                    url: '/ajax/getSubscriber?id=' + id,
                    dataType: 'json',
                    cache: 'false',
                    success: function(data, textStatus, xhr) {
                        $('#subscriber-form-id').val(data.id);
                        $('#subscriber-form-username').val(data.username);
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

                        $('#subscriber-form-add-button').hide();
                        $('#subscriber-form-edit-button').show();
                        $('#subscriber-form-cancel-button').show();
                    },
                    complete: function(xhr, textStatus) {
                        var elapsed = LISTEN.timestamp() - start;
                        $('#latency').text(elapsed);
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
                        confirmPassword: $('#subscriber-form-confirmPassword').val(),
                        realName: $('#subscriber-form-realName').val(),
                        accessNumbers: $('#subscriber-form-accessNumbers').val(),
                        voicemailPin: $('#subscriber-form-voicemailPin').val(),
                        enableEmail: $('#subscriber-form-enableEmailNotification').is(":checked"),
                        enableSms: $('#subscriber-form-enableSmsNotification').is(":checked"),
                        emailAddress: $('#subscriber-form-emailAddress').val(),
                        smsAddress: $('#subscriber-form-smsAddress').val()
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
                        realName: $('#subscriber-form-realName').val(),
                        accessNumbers: $('#subscriber-form-accessNumbers').val(),
                        voicemailPin: $('#subscriber-form-voicemailPin').val(),
                        enableEmail: $('#subscriber-form-enableEmailNotification').is(":checked"),
                        enableSms: $('#subscriber-form-enableSmsNotification').is(":checked"),
                        emailAddress: $('#subscriber-form-emailAddress').val(),
                        smsAddress: $('#subscriber-form-smsAddress').val()
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
                LISTEN.SUBSCRIBERS.clearError();
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
            },
            
            testEmailAddress: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.testEmailAddress');
                LISTEN.SUBSCRIBERS.testAddress('email', $('#subscriber-form-emailAddress').val());
            },
            
            testSmsAddress: function() {
                LISTEN.trace('LISTEN.SUBSCRIBERS.testSmsAddress');
                LISTEN.SUBSCRIBERS.testAddress('sms', $('#subscriber-form-smsAddress').val());
            },
            
            testAddress: function(type, address) {
                SERVER.post({
                    url: '/ajax/testNotificationSettings',
                    properties: {
                        messageType: type,
                        address: address
                    },
                    successCallback: function() {
                        LISTEN.SUBSCRIBERS.showSuccess("Test notification sent to " + address);
                    },
                    errorCallback: function(message) {
                        LISTEN.SUBSCRIBERS.showError(message);
                    }
                });
            }
        }
    }();

    var app = new LISTEN.SUBSCRIBERS.SubscribersApplication();
    LISTEN.registerApp(new LISTEN.Application('subscribers', 'subscribers-application', 'menu-subscribers', app));
});