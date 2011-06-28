<html>
  <head>
    <title><g:message code="page.conferencing.scheduling.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="conferencing"/>
    <meta name="button" content="scheduling"/>
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
    </style>
  </head>
  <body>
    <listen:infoSnippet summaryCode="page.conferencing.scheduling.snippet.summary" contentCode="page.conferencing.scheduling.snippet.content"/>

    <h3>Schedule a Conference</h3>
    <g:form controller="conferencing" action="schedule" method="post">
      <fieldset class="vertical">
        <label>Date &amp; Time</label>
        <joda:datePicker id="date" name="date" value="${scheduledConference?.date}"/>

        <label class="inline-label">from</label>
        <listen:timePicker name="starts" value="${scheduledConference?.starts}"/>

        <label class="inline-label">to</label>
        <listen:timePicker name="ends" value="${scheduledConference?.ends}"/>

        <h3>Email Invitations</h3>

        <!-- TODO add a little info blurb here about what will happen -->

        <div id="emailDetails">
            <label for="emailSubject"><g:message code="scheduledConference.emailSubject.label"/></label>
            <g:textField name="emailSubject" value="${fieldValue(bean: scheduledConference, field: 'emailSubject')}"/>

            <label for="emailBody"><g:message code="scheduledConference.emailBody.label"/></label>
            <g:textArea name="emailBody" value="${fieldValue(bean: scheduledConference, field: 'emailBody')}"/>
        </div>

        <div id="emailCallers">
            <label for="activeCallerAddresses">Active caller email addresses</label>
            <g:textArea name="activeCallerAddresses" value="${fieldValue(bean: scheduledConference, field: 'activeCallerAddresses')}"/>

            <label for="passiveCallerAddresses">Passive caller email addresses</label>
            <g:textArea name="passiveCallerAddresses" value="${fieldValue(bean: scheduledConference, field: 'passiveCallerAddresses')}"/>
        </div>

        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="Schedule & Send Emails"/></li>
        </ul>
      </fieldset>
    </g:form>

    <g:render template="scheduleListTemplate" model="${[list: scheduleLists.future, caption: 'Upcoming Conferences', placeholder: 'You do not have any upcoming conferences', showCancel: true]}"/>
    <g:render template="scheduleListTemplate" model="${[list: scheduleLists.past, caption: 'Past Conferences', placeholder: 'You do not have any past conferences', showCancel: false]}"/>
    <script type="text/javascript">
$(document).ready(function() {
    $('tr.schedule-details-row').each(function() {
        var row = $(this);
        row.hide();

        var linkCell = $('td.col-details', row.prev('tr'));
        var link = $('<a href="#">Toggle Details</a>');
        link.click(function(e) {
            row.toggle();
            return false;
        });

        linkCell.append(link);
    });
    $('.cancel-form').submit(function() {
        return confirm('Are you sure? Calendar cancellations will be sent to all invited callers.');
    });
});
    </script>
  </body>
</html>