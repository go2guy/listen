var Server = {
    post: function(args) {
        var start = Listen.timestamp();
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
                var elapsed = Listen.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    dropCaller: function(id) {
        var start = Listen.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/dropParticipant',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Participant dropped');
            },
            error: function(req) {
                //noticeError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = Listen.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    markVoicemailReadStatus: function(id, readStatus) {
        var start = Listen.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/markVoicemailReadStatus',
            data: { id: id, readStatus: readStatus },
            success: function(data) { /* TODO anything? */ },
            error: function(req) { /* TODO anything? */ },
            complete: function(xhr, textStatus) {
                var elapsed = Listen.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    muteCaller: function(id) {
        var start = Listen.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/muteParticipant',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Participant muted');
            },
            error: function(req) {
                //noticeError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = Listen.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    outdial: function(number, conferenceId) {
        var errorDiv = $('#outdial-dialog .form-error-message');
        errorDiv.hide();
        errorDiv.text('');

        var start = Listen.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/outdial',
            data: { number: number,
                    conferenceId: conferenceId },
            success: function(data) {
                $('#outdial-dialog').slideUp(200);
                Listen.notify('Number ' + number + ' is being dialed');
            },
            error: function(req) {
                errorDiv.text(req.responseText);
                errorDiv.slideDown(200);
                //notify('An error occurred dialing the number - please contact an Administrator.');
            },
            complete: function(xhr, textStatus) {
                var elapsed = Listen.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },
    
    startRecording: function(id) {
        var start = Listen.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/startRecording',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Started Recording');
            },
            error: function(req) {
                //noticeError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = Listen.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },
    
    stopRecording: function(id) {
        var start = Listen.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/stopRecording',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Stopped Recording');
            },
            error: function(req) {
                //noticeError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = Listen.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    },

    unmuteCaller: function(id) {
        var start = Listen.timestamp();
        $.ajax({
            type: 'POST',
            url: '/ajax/unmuteParticipant',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Participant unmuted');
            },
            error: function(req) {
                //noticeError(req.responseText);
            },
            complete: function(xhr, textStatus) {
                var elapsed = Listen.timestamp() - start;
                $('#latency').text(elapsed);
            }
        });
    }
}