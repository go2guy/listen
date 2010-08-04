$(document).ready(function() {

    Listen.Conferences = function() {
        return {
            Application: function() {
                Listen.trace('Listen.Conferences.Application [construct]');
                var interval;
                var dynamicTable = new Listen.DynamicTable({
                    url: '/ajax/getConferenceList',
                    tableId: 'conference-list-table',
                    templateId: 'conference-row-template',
                    retrieveList: function(data) {
                        return data.results;
                    },
                    paginationId: 'conference-list-pagination',
                    updateRowCallback: function(row, data, animate) {
                        Listen.setFieldContent(row.find('.conference-cell-description'), data.description, animate);
                        var status = data.isStarted ? 'Started' : 'Not Started';
                        if(data.isStarted) {
                            status += ' ' + data.startDate;
                        }
                        Listen.setFieldContent(row.find('.conference-cell-status'), status, animate);
                        Listen.setFieldContent(row.find('.conference-cell-callerCount'), data.callerCount, animate);
                        Listen.setFieldContent(row.find('.conference-cell-view'), '<button class="button-view" onclick="Listen.Conferences.viewConference(' + data.id + ');">View</button>', false, true);
                    }
                });

                this.load = function() {
                    Listen.trace('Loading conferences');
                    dynamicTable.pollAndSet(false);
                    interval = setInterval(function() {
                        dynamicTable.pollAndSet(true);
                    }, 1000);
                };

                this.unload = function() {
                    Listen.trace('Unloading conferences');
                    if(interval) {
                        clearInterval(interval);
                    }
                };
            },

            viewConference: function(id) {
                var conference = new Conference(id);
                Listen.switchApp('conferencing', conference);
            }
        }
    }();

    Listen.registerApp(new Listen.Application('conference-list', 'conference-list-application', 'menu-conference-list', new Listen.Conferences.Application()));
});