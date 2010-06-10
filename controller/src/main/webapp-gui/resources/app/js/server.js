var SERVER = {
    post: function(args) {
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
            }
        });
    },

    dropCaller: function(id) {
        $.ajax({
            type: 'POST',
            url: '/ajax/dropParticipant',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Participant dropped');
            },
            error: function(req) {
                //noticeError(req.responseText);
            }
        });
    },

    markVoicemailReadStatus: function(id, readStatus) {
        $.ajax({
            type: 'POST',
            url: '/ajax/markVoicemailReadStatus',
            data: { id: id, readStatus: readStatus },
            success: function(data) { /* TODO anything? */ },
            error: function(req) { /* TODO anything? */ }
        });
    },

    muteCaller: function(id) {
        $.ajax({
            type: 'POST',
            url: '/ajax/muteParticipant',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Participant muted');
            },
            error: function(req) {
                //noticeError(req.responseText);
            }
        });
    },

    outdial: function(number, conferenceId) {
        var errorDiv = $('#outdial-dialog .form-error-message');
        errorDiv.hide();
        errorDiv.text('');

        $.ajax({
            type: 'POST',
            url: '/ajax/outdial',
            data: { number: number,
                    conferenceId: conferenceId },
            success: function(data) {
                $('#outdial-dialog').slideUp(200);
                LISTEN.notify('Number ' + number + ' is being dialed');
            },
            error: function(req) {
                errorDiv.text(req.responseText);
                errorDiv.slideDown(200);
                //notify('An error occurred dialing the number - please contact an Administrator.');
            }
        });
    },
    
    startRecording: function(id) {
        $.ajax({
            type: 'POST',
            url: '/ajax/startRecording',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Started Recording');
            },
            error: function(req) {
                //noticeError(req.responseText);
            }
        });
    },
    
    stopRecording: function(id) {
        $.ajax({
            type: 'POST',
            url: '/ajax/stopRecording',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Stopped Recording');
            },
            error: function(req) {
                //noticeError(req.responseText);
            }
        });
    },

    unmuteCaller: function(id) {
        $.ajax({
            type: 'POST',
            url: '/ajax/unmuteParticipant',
            data: { id: id },
            success: function(data) {
                //noticeSuccess('Participant unmuted');
            },
            error: function(req) {
                //noticeError(req.responseText);
            }
        });
    }
}