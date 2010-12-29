var interact = interact || {};
$(document).ready(function() {
    interact.util = {
        enableLogging: true,
    
        /**
         * Writes a 'trace' log to the console, if available.
         */
        trace: function(message) {
            if(interact.util.enableLogging) {
                this.writeLog('TRACE: ' + message);
            }
        },

        /**
         * Writes a 'debug' log to the console, if available.
         */
        debug: function(message) {
            if(interact.util.enableLogging) {
                this.writeLog('DEBUG:   ' + message);
            }
        },

        /**
         * Writes an 'error' log to the console, if available.
         */
        error: function(message) {
            this.writeLog('ERROR: ' + message);
        },

        /**
         * Writes an unlabeled log (i.e. no severity label) to the console, if available.
         */
        writeLog: function(message) {
            try {
                console.log(message);
                return true;
            } catch(e) {
                try {
                    opera.postError(message);
                    return true;
                } catch(e2) { }
            }
        },
    
        /**
         * Binds the passed scope to the provided function. What this effectively does is make the passed
         * "scope" variable accessible via "this" when the passed function is invoked.
         */
        bind: function(scope, fn) {
            return function () {
                fn.apply(scope, arguments);
            };
        },

        /**
         * Whether or not the provided variable has been defined / is available.
         */
        isDefined: function(variable) {
            return typeof variable != 'undefined'; 
        },

        /**
         * Returns a unix timestamp for the current time.
         */
        timestamp: function() {
            return (new Date()).getTime();
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
         *  - updateFinishedCallback: function callback to invoke when the table finishes updating
         */
        DynamicTable: function(args) {
            var args = args;
            var currentFirst = 0;
            var currentMax = (args.initialMax > 0 ? args.initialMax : 15);
            var queryParameters = {};
            var reverse = args.reverse === true;

            this.setUrl = function(url) {
                args.url = url;
            };

            this.setQueryParameter = function(key, value) {
                queryParameters[key] = value;
            };

            this.removeQueryParameter = function(key, value) {
                delete queryParameters[key];
            };

            this.setReverse = function(r) {
                reverse = r;
            }

            this.update = function(data, animate) {
                var tableRows = [];
                if(args.isList === true) {
                    tableRows = $('#' + args.tableId).children(':not(.placeholder)');
                } else {
                    tableRows = $('#' + args.tableId + ' tbody').children(':not(.placeholder)');
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
                        left.click(interact.util.bind(this, function() {
                            currentFirst = Math.max(first - max, 0);
                            currentMax = max;
                            this.pollAndSet(false);
                        }));
                        left.show();
                    } else {
                        left.hide();
                    }
                    if(first + count + 1 <= total) {
                        right.click(interact.util.bind(this, function() {
                            currentFirst = first + count;
                            currentMax = max;
                            this.pollAndSet(false);
                        }));
                        right.show();
                    } else {
                        right.hide();
                    }
                }

                for(var i = (reverse ? serverList.length - 1 : 0); (reverse ? i >= 0 : i < serverList.length); (reverse ? i-- : i++)) {
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
                        var appendTo = (args.isList === true ? $('#' + args.tableId) : $('#' + args.tableId + ' tbody'));
                        if(reverse) {
                            appendTo.prepend(clone);
                        } else {
                            appendTo.append(clone);
                        }
                        $('#' + args.tableId).find('.placeholder').hide();
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
                            if(serverList.length == 0) {
                                $('#' + args.tableId).find('.placeholder').show();
                            }
                        });
                    }
                }
                
                if(args.updateFinishedCallback) {
                    args.updateFinishedCallback.call(this, data);
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

                    // append any additional query parameters
                    for(var parameter in queryParameters) {
                        if(queryParameters.hasOwnProperty(parameter)) {
                            url += '&' + parameter + '=' + queryParameters[parameter];
                        }
                    }

                    var start = interact.util.timestamp();
                    $.ajax({
                        url: url,
                        dataType: 'json',
                        cache: false,
                        success: interact.util.bind(this, function(data, textStatus, xhr) {
                            this.update(data, animate);
                        }),
                        complete: function(xhr, textStatus) {
                            var elapsed = interact.util.timestamp() - start;
                            $('#latency').text(elapsed);
                        }
                    });
                } else {
                    interact.util.error('Warning - DynamicTable.pollAndSet() invoked without args.url');
                }
            };

            this.clear = function() {
                if(args.isList === true) {
                    $('#' + args.tableId).children(':not(.placeholder)').remove();
                } else {
                    $('#' + args.tableId + ' tbody').children(':not(.placeholder)').remove();
                }
            };
        }, /* DynamicTable */
        
        /**
         * Sets the provided content into the provided field. Will animate the change if animate is true.
         * If asHtml is true, will attempt to set the contents as HTML (otherwise they'll be set as text).
         */
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
                interact.util.highlight(field);
            }
        },
        
        /**
         * Highlights an element for a short duration by setting the text contents bold.
         */
        highlight: function(elem) {
            var orig = elem.css('font-weight');
            if(orig == 'bold') {
                return;
            }
            elem.css('font-weight', 'bold');
            setTimeout(function() {
                elem.css('font-weight', orig);
            }, 1500);
        }
    };
});