<html>
  <head>
    <title><g:message code="page.conferencing.invitations.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="conferencing"/>
    <meta name="button" content="invitations"/>
    <style type="text/css">
fieldset.vertical select {
    display: inline;
    width: auto;
}

textarea {
    height: 50px;
    width: 448px;;
}

textarea#emailBody {
    height: 100px;
}

#emailDetails {
    display: inline-block;
    width: 458px;
}

#emailCallers {
    display: inline-block;
    width: 458px;
}

table.schedule-list {
    margin-top: 10px;
}

.col-when { width: 228px; }
.col-duration { width: 135; }
.col-subject { width: 367px; }
.col-invited { width: 88px; }
.col-details {
    font-size: 12px;
    width: 88px;;
}

.col-details a {
    text-decoration: none;
}

.col-details a:hover {
    color: #CCCCCC;
}

table.schedule-details {
    border-collapse: separate;
    border-spacing: 2px;
    width: 922px;
}

table.schedule-details th,
table.schedule-details td {
    font-size: 14px;
    text-align: left;
}

table.schedule-details th {
    background-color: #176BA3;
    color: #FFFFFF;
    font-style: italic;
    font-weight: normal;
    padding-left: 10px;
    padding-right: 10px;
    width: 100px;
}

table.schedule-details ul li {
    background-color: #FFFFFF;
    display: inline-block;
    margin: 2px;
    overflow: hidden;
    padding: 2px;
    text-overflow: ellipsis;
    width: 144px;
}

.schedule-details-row form {
    display: inline;
}
    </style>
  </head>
  <body>
    <g:if test="${!edit}">
      <listen:infoSnippet summaryCode="page.conferencing.invitations.snippet.summary" contentCode="page.conferencing.invitations.snippet.content"/>
    </g:if>

    <h3><g:message code="page.conferencing.invitations.header${edit ? '.edit' : ''}"/></h3>
    <g:form controller="conferencing" action="${edit ? 'updateInvitation' : 'invite'}" method="post">
      <g:if test="${edit}">
        <g:hiddenField name="id" value="${scheduledConference.id}"/>
      </g:if>
      <fieldset class="vertical">
        <div class="date-fields">
          <label><g:message code="page.conferencing.invitations.header.datetime"/></label>
          <joda:datePicker id="date" name="date" value="${scheduledConference?.date}"/>

          <label class="inline-label"><g:message code="page.conferencing.invitations.from.time.label"/></label>
          <listen:timePicker name="starts" value="${scheduledConference?.starts}"/>

          <label class="inline-label"><g:message code="page.conferencing.invitations.to.time.label"/></label>
          <listen:timePicker name="ends" value="${scheduledConference?.ends}"/>

          <g:if test="${scheduledConference?.isPast()}">
            <ul class="messages warning"><li><g:message code="page.conferencing.invitations.historic.warning"/></li></ul>
          </g:if>
        </div>

        <h3><g:message code="page.conferencing.invitations.header.email"/></h3>

        <!-- TODO add a little info blurb here about what will happen -->

        <div id="emailDetails">
            <label for="emailSubject"><g:message code="scheduledConference.emailSubject.label"/></label>
            <g:textField name="emailSubject" value="${fieldValue(bean: scheduledConference, field: 'emailSubject')}" class="${listen.validationClass(bean: scheduledConference, field: 'emailSubject')}"/>

            <label for="emailBody"><g:message code="scheduledConference.emailBody.label"/></label>
            <g:textArea name="emailBody" value="${fieldValue(bean: scheduledConference, field: 'emailBody')}" class="${listen.validationClass(bean: scheduledConference, field: 'emailBody')}"/>
        </div>

        <div id="emailCallers">
            <label for="activeCallerAddresses"><g:message code="page.conferencing.invitations.active.email.label"/></label>
            <g:textArea name="activeCallerAddresses" value="${fieldValue(bean: scheduledConference, field: 'activeCallerAddresses')}" class="${listen.validationClass(bean: scheduledConference, field: 'activeCallerAddresses')}"/>

            <label for="passiveCallerAddresses"><g:message code="page.conferencing.invitations.passive.email.label"/></label>
            <g:textArea name="passiveCallerAddresses" value="${fieldValue(bean: scheduledConference, field: 'passiveCallerAddresses')}" class="${listen.validationClass(bean: scheduledConference, field: 'passiveCallerAddresses')}"/>
        </div>

        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="${g.message(code: 'page.conferencing.invitations.' + (edit ? 'edit' : 'send') + '.button.label')}"/></li>
        </ul>
      </fieldset>
    </g:form>

    <g:render template="scheduleListTemplate" model="${[list: scheduleLists.future, caption: 'Upcoming Conferences', placeholder: 'You do not have any upcoming conferences', showCancel: true]}"/>
    <g:render template="scheduleListTemplate" model="${[list: scheduleLists.past, caption: 'Past Conferences', placeholder: 'You do not have any past conferences', showCancel: false]}"/>
    <script type="text/javascript">
var invitations = {
    checkAndWarnHistoricDate: function() {
        var y = $('#date_year').val();
        var mo = parseInt($('#date_month').val(), 10) - 1;
        var d = $('#date_day').val();
        var h = $('#starts_hour').val();
        var mn = $('#starts_minute').val();

        var date = new Date(y, mo, d, h, mn, 0, 0);
        var now = new Date();

        log.debug('date ' + date + ', now ' + now);

        var isPast = date.getTime() < now.getTime();
        var hasWarning = $('.date-fields .warning').size() > 0;

        log.debug('isPast? ' + isPast + ', hasWarning? ' + hasWarning);

        if(isPast && !hasWarning) {
            $('.date-fields').append('<ul class="messages warning"><li><g:message code="page.conferencing.invitations.historic.warning"/></li></ul>');
        } else if(!isPast && hasWarning) {
            $('.date-fields .warning').remove();
        }
    }
};
$(document).ready(function() {
    $('tr.schedule-details-row').each(function() {
        var row = $(this);
        row.hide();

        var linkCell = $('td.col-details', row.prev('tr'));
        var link = $('<a href="#">Show/Hide Details</a>');
        link.click(function(e) {
            row.toggle();
            return false;
        });

        linkCell.append(link);
    });
    $('.cancel-form').submit(function() {
        return confirm('Are you sure? Calendar cancellations will be sent to all invited callers.');
    });

    $('.date-fields select').change(invitations.checkAndWarnHistoricDate);

    $.get('${createLink(controller: 'autocomplete', action: 'contacts')}', function(data) {
        function split(val) {
            return val.split(/,\s*/);
        }

        function last(val) {
            return split(val).pop();
        }

        $('#activeCallerAddresses,#passiveCallerAddresses').bind('keydown', function(e) {
            if(e.keyCode === $.ui.keyCode.TAB && $(this).data('autocomplete').menu.active) {
                e.preventDefault();
            }
        }).autocomplete({
            source: function(request, response) {
                var term = last(request.term);
                var matcher = new RegExp($.ui.autocomplete.escapeRegex(term), 'i');
                response($.grep(data.all.emails, function(value) {
                    return matcher.test(value.value)
                            || matcher.test(value.name);
                }));
            },
            delay: 0,
            minLength: 1,
            focus: function() {
                return false;
            },
            select: function(e, ui) {
                var terms = split(this.value);

                // replace the entered text with the selected autocompleted value
                terms.pop();
                terms.push(ui.item.value);
                terms.push('');

                this.value = terms.join(', ');
                return false;
            }
        });
    });
});
    </script>
  </body>
</html>