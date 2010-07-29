$(document).ready(function() {
    var application = new SystemConfigurationApplication();

    LISTEN.registerApp(new LISTEN.Application('sysconfig', 'sysconfig-application', 'menu-sysconfig', application));

    function SystemConfigurationApplication() {
        this.load = function() {
            LISTEN.log('Loading system configuration');
            var start = LISTEN.timestamp();
            $.ajax({
                url: '/ajax/getProperties',
                dataType: 'json',
                cache: false,
                success: function(data, textStatus, xhr) {
                    $('#smtp-server').val(data['com.interact.listen.mail.smtpHost']);
                    $('#smtp-username').val(data['com.interact.listen.mail.smtpUsername']);
                    $('#smtp-password').val(data['com.interact.listen.mail.smtpPassword']);
                    $('#from-address').val(data['com.interact.listen.mail.fromAddress']);

                    clearAllDnisRows();
                    var dnis = data['com.interact.listen.dnisMapping'];
                    var mappings = dnis.split(';');
                    for(var i = 0; i < mappings.length; i++) {
                        var mapping = mappings[i].split(':');
                        addDnisRow(mapping[0], mapping[1]);
                    }

                    $('#conferencing-configuration-pinLength').val(data['com.interact.listen.conferencing.pinLength']);
                    $('#alerts-configuration-realizeUrl').val(data['com.interact.listen.realizeUrl']);
                    $('#alerts-configuration-realizeAlertName').val(data['com.interact.listen.realizeAlertName']);
                },
                complete: function(xhr, textStatus) {
                    var elapsed = LISTEN.timestamp() - start;
                    $('#latency').text(elapsed);
                }
            });
        };

        this.unload = function() {
            LISTEN.log('Unloading system configuration');
            // no-op
        };
    };

    function clearAllDnisRows() {
        $('#dnis-mapping-form tbody tr').not(':last').remove();
    };

    function addDnisRow(number, destination) {
        var n = (number ? number : '');
        var d = (destination ? destination : '');

        var clone = $('#dnis-row-template').clone();
        $('.dnis-mapping-number', clone).val(n);

        $('#dnis-mapping-form tbody tr:last').before(clone);
        if(d != 'voicemail' && d != 'mailbox' && d != 'conferencing' && d != 'directVoicemail') { // assume custom
            $('select option[value=\'custom\']', clone).attr('selected', 'selected');
            $('.dnis-mapping-custom-destination', clone).val(d).show();
        } else {
            $('select option[value=\'' + d + '\']', clone).attr('selected', 'selected');
        }
        $('.icon-delete', clone).click(function() {
            $(this).parent().parent().remove();
        });
        $('select', clone).change(function() {
            var input = $('.dnis-mapping-custom-destination', $(this).parent().parent());
            if($(this).val() == 'custom') {
                input.show();
            } else {
                input.hide().val('');
            }
        });
    }

    $('#add-dnis-mapping').click(function() {
        addDnisRow();
        return false;
    });

    $('#mail-form').submit(function() {
        $('#mail-form .form-error-message').text('').hide();
        var start = LISTEN.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/setProperties',
            data: { 'com.interact.listen.mail.smtpHost': $('#smtp-server').val(),
                    'com.interact.listen.mail.smtpUsername': $('#smtp-username').val(),
                    'com.interact.listen.mail.smtpPassword': $('#smtp-password').val(),
                    'com.interact.listen.mail.fromAddress': $('#from-address').val() },
            success: function(data) {
                application.load();
                var elem = $('#mail-form .form-success-message')
                elem.text('Mail settings updated').slideDown(100);
                setTimeout(function() {
                    elem.slideUp(100);
                }, 2000);
            },
            error: function(xhr) {
                $('#mail-form .form-error-message').text(xhr.responseText).slideDown(100);
            },
            complete: function(xhr, textStatus) {
                var elapsed = LISTEN.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
        return false;
    });

    $('#dnis-mapping-form').submit(function() {
        $('#dnis-mapping-form .form-error-message').text('').hide();
        var value = '';
        var rows = $('#dnis-mapping-form tr');
        var num = 0;
        for(var i = 0; i < rows.length - 1; i++) {
            var entry = '';
            var number = $('.dnis-mapping-number', rows[i]).val();
            if(number.length == 0) {
                continue;
            }
            var destination = $('select', rows[i]).val();
            entry += number + ':';
            if(destination != 'voicemail' && destination != 'mailbox' && destination != 'conferencing' && destination != 'directVoicemail') {
                var customVal = $('.dnis-mapping-custom-destination', rows[i]).val();
                if(customVal == '') {
                    continue;
                }
                entry += customVal;
            } else {
                entry += destination;
            }
            entry += ';';
            value += entry;
            num++;
        }
        if(num > 0 && value.length > 0) {
            value = value.substring(0, value.length - 1); // remove last semicolon
        }

        var start = LISTEN.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/setProperties',
            data: { 'com.interact.listen.dnisMapping': value },
            success: function(data) {
                application.load();
                var elem = $('#dnis-mapping-form .form-success-message')
                elem.text('DNIS mappings updated').slideDown(100);
                setTimeout(function() {
                    elem.slideUp(100);
                }, 2000);
            },
            error: function(xhr) {
                $('#dnis-mapping-form .form-error-message').text(xhr.responseText).slideDown(100);
            },
            complete: function(xhr, textStatus) {
                var elapsed = LISTEN.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });

        return false;
    });

    $('#conferencing-configuration-form').submit(function() {
        $('#conferencing-configuration-form .form-error-message').text('').hide();
        var start = LISTEN.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/setProperties',
            data: { 'com.interact.listen.conferencing.pinLength': $('#conferencing-configuration-pinLength').val() },
            success: function(data) {
                application.load();
                var elem = $('#conferencing-configuration-form .form-success-message')
                elem.text('Conferencing settings updated').slideDown(100);
                setTimeout(function() {
                    elem.slideUp(100);
                }, 2000);
            },
            error: function(xhr) {
                $('#conferencing-configuration-form .form-error-message').text(xhr.responseText).slideDown(100);
            },
            complete: function(xhr, textStatus) {
                var elapsed = LISTEN.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
        return false;
    });

    $('#alerts-configuration-form').submit(function() {
        $('#alerts-configuration-form .form-error-message').text('').hide();
        var start = LISTEN.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/setProperties',
            data: { 'com.interact.listen.realizeUrl': $('#alerts-configuration-realizeUrl').val(),
                    'com.interact.listen.realizeAlertName': $('#alerts-configuration-realizeAlertName').val() },
            success: function(data) {
                application.load();
                var elem = $('#alerts-configuration-form .form-success-message')
                elem.text('Alert settings updated').slideDown(100);
                setTimeout(function() {
                    elem.slideUp(100);
                }, 2000);
            },
            error: function(xhr) {
                $('#alerts-configuration-form .form-error-message').text(xhr.responseText).slideDown(100);
            },
            complete: function(xhr, textStatus) {
                var elapsed = LISTEN.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
        return false;
    });
});
