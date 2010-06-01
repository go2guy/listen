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

                this.menuOff = function() {
                    menuItem.removeClass('selected');
                };
                
                this.menuOn = function() {
                    menuItem.addClass('selected');
                };

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
                    currentApplication.menuOff();
                    to.menuOn();
                    currentApplication.swapWith(to);
                } else {
                    to.menuOn();
                    to.show();
                }
                currentApplication = to;
            },

            setContent: function(applicationName, content) {
                for(var i = 0; i < applications.length; i++) {
                    if(applications[i].name == applicationName) {
                        if(applications[i].content) {
                            applications[i].content.unload();
                        }
                        applications[i].content = content;
                        applications[i].content.load();
                        break;
                    }
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

// TODO namespace
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

// TODO namespace
function withLoadingIndicator(callback, callbackArgs) {
    var loading = $('#loading');
    loading.show();
    callback.apply(this, callbackArgs);
    loading.hide();
}