var LISTEN;
$(document).ready(function() {
    $.ajaxSetup({
        error: function(xhr, textStatus, errorThrown) {
            if(xhr && xhr.status == 401) {
                window.location = '/logout';
            } else {
                LISTEN.log('ERROR: textStatus = [' + textStatus + '], xhrStatus = [' + xhr.status + ']');
            }
        }
    });

    $('.tab-container').each(function(containerIndex, containerElement) {
        var tabs = $('.tabs li', containerElement);
        var panels = $('.tab-content, .tab-content-default', containerElement);

        panels.each(function(panelIndex, panelElement) {
            if($(panelElement).hasClass('tab-content-default')) {
                $(tabs[panelIndex]).addClass('current');
            }
            $(tabs[panelIndex]).click(function(event) {
                $(event.target).addClass('current');
                panels.each(function(i, e) {
                    if(i != panelIndex) {
                        $(e).hide();
                        $(tabs[i]).removeClass('current');
                    }
                });
                $(panelElement).show();
            });
        });
    });

    var failedTries = 0;
    setInterval(function() {
        var start = LISTEN.timestamp();
        $.ajax({
            url: '/meta/ping?auth=true',
            cache: 'false',
            success: function(data, textStatus, xhr) {
                if(!data || data != 'pong') {
                    if(failedTries++ > 2) {
                        $.modal($('#communication-error'), {
                            overlayCss: {
                                'background-color': '#CCCCCC',
                                'opacity': .5
                            }
                        });
                    }
                } else if(data == 'pong' && failedTries > 2) {
                    failedTries = 0;
                    $.modal.close();
                }
            },
            error: function(xhr, textStatus, errorThrown) {
                if(xhr && xhr.status == 401) {
                    window.location = '/logout';
                    return;
                }
                if(failedTries++ > 2) {
                    $.modal($('#communication-error'), {
                        overlayCss: {
                            'background-color': '#CCCCCC',
                            'opacity': .5
                        }
                    });
                }
            },
            complete: function(xhr, textStatus) {
                var elapsed = LISTEN.timestamp() - start;
                $('#pinglatency').text(elapsed);
            }
        });
    }, 2000);

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
             *  - updateRowCallback(row, data, animate): function callback that updates a specific row
             *  - retrieveList(data): function callback that returns the actual list of data from the provided data
             *  - templateId: id of row node containing template for a data row in this table, or function to call
             *                that retrieves the template id based on the data row
             */
            DynamicTable: function(args) {
                var args = args;
                var currentFirst = 0;
                var currentMax = 15;

                this.setUrl = function(url) {
                    args.url = url;
                };

                this.update = function(data, animate) {
                    var tableRows = [];
                    if(args.isList === true) {
                        tableRows = $('#' + args.tableId).find('li:not(.placeholder)');
                    } else {
                        tableRows = $('#' + args.tableId + ' tbody').find('tr:not(.placeholder)');
                    }
                    var serverList = args.retrieveList.call(this, data);
                    var ids = [];

                    if(args.countContainer && args.retrieveCount) {
                        var container = $('#' + args.countContainer);
                        var count = args.retrieveCount.call(this, data);
                        if(container.text() != count) {
                            container.text(count);
                        }
                    }

                    if(args.paginationId) {
                        var count = data.count;
                        var first = data.first;
                        var total = data.total;
                        var max = data.max;

                        var pagination = $('#' + args.paginationId);
                        $('.pagination-current', pagination).text((count > 0 ? first + 1 : '0') + '-' + (first + count));
                        $('.pagination-total', pagination).text(total);

                        var left = $('.icon-pageleft', pagination);
                        var right = $('.icon-pageright', pagination);

                        left.unbind('click');
                        right.unbind('click');
                        if(first > 0) {
                            left.click(LISTEN.bind(this, function() {
                                currentFirst = Math.max(first - max, 0);
                                currentMax = max;
                                this.pollAndSet(false);
                            }));
                            left.show();
                        } else {
                            left.hide();
                        }
                        if(first + count + 1 <= total) {
                            right.click(LISTEN.bind(this, function() {
                                currentFirst = first + count;
                                currentMax = max;
                                this.pollAndSet(false);
                            }));
                            right.show();
                        } else {
                            right.hide();
                        }
                    }

                    for(var i = (args.reverse ? serverList.length - 1 : 0); (args.reverse ? i >= 0 : i < serverList.length); (args.reverse ? i-- : i++)) {
                        var found = false;
                        var serverItem = serverList[i];
                        for(var j = 0; j < tableRows.length; j++) {
                            var tableRow = $(tableRows[j]);
                            if(tableRow.attr('id') == args.tableId + '-row-' + serverItem.id) {
                                args.updateRowCallback.call(this, tableRow, serverItem, animate);
                                found = true;
                                break;
                            }
                        }

                        if(!found) {
                            var templateId = args.templateId;
                            if(typeof templateId === 'function') {
                                templateId = templateId.call(this, serverItem);
                            }

                            var clone = $('#' + templateId).clone();
                            clone.attr('id', args.tableId + '-row-' + serverItem.id);
                            args.updateRowCallback.call(this, clone, serverItem, animate);
                            clone.css('opacity', 0);
//                            if(args.alternateRowColors) {
//                                if(args.reverse) {
//                                    clone.addClass((serverList.length - i) % 2 == 0 ? 'odd' : 'even');
//                                } else {
//                                    clone.addClass(i % 2 == 0 ? 'odd' : 'even');
//                                }
//                            }
                            var appendTo = (args.isList === true ? $('#' + args.tableId) : $('#' + args.tableId + ' tbody'));
                            if(args.reverse) {
                                appendTo.prepend(clone);
                            } else {
                                appendTo.append(clone);
                            }
                            clone.animate({ opacity: 1 }, (animate === true ? 1000 : 0));
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
                            row.animate({ opacity: 0 }, (animate === true ? 1000 : 0), function() {
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

                this.pollAndSet = function(animate) {
                    if(args.url) {
                        var url = args.url;
                        if(url.indexOf('?') < 0) {
                            url += '?';
                        } else {
                            url += '&';
                        }
                        url += 'first=' + currentFirst + '&max=' + currentMax;
                        var start = LISTEN.timestamp();
                        $.ajax({
                            url: url,
                            dataType: 'json',
                            cache: false,
                            success: LISTEN.bind(this, function(data, textStatus, xhr) {
                                this.update(data, animate);
                            }),
                            complete: function(xhr, textStatus) {
                                var elapsed = LISTEN.timestamp() - start;
                                $('#latency').text(elapsed);
                            }
                        });
                    } else {
                        LISTEN.log('Warning - DynamicTable.pollAndSet() invoked without args.url');
                    }
                };
            },

            highlight: function(elem) {
                var orig = elem.css('font-weight');
                if(orig == 'bold') {
                    return;
                }
                elem.css('font-weight', 'bold');
                setTimeout(function() {
                    elem.css('font-weight', orig);
                }, 1500);
            },

            setFieldContent: function(field, content, animate, asHtml) {
                var changed = false;
                if(asHtml === true) {
                    if(field.html() != content) {
                        field.html(content);
                        changed = true;
                    }
                } else {
                    var c = String(content);
                    if(field.text() != c) {
                        field.text(c);
                        changed = true;
                    }
                }
                if(changed && animate === true) {
                    LISTEN.highlight(field);
                }
            },

            getCurrentApplication: function() {
                return currentApplication;
            },

            registerApp: function(app) {
                applications.push(app);
                if(!currentApplication) {
                    LISTEN.trace('[' + app.name + '] is the first registered application - loading it for display');
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
                    if(withContent) {
                        toApp.content = withContent;
                    }
                    if(toApp.content) {
                        toApp.content.load();
                    }
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

            trace: function(message) {
                if(LISTEN.enableLogging) {
                    this.writeLog('TRACE: ' + message);
                }
            },

            log: function(message) {
                if(LISTEN.enableLogging) {
                    this.writeLog('LOG:   ' + message);
                }
            },

            writeLog: function(message) {
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
            },

            isDefined: function(variable) {
                return typeof variable != 'undefined'; 
            },

            timestamp: function() {
                return (new Date()).getTime();
            }
        };

        return pub;
    }();
});

// TODO namespace
function withLoadingIndicator(callback, callbackArgs) {
    var loading = $('#loading');
    loading.show();
    callback.apply(this, callbackArgs);
    loading.hide();
}