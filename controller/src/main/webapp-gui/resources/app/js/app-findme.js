$(document).ready(function() {
    $('#findme-save').click(function() {
        Listen.FindMe.saveConfiguration();
    });

    Listen.FindMe = function() {
        var accessNumbers = [];
    
        return {
            Application: function() {
                this.load = function() {
                    Listen.trace('Loading FindMe');

                    function init() {
                        var saveButton = $('#findme-application .application-content #findme-save');
                        var whenSomebodyCalls = $('#findme-application .findme-text-when-somebody-calls');
    
                        var start = Listen.timestamp();
                        var after = whenSomebodyCalls;
                        $.ajax({
                            url: Listen.url('/ajax/getFindMeConfiguration'),
                            dataType: 'json',
                            cache: false,
                            success: function(data, textStatus, xhr) {
                                for(var i = 0, length = data.length; i < length; ++i) {
                                    var group = data[i];
                                    if(group.length > 0) {
                                        if(i > 0) {
                                            var text = $('<div class="findme-text-if-i-dont-answer">If I don\'t answer...</div>');
                                            after.after(text);
                                            after = text;
                                        }
                                        after = Listen.FindMe.addNewGroup(after, false, group);
                                    }
                                }
                            },
                            complete: function(xhr, textStatus) {
                                var elapsed = Listen.timestamp() - start;
                                $('#latency').text(elapsed);
                            }
                        });
    
                        var o = $('#findme-application .findme-text-if-i-dont-answer');
                        $('select', o).change(function(e) {
                            var select = $(e.target);
                            var selected = $(':selected', select);
                            if(selected.text() == 'Dial...') {
                                var count = $('.findme-simultaneous-numbers').size(); // # of groups before adding new one 
                                var group = Listen.FindMe.addNewGroup(select.parent(), true);
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
                        url: Listen.url('/ajax/getSubscriber'),
                        dataType: 'json',
                        cache: false,
                        success: function(data, textStatus, xhr) {
                            for(var i = 0, len = data.accessNumbers.length; i < len; ++i) {
                                var an = data.accessNumbers[i];
                                accessNumbers.push(an.number);
                            }
                            init();
                        }
                    });
                };
                
                this.unload = function() {
                    Listen.trace('Unloading FindMe');
                    $('#findme-application .application-content div').not('.help').not('.findme-text-when-somebody-calls').not('.findme-text-if-i-dont-answer:last').remove();
                };
            },
            
            addNewGroup: function(afterElement, animate, entries) {
                var group = Listen.FindMe.buildGroupElement(entries);
                group.css('opacity', 0);
                $(afterElement).after(group);
                group.animate({ opacity: 1 }, (animate === true ? 500 : 0));
                return group;
            },
            
            buildGroupElement: function(entries) {
                var html = '<div class="findme-simultaneous-numbers">';
                html += '<div class="findme-text-dial">Dial the following number</div>';
                html += '<div class="findme-group-buttons">';
                html += '<button class="button-add">Add Number</button>';
                html += '<button class="button-delete">Delete Group</button>';
                //html += '<button class="button-cancel">Disable Group</button>';
                html += '</div>';
                html += '</div>';
                
                var el = $(html);
                
                var addDialedNumber = function(buttonsContainer, forNumber, ringTime, isDisabled) {
                    var dialedNumber = Listen.FindMe.buildDialedNumberElement(forNumber, ringTime, isDisabled);
                    if($('.findme-dialed-number, .findme-dialed-number-disabled', buttonsContainer.parent()).size() > 0) {
                        $('.findme-text-dial', buttonsContainer.parent()).text('Dial the following numbers at the same time');
                    }
                    buttonsContainer.before(dialedNumber);
                };
                
                $('.button-add', el).click(function(e) {
                    addDialedNumber($($(e.target).parent()), '', 8, false);
                });

                $('.button-delete', el).click(function(e) {
                    var group = $(e.target).parent().parent();
                    Listen.FindMe.removeGroup(group);
                });

                if(!Listen.isDefined(entries)) {
                    // give them a default form field
                    addDialedNumber($('.findme-group-buttons', el), '', 8, false);
                } else {
                    var buttonsEl = $('.findme-group-buttons', el);
                    for(var i = 0, length = entries.length; i < length; ++i) {
                        var entry = entries[i];
                        addDialedNumber(buttonsEl, entry.number, entry.duration, !entry.enabled);
                    }
                }
                return el;
            },
            
            buildDialedNumberElement: function(forNumber, ringTime, isDisabled) {
                var html = '<div class="findme-dialed-number' + (isDisabled === true ? '-disabled' : '') + '">';
                html += '<input type="text"' + (isDisabled === true ? ' readonly="readonly"' : '') + ' value="' + forNumber + '"/>';
                html += '<span>for</span><input type="text" class="findme-ring-seconds"' + (isDisabled === true ? ' readonly="readonly"' : '') + ' value="' + ringTime + '"/><span>seconds</span>';
                html += '<button type="button" class="icon-delete" title="Remove this number"></button>';
                html += '<button type="button" class="icon-toggle-off" title="Re-enable this number"></button>';
                html += '<button type="button" class="icon-toggle-on" title="Temporarily disable this number"></button>';
                html += '</div>';
                
                var el = $(html);
                $('input:first', el).autocomplete({source: accessNumbers, delay: 0, minLength: -1});
                
                $('.icon-delete', el).click(function(e) {
                    var number = $(e.target).parent();
                    var group = number.parent();
                    
                    // if there's only one number left in the group, delete the entire group
                    if($('.findme-dialed-number', group).size() <= 1) {
                        Listen.FindMe.removeGroup(group);
                    } else {
                        number.remove();
                        // change the 'Dial the following...' text to be singular if there's only one number
                        var numberCount = $('.findme-dialed-number', group).size();
                        if(numberCount < 2) {
                            $('.findme-text-dial', group).text('Dial the following number');
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
                    
                    parent.addClass('findme-dialed-number');
                    parent.removeClass('findme-dialed-number-disabled');
                });
                
                disableButton.click(function(e) {
                    var parent = $(e.target).parent();
                    $('input', parent).attr('readonly', 'readonly');
                    $('.icon-toggle-off', parent).show();
                    $('.icon-toggle-on', parent).hide();
                    
                    parent.addClass('findme-dialed-number-disabled');
                    parent.removeClass('findme-dialed-number');
                });

                return el;
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
                $('.findme-simultaneous-numbers').each(function(i, it) {
                    var group = [];
                    Listen.trace('GROUP ' + it);
                    $('.findme-dialed-number, .findme-dialed-number-disabled', it).each(function(j, dial) {
                        var inputs = $('input', dial);
                        Listen.trace('  Number: ' + inputs.eq(0).val());
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
                var saveButton = $('#findme-application .button-save');
                var bg = '#FEFEFE';
                saveButton.text('Saving...').attr('readonly', 'readonly').attr('disabled', 'disabled').css('background-color', '#DDDDDD');
                var findme = Listen.FindMe.buildObjectFromMarkup();
                Server.post({
                    url: Listen.url('/ajax/saveFindMeConfiguration'),
                    properties: {
                        findme: JSON.stringify(findme)
                    },
                    successCallback: function(data, textStatus, xhr) {
                        saveButton.text('Saved').removeAttr('readonly').removeAttr('disabled').css('background-color', '#00FF00');
                        setTimeout(function() {
                            saveButton.text('Save').css('background-color', bg);
                        }, 3000);
                    },
                    errorCallback: function(message) {
                        saveButton.text('Error').removeAttr('readonly').removeAttr('disabled').css('background-color', '#FF0000');
                        setTimeout(function() {
                            saveButton.text('Save').css('background-color', bg);
                        }, 3000);
                    }
                });
            }
        }
    }();
    Listen.registerApp(new Listen.Application('findme', 'findme-application', 'menu-findme', new Listen.FindMe.Application()));
});