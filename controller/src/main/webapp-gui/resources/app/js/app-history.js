var interact = interact || {};
var History;
$(document).ready(function() {
    History = function() {
        return {
            Application: function() {
                interact.util.trace('History.Application [construct]');
                var first = 0;
                var max = 25;

                var historyList = new interact.util.DynamicTable({
                    url: interact.listen.url('/ajax/getHistoryList'),
                    tableId: 'history-list',
                    isList: true,
                    templateId: function(dataRow) {
                        if(dataRow.type == 'Action') {
                            return 'history-action-template';
                        } else if(dataRow.type == 'Call') {
                            return 'history-call-template';
                        } else {
                            interact.util.error('No template found for [' + dataRow.type + ']');
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
                            interact.util.setFieldContent(row.find('.history-action-date'), data.date, animate);
                            interact.util.setFieldContent(row.find('.history-action-type'), '<div class="image-edit"></div>', animate, true);
                            interact.util.setFieldContent(row.find('.history-action-subscriber'), data.subscriber, animate);

                            var description = '<b>' + data.action + '</b> [' + data.channel + '] - ' + data.description;
                            interact.util.setFieldContent(row.find('.history-action-description'), description, animate, true);
                        } else if(data.type == 'Call') {
                            interact.util.setFieldContent(row.find('.history-call-date'), data.date, animate);
                            interact.util.setFieldContent(row.find('.history-call-type'), '<div class="image-outdial"></div>', animate, true);
                            interact.util.setFieldContent(row.find('.history-call-subscriber'), data.subscriber, animate);

                            var description = '<b>' + data.ani + '</b> dialed <b>' + data.dnis + '</b> <i>(' + data.duration + ')</i>';
                            interact.util.setFieldContent(row.find('.history-call-description'), description, animate, true);
                        }
                    }
                });

                this.load = function() {
                    interact.util.trace('History.Application.load');
                    historyList.pollAndSet();
                };
            }
        }
    }();

    new History.Application().load();
});