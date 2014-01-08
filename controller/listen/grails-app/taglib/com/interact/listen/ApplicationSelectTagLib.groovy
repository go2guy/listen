package com.interact.listen

import com.interact.listen.license.ListenFeature

class ApplicationSelectTagLib {
    static namespace = 'listen'

    def applicationService
    def licenseService
    def springSecurityService

    def applicationSelect = { attrs ->
        def name = attrs.name
        def id = attrs.id ?: name
        def value = attrs.value ?: ''
        def hide = attrs.hide as boolean ? true : false

        def exclude = (attrs.exclude ? attrs.exclude.split(',') : []) as Set

        def apps = applicationService.listApplications()

        def organization = attrs.organization
        if(!organization) {
            def user = springSecurityService.getCurrentUser()
            if(!user) throwTagError 'Tag [applicationSelect] is missing required attribute [organization] (no current user/organization)'
            organization = user.organization
        }

        if(!organization) log.warn "Null organization used for tag [applicationSelect]"

        out << '<select' + (name ? ' name="' + name + '"' : '') + (id ? ' id="' + id + '"' : '') + ' class="application-select"' + (hide ? ' style="display: none;"' : '') + ' class="application-select">' 
        apps.each { application ->
            if(!exclude.contains(application)) {
                out << '<option' + (value == application ? ' selected="selected"' : '') + ">${application}</option>"
            }
        }
        if(value.trim().length() > 0 && !apps.contains(value)) {
            out << '<option selected="selected">' + value + '</option>'
        }
        if(licenseService.canAccess(ListenFeature.CUSTOM_APPLICATIONS, organization)) {
            out << '<option>Custom Application...</option>'
        }
        out << '</select>'
    }

    def customApplicationStyles = { attrs ->
        if(licenseService.canAccess(ListenFeature.CUSTOM_APPLICATIONS)) {
            out << '<style type="text/css">'
            out << '#custom-application-dialog input { display: block; width: 300px; }'
            out << '</style>'
        }
    }

    def customApplicationScripts = { attrs ->
        if(licenseService.canAccess(ListenFeature.CUSTOM_APPLICATIONS)) {
            out << '''
<div id="custom-application-dialog" title="Add Custom Application" style="display: none;">
  <form>
    <fieldset>
      <label>
        Name
        <input type="text"/>
      </label>
      <span class="status"></span>
    </fieldset>
  </form>
</div>
<script type="text/javascript">
$(document).ready(function() {
    $('#custom-application-dialog').dialog({
        autoOpen: false,
        draggable: false,
        modal: true,
        position: 'center',
        resizable: false,
        width: 400
    });
    $('.application-select').change(function(e) {
        var sel = $(e.target);
        if(sel.val() == 'Custom Application...') {

            var dialog = $('#custom-application-dialog');

            var close = function() {
                $('input', dialog).val('');
                $('.status', dialog).text('');
                dialog.dialog('close');
            }

            var okay = function() {
                var name = $('input', dialog).val();
                if(name.indexOf('Custom Application') >= 0) {
                    $('.status', dialog).html('<pre style="word-wrap: break-word; white-space: pre-wrap;">Name cannot contain "Custom Application"</pre>');
                    return;
                }
                
                if($.trim(name) !== '') {
                    sel.prepend('<option>' + name + '</option>');
                    sel.val(name);
                } else {
                    util.selectFirst(sel);
                }
                close();
            }

            var cancel = function() {
                util.selectFirst(sel);
                close();
            }

            $('form', dialog).submit(function() {
                okay();
                return false;
            });

            dialog.dialog('option', 'buttons', { 'Okay': okay, 'Cancel': cancel });
            dialog.dialog('open');
            $('input', dialog).focus();
        }
    });
});
</script>
'''
        }
    }
}
