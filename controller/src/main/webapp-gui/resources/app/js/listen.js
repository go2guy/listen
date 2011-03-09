var interact = interact || {};
$(document).ready(function() {
    interact.listen = {
    
        /**
         * Returns a URL that is correct for the web application's context path.
         * Relies on a global variable named "CONTEXT" being set with the context root.
         */
        url: function(url) {
            return CONTEXT + url;
        },
        
        notifySuccess: function(message, stay) {
            interact.util.trace('Displaying success notification');
            interact.listen.notify(message, true, true, stay === true);
        },
        
        notifyError: function(message, stay) {
            interact.util.trace('Displaying error notification');
            interact.listen.notify(message, false, true, stay === true);
        },

        notify: function(message, success, close, stay) {
            $('.announcement-success, .announcement-error').remove(); // clear other notifications
            var div = $('<div></div>').text(message).addClass('announcement-' + (success ? 'success' : 'error')).css('display', 'none');
            if(close) {
                var button = $('<button type="button" class="icon-cancel"></button>');
                button.click(function(e) {
                    $(e.target).parent().hide();
                });
                div.append(button);
            }
            $('body').append(div);
            div.slideDown(250);
            setTimeout(function() {
                div.slideUp(250);
            }, 4000);
        },
        
        checkBlacklist: function(inputEl) {
            var input = $(inputEl);
            var destination = $.trim(input.val());
            interact.util.trace('Checking blacklist for destination [' + destination + ']');
            
            var clearIndicator = function(input) {
                input.next('.blacklisted-indicator').remove();
                input.removeClass('is-blacklisted');
            }
            
            if(destination == '') {
                clearIndicator(input);
            } else {
                $.ajax({
                    url: interact.listen.url('/ajax/isBlacklisted?destination=' + destination),
                    dataType: 'json',
                    cache: 'false',
                    success: function(data, textStatus, xhr) {
                        if(data.isBlacklisted) {
                            interact.util.trace('Destination [' + destination + '] is blacklisted');
                            input.addClass('is-blacklisted');
                            if(input.next('.blacklisted-indicator').length == 0) {
                                input.after('<span class="blacklisted-indicator" style="left: 229px;" title="You are not allowed to dial this number.">!</span>');
                            }
                        } else {
                            interact.util.trace('Destination [' + destination + '] is allowed');
                            clearIndicator(input);
                        }
                    }
                });
            }
        }
    };
    
    // if we ever receive a response with a 401 (Unauthorized) status, send them to the login page
    $.ajaxSetup({
        error: function(xhr, textStatus, errorThrown) {
            if(xhr && xhr.status == 401) {
                window.location = interact.listen.url('/logout');
            } else {
                interact.util.error('textStatus = [' + textStatus + '], xhrStatus = [' + xhr.status + ']');
            }
        }
    });
    
    // start a ping thread - if we ever lose communication to the server, don't let them use the screens
    // (since they won't work or be up to date anyway)
    function showUnavailableModal() {
        var exists = $('#communication-error').is(':visible');
        if(!exists) {
            var div = $('<div id="communication-error" class="announcement-error">Server is unavailable, please wait...</div>');
            $('body').append(div);
            $('#modal-overlay').show();
        }
    }
    function hideUnavailableModal() {
        $('#communication-error').hide();
        $('#modal-overlay').hide();
    }
    var failedTries = 0;
    setInterval(function() {
        var start = interact.util.timestamp();
        $.ajax({
            url: interact.listen.url('/meta/ping?auth=true'),
            dataType: 'json',
            cache: 'false',
            success: function(data, textStatus, xhr) {
                if(!data || !data.pong) {
                    if(failedTries++ > 2) {
                        showUnavailableModal();
                    }
                } else if(data.pong && failedTries > 2) {
                    failedTries = 0;
                    hideUnavailableModal();
                }
                
                if(data && data.pong) {
                    interact.util.setFieldContent($('#voicemail-new-count'), data.newVoicemailCount, false, false);
                }
            },
            error: function(xhr, textStatus, errorThrown) {
                if(xhr && xhr.status == 401) {
                    window.location = '/logout';
                    return;
                }
                if(failedTries++ > 2) {
                    showUnavailableModal();
                }
            },
            complete: function(xhr, textStatus) {
                var elapsed = interact.util.timestamp() - start;
                $('#pinglatency').text(elapsed);
            }
        });
    }, 2000);
    
    $('.tab-container').each(function(containerIndex, containerElement) {
        var tabs = $('.tabs a', containerElement);
        var panels = $('.tab-content, .tab-content-default', containerElement);

        panels.each(function(panelIndex, panelElement) {
            if($(panelElement).hasClass('tab-content-default')) {
                $(tabs[panelIndex]).addClass('current');
            }
            $(tabs[panelIndex]).click(function(event) {
                $(event.target).addClass('current');
                panels.each(function(i, e) {
                    if(i != panelIndex) {
                        $(e).hide();
                        $(tabs[i]).removeClass('current');
                    }
                });
                $(panelElement).show();
            });
        });
    });

    $('label.required').each(function(index, elem) {
        var text = $(elem).text();
        $(elem).text(text + '*');
    });

    $('input.possibly-blacklisted').each(function(i, el) {
        $(el).change(function(e) {
            interact.listen.checkBlacklist(e.target);
        });
    });
});