$(document).ready(function() {
    function initWatermark(field) {
        var setWatermark = function() {
            if($(this).val() == '') {
                $(this).css('background-image', "url('./resources/app/images/new/" + $(this).attr('id') + "_watermark.png')");
            } else {
                $(this).css('background-image', 'inherit');
            }
        };

        field.css('background-image', "url('./resources/app/images/new/" + field.attr('id') + "_watermark.png')");
        field.blur(setWatermark);
        field.change(setWatermark);
        field.keyup(setWatermark);
    }

    var username = $('#username');
    var password = $('#password');

    initWatermark(username);
    initWatermark(password);

    username.focus();
});