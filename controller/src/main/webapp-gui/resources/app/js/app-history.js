$(document).ready(function() {
    LISTEN.HISTORY = function() {
        return {
            HistoryApplication: function() {
                LISTEN.trace('LISTEN.HISTORY.HistoryApplication [construct]');
                var first = 0;
                var max = 50;

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
                            LISTEN.setFieldContent(row.find('.history-call-direction'), '&raquo;', false, true);
                            LISTEN.setFieldContent(row.find('.history-call-dnis'), data.dnis);
                            LISTEN.setFieldContent(row.find('.history-call-service'), data.service);
                            LISTEN.setFieldContent(row.find('.history-call-duration'), data.duration);
                        }
                    }
                });

                var pollAndSet = function() {
                    LISTEN.trace('LISTEN.HISTORY.HistoryApplication.pollAndSet');
                    $.ajax({
                        url: '/ajax/getHistoryList?first=' + first + '&max=' + max,
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                            $('#history-list').find('li').remove();
                            historyList.update(data, false);

                            var pagination = $('#history-pagination');
                            $('.pagination-current', pagination).text((data.count > 0 ? data.first + 1 : '0') + '-' + (data.first + data.count));
                            $('.pagination-total', pagination).text(data.total);

                            var left = $('.pagination-left', pagination);
                            var right = $('.pagination-right', pagination);

                            left.unbind('click');
                            right.unbind('click');
                            if(data.first > 0) {
                                left.click(function() {
                                    first = Math.max(data.first - data.max, 0);
                                    max = data.max;
                                    pollAndSet();
                                });
                                left.show();
                            } else {
                                left.hide();
                            }
                            if((data.first + data.count + 1) <= data.total) {
                                right.click(function() {
                                    first = data.first + data.count;
                                    max = data.max;
                                    pollAndSet();
                                });
                                right.show();
                            } else {
                                right.hide();
                            }
                        }
                    });
                };

                this.load = function() {
                    LISTEN.trace('LISTEN.HISTORY.HistoryApplication.load');
                    pollAndSet();
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