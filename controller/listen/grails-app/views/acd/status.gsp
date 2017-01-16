<html>
  <head>
    <title><g:message code="page.acd.status.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="acd"/>
    <meta name="button" content="status"/>

    <style type="text/css">
        div.panel {
          background-color: #EBEEF5;
          border: 1px solid #6DACD6;
          border-radius: 5px;
          display: block;
          margin: 10px 0 0 0;
          padding: 5px;
          -webkit-border-radius: 5px;
          width: 288px;
        }

        div #left-column {
          width: 65%;
          float: left;
        }

        div #right-column {
          width: 30%;
          float: left;
          margin: 20px 10px 10px 20px;
        }

        div.padded {
          padding: 10px 0px 0px 0px;
        }

        td.col-button {
            text-align: left;
            width: 10%;
        }

        #currentCall table .col-hold-button
        {
            text-align: center;
            width: 11%;
        }

        table.fixed {
          table-layout: fixed;
        }

        td.overflow {
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }

        .holdBox { width: 50px; }

        .caller { width: 125px; }

        .callerSkill { width: 150px; }

        .template { display: none; }
        .initially-hidden { display: none; }
        .hidden { display: none; }

    </style>

    <script type="text/javascript">
    // Given a time in milliseconds returns a string representation in HH:MM:SS format
    function formatTime(time) {
        var milliInHour = 60 * 60 * 1000;
        var milliInMinute = 60 * 1000;

        var hours = Math.floor(time / milliInHour);

        if (hours.toString().length == 1)
        {
            hours = "0" + hours;
        }

        var minutes = Math.floor( (time - (hours * milliInHour)) / milliInMinute);

        if (minutes.toString().length == 1)
        {
            minutes = "0" + minutes;
        }

        var seconds = Math.floor(((time - (hours * milliInHour) - (minutes * milliInMinute)) / 1000 ) );

        if (seconds.toString().length == 1)
        {
            seconds = "0" + seconds;
        }

        return (hours + ':' + minutes + ':' + seconds);
    }

    // Removes the annoyances of a mysql timestamp - returns YYYY-MM-DD HH:MM:SS format
    function formatDate(date) {
      date = date.split(/[- : T]/);
      return (date[0] + "-" + date[1] + "-" + date[2] + " " + date[3] + ":" + date[4] + ":" + date[5]);
    }

    // Given a mysql timestamp returns current difference in HH:MM:SS format
    function getDifference(start,end) {
        if(!start || !end)
        {
            return 'N/A';
        }

        var startTime = new Date(start);
        var endTime = new Date();
        if(end != 'now')
        {
            endTime = new Date(end);
        }

        difference = endTime.getTime() - startTime.getTime();

        return formatTime(difference);
    }
    </script>

  </head>
  <body>

    <div id="left-column">
      <div id="currentCall">
        <h3>Current Call:</h3>
        <form id="callForm" method="POST">
        <div id="callListDiv">
          <table>

          <thead>
            <tr>
              <th class="holdBox">Hold</th>
              <th class="caller">Caller</th>
              <th class="callerSkill">Skill</th>
              <th id="transferHeader" width=7%></th>
              <th id="disconnectHeader" width=7%></th>
              <th></th>
            </tr>
          </thead>

          <tbody>
            <table id="callTable">
              <g:each in="${currentCalls}" status="i" var="thisCall">
                <tr class="even" data-id="${thisCall.id}">
                <input id="hiddenCallId" type="hidden" name="id" value="${thisCall.id}"/>
                  <td class="holdBox">
                    <g:checkBox name="holdCheckBox" value="${thisCall.id}" class="case holdCheckbox"
                                checked="${thisCall.onHold}" onchange="toggleHold(this, this.value)"/>
                  </td>
                  <td class="caller">${thisCall.ani}</td>
                  <td class="callerSkill">${thisCall.skill.description}</td>
                  <td class="transfer-button"  width=7%>
                    <button type="button" class="transferButton" id="transferButton"
                            value="${thisCall.id}"
                            onclick="transferClicked(this, this.value)">Transfer</button>
                  </td>
                  <td class="disconnect-button">
                    <button type="button" class="disconnectButton" id="disconnectButton"
                            value="${thisCall.id}"
                            onclick="disconnectClicked(this, this.value)">Voicemail</button>
                  </td>
                  <td class="transfer-dropdown hidden">
                    <div id="transferDropdownDiv">
                      <select class="transferAgentSelect" id="transferAgents"></select>
                      <button type="button" class="submitTransferButton" id="submitTransferButton"
                              onclick="submitTransferClicked(this)">Transfer Call</button>
                      <button type="button" class="cancelTransferButton" id="cancelTransferButton"
                              onclick="cancelTransferClicked(this)">Cancel</button>
                    </div> <!-- transferDropdownDiv -->
                  </td>
                </tr>
              </g:each>
            </table> <!-- callTable -->
          </tbody> <!-- ???.tbody -->

          </table> <!-- ??? -->
        </div> <!-- callListDiv -->
        </form> <!-- callForm -->
      </div> <!-- currentCall -->

      <div class="padded" id="agent-queue">
        <h3>Waiting Calls:</h3>
        <div>
          <g:if test="${callTotal > 0}">
            <table id="callQueue" cellspacing="0" cellpadding="0" class="fixed">

            <thead>
              <tr>
                <th>Caller</th>
                <th>Skill</th>
                <th>Wait Time</th>
                <th>Last Activity</th>
              </tr>
            </thead>

            <tbody>
              <g:set var="row_count" value="${0}"/>
              <g:each in="${calls}" var="call">
                <g:set var="column_count" value="${0}"/>
                <tr class="${++row_count % 2 == 0 ? 'even' : 'odd'}">
                  <td class="overflow">${call.ani}</td>
                  <td class="overflow">${call.skill.description}</td>
                  <script type="text/javascript">
                  document.write('<td class="overflow">' + getDifference('${call.enqueueTime}','now') + '</td>');
                  document.write('<td class="overflow">' + getDifference('${call.lastModified}','now') + '</td>');
                  </script>
                </tr>
              </g:each>
            </tbody>

            </table> <!-- callQueue -->
          </g:if>
          <g:else>
            <span><g:message code="page.acd.status.noCalls"/></span> 
          </g:else>
        </div>
      </div> <!-- agent-queue -->

    <div class="padded" id="agent-history">
      <h3>Call History:</h3>
      <div>
        <g:if test="${historyTotal > 0}">
          <table id="history-table" cellspacing="0" cellpadding="0" class="fixed">

            <thead>
              <tr>
                <th>Caller</th>
                <th>Skill</th>
                <th>Agent Time</th>
                <th>Queue Time</th>
              </tr>
            </thead>

            <tbody>
              <g:set var="row_count" value="${0}"/>
              <g:each in="${acdCallHistory}" var="call">
                <tr class="${++row_count % 2 == 0 ? 'even' : 'odd'}"/>
                  <td class="overflow">${call.ani}</td>
                  <td class="overflow">${call.skill.description}</td>
                  <script type="text/javascript">
                    document.write('<td class="overflow">' + getDifference('${call.agentCallStart}','${call.agentCallEnd}') + '</td>');
                    document.write('<td class="overflow">' + getDifference('${call.enqueueTime}','${call.dequeueTime}') + '</td>');
                  </script>
                </tr>
              </g:each>
            </tbody>

          </table> <!-- history-table -->
        </g:if>
        <g:else>
          <span><g:message code="page.acd.status.noHistory"/></span>
        </g:else>
      </div>
    </div> <!-- agent-history -->
    </div> <!-- left-column -->

    <div id="right-column">
      <div id="agent-status" class="panel">
        <h3><g:message code="page.acd.status.yourStatus.label"/></h3>

        <div id="agent-status-toggle">
          <g:form controller="acd" action="toggleStatus" method="post">
          <g:set var="titleMessage" value="" />
          <g:submitButton id="statusButton" name="toggle_status" value="${status}" class="statusButton ${status}"
                          title="${g.message(code: 'page.acd.status.button.' + status)}"
                          disabled="${statusDisabled}" />
          </g:form>
        </div> <!-- agent-status-toggle -->
        <div class="padded" id="agent-number">
          <g:form controller="acd" action="updateNumber" method="post">
            <h3><g:message code="page.acd.status.yourNumber.label"/></h3>
            <g:select name="contactNumber" optionKey="id" optionValue="number" from="${phoneNumbers}"
                      value="${contactNumber != null ? contactNumber.id : ''}" 
                      noSelection="['':'-- Select Contact Number --']"
                      onchange="submit()"/>
          </g:form>
        </div> <!-- agent-number -->
        <div class="padded" id="agent-skills">
          <h3><g:message code="page.acd.status.yourSkills.label"/></h3>
          <g:if test="${userSkills.size() > 0}">
            <ul>
              <g:each in="${userSkills}" var="userSkill">
                <li>${userSkill.skill.description}</li>
              </g:each>
            </ul>
          </g:if>
          <g:else>
            <g:message code="page.acd.status.noConfiguredSkills.label"/>
          </g:else>
        </div> <!-- agent-skills -->
      </div> <!-- agent-status -->
    </div> <!-- right-column -->

    <table class="template">
      <tr class='even' id="call-row-template">
        <input id="hiddenCallId" type="hidden" name="id" value=""/>
        <td class="holdBox">
          <g:checkBox name="holdCheckBox" value="" class="case holdCheckbox" checked="" onchange="toggleHold(this, this.value)"/>
        </td>
        <td class="caller"></td>
        <td class="callerSkill"></td>
        <td class="transfer-button" width=7%>
          <button type="button" class="transferButton" id="transferButton" value=""
                  onclick="transferClicked(this, this.value)">Transfer</button>
        </td>
        <td class="disconnect-button" >
          <button type="button" class="disconnectButton" id="disconnectButton"
                  value=""
                  onclick="disconnectClicked(this, this.value)">Voicemail</button>
        </td>
        <td class="transfer-dropdown hidden">
          <div id="transferDropdownDiv">
            <select class="transferAgentSelect" id="transferAgents"></select>
              <button type="button" class="submitTransferButton" id="submitTransferButton"
                      onclick="submitTransferClicked(this)">Transfer Call</button>
              <button type="button" class="cancelTransferButton" id="cancelTransferButton"
                      onclick="cancelTransferClicked(this)">Cancel</button>
          </div>
        </td>
      </tr>
    </table>

    <script type="text/javascript">
    $(document).ready( function () {
      setTimeout(agentStatus.poll,5000);
    });

    var agentStatus = {
      poll: function() {
        $.ajax({
          // passing in params to ensure sort and pagination are kept
          url: '${createLink(action: 'pollStatus')}',
          dataType: 'json',
          cache: false,
          success: function(data) {
            if ( data && data.calls.length > 0 )
            {
              // update agent call queue
              var tbody = $("#agent-queue > div > table > tbody");
              // tbody doesn't exist! (jquery has a weird way of checking this)
              if ( tbody.length == 0 ) {
                // build it from scratch
                var div = $("#agent-queue > div");
                div.empty();
                div.append('<table id="callQueue" cellspacing="0" cellpadding="0" class="fixed"></table>');
                var table = $("#callQueue");
                table.append('<thead><tr><th>Caller</th><th>Skill</th><th>Wait Time</th><th>Last Activity</th></thead>');
                table.append('<tbody></tbody>');
                tbody = $("agent-queue > div > table > tbody");
              }
              tbody.empty();
              var tr;
              var row_count = 0;
              data.calls.forEach(function(call) {
                tr = "";

                tr += '<tr class="' + (++row_count % 2 == 0 ? 'even' : 'odd') + '">';
                tr += '<td class="overflow">' + call.ani + '</td>';
                tr += '<td class="overflow">' + call.skill + '</td>';
                tr += '<td class="overflow">' + getDifference(call.enqueueTime,'now') + '</td>';
                tr += '<td class="overflow">' + getDifference(call.lastModified,'now') + '</td></tr>';

                tbody.append(tr);
              });
            } // if ( data && ... )
            else
            { // there were no calls in the queue
              var tbody = $("#agent-queue > div > table > tbody");
              // if there were previously calls ( aka tbody exists )
              if ( tbody.length > 0 ) {
                // we need to remove the table
                var div = $("#agent-queue > div");
                div.empty();
                div.append('<span>You have no waiting calls.</span>');
              } // if ( tbody.length > 0 ) ...
            }

            //Now Check for change in status
            if(data && data.userStatus)
            {
                if($('#statusButton')[0].value != data.userStatus)
                {
                    $('#statusButton')[0].value = data.userStatus;

                    if(data.userStatus == 'Unavailable')
                    {
                        $('.statusButton').removeClass('Available');
                        $('.statusButton').addClass('Unavailable');
                    }
                    else
                    {
                        $('.statusButton').removeClass('Unavailable');
                        $('.statusButton').addClass('Available');
                    }

                    $('#statusButton')[0].title = data.userStatusTitle;
                }
            }
          } // success
        }); // $.ajax

        setTimeout(agentStatus.poll,5000);
      } // agentStatus.poll
    }; // agentStatus


    function transferClicked(e, callId) {
        $('.transfer-button').addClass('hidden');
        $('.disconnect-button').addClass('hidden');
        $('#transferHeader')[0].innerHTML = "Transfer";

        $.ajax({
            url: '${createLink(action: 'availableTransferAgents')}?id=' + callId,
            dataType: 'json',
            cache: false,
            success: function(data)
            {
                $('.transferAgentSelect').empty();

                if(data && data.users)
                {
                    for(var i = 0; i < data.users.length; i++)
                    {
                        var thisAgent = data.users[i];
                        $('.transferAgentSelect').
                                append('<option value="' + thisAgent.id + '">' + thisAgent.realName + '</option>');
                    }
                }
            }
        });

        $('.transfer-dropdown').removeClass('hidden');
        return true;
    }

    function submitTransferClicked(e) {
        if(confirm('Transfer Call?'))
        {
            var userId = $('.transferAgentSelect')[0].value;
            var callId = $('#hiddenCallId')[0].value;

            $.ajax({
                url: '${createLink(action: 'transferCaller')}?id=' + callId + '&userId=' + userId,
                dataType: 'json',
                cache: false,
                success: function(data)
                {
                    if(data && data.success == "true")
                    {
                        listen.showSuccessMessage('Call transferred.')
                        $('.transfer-dropdown').addClass('hidden');
                        $('.transfer-button').removeClass('hidden');
                        $('.transfer-button').disabled = true;
                        $('.disconnect-button').removeClass('hidden');
                        $('.disconnect-button').disabled = true;
                        $('#transferHeader')[0].innerHTML = "";
                    }
                    else
                    {
                        listen.showErrorMessage('Unable to transfer call.')
                        $('.transfer-button').removeClass('hidden');
                        $('.disconnect-button').removeClass('hidden');
                        $('.transfer-dropdown').addClass('hidden');
                        $('#transferHeader')[0].innerHTML = "";
                    }
                }
            });

            return true;
        }
        else
        {
            return false;
        }
    }

    function cancelTransferClicked(e) {
        $('.transfer-button').removeClass('hidden');
        $('.disconnect-button').removeClass('hidden');
        $('.transfer-dropdown').addClass('hidden');
        $('#transferHeader')[0].innerHTML = "";
        return true;
    }

    function disconnectClicked(e) {
        if(confirm('Transfer the call to Voicemail?'))
        {
            var callId = $('#hiddenCallId')[0].value;

            $.ajax({
                url: '${createLink(action: 'disconnectCaller')}?id=' + callId,
                dataType: 'json',
                cache: false,
                success: function(data)
                {
                    if(data && data.success == "true")
                    {
                        listen.showSuccessMessage('Transferred to Voicemail.')
                        $('.transfer-dropdown').addClass('hidden');
                        $('#transferHeader')[0].innerHTML = "";
                    }
                    else
                    {
                        listen.showErrorMessage('Unable to transfer the call to Voicemail.')
                        $('.transfer-button').removeClass('hidden');
                        $('.transfer-dropdown').addClass('hidden');
                        $('#transferHeader')[0].innerHTML = "";
                    }
                }
            });

            return true;
        }
        else
        {
            return false;
        }
    }

    function toggleHold(element, callId)
    {
        if(element.checked)
        {
            $.ajax({
                url: '${createLink(action: 'callerOnHold')}?id=' + callId,
                dataType: 'json',
                cache: false,
                success: function(data)
                {
                    if(data)
                    {
                        $('.holdCheckbox')[0].checked = data.onHold;
                    }
                }
            });
        }
        else
        {
            $.ajax({
                url: '${createLink(action: 'callerOffHold')}?id=' + callId,
                dataType: 'json',
                cache: false,
                success: function(data)
                {
                    if(data)
                    {
                        $('.holdCheckbox')[0].checked = data.onHold;
                    }
                }
            });
        }
    }

    var callList =
    {
        poll: function() {
            $.ajax({
                url: '${createLink(action: 'polledCalls')}',
                dataType: 'json',
                cache: false,
                success: function(data)
                {
                    // 1. loop through table rows and remove rows that don't exist in the new data

                    var tbody = $('#callTable');

                    $('tr', tbody).each(function() {
                        var tr = $(this);
                        var rowId = parseInt(tr.attr('data-id'), 10);

                        var exists = false;
                        if(data && data.calls)
                        {
                            for(var i = 0; i < data.calls.length; i++)
                            {
                                if(data.calls[i].id == rowId)
                                {
                                    exists = true;
                                    break;
                                }
                            }
                        }
                        if(!exists) {
                            tr.remove();
                        }
                    });

                    // 2. loop through new rows and move existing rows / add new rows

                    if(data && data.calls && data.calls.length > 0)
                    {
                        for(var i = 0; i < data.calls.length; i++)
                        {
                            var call = data.calls[i];

                            var position = -1;
                            var tr; // will be set if a table row is found for this call
                            $('tr', tbody).each(function(index) {
                                var rowId = parseInt($(this).attr('data-id'), 10);
                                if(rowId === call.id) {
                                    position = index;
                                    tr = $(this);
                                }
                            });

                            if(position === -1)
                            {
                                tr = $('#call-row-template').clone(true).removeAttr('id');
                                callList.populate(tr, call);

                                if(i === 0 || $('tr', tbody).length === 0)
                                {
                                    tbody.prepend(tr);
                                }
                            }
                        }
                    }
                }
            });
        },

        populate: function(row, call) {
            callList.updateField($('td.caller', row), call.ani);
            callList.updateField($('td.callerSkill', row), call.skill);
            row.attr('data-id', call.id);

            $('input[name="id"]', row).val(call.id);

            $('.holdCheckbox', row)[0].value=call.id

            if(call.onHold)
            {
                $('.holdCheckbox', row)[0].checked=true
            }
            else
            {
                $('.holdCheckbox', row)[0].checked=false;
            }

            $('.transferButton', row)[0].value = call.id;
            $('.disconnectButton', row)[0].value = call.id;
            $('.transfer-button', row).removeClass('hidden');
            $('.disconnect-button', row).removeClass('hidden');

            return;
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
        }
    };

    $(document).ready(function()
    {
        setInterval(callList.poll, 2000);
    });
    
    // TODO: Change it up for Call History!!!

    $(document).ready( function () {
      setInterval(acdCallHistory.poll,1000);
    });

    var acdCallHistory = {
      poll: function() {
        $.ajax({
          // passing in params to ensure sort and pagination are kept
          url: '${createLink(action: 'pollHistory')}',
          dataType: 'json',
          cache: false,
          success: function(data) {
            if ( data && data.calls.length > 0 )
            {
              // update call history
              var tbody = $("#agent-history > div > table > tbody");
              // tbody doesn't exist! (jquery has a weird way of checking this)
              if ( tbody.length == 0 ) {
                // build it from scratch
                var div = $("#agent-history > div");
                div.empty();
                div.append('<table id="history-table" cellspacing="0" cellpadding="0" class="fixed"></table>');
                var table = $("#callQueue");
                table.append('<thead><tr><th>Caller</th><th>Skill</th><th>Agent Time</th><th>Queue Time</th></thead>');
                table.append('<tbody></tbody>');
                tbody = $("agent-history > div > table > tbody");
              }
              tbody.empty();
              var tr;
              var row_count = 0;
              data.calls.forEach(function(call) {
                tr = "";
                tr += '<tr class="' + (++row_count % 2 == 0 ? 'even' : 'odd') + '">';
                tr += '<td class="overflow">' + call.ani + '</td>';
                tr += '<td class="overflow">' + call.skill + '</td>';
                tr += '<td class="overflow">' + getDifference(call.start, call.end) + '</td>';
                tr += '<td class="overflow">' + getDifference(call.enqueueTime,call.dequeueTime) + '</td></tr>';

                tbody.append(tr);
              });
            } // if ( data && ... )
            else
            { // there were no calls in the queue
              var tbody = $("#agent-history > div > table > tbody");
              // if there were previously calls ( aka tbody exists )
              if ( tbody.length > 0 ) {
                // we need to remove the table
                var div = $("#agent-history > div");
                div.empty();
                div.append('<span>You have no recent call history.</span>');
              } // if ( tbody.length > 0 ) ...
            }

            //Now Check for change in status
            if(data && data.userStatus)
            {
                if($('#statusButton')[0].value != data.userStatus)
                {
                    $('#statusButton')[0].value = data.userStatus;

                    if(data.userStatus == 'Unavailable')
                    {
                        $('.statusButton').removeClass('Available');
                        $('.statusButton').addClass('Unavailable');
                    }
                    else
                    {
                        $('.statusButton').removeClass('Unavailable');
                        $('.statusButton').addClass('Available');
                    }

                    $('#statusButton')[0].title = data.userStatusTitle;
                }
            }
          } // success
        }); // $.ajax
      } // acdCallHistory.poll
    }; // acdCallHistory
    
    </script>
  </body>

</html>
