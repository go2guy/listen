$(document).ready(function() {
    LISTEN.registerApp(new LISTEN.Application('conference-list', 'conference-list-application', 'menu-conference-list', new ConferenceList()));
});

function ConferenceList() {
    var interval;

    var dynamicTable = new LISTEN.DynamicTable({
        tableId: 'conference-list-table',
        templateId: 'conference-row-template',
        retrieveList: function(data) {
            return data.results;
        },
        updateRowCallback: function(row, data, setId) {
            if(setId) {
                row.attr('id', 'conference-list-table-row-' + data.id);
            }
            row.find('.conference-cell-description').text(data.description);
            row.find('.conference-cell-status').text(data.isStarted ? 'Started' : 'Not Started');

            var viewHtml = '<button class="view-button" onclick="viewConference(' + data.id + ');">View</button>';
            var viewCell = row.find('.conference-cell-view');
            if(viewCell.html() != viewHtml) {
                viewCell.html(viewHtml);
            }
        }
    });

    var pollAndSet = function(withAnimation) {
        $.ajax({
            url: '/ajax/getConferenceList',
            dataType: 'json',
            cache: false,
            success: function(data, textStatus, xhr) {
                dynamicTable.update(data);
            }
        });
    };

    this.load = function() {
        pollAndSet(false);
        interval = setInterval(function() {
            pollAndSet(true);
        }, 1000);
        //$('#conference-list-window').show();
    };

    this.unload = function() {
        if(interval) {
            clearInterval(interval);
        }
    };
}

function viewConference(id) {
    var conference = new Conference(id);
    LISTEN.switchApp('conferencing', conference);
}