var interact = interact || {};
interact.pbx = {
    subscriberNames: [],

    loadRestrictions: function() {
        $.ajax({
            url: interact.listen.url('/ajax/getCallRestrictions'),
            dataType: 'json',
            cache: false,
            success: function(data, textStatus, xhr) {
                if(data.length > 0) {
                    for(var i = 0; i < data.length; i++) {
                        interact.pbx.newRestriction(data[i].destination, data[i].target, data[i].subscribers);
                    }
                } else {
                    interact.pbx.newRestriction(); // blank restriction
                }
            }
        });
    },

    addSubscriber: function(beforeElement, number, focus) {
        var markup = '';
        markup += '<label class="restricted-subscriber">';
        markup += 'Subscriber <input type="text"/>';
        markup += '</label>';
        var label = $(markup);

        $('input', label).val(number).autocomplete({
            source: interact.pbx.subscriberNames,
            delay: 0,
            minLength: -1
        });
        $(beforeElement).before(label);
        if(focus) {
            $('input', label).focus();
        }
    },

    moreSubscribers: function(beforeElement, num) {
        var n = num || 3;
        for(var i = n; i > 0; i--) {
            interact.pbx.addSubscriber(beforeElement, '', i == n);
        }
        interact.pbx.toggleRemoveButtons($(beforeElement).parent());
    },

    removeSubscriber: function(buttonElement) {
        var el = $(buttonElement);
        var fieldset = $(el).parent().parent();
        el.parent().remove(); // parent should be a label
        interact.pbx.toggleRemoveButtons(fieldset);
    },

    toggleRemoveButtons: function(fieldset) {
        var subscribersLeft = $('.restricted-subscriber', fieldset).length;
        $('.restricted-subscriber button', fieldset).remove();
        if(subscribersLeft > 1) {
            var button = $('<button type="button" class="button-remove remove-subscriber" tabindex="-1">Remove</button>');
            $('.restricted-subscriber', fieldset).append(button);
        }
        $('.restricted-subscriber button', fieldset).click(function(e) {
            interact.pbx.removeSubscriber(e.target);
        });
    },

    newRestriction: function(destination, target, subscribers) {
        var copy = $('#template').clone(true, true);
        copy.removeAttr('id');
        $('.target', copy).val(target !== undefined ? target : 'EVERYONE');
        if(destination !== undefined) {
            $('.destination', copy).val(destination);
        }
        
        if(subscribers !== undefined) {
            var before = $('.more-subscribers', copy);
            for(var i = 0; i < subscribers.length; i++) {
                interact.pbx.addSubscriber(before, subscribers[i]);
            }
        }

        $('#page-buttons').before(copy);
        interact.pbx.toggleFieldsetStyle(copy);
        $('.destination', copy).focus();
    },
    
    toggleFieldsetStyle: function(fieldset) {
        var target = $('.target', fieldset);
        var current = target.val();
        fieldset.toggleClass('deny-everyone', current == 'EVERYONE');
        fieldset.toggleClass('deny-everyone-except', current == 'EVERYONE_EXCEPT');
        fieldset.toggleClass('deny-subscribers', current == 'SUBSCRIBERS');
    },
    
    buildJson: function() {
        var restrictions = [];
        $('.restriction').each(function(i, it) {
            var restriction = {
                destination: $.trim($('.destination', it).val())
            };
            restriction.target = $('.target', it).val();
            restriction.subscribers = [];
            $('.restricted-subscriber input').each(function(j, input) {
                var val = $.trim($(input).val());
                if(val != '') {
                    restriction.subscribers.push(val);
                }
            });
            
            // only add the restriction if it's populated correctly
            if(restriction.destination == '') return;
            if((restriction.target == 'SUBSCRIBERS' || restriction.target == 'EVERYONE_EXCEPT') &&
                    restriction.subscribers.length == 0) {
                // if it's 'SUBSCRIBERS' or 'EVERYONE_EXCEPT', we expect at least one subscriber value
                return;
            }

            restrictions.push(restriction);
        });
        return restrictions;
    }
};

$(document).ready(function() {

    $('.target').change(function(e) {
        var sel = $(e.target);
        var fieldset = sel.parents('fieldset').eq(0); // first fieldset ancestor
        if(sel.val() == 'SUBSCRIBERS' || sel.val() == 'EVERYONE_EXCEPT') {
            var before = $('.more-subscribers', fieldset)
            if($('.restricted-subscriber', fieldset).length == 0) {
                interact.pbx.moreSubscribers(before, 1); // add one, we don't know that they want more than that
            }
            $('.more-subscribers', fieldset).show();
            $('.restricted-subscriber:first input', fieldset).focus();
        } else {
            $('.restricted-subscriber', fieldset).remove();
            $('.more-subscribers', fieldset).hide();
        }
    });

    $('.remove-subscriber').click(function(e) {
        interact.pbx.removeSubscriber(e.target);
    });

    $('.more-subscribers').click(function(e) {
        interact.pbx.moreSubscribers(e.target, 3); // add three, they obviously want more; give them more than just one more
    });

    $('#new-restriction').click(function() {
        interact.pbx.newRestriction();
    });

    $('.delete-restriction').click(function(e) {
        $(e.target).parent().remove();
    });
    
    $('.target').change(function(e) {
        interact.pbx.toggleFieldsetStyle($(e.target).parent().parent());
    });
    
    $('#save-configuration').click(function() {
        var saveButton = $('#findme-save');
        saveButton.attr('readonly', 'readonly').attr('disabled', 'disabled');
        var restrictions = interact.pbx.buildJson();

        Server.post({
            url: interact.listen.url('/ajax/saveCallRestrictions'),
            properties: {
                restrictions: JSON.stringify(restrictions)
            },
            successCallback: function(data, textStatus, xhr) {
                saveButton.removeAttr('readonly').removeAttr('disabled');
                interact.listen.notifySuccess('Call restrictions saved');
            },
            errorCallback: function(message) {
                saveButton.removeAttr('readonly').removeAttr('disabled');
                interact.listen.notifyError(message);
            }
        });
    });
    
    $.ajax({
        url: interact.listen.url('/ajax/getSubscriberList?max=1000'),
        dataType: 'json',
        cache: false,
        success: function(data, textStatus, xhr) {
            
            for(var i = 0, len = data.results.length; i < len; i++) {
                var subscriber = data.results[i];
                interact.pbx.subscriberNames.push(subscriber.username);
            }

            $('.restricted-number input').autocomplete({
                source: interact.pbx.subscriberNames,
                delay: 0,
                minLength: -1
            });
        }
    });

    interact.pbx.loadRestrictions();
});