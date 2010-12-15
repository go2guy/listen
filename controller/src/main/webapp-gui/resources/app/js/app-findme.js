$(document).ready(function() {
    Listen.FindMe = function() {
        return {
            Application: function() {
                this.load = function() {
                    Listen.trace('Loading FindMe');
                    var saveButton = $('#findme-application .application-content #findme-save');
                    var whenSomebodyCalls = $('<div class="findme-text-when-somebody-calls">When somebody calls me,</div>');
                    saveButton.before(whenSomebodyCalls);
                    var group = Listen.FindMe.addNewGroup(whenSomebodyCalls, false);
                    
                    var option = '<div class="findme-text-if-i-dont-answer">';
                    option += '<span>If I don\'t answer,</span>';
                    option += '<select><option selected="selected" value="voicemail">Send the caller to my voicemail</option><option value="dial">Dial...</option></select>';
                    option += '</div.';
                    
                    var o = $(option);
                    $('select', o).change(function(e) {
                        var select = $(e.target);
                        var selected = $(':selected', select);
                        if(selected.text() == 'Dial...') {
                            var count = $('.findme-simultaneous-numbers').size(); // # groups before adding new one 
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
                    
                    group.after(o);
                };
                
                this.unload = function() {
                    Listen.trace('Unloading FindMe');
                    var divs = $('#findme-application .application-content div').not('.help').remove();
                };
            },
            
            addNewGroup: function(afterElement, animate) {
                var group = Listen.FindMe.buildGroupElement();
                group.css('opacity', 0);
                $(afterElement).after(group);
                group.animate({ opacity: 1 }, (animate === true ? 500 : 0));
                return group;
            },
            
            buildGroupElement: function() {
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
                    var dialedNumber = Listen.FindMe.buildDialedNumberElement('', 8, false);
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

                // give them a default form field                
                addDialedNumber($('.findme-group-buttons', el), '', 8, false);
                return el;
            },
            
            buildDialedNumberElement: function(forNumber, ringTime, isDisabled) {
                var html = '<div class="findme-dialed-number' + (isDisabled === true ? '-disabled' : '') + '" value="' + forNumber + '">';
                html += '<input type="text"' + (isDisabled === true ? ' readonly="readonly"' : '') + '/>';
                html += '<span>for</span><input type="text" class="findme-ring-seconds"' + (isDisabled === true ? ' readonly="readonly"' : '') + ' value="' + ringTime + '"/><span>seconds</span>';
                html += '<button type="button" class="icon-delete" title="Remove this number"></button>';
                html += '<button type="button" class="icon-toggle-off" title="Re-enable this number"></button>';
                html += '<button type="button" class="icon-toggle-on" title="Temporarily disable this number"></button>';
                html += '</div>';
                
                var el = $(html);
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
            }
        }
    }();
    Listen.registerApp(new Listen.Application('findme', 'findme-application', 'menu-findme', new Listen.FindMe.Application()));
});