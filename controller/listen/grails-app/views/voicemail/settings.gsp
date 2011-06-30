<html>
  <head>
    <title><g:message code="page.voicemail.settings.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="voicemail"/>
    <meta name="button" content="settings"/>
    <meta name="page-header" content="${g.message(code: 'page.voicemail.settings.header')}"/>
    <style type="text/css">
fieldset fieldset { border-width: 0; }
.time-restriction-placeholder {
    font-size: 13px;
    font-style: italic;
    font-weight: normal;
    margin: 0 0 5px 0;
}

fieldset.vertical .time-restriction {
    border-collapse: separate;
    border-spacing: 0px 5px;
    display: block;
    font-size: 12px;
    padding: 2px 5px;
}

fieldset.vertical .time-restriction tbody tr {
    background-color: #E4F0FB; /* swatch:subdued */
}

fieldset.vertical .time-restriction .time-cell {
    padding-left: 8px;
    padding-right: 8px;
}

fieldset.vertical .time-restriction .day-cell {
    text-align: center;
}

fieldset.vertical .time-restriction select {
    display: inline;
    width: 60px;
}

.templates {
    display: none;
}
    </style>
  </head>
  <body>
    <g:form controller="voicemail" action="saveSettings">
      <fieldset class="vertical">
        <label for="passcode"><g:message code="voicemailPreferences.passcode.label"/></label>
        <g:textField name="passcode" value="${fieldValue(bean: preferences, field: 'passcode')}" class="${listen.validationClass(bean: preferences, field: 'passcode')}"/>

        <label for="playbackOrder"><g:message code="voicemailPreferences.playbackOrder.label"/></label>
        <g:select name="playbackOrder" from="${com.interact.listen.voicemail.PlaybackOrder.values()}" optionKey="key" value="${preferences?.playbackOrder?.name()}" class="${listen.validationClass(bean: preferences, field: 'playbackOrder')}"/>

        <label><g:message code="page.voicemail.settings.newVoicemail"/></label>

        <label for="transcribe">
          <g:checkBox name="transcribe" value="${preferences?.transcribe}"/>
          <g:message code="page.voicemail.settings.transcribe"/>
        </label>

        <label for="isEmailNotificationEnabled">
          <g:checkBox name="isEmailNotificationEnabled" value="${preferences?.isEmailNotificationEnabled}"/>
          <g:message code="page.voicemail.settings.sendEmail"/>
        </label>

        <fieldset>
          <g:set var="userEmail" value="${preferences?.emailNotificationAddress?.equals(preferences.user.emailAddress) || !preferences.emailNotificationAddress}"/>
          <label>
            <g:radio name="emailSource" value="current" checked="${userEmail}"/>
            <g:message code="page.voicemail.settings.useCurrentEmail"/> (<g:fieldValue bean="${preferences?.user}" field="emailAddress"/>)
          </label>

          <label class="inline-label">
            <g:radio name="emailSource" value="custom" checked="${!userEmail}"/>
            <g:message code="page.voicemail.settings.useAnotherEmail"/>
          </label>
          <g:textField name="emailNotificationAddress" style="display: inline-block; margin-left: 10px;" value="${!userEmail ? preferences.emailNotificationAddress?.encodeAsHTML() : ''}" class="${listen.validationClass(bean: preferences, field: 'emailNotificationAddress')}"/>

          <button type="button" id="sendTestEmail"><g:message code="page.voicemail.settings.button.sendTestEmail"/></button>

          <g:set var="hasEmailTimeRestrictions" value="${preferences.emailTimeRestrictions.size() > 0}"/>
          <fieldset class="time-restrictions" id="emailTimeRestrictions">
            <h4 class="time-restriction-placeholder">
              <g:if test="${hasEmailTimeRestrictions}">
                <g:message code="page.voicemail.settings.restriction.restrictedLabel"/>
              </g:if>
              <g:else>
                <g:message code="page.voicemail.settings.restriction.notRestrictedLabel"/>
              </g:else>
            </h4>
            <g:set var="restrictions" value="${preferences.emailTimeRestrictions.sort { a, b -> a.startTime.equals(b.startTime) ? a.endTime.compareTo(b.endTime) : a.startTime.compareTo(b.startTime) }}"/>
            <g:render template="/shared/timeRestrictionTable" model="${[visible: hasEmailTimeRestrictions, prefix: 'emailTimeRestrictions', restrictions: restrictions]}"/>
            <button type="button" id="addEmailTimeRestriction"><g:message code="page.voicemail.settings.button.addRestriction"/></button>
          </fieldset>
        </fieldset>

        <label for="isSmsNotificationEnabled">
          <g:checkBox name="isSmsNotificationEnabled" value="${preferences?.isSmsNotificationEnabled}"/>
          <g:message code="page.voicemail.settings.sendSms"/>
        </label>

        <fieldset>
          <label for="smsNotificationNumber" class="inline-label">
            <g:message code="page.voicemail.settings.smsPhoneNumber"/>
            <g:textField name="smsNotificationNumber" value="${preferences?.smsNotificationNumber()?.encodeAsHTML()}" class="${listen.validationClass(bean: preferences, field: 'smsPhoneNumber')}"/>
          </label>

          <label for="smsNotificationProvider" class="inline-label">
            <g:message code="page.voicemail.settings.smsMobileProvider"/>
            <listen:mobileProviderSelect name="smsNotificationProvider" value="${preferences?.smsNotificationProvider()}"/>
          </label>

          <button type="button" id="sendTestSms"><g:message code="page.voicemail.settings.button.sendTestSms"/></button>

          <g:set var="hasSmsTimeRestrictions" value="${preferences.smsTimeRestrictions.size() > 0}"/>
          <fieldset class="time-restrictions" id="smsTimeRestrictions">
            <h4 class="time-restriction-placeholder">
              <g:if test="${hasSmsTimeRestrictions}">
                <g:message code="page.voicemail.settings.restriction.restrictedLabel"/>
              </g:if>
              <g:else>
                <g:message code="page.voicemail.settings.restriction.notRestrictedLabel"/>
              </g:else>
            </h4>
            <g:set var="restrictions" value="${preferences.smsTimeRestrictions.sort { a, b -> a.startTime.equals(b.startTime) ? a.endTime.compareTo(b.endTime) : a.startTime.compareTo(b.startTime) }}"/>
            <g:render template="/shared/timeRestrictionTable" model="${[visible: hasSmsTimeRestrictions, prefix: 'smsTimeRestrictions', restrictions: restrictions]}"/>            
            <button type="button" id="addSmsTimeRestriction"><g:message code="page.voicemail.settings.button.addRestriction"/></button>
          </fieldset>

          <label for="recurringNotificationsEnabled">
            <g:checkBox name="recurringNotificationsEnabled" value="${preferences?.recurringNotificationsEnabled}"/>
            <g:message code="page.voicemail.settings.recurringSms"/>
          </label>
        </fieldset>

        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/></li>
        </ul>
      </fieldset>
    </g:form>
    <table class="templates">
      <tbody>
        <g:set var="template" value="${[startTime: new org.joda.time.LocalTime(8, 0), endTime: new org.joda.time.LocalTime(17, 0), monday: false, tuesday: false, wednesday: false, thursday: false, friday: false, saturday: false, sunday: false]}"/>
        <g:render template="/shared/timeRestrictionRow" model="${[id: 'timeRestrictionTemplate', prefix: 'template', restriction: template]}"/>
      </tbody>
    </table>
    <script type="text/javascript">
$(document).ready(function() {
    $('#passcode').focus();

    $('#isEmailNotificationEnabled, #isSmsNotificationEnabled').change(function(e) {
        var box = $(e.target);
        box.parent().next('fieldset').toggle(box.is(':checked'));
    });
    $('#isEmailNotificationEnabled, #isSmsNotificationEnabled').each(function() {
        var box = $(this);
        box.parent().next('fieldset').toggle(box.is(':checked'));
    });

    $('#sendTestEmail').click(function() {
        $.post('${request.contextPath + '/voicemail/sendTestEmail'}', {
            address: $('#emailNotificationAddress').val()
        });
        message('A test email has been sent', $(this));
        return false;
    });

    // TODO disable 'send test sms' button if there is no number in the field
    $('#sendTestSms').click(function() {
        var number = $('#smsNotificationNumber').val();
        var provider = $('#smsNotificationProvider').val();
        if($.trim(number) != '') {
            var address = number + '@' + provider
            $.post('${request.contextPath + '/voicemail/sendTestSms'}', {
                address: address
            });
            message('A test SMS has been sent', $(this));
        }
        return false;
    });

    function message(text, button) {
        var message = $('<ul class="messages success"><li>' + text + '</li></ul>');
        $('ul.button-menu').after(message);
        button.attr('disabled', 'disabled').addClass('disabled');
        setTimeout(function() {
            message.slideUp(500, function() {
                message.remove();
            });
        }, 2000);
        setTimeout(function() {
            button.removeAttr('disabled').removeClass('disabled');
        }, 5000);
    }

    function nextIndex(fieldset) {
        var index = -1;
        $('table tbody .time-restriction-row', fieldset).each(function() {
            index = Math.max(index, $(this).attr('data-index'));
        });
        return index + 1;
    }

    function renameFields(parent, target, replacement) {
        $('[name=' + target + '.startTime]', parent).attr('name', replacement + '.startTime').attr('id', replacement + '.startTime');
        $('[name=' + target + '.startTime_hour]', parent).attr('name', replacement + '.startTime_hour').attr('id', replacement + '.startTime_hour');
        $('[name=' + target + '.startTime_minute]', parent).attr('name', replacement + '.startTime_minute').attr('id', replacement + '.startTime_minute');

        $('[name=' + target + '.endTime]', parent).attr('name', replacement + '.endTime').attr('id', replacement + '.endTime');
        $('[name=' + target + '.endTime_hour]', parent).attr('name', replacement + '.endTime_hour').attr('id', replacement + '.endTime_hour');
        $('[name=' + target + '.endTime_minute]', parent).attr('name', replacement + '.endTime_minute').attr('id', replacement + '.endTime_minute');

        $('[name=' + target + '.monday]', parent).attr('name', replacement + '.monday').attr('id', replacement + '.monday');
        $('[name=_' + target + '.monday]', parent).attr('name', '_' + replacement + '.monday').attr('id', '_' + replacement + '.monday');

        $('[name=' + target + '.tuesday]', parent).attr('name', replacement + '.tuesday').attr('id', replacement + '.tuesday');
        $('[name=_' + target + '.tuesday]', parent).attr('name', '_' + replacement + '.tuesday').attr('id', '_' + replacement + '.tuesday');

        $('[name=' + target + '.wednesday]', parent).attr('name', replacement + '.wednesday').attr('id', replacement + '.wednesday');
        $('[name=_' + target + '.wednesday]', parent).attr('name', '_' + replacement + '.wednesday').attr('id', '_' + replacement + '.wednesday');

        $('[name=' + target + '.thursday]', parent).attr('name', replacement + '.thursday').attr('id', replacement + '.thursday');
        $('[name=_' + target + '.thursday]', parent).attr('name', '_' + replacement + '.thursday').attr('id', '_' + replacement + '.thursday');

        $('[name=' + target + '.friday]', parent).attr('name', replacement + '.friday').attr('id', replacement + '.friday');
        $('[name=_' + target + '.friday]', parent).attr('name', '_' + replacement + '.friday').attr('id', '_' + replacement + '.friday');

        $('[name=' + target + '.saturday]', parent).attr('name', replacement + '.saturday').attr('id', replacement + '.saturday');
        $('[name=_' + target + '.saturday]', parent).attr('name', '_' + replacement + '.saturday').attr('id', '_' + replacement + '.saturday');

        $('[name=' + target + '.sunday]', parent).attr('name', replacement + '.sunday').attr('id', replacement + '.sunday');
        $('[name=_' + target + '.sunday]', parent).attr('name', '_' + replacement + '.sunday').attr('id', '_' + replacement + '.sunday');
    }

    $('#addEmailTimeRestriction').click(function() {
        var fieldset = $('#emailTimeRestrictions');
        addTimeRestriction(fieldset, 'emailTimeRestrictions');
    });

    $('#addSmsTimeRestriction').click(function() {
        var fieldset = $('#smsTimeRestrictions');
        addTimeRestriction(fieldset, 'smsTimeRestrictions');
    });

    function addTimeRestriction(fieldset, prefix) {
        var index = nextIndex(fieldset);
        var clone = $('#timeRestrictionTemplate').clone(true).removeAttr('id');
        clone.attr('data-index', index);
        renameFields(clone, 'template', prefix + '[' + index + ']');
        $('table tbody', fieldset).append(clone);
        $('table', fieldset).show();
        $('.time-restriction-placeholder', fieldset).text('<g:message code="page.voicemail.settings.restriction.restrictedLabel"/>');
    }

    $('.time-restriction-row button').click(function() {
        var button = $(this);
        var fieldset = button.closest('.time-restrictions');
        if($('tr', button.closest('tbody')).length == 1) {
            $('table', fieldset).hide();
            $('.time-restriction-placeholder', fieldset).text('<g:message code="page.voicemail.settings.restriction.notRestrictedLabel"/>');
        }
        $(this).closest('tr').remove();
        var prefix = fieldset.attr('id');
        $('table tbody tr', fieldset).each(function(i) {
            var tr = $(this);
            var oldIndex = tr.attr('data-index');
            tr.attr('data-index', i);
            renameFields(tr, prefix + '[' + oldIndex + ']', prefix + '[' + i + ']');
        })
    });

    $('#emailNotificationAddress').focus(function() {
        $('#emailSource[value=custom]').attr('checked', 'checked');
    });
});
    </script>
  </body>
</html>