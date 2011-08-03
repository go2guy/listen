package com.interact.listen

class AutocompleteTagLib {
    static namespace = 'listen'

    def autocomplete = { attrs ->
        if(!attrs.selector) throwTagError 'Tag [autocomplete] is missing required attribute [selector]'
        if(!attrs.data) throwTagError 'Tag [autocomplete] is missing required attribute [data]'
        out << """
<script type="text/javascript">
\$(document).ready(function() {
    \$.get('${request.contextPath}/autocomplete/contacts', function(data) {
        \$('${attrs.selector}').autocomplete({
            source: function(request, response) {
                var matcher = new RegExp(\$.ui.autocomplete.escapeRegex(request.term), 'i');
                response(\$.grep(data.${attrs.data}, function(value) {
                    return matcher.test(value.value)
                            || matcher.test(value.name);
                }));
            },
            delay: 0,
            minLength: 1
        });
    });
});
</script>
"""

        if(attrs.providerSelector) {
            out << """
<script type="text/javascript">
\$(document).ready(function() {
    var provider = \$('${attrs.providerSelector}');
    \$('${attrs.selector}').bind('autocompleteselect', function(e, ui) {
        if(provider.size() > 0 && ui.item.hasOwnProperty('provider')) {
            provider.val(ui.item.provider);
        }
    });
});
</script>
"""
        }
    }
}
