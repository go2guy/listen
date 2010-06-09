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

            enableLogging: true,

            bind: function(scope, fn) {
                return function () {
                    fn.apply(scope, arguments);
                };
            },

            Application: function(name, windowId, menuId, content) {
                this.name = name;
                var windowId = windowId;
                var menuId = menuId;
                this.content = content;

                var windowDiv = $('#' + windowId);
                var menuItem = $('#' + menuId);
                if(menuItem && windowDiv) {
                    menuItem.click(LISTEN.bind(this, function() {
                        pub.switchApp(this);
                    }));
                }

                this.menuOff = function() {
                    menuItem.removeClass('current');
                };

                this.menuOn = function() {
                    menuItem.addClass('current');
                };

                this.swapWith = function(other, withContent) {
                    LISTEN.log('Swapping, this = [' + this.name + '], other = [' + other.name + ']');
                    if(this === other) {
                        LISTEN.log('Tried to switch to same application, no switch will be performed');
                        return;
                    }
                    this.hide(LISTEN.bind(this, function() {
                        if(this.content) {
                            this.content.unload();
                        }
                        if(withContent) {
                            other.content = withContent;
                        }
                        if(other.content) {
                            other.content.load();
                        }
                        other.show();
                    }));
                };

                this.hide = function(callback) {
                    if(callback) {
                        windowDiv.hide(0, callback);
                    } else {
                        windowDiv.hide();
                    }
                };

                this.show = function() {
                    windowDiv.show();
                };
            },

            /**
             * Given a set of data, adds/updates/removes rows from a table.
             * Available args:
             *  - tableId: id of table node to update
             *  - countContainer: id of node that should be updated with the row count (optional)
             *  - retrieveCount(data): function callback that returns the row count from the data (optional)
             *  - reverse: whether or not to reverse the table order, putting the last rows in the data first
             *             (optional, default = false)
             *  - updateRowCallback(row, data, setId): function callback that updates a specific row
             *  - retrieveList(data): function callback that returns the actual list of data from the provided data
             *  - templateId: id of row node containing template for a data row in this table
             */
            DynamicTable: function(args) {
                var interval;
                var args = args;

                this.update = function(data, withAnimation) {
                    var tableRows = $('#' + args.tableId + ' tbody').find('tr:not(.placeholder)');
                    var serverList = args.retrieveList.call(this, data);
                    var ids = [];

                    if(args.countContainer && args.retrieveCount) {
                        var container = $('#' + args.countContainer);
                        var count = args.retrieveCount.call(this, data);
                        if(container.text() != count) {
                            container.text(count);
                        }
                    }

                    for(var i = (args.reverse ? serverList.length - 1 : 0); (args.reverse ? i >= 0 : i < serverList.length); (args.reverse ? i-- : i++)) {
                        var found = false;
                        var serverItem = serverList[i];
                        for(var j = 0; j < tableRows.length; j++) {
                            var tableRow = $(tableRows[j]);
                            if(tableRow.attr('id') == args.tableId + '-row-' + serverItem.id) {
                                args.updateRowCallback.call(this, tableRow, serverItem, false);
                                found = true;
                                break;
                            }
                        }

                        if(!found) {
                            var clone = $('#' + args.templateId).clone();
                            clone.attr('id', args.tableId + '-row-' + serverItem.id);
                            args.updateRowCallback.call(this, clone, serverItem, true);
                            clone.css('opacity', 0);
                            if(args.alternateRowColors) {
                                if(args.reverse) {
                                    clone.addClass((serverList.length - i) % 2 == 0 ? 'odd' : 'even');
                                } else {
                                    clone.addClass(i % 2 == 0 ? 'odd' : 'even');
                                }
                            }
                            if(args.reverse) {
                                $('#' + args.tableId + ' tbody').prepend(clone);
                            } else {
                                $('#' + args.tableId + ' tbody').append(clone);
                            }
                            clone.animate({ opacity: 1 }, (withAnimation === true ? 1000 : 0));
                        }

                        ids.push(args.tableId + '-row-' + serverItem.id);
                    }

                    // remove table rows that no longer exist on the server
                    for(var i = 0; i < tableRows.length; i++) {
                        var found = false;
                        var row = $(tableRows[i]);
                        for(var j = 0; j < ids.length; j++) {
                            if(row.attr('id') == ids[j]) {
                                found = true;
                                break;
                            }
                        }

                        if(!found) {
                            row.animate({ opacity: 0 }, (withAnimation ? 1000 : 0), function() {
                                $(this).remove();
                            });
                        }
                    }

                    if(serverList.length == 0) {
                        $('#' + args.tableId).find('.placeholder').show();
                    } else {
                        $('#' + args.tableId).find('.placeholder').hide();
                    }
                };
            },

            getCurrentApplication: function() {
                return currentApplication;
            },

            registerApp: function(app) {
                applications.push(app);
                // if this is the first app registered, go ahead and load it
                if(!currentApplication) {
                    this.switchApp(app);
                }
            },

            switchApp: function(to, withContent) {
                var toApp = to;
                if(typeof to === "string") {
                    for(var i = 0; i < applications.length; i++) {
                        if(applications[i].name == to) {
                            toApp = applications[i];
                            break;
                        }
                    }
                }
                if(currentApplication) {
                    currentApplication.menuOff();
                    toApp.menuOn();
                    currentApplication.swapWith(toApp, withContent);
                } else {
                    toApp.menuOn();
                    toApp.show();
                }
                currentApplication = toApp;
            },

            notify: function(message, isError, stay) {
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
            },

            log: function(message) {
                if(LISTEN.enableLogging) {
                    try {
                        console.log(message);
                        return true;
                    } catch(e) {
                        try {
                            opera.postError(message);
                            return true;
                        } catch(e2) { }
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
function withLoadingIndicator(callback, callbackArgs) {
    var loading = $('#loading');
    loading.show();
    callback.apply(this, callbackArgs);
    loading.hide();
}