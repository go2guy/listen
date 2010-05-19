var LISTEN;
$(document).ready(function() {
    $.ajaxSetup({
        error: function(xhr, textStatus, errorThrown) {
            if(xhr && xhr.status == 401) {
                window.location = '/logout';
            }
        }
    });

    LISTEN = function() {

        var applications = [];
        var currentApplication;

        var pub = {

            Application: function(name, windowId, menuId, position, content) {
                this.name = name;
                var windowId = windowId;
                var menuId = menuId;
                var position = position;
                this.content = content;

                var windowDiv = $('#' + windowId);
                var menuItem = $('#' + menuId);
                if(menuItem && windowDiv) {
                    var application = this;
                    menuItem.click(function() {
                        // use 'application' since 'this' will be in the function scope, not the parent object's scope
                        pub.switchApp(application);
                    });
                }

                this.getPosition = function() {
                    return position;
                };

                this.swapWith = function(other) {
                    if(this === other) {
                        return;
                    }
                    this.hide(position < other.getPosition() ? 'left' : 'right', function() {
                        other.show(position < other.getPosition() ? 'right' : 'left');
                    });
                };

                this.hide = function(direction, callback) {
                    if(callback) {
                        windowDiv.hide('slide', { direction: direction ? direction : 'left' }, 250, callback);
                    } else {
                        windowDiv.hide('slide', { direction: direction ? direction : 'left' }, 250);
                    }
                    if(this.content) {
                        this.content.unload();
                    }
                };

                this.show = function(direction) {
                    if(this.content) {
                        this.content.load();
                    }
                    windowDiv.show('slide', { direction: direction ? direction : 'right' }, 250);
                };
            },

            registerApp: function(app) {
                applications.push(app);
            },

            switchApp: function(to) {
                if(currentApplication) {
                    currentApplication.swapWith(to);
                } else {
                    to.show();
                }
                currentApplication = to;
            },

            setContent: function(applicationName, content) {
                var application;
                for(var app in applications) {
                    if(app.getName() == applicationName) {
                        application = app;
                        break;
                    }
                }
                if(application) {
                    if(application.content) {
                        application.content.unload();
                    }
                    application.content = content;
                    application.content.load();
                }
            }
        };

        return pub;
    }();

    $('#main-menu-handle').click(function() {
        $('#main-menu').animate({
            height: 'toggle'
        }, 500);
    });
});

function notify(message, isError, stay) {
    var div = $('#notification');
    if(isError === true) {
        div.addClass('error');
    } else {
        div.removeClass('error');
    }
    $('#notification').text(message);
    $('#notification').slideDown(200);

    if(stay !== true) {
        setTimeout(function() {
            $('#notification').slideUp(200);
        }, 3000);
    }
}