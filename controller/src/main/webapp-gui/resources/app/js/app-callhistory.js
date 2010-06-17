$(document).ready(function() {
    LISTEN.CALLHISTORY = function() {
        return {
            CallHistoryApplication: function() {
                LISTEN.trace('LISTEN.CALLHISTORY.CallHistoryApplication [construct]');
                var interval;
                var dynamicTable = new LISTEN.DynamicTable({
                    tableId: 'callhistory-table',
                    templateId: 'callhistory-row-template',
                    retrieveList: function(data) {
                        return data;
                    },
                    reverse: true,
                    updateRowCallback: function(row, data) {
                        var dateCell = row.find('.callhistory-cell-date');
                        if(dateCell.text() != data.date) {
                            dateCell.text(data.date);
                        }

                        var subscriberCell = row.find('.callhistory-cell-subscriber');
                        if(subscriberCell.text() != data.subscriber) {
                            subscriberCell.text(data.subscriber);
                        }

                        var serviceCell = row.find('.callhistory-cell-service');
                        if(serviceCell.text() != data.service) {
                            serviceCell.text(data.service);
                        }

                        var durationCell = row.find('.callhistory-cell-duration');
                        if(durationCell.text() != data.duration) {
                            durationCell.text(data.duration);
                        }

                        var aniCell = row.find('.callhistory-cell-ani');
                        if(aniCell.text() != data.ani) {
                            aniCell.text(data.ani);
                        }

                        var dnisCell = row.find('.callhistory-cell-dnis');
                        if(dnisCell.text() != data.dnis) {
                            dnisCell.text(data.dnis);
                        }
                    }
                });

                var pollAndSet = function() {
                    LISTEN.trace('LISTEN.CALLHISTORY.CallHistoryApplication.pollAndSet');
                    $.ajax({
                        url: '/ajax/getCallHistoryList',
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                            dynamicTable.update(data);
                        }
                    });
                };

                this.load = function() {
                    LISTEN.trace('LISTEN.CALLHISTORY.CallHistoryApplication.load');
                    pollAndSet();
                    interval = setInterval(function() {
                        pollAndSet();
                    }, 1000);
                };

                this.unload = function() {
                    LISTEN.trace('LISTEN.CALLHISTORY.CallHistoryApplication.unload');
                    if(interval) {
                        clearInterval(interval);
                    }
                };
            }
        }
    }();

    var app = new LISTEN.CALLHISTORY.CallHistoryApplication();
    LISTEN.registerApp(new LISTEN.Application('callhistory', 'callhistory-application', 'menu-callhistory', app));
});