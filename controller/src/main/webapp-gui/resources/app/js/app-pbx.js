var interact = interact || {};
interact.pbx = {
    subscriberNames: [],

    moreSubscribers: function(beforeElement, num) {
        var n = num || 3;
        for(var i = n; i > 0; i--) {
            var markup = '';
            markup += '<label class="restricted-subscriber">';
            markup += 'Subscriber <input type="text"/>';
            if(n > 1) {
                markup += '<button type="button" class="button-delete remove-subscriber" tabindex="-1">Remove</button>';
            }
            markup += '</label>';
            var label = $(markup);

            $('input', label).autocomplete({
                source: interact.pbx.subscriberNames,
                delay: 0,
                minLength: -1
            });
            
            $(beforeElement).before(label);
            if(i == n) {
                $('input', label).focus();
            }
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

    newRestriction: function() {
        var copy = $('#template').clone(true, true);
        copy.removeAttr('id');
        $('.target', copy).val('Everyone');
        interact.pbx.toggleFieldsetStyle($('.directive', copy));
        $('#page-buttons').before(copy);

        $('.destination', copy).focus();
    },
    
    toggleFieldsetStyle: function(directiveSelect) {
        var directive = $(directiveSelect);
        var fieldset = directive.parent().parent();
        var current = directive.val();
        fieldset.toggleClass('deny', current == 'deny');
        fieldset.toggleClass('allow', current == 'allow');
    }
};

$(document).ready(function() {

    $('.target').change(function(e) {
        var sel = $(e.target);
        var fieldset = sel.parents('fieldset').eq(0); // first fieldset ancestor
        if(sel.val() == 'Subscribers') {
            var before = $('.more-subscribers', fieldset)
            interact.pbx.moreSubscribers(before, 1); // add one, we don't know that they want more than that
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
    
    $('.directive').change(function(e) {
        interact.pbx.toggleFieldsetStyle(e.target);
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
    
    interact.pbx.newRestriction();
});