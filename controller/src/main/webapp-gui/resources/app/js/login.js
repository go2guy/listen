$(document).ready(function() {
    function initWatermark(field) {
        var setWatermark = function() {
            if($(this).val() == '') {
                var value = 'url(resources/app/images/new/' + $(this).attr('id') + '_watermark.png)';
                $(this).css('background-image', value);
            } else {
                $(this).css('background-image', "url('')");
            }
        };

        field.css('background-image', 'url(resources/app/images/new/' + field.attr('id') + '_watermark.png)');
        field.blur(setWatermark);
        field.focus(function() {
            $(this).css('background-image', "url('')");
        });
    }

    var username = $('#username');
    var password = $('#password');

    initWatermark(username);
    initWatermark(password);

    username.focus();
});