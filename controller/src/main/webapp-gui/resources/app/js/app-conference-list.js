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
        updateRowCallback: function(row, data) {
            var descriptionCell = row.find('.conference-cell-description');
            if(descriptionCell.text() != data.description) {
                descriptionCell.text(data.description);
                this.highlight(descriptionCell);
            }

            var statusCell = row.find('.conference-cell-status');
            var statusText = (data.isStarted ? 'Started' : 'Not Started');
            if(statusCell.text() != statusText) {
                statusCell.text(statusText);
                this.highlight(statusCell);
            }

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
        LISTEN.log('Loading conference list');
        pollAndSet(false);
        interval = setInterval(function() {
            pollAndSet(true);
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