$(document).ready(function() {
    var application = new SystemConfigurationApplication();

    LISTEN.registerApp(new LISTEN.Application('sysconfig', 'sysconfig-application', 'menu-sysconfig', application));

    function SystemConfigurationApplication() {
        this.load = function() {
            LISTEN.log('Loading system configuration');
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
        $('input', clone).val(n);
        $('select option[value=\'' + d + '\']', clone).attr('selected', 'selected');
        $('.delete-button', clone).click(function() {
            $(this).parent().parent().remove();
        });
        $('#dnis-mapping-form tbody tr:last').before(clone);
    }

    $('#add-dnis-mapping').click(function() {
        addDnisRow();
        return false;
    });

    $('#mail-form').submit(function() {
        $.ajax({
            type: 'POST',
            url: '/ajax/setProperties',
            data: { 'com.interact.listen.mail.smtpHost': $('#smtp-server').val(),
                    'com.interact.listen.mail.smtpUsername': $('#smtp-username').val(),
                    'com.interact.listen.mail.smtpPassword': $('#smtp-password').val(),
                    'com.interact.listen.mail.fromAddress': $('#from-address').val() },
            success: function(data) {
                application.load();
                LISTEN.notify('Mail settings updated');
            }
        });
        return false;
    });

    $('#dnis-mapping-form').submit(function() {
        var value = '';
        var rows = $('#dnis-mapping-form tr');
        var num = 0;
        for(var i = 0; i < rows.length - 1; i++) {
            var number = $('input:text', rows[i]).val();
            if(number.length == 0) {
                continue;
            }
            var destination = $('select', rows[i]).val();
            value += number + ':' + destination + ';';
            num++;
        }
        if(num > 0 && value.length > 0) {
            value = value.substring(0, value.length - 1); // remove last semicolon
        }

        $.ajax({
            type: 'POST',
            url: '/ajax/setProperties',
            data: { 'com.interact.listen.dnisMapping': value },
            success: function(data) {
                application.load();
                LISTEN.notify('DNIS mappings updated');
            }
        });

        return false;
    });
});
