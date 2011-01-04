var interact = interact || {};
var Subscribers;
$(document).ready(function() {
    $('#subscriber-form-cancel-button').click(function() {
        Subscribers.resetForm();
        return false;
    });

    $('#subscriber-form-testEmail-button').click(function() {
        Subscribers.testEmailAddress();
        return false;
    });

    $('#subscriber-form-testSms-button').click(function() {
        Subscribers.testSmsAddress();
        return false;
    });

    $('#subscriber-form').submit(function() {
        if($('#subscriber-form-add-button').is(':visible')) {
            Subscribers.addSubscriber();
        } else {
            Subscribers.editSubscriber();
        }
        return false;
    });

    $('#subscriber-form-addAccessNumber').click(function() {
        Subscribers.addAccessNumberRow();
    });

    Subscribers = function() {
        return {
            Application: function() {
                interact.util.trace('Subscribers.Application [construct]');
                var interval;
                var dynamicTable = new interact.util.DynamicTable({
                    url: interact.listen.url('/ajax/getSubscriberList'),
                    tableId: 'subscribers-table',
                    templateId: 'subscriber-row-template',
                    retrieveList: function(data) {
                        return data.results;
                    },
                    paginationId: 'subscribers-pagination',
                    alternateRowColors: true,
                    updateRowCallback: function(row, data, animate) {
                        interact.util.setFieldContent(row.find('.subscriber-cell-username'), data.username, animate);

                        var numbers = '';
                        for(var i = 0; i < data.accessNumbers.length; i++) {
                            numbers += data.accessNumbers[i].number;
                            if(i < data.accessNumbers.length - 1) {
                                numbers += ' ';
                            }
                        }

                        interact.util.setFieldContent(row.find('.subscriber-cell-accessNumbers'), numbers, animate);
                        interact.util.setFieldContent(row.find('.subscriber-cell-lastLogin'), data.lastLogin, animate);
                        interact.util.setFieldContent(row.find('.subscriber-cell-editButton'), '<button type="button" class="icon-edit" title="Edit subscriber" onclick="Subscribers.loadSubscriber(' + data.id + ');"></button>', false, true);
                        if(data.isCurrentSubscriber) {
                            interact.util.setFieldContent(row.find('.subscriber-cell-deleteButton'), '', false);
                        } else {
                            interact.util.setFieldContent(row.find('.subscriber-cell-deleteButton'), '<button type="button" class="icon-delete" title="Delete subscriber" onclick="Subscribers.confirmDeleteSubscriber(' + data.id + ');"></button>', false, true);
                        }
                    }
                });

                this.load = function() {
                    interact.util.trace('Subscribers.Application.load');
                    dynamicTable.pollAndSet(false);
                    interval = setInterval(function() {
                        dynamicTable.pollAndSet(true);
                    }, 1000);
                };
            },

            loadSubscriber: function(id) {
                interact.util.trace('Subscribers.loadSubscriber ' + id);
                Subscribers.resetForm();
                var start = interact.util.timestamp();
                $.ajax({
                    url: interact.listen.url('/ajax/getSubscriber?id=' + id),
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

                        $('#subscriber-form-workEmailAddress').val(data.workEmailAddress);

                        Subscribers.clearAllAccessNumberRows();
                        for(var i = 0; i < data.accessNumbers.length; i++) {
                            Subscribers.addAccessNumberRow(data.accessNumbers[i].number, data.accessNumbers[i].messageLight, data.accessNumbers[i].numberType, data.accessNumbers[i].publicNumber);
                        }

                        if(data.voicemailPin !== null) {
                            $('#subscriber-form-voicemailPin').val(data.voicemailPin);
                        }

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

                        if(data.enableAdmin) {                        
                            $('#subscriber-form-isAdmin').attr('checked', true);
                        }

                        if(data.enableTranscription) {                        
                            $('#subscriber-form-transcription').attr('checked', true);
                        }

                        $('#subscriber-form-voicemailPlaybackOrder').val(data.voicemailPlaybackOrder);

                        $('#subscriber-form-add-button').hide();
                        $('#subscriber-form-edit-button').show();
                        $('#subscriber-form-cancel-button').show();
                    },
                    complete: function(xhr, textStatus) {
                        var elapsed = interact.util.timestamp() - start;
                        $('#latency').text(elapsed);
                    }
                });
            },

            resetForm: function() {
                interact.util.trace('Subscribers.resetForm');
                $('#subscriber-form')[0].reset();
                $('#subscriber-form-cancel-button').hide();
                $('#subscriber-form-edit-button').hide();
                $('#subscriber-form-add-button').show();
                $('#subscriber-form-accountType').val('Local');
                $('#subscriber-form-username').removeAttr('readonly').removeClass('disabled');
                $('#subscriber-form-password').removeAttr('readonly').removeClass('disabled');
                $('#subscriber-form-confirmPassword').removeAttr('readonly').removeClass('disabled');
                $('#subscriber-form-accountType').text('Local');
                Subscribers.clearAllAccessNumberRows();
            },

            addSubscriber: function() {
                interact.util.trace('Subscribers.addSubscriber');
                Subscribers.disableButtons();
                Server.post({
                    url: interact.listen.url('/ajax/addSubscriber'),
                    properties: {
                        username: $('#subscriber-form-username').val(),
                        password: $('#subscriber-form-password').val(),
                        confirmPassword: $('#subscriber-form-confirmPassword').val(),
                        realName: $('#subscriber-form-realName').val(),
                        workEmailAddress: $('#subscriber-form-workEmailAddress').val(),
                        accessNumbers: Subscribers.buildAccessNumberString(),
                        voicemailPin: $('#subscriber-form-voicemailPin').val(),
                        enableEmail: $('#subscriber-form-enableEmailNotification').is(":checked"),
                        enableSms: $('#subscriber-form-enableSmsNotification').is(":checked"),
                        emailAddress: $('#subscriber-form-emailAddress').val(),
                        smsAddress: $('#subscriber-form-smsAddress').val(),
                        enablePaging: $('#subscriber-form-paging').is(":checked"),
                        enableAdmin: $('#subscriber-form-isAdmin').is(":checked"),
                        enableTranscription: $('#subscriber-form-transcription').is(":checked"),
                        voicemailPlaybackOrder: $('#subscriber-form-voicemailPlaybackOrder').val()
                    },
                    successCallback: function() {
                        Subscribers.resetForm();
                        interact.listen.notifySuccess('Subscriber added');
                        Subscribers.enableButtons();
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },

            editSubscriber: function() {
                interact.util.trace('Subscribers.editSubscriber');
                Subscribers.disableButtons();
                Server.post({
                    url: interact.listen.url('/ajax/editSubscriber'),
                    properties: {
                        id: $('#subscriber-form-id').val(),
                        username: $('#subscriber-form-username').val(),
                        password: $('#subscriber-form-password').val(),
                        confirmPassword: $('#subscriber-form-confirmPassword').val(),
                        realName: $('#subscriber-form-realName').val(),
                        workEmailAddress: $('#subscriber-form-workEmailAddress').val(),
                        accessNumbers: Subscribers.buildAccessNumberString(),
                        voicemailPin: $('#subscriber-form-voicemailPin').val(),
                        enableEmail: $('#subscriber-form-enableEmailNotification').is(":checked"),
                        enableSms: $('#subscriber-form-enableSmsNotification').is(":checked"),
                        emailAddress: $('#subscriber-form-emailAddress').val(),
                        smsAddress: $('#subscriber-form-smsAddress').val(),
                        enablePaging: $('#subscriber-form-paging').is(":checked"),
                        enableAdmin: $('#subscriber-form-isAdmin').is(":checked"),
                        enableTranscription: $('#subscriber-form-transcription').is(":checked"),
                        voicemailPlaybackOrder: $('#subscriber-form-voicemailPlaybackOrder').val()
                    },
                    successCallback: function() {
                        Subscribers.resetForm();
                        interact.listen.notifySuccess('Subscriber updated');
                        Subscribers.enableButtons();
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },

            confirmDeleteSubscriber: function(id) {
                interact.util.trace('Subscribers.confirmDeleteSubscriber');
                if(confirm('Are you sure?')) {
                    Subscribers.deleteSubscriber(id);
                }
            },

            deleteSubscriber: function(id) {
                interact.util.trace('Subscribers.deleteSubscriber');
                Server.post({
                    url: interact.listen.url('/ajax/deleteSubscriber'),
                    properties: { id: id }
                });
            },

            disableButtons: function() {
                interact.util.trace('Subscribers.disableButtons');
                $('#subscriber-form button').attr('readonly', 'readonly');
            },

            enableButtons: function() {
                interact.util.trace('Subscribers.enableButtons');
                $('#subscriber-form button').removeAttr('readonly');
            },

            testEmailAddress: function() {
                interact.util.trace('Subscribers.testEmailAddress');
                Subscribers.testAddress('email', $('#subscriber-form-emailAddress').val());
            },

            testSmsAddress: function() {
                interact.util.trace('Subscribers.testSmsAddress');
                Subscribers.testAddress('sms', $('#subscriber-form-smsAddress').val());
            },

            testAddress: function(type, address) {
                Server.post({
                    url: interact.listen.url('/ajax/testNotificationSettings'),
                    properties: {
                        messageType: type,
                        address: address
                    },
                    successCallback: function() {
                        interact.listen.notifySuccess('Test notification sent to ' + address);
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },

            clearAllAccessNumberRows: function() {
                $('#subscriber-form-accessNumbersTable tbody tr').not(':last').remove();
            },

            addAccessNumberRow: function(number, messageLight, numberType, publicNumber) {
                var clone = $('#accessNumber-row-template').clone();
                clone.removeAttr('id');
                $('.accessNumber-row-number', clone).val(number);
                if(messageLight) {
                    $('.accessNumber-row-messageLight', clone).attr('checked', 'checked');
                } else {
                    $('.accessNumber-row-messageLight', clone).removeAttr('checked');
                }
                $('.accessNumber-row-numberType', clone).val(numberType);
                if(publicNumber) {
                    $('.accessNumber-row-publicNumber', clone).attr('checked', 'checked');
                } else {
                    $('.accessNumber-row-publicNumber', clone).removeAttr('checked');
                }
                $('.icon-delete', clone).click(function() {
                    $(this).parent().parent().remove();
                });
                $('#subscriber-form-accessNumbersTable tbody tr:last').before(clone);
            },

            buildAccessNumberString: function() {
                var value = '';
                var rows = $('#subscriber-form-accessNumbersTable tr');
                for(var i = 0; i < rows.length - 1; i++) {
                    var number = $('.accessNumber-row-number', rows[i]).val();
                    if(number.length == 0) {
                        continue;
                    }
                    var messageLight = $('.accessNumber-row-messageLight', rows[i]).is(':checked');
                    var numberType = $('.accessNumber-row-numberType', rows[i]).val();
                    var publicNumber = $('.accessNumber-row-publicNumber', rows[i]).is(':checked');
                    value += number + ':' + messageLight + ':' + numberType + ':' + publicNumber + ';';
                }
                if(value.length > 0) {
                    value = value.substring(0, value.length - 1); // remove last semicolon
                }
                return value;
            }
        }
    }();

    new Subscribers.Application().load();
});