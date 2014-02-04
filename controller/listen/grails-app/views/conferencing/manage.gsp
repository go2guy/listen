<html>
  <head>
    <title><g:message code="page.conferencing.manage.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="conferencing"/>
    <meta name="button" content="manage"/>
    <style type="text/css">
#content-area {
    overflow: hidden;
    display: inline-block;
    display: block;
}

#conference-status {
    display: block;
    float: right;
    font-weight: bold;
    border-width: 0;
}

.conference-status-waiting {
    color: #CC0000;
}

.conference-status-started {
    color: #00CC00;
}

#callers {
    float: left;
    min-height: 450px;
    width: 616px;
}

#callers table {
    border-collapse: separate;
    border-spacing: 0 3px;
}

#callers table .col-status { width: 7%; }
#callers table .col-caller { width: 49%; }
#callers table .col-joined { width: 23%; }
#callers table .col-mute-button {
    text-align: center;
    width: 11%;
}
#callers table .col-drop-button {
    text-align: center;
    width: 10%;
}

.status-icon-admin { color: #E0B909; }
tr.passive { color: #999999; }

#pins,
#outdialing,
#recording {
    background-color: #EBEEF5;
    border: 1px solid #6DACD6;
    float: right;
    padding: 5px;
    width: 288px;

    border-radius: 5px;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
}

#pins table {
    border-collapse: separate;
    border-spacing: 0 3px;
}

#pins table tr {
    background-color: #F6F6F6;
}

#pins table .col-pinType {
    width: 40%;
}

#pins table .col-pin {
    font-size: 18px;
    letter-spacing: 3px;
    text-align: right;
    width: 60%;
}

#pins table .active-pin {
    font-weight: bold;
}

#outdialing fieldset {
    border-width: 0;
    padding: 0;
}

#outdialing input[type=text],
#outdialing select {
    width: 275px;
}

#outdialing input[type=submit] {
    display: block;
    margin: 5px auto;
    width: 90px;
}

#recording p {
    display: block;
    font-size: 14px;
    margin: 5px;
    padding: 5px 0;
}

#recording p.recording-status {
    background-color: #F6F6F6;
    text-align: center;
}

#recording p.recording-list-link {
    border-color: #6DACD6;
    border-style: solid;
    border-width: 1px 0 0 0;
    text-align: right;
}

#recording a {
    text-decoration: none;
}

#recording a:hover {
    color: #CCCCCC;
}

span.recording-status {
    display: inline-block;
    font-size: 16px;
    font-weight: bold;
    position: relative;
    top: 1px;
}

span.recording-status.stopped {
    color: #000000;
/*    content: '\25A0';
    margin-left: 5px;*/
}

span.recording-status.started {
    color: #CC0000;
/*    content: '\25CF';
    margin-left: 5px;*/
}

#recording input {
    display: block;
    margin: 5px auto;
    width: 120px;
}

/*#history {
    float: left;
    width: 100%;
}

#history table {
    font-size: 14px;
}

#history p {
    display: block;
    font-size: 14px;
    margin: 0 5px 5px 5px;
    padding: 5px 0;
    text-align: right;
}

#history p a {
    text-decoration: none;
}

#history p a:hover {
    color: #CCCCCC;
}*/

.panel {
    display: block;
    margin: 10px 0 0 0;
}

.panel h3 {
    border-width: 0;
    display: block;
    font-size: 14px;
    font-weight: bold;
    margin: 0;
    padding-bottom: 3px;
}

#no-caller-placeholder {
    display: block;
    background-color: #EBEEF5;
    padding: 5px;
}

.templates { display: none; }
.initially-hidden { display: none; }
    </style>
  </head>
  <body>
%{-- %%mark%% --}%
    <h3 id="conference-status" class="conference-status-${conference.isStarted ? 'started' : 'waiting'}">${conference.isStarted ? 'Started (' + listen.prettytime(date: conference.startTime) + ')' : 'Waiting for administrator'}</h3>
    <h3 id="conference-name">${fieldValue(bean: conference, field: 'description')}</h3>

    <!-- TODO add running timer to conference status -->

    <!-- TODO add an indicator message when the following events occur -->
    <!-- * Caller joins conference -->
    <!-- * Caller leaves conference -->
    <!-- * Caller dropped from conference -->
    <!-- * Caller was muted by admin -->
    <!-- * Caller was unmuted by admin -->
    <!-- * Caller muted themselves? -->
    <!-- * Caller unmuted themselves? -->
    <!-- * Number(s) X was/were dialed on-demand -->
    <!-- * Conference started -->
    <!-- * Conference ended -->
    <!-- * Conference recording started -->
    <!-- * Conference recording ended -->
    <!-- -->
    <!-- Perhaps the messages could "stack" and remain for about 5-10 seconds, so multiple events happening fast don't get missed -->
    <!-- These should still be written as histories, but perhaps as action histories now instead of having a separate conference history (maybe a subclass of action history that references the conference?)-->
    <!-- Messages could/should indicate channel, e.g. "Admin muted caller X via TUI" -->

    <div id="callers" class="panel">
      <table style="display: ${participantList?.size() > 0 ? 'table' : 'none'};">
        <thead>
          <tr>
            <th class="col-status"></th>
            <g:sortableColumn property="ani" title="Caller" class="col-caller"/>
            <g:sortableColumn property="dateCreated" title="Joined" class="col-joined"/>
            <th class="col-mute-button"></th>
            <th class="col-drop-button"></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${participantList}" var="participant" status="i">
            <tr class="odd${participant.isAdmin ? ' admin' : ''}${participant.isPassive ? ' passive' : ''}" data-id="${participant.id}">
              <td class="col-status">
                <span title="Conference administrator" class="status-icon-admin" style="visibility: ${participant.isAdmin ? 'visible' : 'hidden'};">&#9733;</span>
                <span title="Caller cannot talk" class="status-icon-muted" style="visibility: ${participant.isAdminMuted || participant.isMuted || participant.isPassive ? 'visible' : 'hidden'};">&otimes;</span>
              </td>
              <td class="col-caller">${participant.displayName().encodeAsHTML()}</td>
              <td class="col-joined"><listen:prettytime date="${participant.dateCreated}"/></td>
              <td class="col-mute-button">
                <g:form controller="conferencing" action="unmuteCaller" method="post" class="unmute-form ajax-form disable-on-submit${participant.isAdminMuted ? '' : ' initially-hidden'}">
                  <input type="hidden" name="id" value="${participant.id}"/>
                  <input type="submit" value="Unmute"/>
                </g:form>
                <g:form controller="conferencing" action="muteCaller" method="post" class="mute-form ajax-form disable-on-submit${!(participant.isPassive || participant.isAdmin || participant.isAdminMuted) ? '' : ' initially-hidden'}">
                  <input type="hidden" name="id" value="${participant.id}"/>
                  <input type="submit" value="Mute"/>
                </g:form>
              </td>
              <td class="col-drop-button">
                <g:form controller="conferencing" action="dropCaller" method="post" class="drop-form ajax-form disable-on-submit confirm-before-submit${participant.isAdmin ? ' initially-hidden' : ''}">
                  <input type="hidden" name="id" value="${participant.id}"/>
                  <input type="submit" value="Drop"/>
                </g:form>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>
      <div class="pagination" style="display: ${participantList?.size() > 0 ? 'block' : 'none'};">
        <listen:paginateTotal total="${participantTotal}" messagePrefix="paginate.total.callers"/>
        <g:paginate total="${participantTotal}" maxsteps="5"/>
      </div>
      <div id="no-caller-placeholder" style="display: ${participantList?.size() > 0 ? 'none' : 'block'};">
        Nobody has joined the conference.
      </div>
    </div>

    <div id="pins" class="panel">
      <h3>PINs</h3>
      <table>
        <tbody>
          <g:if test="${conference?.pins?.size() > 0}">
            <g:each in="${conference.pins.sort { it.pinType.name() }}" var="pin">
              <tr><td class="col-pinType ${pin.pinType.name().toLowerCase()}-pin">${pin.pinType.displayName().encodeAsHTML()}</td><td class="col-pin ${pin.pinType.name().toLowerCase()}-pin">${pin.number}</td></tr>
            </g:each>
          </g:if>
          <g:else>
            <tr><td>No pins configured</td></tr>
          </g:else>
        </tbody>
      </table>
    </div>

    <div id="outdialing" class="panel" style="display: ${conference.isStarted ? 'block' : 'none'};">
      <h3>On Demand</h3>
      <g:form controller="conferencing" action="outdial" method="post" class="ondemand-form ajax-form disable-on-submit">
        <input type="hidden" name="id" value="${conference.id}"/>
        <fieldset>
          <label for="onDemandNumber">Dial Number</label>
          <input type="text" id="onDemandNumber" name="onDemandNumber" class="clear-after-submit" autocomplete="off"/>
          <listen:autocomplete selector="#onDemandNumber" data="all.phones"/>

          <label for="onDemandMode">Mode</label>
          <select id="onDemandMode" name="onDemandMode">
            <option value="interactive">Let me talk to the person first</option>
            <option value="automated">Play pre-recorded message first</option>
          </select>
          <input type="submit" value="Make Call" disabled="disabled" readonly="readonly" class="disabled" title="Please enter a number to dial"/>
        </fieldset>
      </g:form>
    </div>

    <div id="recording" class="panel" style="display: ${conference.isStarted ? 'block' : 'none'};">
      <h3>Recording</h3>
      <p class="recording-status">
        Conference recording is <span class="recording-status ${conference.isRecording ? 'started' : 'stopped'}">${conference.isRecording ? 'started &#9679;' : 'stopped &#9632;'}</span>
      </p>
      <g:form controller="conferencing" action="stopRecording" class="stop-recording-form ajax-form disable-on-submit${conference.isRecording ? '' : ' initially-hidden'}">
        <input type="hidden" name="id" value="${conference.id}"/>
        <input type="submit" value="Stop Recording"/>
      </g:form>
      <g:form controller="conferencing" action="startRecording" class="start-recording-form ajax-form disable-on-submit${conference.isRecording ? ' initially-hidden' : ''}">
        <input type="hidden" name="id" value="${conference.id}"/>
        <input type="submit" value="Start Recording"/>
      </g:form>
      <p class="recording-list-link"><g:link controller="conferencing" action="recordings">View Recordings List</g:link></p>
    </div>

    <table class="templates">
      <tr id="participant-row-template" class="odd">
        <td class="col-status">
          <span title="Conference administrator" class="status-icon-admin" style="visibility: 'hidden';">&#9733;</span>
          <span title="Caller cannot talk" class="status-icon-muted" style="visibility: 'hidden';">&otimes;</span>
        </td>
        <td class="col-caller"></td>
        <td class="col-joined"></td>
        <td class="col-mute-button">
          <g:form controller="conferencing" action="unmuteCaller" method="post" class="unmute-form ajax-form disable-on-submit">
            <input type="hidden" name="id" value=""/>
            <input type="submit" value="Unmute"/>
          </g:form>
          <g:form controller="conferencing" action="muteCaller" method="post" class="mute-form ajax-form disable-on-submit">
            <input type="hidden" name="id" value="}"/>
            <input type="submit" value="Mute"/>
          </g:form>
        </td>
        <td class="col-drop-button">
          <g:form controller="conferencing" action="dropCaller" method="post" class="drop-form ajax-form disable-on-submit confirm-before-submit">
            <input type="hidden" name="id" value=""/>
            <input type="submit" value="Drop"/>
          </g:form>
        </td>
      </tr>
    </table>

    <script type="text/javascript">

var conference = {
    poll: function() {
        $.ajax({
            url: '${createLink(action: 'polledConference', params: [id: conference.id, sort: params.sort, order: params.order, max: params.max, offset: params.offset])}',
            dataType: 'json',
            cache: false,
            success: function(data) {

                // 0. update conference details

                var h3 = $('#conference-status');
                if(data.conference.isStarted && !h3.hasClass('conference-status-started')) {
                    h3.removeClass('conference-status-waiting').addClass('conference-status-started').html('Started (' + data.conference.started + ')');
                    $('#outdialing, #recording').fadeIn(1000);
                } else if(!data.conference.isStarted && !h3.hasClass('conference-status-waiting')) {
                    h3.removeClass('conference-status-started').addClass('conference-status-waiting').text('Waiting for administrator');
                    $('#outdialing, #recording').fadeOut(1000);
                } else if(data.conference.isStarted && h3.text() != 'Started (' + data.conference.started + ')') {
                    h3.text('Started (' + data.conference.started + ')');
                }

                var recordingStatus = $('span.recording-status');
                if(data.conference.isRecording && !recordingStatus.hasClass('started')) {
                    recordingStatus.removeClass('stopped').addClass('started').html('started &#9679;');
                    $('.start-recording-form').hide();
                    $('.stop-recording-form').show();
                } else if(!data.conference.isRecording && !recordingStatus.hasClass('stopped')) {
                    recordingStatus.removeClass('started').addClass('stopped').html('stopped &#9632;');
                    $('.stop-recording-form').hide();
                    $('.start-recording-form').show();
                }

                var placeholder = $('#no-caller-placeholder');
                var table = $('#callers > table, #callers > div.pagination');
                if(data.participants.list.length == 0 && !placeholder.is(':visible')) {
                    table.hide(0, function() {
                        placeholder.show();
                    });
                } else if(data.participants.list.length > 0 && placeholder.is(':visible')) {
                    placeholder.hide(0, function() {
                        table.show();
                    });
                }

                // 1. loop through table rows and remove rows that don't exist in the new data

                var tbody = $('#callers table tbody');

                $('tr', tbody).each(function() {
                    var tr = $(this);
                    var rowId = parseInt(tr.attr('data-id'), 10);
                    if($.inArray(rowId, data.participants.ids) === -1) {
                        tr.remove();
                    }
                });

                // 2. loop through new rows and move existing rows / add new rows

                for(var i = 0; i < data.participants.list.length; i++) {
                    var participant = data.participants.list[i];

                    // 2a. find position of participant in existing table

                    var position = -1;
                    var tr; // will be set if a table row is found for this participant
                    $('tr', tbody).each(function(index) {
                        var rowId = parseInt($(this).attr('data-id'), 10);
                        if(rowId === participant.id) {
                            position = index;
                            tr = $(this);
                        }
                    });

                    // 2b. if position is the same, move onto next participant

                    if(position === i) {
                        conference.populate(tr, participant, true);
                        continue;
                    }

                    // 2c. if participant wasn't found, add it to the table
                    // 2d. (else) if participant did exist, it needs to be moved

                    if(position === -1) {
                        tr = $('#participant-row-template').clone(true).removeAttr('id');
                        conference.populate(tr, participant, false);
                    
                        if(i === 0 || $('tr', tbody).length === 0) {
                            tbody.prepend(tr);
                        } else {
                            $('tr:eq(' + (i - 1) + ')', tbody).after(tr);
                        }

                        conference.highlight(tr);
                    } else {
                        // participant did exist in table , but needs to be moved
                        conference.populate(tr, participant, false);
                        $('tr:eq(' + (i - 1) + ')', tbody).after(tr);
                    }
                }

                conference.redrawPagination(data.participants.total, data.participants.max, data.participants.offset, data.participants.sort, data.participants.order);

                // TODO destroy/create pagination and page user to the appropriate page
                // - participant dropped removes the currently viewed page --> user should see the previous page
            }
        });
    },

    populate: function(row, participant, animate) {
        var changed = false;
        changed = conference.updateField($('td.col-caller', row), participant.displayName) || changed;
        conference.updateField($('td.col-joined', row), participant.joined);
        if(row.attr('data-id') != participant.id) {
            changed = true;
            row.attr('data-id', participant.id);
        }

        changed = conference.updateVisibility($('.status-icon-admin', row), participant.isAdmin ? 'visible' : 'hidden') || changed;
        changed = conference.updateVisibility($('.status-icon-muted', row), participant.isAdminMuted || participant.isMuted || participant.isPassive ? 'visible' : 'hidden') || changed;

        $('.unmute-form input[name="id"], .mute-form input[name="id"], .drop-form input[name="id"]', row).val(participant.id);
        $('.unmute-form', row).toggle(participant.isAdminMuted);
        $('.mute-form', row).toggle(!(participant.isPassive || participant.isAdmin || participant.isAdminMuted));
        $('.drop-form', row).toggle(!participant.isAdmin);

        if(changed && animate === true) {
            conference.highlight(row);
        }
        return changed;
    },

    highlight: function(row) {
       row.css('background-color', '#97A5F7');
       row.animate({ backgroundColor: '#E4E6ED' }, { duration: 2000 });
    },

    updateVisibility: function(element, visibility) {
        if(element.css('visibility') != visibility) {
            element.css('visibility', visibility);
            return true;
        }
        return false;
    },

    updateField: function(field, content, asHtml) {
        var changed = false;
        if(asHtml === true) {
            if(field.html() != content) {
                field.html(content);
                changed = true;
            }
        } else {
            var c = String(content);
            if(field.text() != c) {
                field.text(c);
                changed = true;
            }
        }
        return changed;
    },

    redrawPagination: function(total, max, offset, sort, order) {
        $.ajax({
            url: '${createLink(action: 'ajaxPagination')}?c=conferencing&a=manage&total=' + total + '&max=' + max + '&offset=' + offset + '&sort=' + sort + '&order=' + order,
            dataType: 'html',
            cache: false,
            success: function(data) {
                var current = $('div.pagination');
                if(current.html() != data) {
                    $('div.pagination > *').remove();
                    current.html(data);
                }
            }
        });
    },

    toggleOutdialButton: function() {
        var number = $.trim($('#onDemandNumber').val());
        var enable = number.length > 0
        var button = $('#outdialing input[type="submit"]');
        if(enable && button.hasClass('disabled')) {
            button.removeAttr('disabled').removeAttr('readonly').removeClass('disabled').attr('title', 'Make call');
        } else if(!enable && !button.hasClass('disabled')) {
            button.attr('disabled', 'disabled').attr('readonly', 'readonly').addClass('disabled').attr('title', 'Please enter a number to dial');
        }
    }
};

$(document).ready(function() {
    setInterval(conference.poll, 1000);

    $('#onDemandNumber').keyup(function(e) {
        util.typewatch(function() {
            conference.toggleOutdialButton();
        }, 250);
    }).bind('paste', function(e) {
        // delay since it takes time for the text to actually paste
        setTimeout(conference.toggleOutdialButton, 100);
    });

    $('.ondemand-form').submit(function(e) {
        conference.toggleOutdialButton();
    });
});
    </script>
  </body>
</html>
