$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('conference-list', 'conference-list-application', 'menu-conference-list', new ConferenceList()));
});

function ConferenceList() {
    var interval;

    var dynamicTable = new LISTEN.DynamicTable({
        url: '/ajax/getConferenceList',
        tableId: 'conference-list-table',
        templateId: 'conference-row-template',
        retrieveList: function(data) {
            return data.results;
        },
        paginationId: 'conference-list-pagination',
        updateRowCallback: function(row, data, animate) {
            LISTEN.setFieldContent(row.find('.conference-cell-description'), data.description, animate);
            var status = data.isStarted ? 'Started' : 'Not Started';
            if(data.isStarted) {
                status += ' ' + data.startDate;
            }
            LISTEN.setFieldContent(row.find('.conference-cell-status'), status, animate);
            LISTEN.setFieldContent(row.find('.conference-cell-callerCount'), data.callerCount, animate);
            LISTEN.setFieldContent(row.find('.conference-cell-view'), '<button class="button-view" onclick="viewConference(' + data.id + ');">View</button>', false, true);
        }
    });

    this.load = function() {
        LISTEN.log('Loading conference list');
        dynamicTable.pollAndSet(false);
        interval = setInterval(function() {
            dynamicTable.pollAndSet(true);
        }, 1000);
    };

    this.unload = function() {
        LISTEN.log('Unloading conference-list');
        if(interval) {
            clearInterval(interval);
        }
    };
}

function viewConference(id) {
    var conference = new Conference(id);
    LISTEN.switchApp('conferencing', conference);
}