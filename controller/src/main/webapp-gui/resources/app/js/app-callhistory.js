$(document).ready(function() {
    LISTEN.CALLHISTORY = function() {
        return {
            CallHistoryApplication: function() {
                LISTEN.trace('LISTEN.CALLHISTORY.CallHistoryApplication [construct]');
                var cdrInterval, historyInterval;
                var callHistoryTable = new LISTEN.DynamicTable({
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

                var subscriberHistoryTable = new LISTEN.DynamicTable({
                    tableId: 'subscriberhistory-table',
                    templateId: 'subscriberhistory-row-template',
                    retrieveList: function(data) {
                        return data;
                    },
                    reverse: true,
                    updateRowCallback: function(row, data) {
                        var dateCell = row.find('.subscriberhistory-cell-date');
                        if(dateCell.text() != data.date) {
                            dateCell.text(data.date);
                        }

                        var subscriberCell = row.find('.subscriberhistory-cell-subscriber');
                        if(subscriberCell.text() != data.subscriber) {
                            subscriberCell.text(data.subscriber);
                        }

                        var actionCell = row.find('.subscriberhistory-cell-action');
                        if(actionCell.text() != data.action) {
                            actionCell.text(data.action);
                        }

                        var descriptionCell = row.find('.subscriberhistory-cell-description');
                        if(descriptionCell.text() != data.description) {
                            descriptionCell.text(data.description);
                        }

                        var onSubscriberCell = row.find('.subscriberhistory-cell-onSubscriber');
                        if(onSubscriberCell.text() != data.onSubscriber) {
                            onSubscriberCell.text(data.onSubscriber);
                        }

                        var channelCell = row.find('.subscriberhistory-cell-channel');
                        if(channelCell.text() != data.channel) {
                            channelCell.text(data.channel);
                        }
                    }
                });

                var pollAndSetCdrs = function() {
                    LISTEN.trace('LISTEN.CALLHISTORY.CallHistoryApplication.pollAndSetCdrs');
                    $.ajax({
                        url: '/ajax/getCallHistoryList',
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                            callHistoryTable.update(data);
                        }
                    });
                };

                var pollAndSetSubscriberHistory = function() {
                    LISTEN.trace('LISTEN.CALLHISTORY.CallHistoryApplication.pollAndSetSubscriberHistory');
                    $.ajax({
                        url: '/ajax/getSubscriberHistoryList',
                        dataType: 'json',
                        cache: 'false',
                        success: function(data, textStatus, xhr) {
                            subscriberHistoryTable.update(data);
                        }
                    });
                };

                this.load = function() {
                    LISTEN.trace('LISTEN.CALLHISTORY.CallHistoryApplication.load');
                    pollAndSetCdrs();
                    cdrInterval = setInterval(function() {
                        pollAndSetCdrs();
                    }, 1000);

                    pollAndSetSubscriberHistory();
                    subscriberHistoryInterval = setInterval(function() {
                        pollAndSetSubscriberHistory();
                    }, 1000);
                };

                this.unload = function() {
                    LISTEN.trace('LISTEN.CALLHISTORY.CallHistoryApplication.unload');
                    if(cdrInterval) {
                        clearInterval(cdrInterval);
                    }
                    if(subscriberHistoryInterval) {
                        clearInterval(subscriberHistoryInterval)
                    }
                };
            }
        }
    }();

    var app = new LISTEN.CALLHISTORY.CallHistoryApplication();
    LISTEN.registerApp(new LISTEN.Application('callhistory', 'callhistory-application', 'menu-callhistory', app));
});