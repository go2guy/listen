$(document).ready(function() {
    LISTEN.HISTORY = function() {
        return {
            HistoryApplication: function() {
                LISTEN.trace('LISTEN.HISTORY.HistoryApplication [construct]');
                var interval;
                var historyList = new LISTEN.DynamicTable({
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
                    updateRowCallback: function(row, data) {
                        if(data.type == 'Action') {
                            LISTEN.setFieldContent(row.find('.history-action-date'), data.date);
                            LISTEN.setFieldContent(row.find('.history-action-type'), 'Action');
                            LISTEN.setFieldContent(row.find('.history-action-subscriber'), data.subscriber);
                            LISTEN.setFieldContent(row.find('.history-action-action'), data.action);
                            LISTEN.setFieldContent(row.find('.history-action-description'), data.description);
                            LISTEN.setFieldContent(row.find('.history-action-onSubscriber'), data.onSubscriber);
                            LISTEN.setFieldContent(row.find('.history-action-channel'), data.channel);
                        } else if(data.type == 'Call') {
                            LISTEN.setFieldContent(row.find('.history-call-date'), data.date);
                            LISTEN.setFieldContent(row.find('.history-call-type'), 'Call');
                            LISTEN.setFieldContent(row.find('.history-call-subscriber'), data.subscriber);
                            LISTEN.setFieldContent(row.find('.history-call-ani'), data.ani);
                            LISTEN.setFieldContent(row.find('.history-call-direction'), data.direction == 'INBOUND' ? '&laquo;' : '&raquo;', false, true);
                            LISTEN.setFieldContent(row.find('.history-call-dnis'), data.dnis);
                            LISTEN.setFieldContent(row.find('.history-call-service'), data.service);
                            LISTEN.setFieldContent(row.find('.history-call-duration'), data.duration + ' seconds');
                        }
                    }
                });

                var pollAndSet = function() {
                    LISTEN.trace('LISTEN.HISTORY.HistoryApplication.pollAndSet');
                    $.ajax({
                        url: '/ajax/getHistoryList',
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                            historyList.update(data);
                        }
                    });
                };

                this.load = function() {
                    LISTEN.trace('LISTEN.HISTORY.HistoryApplication.load');
                    pollAndSet();
                    interval = setInterval(function() {
                        pollAndSet();
                    }, 1000);
                };

                this.unload = function() {
                    LISTEN.trace('LISTEN.HISTORY.HistoryApplication.unload');
                    if(interval) {
                        clearInterval(interval);
                    }
                };
            }
        }
    }();

    var app = new LISTEN.HISTORY.HistoryApplication();
    LISTEN.registerApp(new LISTEN.Application('history', 'history-application', 'menu-history', app));
});