var interact = interact || {};
var FindMe;
$(document).ready(function() {
    $('#findme-save').click(function() {
        FindMe.saveConfiguration();
    });

    FindMe = function() {
        var accessNumbers = [];
        var forwardedNumbers = []; // array of objects, each object is an object, e.g. { from: '1234', to: '4321' }
    
        return {
            Application: function() {
                this.load = function() {
                    interact.util.trace('Loading FindMe');

                    function init() {
                        var saveButton = $('#findme-save');
                        var whenSomebodyCalls = $('.when-somebody-calls');
    
                        var start = interact.util.timestamp();
                        var after = whenSomebodyCalls;
                        $.ajax({
                            url: interact.listen.url('/ajax/getFindMeConfiguration'),
                            dataType: 'json',
                            cache: false,
                            success: function(data, textStatus, xhr) {
                                for(var i = 0, length = data.length; i < length; ++i) {
                                    var group = data[i];
                                    if(group.length > 0) {
                                        if(i > 0) {
                                            var text = $('<div class="if-i-dont-answer">If I don\'t answer...</div>');
                                            after.after(text);
                                            after = text;
                                        }
                                        after = FindMe.addNewGroup(after, false, group);
                                    }
                                }
                            },
                            complete: function(xhr, textStatus) {
                                var elapsed = interact.util.timestamp() - start;
                                $('#latency').text(elapsed);
                            }
                        });
    
                        var o = $('.if-i-dont-answer');
                        $('select', o).change(function(e) {
                            var select = $(e.target);
                            var selected = $(':selected', select);
                            if(selected.text() == 'Dial...') {
                                var count = $('.simultaneous-numbers').size(); // # of groups before adding new one 
                                var group = FindMe.addNewGroup(select.parent(), true);
                                var clone = select.parent().clone(true);
                                $('select', clone).val('voicemail');
                                group.after(clone);
                                if(count > 0) {
                                    select.remove();
                                } else {
                                    select.parent().remove();
                                }
                            }
                        });
                    }

                    $.ajax({
                        url: interact.listen.url('/ajax/getSubscriber'),
                        dataType: 'json',
                        cache: false,
                        success: function(data, textStatus, xhr) {
                            for(var i = 0, len = data.accessNumbers.length; i < len; ++i) {
                                var an = data.accessNumbers[i];
                                accessNumbers.push(an.number);
                                if(an.forwardedTo != '') {
                                    forwardedNumbers.push({
                                        from: an.number,
                                        to: an.forwardedTo
                                    });
                                }
                            }
                            init();
                        }
                    });
                };
            },
            
            addNewGroup: function(afterElement, animate, entries) {
                var group = FindMe.buildGroupElement(entries);
                group.css('opacity', 0);
                $(afterElement).after(group);
                group.animate({ opacity: 1 }, (animate === true ? 500 : 0));
                return group;
            },
            
            buildGroupElement: function(entries) {
                var html = '<div class="simultaneous-numbers">';
                html += '<div class="dial">Dial the following number</div>';
                html += '<div class="group-buttons">';
                html += '<button class="button-delete">Delete Group</button>';
                html += '<button class="button-add">Add Number</button>';
                //html += '<button class="button-cancel">Disable Group</button>';
                html += '</div>';
                html += '</div>';
                
                var el = $(html);
                
                var addDialedNumber = function(buttonsContainer, forNumber, ringTime, isDisabled) {
                    var dialedNumber = FindMe.buildDialedNumberElement(forNumber, ringTime, isDisabled);
                    if($('.dialed-number, .dialed-number-disabled', buttonsContainer.parent()).size() > 0) {
                        $('.dial', buttonsContainer.parent()).text('Dial the following numbers at the same time');
                    }
                    buttonsContainer.before(dialedNumber);
                };
                
                $('.button-add', el).click(function(e) {
                    addDialedNumber($($(e.target).parent()), '', 8, false);
                });

                $('.button-delete', el).click(function(e) {
                    var group = $(e.target).parent().parent();
                    FindMe.removeGroup(group);
                });

                if(!interact.util.isDefined(entries)) {
                    // give them a default form field
                    addDialedNumber($('.group-buttons', el), '', 8, false);
                } else {
                    var buttonsEl = $('.group-buttons', el);
                    for(var i = 0, length = entries.length; i < length; ++i) {
                        var entry = entries[i];
                        addDialedNumber(buttonsEl, entry.number, entry.duration, !entry.enabled);
                    }
                }
                return el;
            },
            
            buildDialedNumberElement: function(forNumber, ringTime, isDisabled) {
                var html = '<div class="dialed-number' + (isDisabled === true ? '-disabled' : '') + '">';
                html += '<button type="button" class="icon-delete" title="Remove this number"></button>';
                html += '<button type="button" class="icon-toggle-off" title="Re-enable this number"></button>';
                html += '<button type="button" class="icon-toggle-on" title="Temporarily disable this number"></button>';
                html += '<input type="text"' + (isDisabled === true ? ' readonly="readonly"' : '') + ' value="' + forNumber + '"/>';
                html += '<span>for</span><input type="text" class="ring-seconds"' + (isDisabled === true ? ' readonly="readonly"' : '') + ' value="' + ringTime + '"/><span>seconds</span>';
                html += '</div>';

                var el = $(html);
                FindMe.toggleForwardedIndicator(el);
                $('input:first', el).autocomplete({source: accessNumbers, delay: 0, minLength: -1}).change(function() {
                    FindMe.toggleForwardedIndicator($(this).parent());
                });
                
                $('.icon-delete', el).click(function(e) {
                    var number = $(e.target).parent();
                    var group = number.parent();
                    
                    // if there's only one number left in the group, delete the entire group
                    if($('.dialed-number', group).size() <= 1) {
                        FindMe.removeGroup(group);
                    } else {
                        number.remove();
                        // change the 'Dial the following...' text to be singular if there's only one number
                        var numberCount = $('.dialed-number', group).size();
                        if(numberCount < 2) {
                            $('.dial', group).text('Dial the following number');
                        }
                    }
                });
                
                var reenableButton = $('.icon-toggle-off', el);
                var disableButton = $('.icon-toggle-on', el);
                
                if(isDisabled === true) {
                    reenableButton.show();
                    disableButton.hide();
                } else {
                    reenableButton.hide();
                    disableButton.show();
                }
                
                reenableButton.click(function(e) {
                    var parent = $(e.target).parent();
                    $('input', parent).removeAttr('readonly');
                    $('.icon-toggle-off', parent).hide();
                    $('.icon-toggle-on', parent).show();
                    
                    parent.addClass('dialed-number');
                    parent.removeClass('dialed-number-disabled');
                });
                
                disableButton.click(function(e) {
                    var parent = $(e.target).parent();
                    $('input', parent).attr('readonly', 'readonly');
                    $('.icon-toggle-off', parent).show();
                    $('.icon-toggle-on', parent).hide();
                    
                    parent.addClass('dialed-number-disabled');
                    parent.removeClass('dialed-number');
                });

                return el;
            },
            
            toggleForwardedIndicator: function(dialedNumberEl) {
                var number = $('input:first', dialedNumberEl).val();
                var forwardedTo = '';
                for(var i = 0; i < forwardedNumbers.length; i++) {
                    if(forwardedNumbers[i].from == number) {
                        forwardedTo = forwardedNumbers[i].to;
                    }
                }
                if(forwardedTo != '' && $('.forwarded-to', dialedNumberEl).size() == 0) {
                    $('.ring-seconds', dialedNumberEl).next().after('<span class="forwarded-to">( Forwarded to <b>' + forwardedTo + '</b> )</span>');
                } else {
                    $('.forwarded-to', dialedNumberEl).remove();
                }
            },
            
            removeGroup: function(groupElement) {
                var prev = $(groupElement).prev();
                var next = $(groupElement).next();
                if(prev.text() != 'When somebody calls me,') {
                    prev.animate({ opacity: 0 }, 250, 'linear', function() {
                        $(this).remove();
                    });
                } else if($('select', next).size() == 0) {
                    next.animate({ opacity: 0 }, 250, 'linear', function() {
                        $(this).remove();
                    });
                }
                $(groupElement).animate({ opacity: 0 }, 250, 'linear', function() {
                    $(this).remove();
                });
            },
            
            buildObjectFromMarkup: function() {
                var groups = [];
                $('.simultaneous-numbers').each(function(i, it) {
                    var group = [];
                    interact.util.trace('GROUP ' + it);
                    $('.dialed-number, .dialed-number-disabled', it).each(function(j, dial) {
                        var inputs = $('input', dial);
                        interact.util.trace('  Number: ' + inputs.eq(0).val());
                        var entry = {
                            number: inputs.eq(0).val(),
                            duration: inputs.eq(1).val(),
                            enabled: $('.icon-toggle-on', dial).is(':visible')
                        };
                        if(entry.number != '') {
                            group.push(entry);
                        }
                    });
                    if(group.length > 0) {
                        groups.push(group);
                    }
                });
                return groups;
            },
            
            saveConfiguration: function() {
                var saveButton = $('#findme-save');
                saveButton.attr('readonly', 'readonly').attr('disabled', 'disabled');
                var findme = FindMe.buildObjectFromMarkup();
                Server.post({
                    url: interact.listen.url('/ajax/saveFindMeConfiguration'),
                    properties: {
                        findme: JSON.stringify(findme)
                    },
                    successCallback: function(data, textStatus, xhr) {
                        saveButton.removeAttr('readonly').removeAttr('disabled');
                        interact.listen.notifySuccess('Find Me / Follow Me configuration saved');
                    },
                    errorCallback: function(message) {
                        saveButton.removeAttr('readonly').removeAttr('disabled');
                        interact.listen.notifyError(message);
                    }
                });
            }
        }
    }();
    
    new FindMe.Application().load();
});