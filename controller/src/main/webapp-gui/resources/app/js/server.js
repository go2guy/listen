var Server = {
    post: function(args) {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: args.url,
            data: args.properties,
            success: function(data, textStatus, xhr) {
                if(args.successCallback) {
                    args.successCallback.call(this);
                }
            },
            error: function(xhr, textStatus, errorThrown) {
                if(args.errorCallback) {
                    args.errorCallback.call(this, xhr.responseText);
                }
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    dropCaller: function(id) {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/dropParticipant'),
            data: { id: id },
            success: function(data) {
                interact.listen.notifySuccess('Caller has been dropped');
            },
            error: function(req) {
                interact.listen.notifyError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    markVoicemailReadStatus: function(id, readStatus) {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/markVoicemailReadStatus'),
            data: { id: id, readStatus: readStatus },
            success: function(data) { /* TODO anything? */ },
            error: function(req) {
                interact.listen.notifyError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    muteCaller: function(id) {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/muteParticipant'),
            data: { id: id },
            success: function(data) {
                interact.listen.notifySuccess('Participant has been muted');
            },
            error: function(req) {
                interact.listen.notifyError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    outdial: function(number, conferenceId, interrupt) {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/outdial'),
            data: { number: number,
                    conferenceId: conferenceId, 
                    interrupt: interrupt},
            success: function(data) {
                interact.listen.notifySuccess('Number ' + number + ' is being dialed');
            },
            error: function(req) {
                interact.listen.notifyError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },
    
    startRecording: function(id) {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/startRecording'),
            data: { id: id },
            success: function(data) {
                interact.listen.notifySuccess('Started recording');
            },
            error: function(req) {
                interact.listen.notifyError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },
    
    stopRecording: function(id) {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/stopRecording'),
            data: { id: id },
            success: function(data) {
                interact.listen.notifySuccess('Stopped recording');
            },
            error: function(req) {
                interact.listen.notifyError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    unmuteCaller: function(id) {
        var start = interact.util.timestamp();
        $.ajax({
            type: 'POST',
            url: interact.listen.url('/ajax/unmuteParticipant'),
            data: { id: id },
            success: function(data) {
                interact.listen.notifySuccess('Participant has been unmuted');
            },
            error: function(req) {
                interact.listen.notifyError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    }
}