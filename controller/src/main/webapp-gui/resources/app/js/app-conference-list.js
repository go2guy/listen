var interact = interact || {};
var Conferences;
$(document).ready(function() {

    Conferences = function() {
        return {
            Application: function() {
                interact.util.trace('Conferences.Application [construct]');
                var interval;
                var dynamicTable = new interact.util.DynamicTable({
                    url: interact.listen.url('/ajax/getConferenceList'),
                    tableId: 'conference-list-table',
                    templateId: 'conference-row-template',
                    retrieveList: function(data) {
                        return data.results;
                    },
                    paginationId: 'conference-list-pagination',
                    updateRowCallback: function(row, data, animate) {
                        interact.util.setFieldContent(row.find('.conference-cell-description'), data.description, animate);
                        var status = data.isStarted ? 'Started' : 'Not Started';
                        if(data.isStarted) {
                            status += ' ' + data.startDate;
                        }
                        interact.util.setFieldContent(row.find('.conference-cell-status'), status, animate);
                        interact.util.setFieldContent(row.find('.conference-cell-callerCount'), data.callerCount, animate);
                        interact.util.setFieldContent(row.find('.conference-cell-view'), '<button class="button-view" onclick="Conferences.viewConference(' + data.id + ');">View</button>', false, true);
                    }
                });

                this.load = function() {
                    interact.util.trace('Loading conferences');
                    dynamicTable.pollAndSet(false);
                    interval = setInterval(function() {
                        dynamicTable.pollAndSet(true);
                    }, 1000);
                };
            },

            viewConference: function(id) {
                window.location = interact.listen.url('/conferencing?id=' + id);
            }
        }
    }();

    new Conferences.Application().load();
});