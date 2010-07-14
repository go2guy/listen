$(document).ready(function() {
    LISTEN.HISTORY = function() {
        return {
            HistoryApplication: function() {
                LISTEN.trace('LISTEN.HISTORY.HistoryApplication [construct]');
                var first = 0;
                var max = 25;

                var historyList = new LISTEN.DynamicTable({
                    url: '/ajax/getHistoryList',
                    tableId: 'history-list',
                    isList: true,
                    templateId: function(dataRow) {
                        if(dataRow.type == 'Action') {
                            return 'history-action-template';
                        } else if(dataRow.type == 'Call') {
                            return 'history-call-template';
                        } else {
                            LISTEN.log('No template found for [' + dataRow.type + ']');
                            return false;
                        }
                    },
                    retrieveList: function(data) {
                        return data.results;
                    },
                    reverse: true,
                    paginationId: 'history-pagination',
                    updateRowCallback: function(row, data, animate) {
                        var c = 'history-row-' + data.service;
                        if(!row.hasClass(c)) {
                            row.removeClass();
                            row.addClass(c);
                        }
                        if(data.type == 'Action') {
                            LISTEN.setFieldContent(row.find('.history-action-date'), data.date, animate);
                            LISTEN.setFieldContent(row.find('.history-action-type'), '<div class="image-edit"></div>', animate, true);
                            LISTEN.setFieldContent(row.find('.history-action-subscriber'), data.subscriber, animate);

                            var description = '<b>' + data.action + '</b> [' + data.channel + '] - ' + data.description;
                            if(data.onSubscriber && data.onSubscriber.length > 0 && data.onSubscriber != data.subscriber) {
                                description += ' <i>for ' + data.onSubscriber + '</i>';
                            }
                            LISTEN.setFieldContent(row.find('.history-action-description'), description, animate, true);
                        } else if(data.type == 'Call') {
                            LISTEN.setFieldContent(row.find('.history-call-date'), data.date, animate);
                            LISTEN.setFieldContent(row.find('.history-call-type'), '<div class="image-outdial"></div>', animate, true);
                            LISTEN.setFieldContent(row.find('.history-call-subscriber'), data.subscriber, animate);

                            var description = '<b>' + data.ani + '</b> dialed <b>' + data.dnis + '</b> <i>(' + data.duration + ')</i>';
                            LISTEN.setFieldContent(row.find('.history-call-description'), description, animate, true);
                        }
                    }
                });

                this.load = function() {
                    LISTEN.trace('LISTEN.HISTORY.HistoryApplication.load');
                    historyList.pollAndSet();
                };

                this.unload = function() {
                    // no-op
                };
            }
        }
    }();

    var app = new LISTEN.HISTORY.HistoryApplication();
    LISTEN.registerApp(new LISTEN.Application('history', 'history-application', 'menu-history', app));
});