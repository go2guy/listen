var callerTable;

$(document).ready(function() {

    $('#myDialog').dialog({
        autoOpen: true,
        draggable: true,
        resizable: true,
        height: 300,
        width: 400,
        position: [50, 50]
    });

    callerTable = $('#callerTable').dataTable({
        bPaginate: false,
        bFilter: false,
        bInfo: false
    });

    getConferenceParticipants(1);
    var refresh = setInterval(function() {
        getConferenceParticipants(1);
    }, 1000);
});

function getConferenceParticipants(id) {
    $.getJSON('/getConferenceParticipants?conference=' + id, function(data) {
        callerTable.fnClearTable();
        for(var i = 0; i < data.count; i++) {
            callerTable.fnAddData([data.results[i].number, data.results[i].isAdmin, data.results[i].isHolding, data.results[i].isMuted])
        }
    })
}