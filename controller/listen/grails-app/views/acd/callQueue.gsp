<html>
  <head>
    <title><g:message code="page.acd.callQueue.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="acd"/>
    <meta name="button" content="callQueue"/>

    <style type="text/css">
      table.fixed {
        table-layout: fixed;
      }

      .td {
        white-space: nowrap;
        overflow: hidden;
      }

      .userColumn {
        width: 20%
      }

      .callerColumn {
          width: 15%
      }

      .skillColumn {
          width: 18%
      }

      .statusColumn {
          width: 17%
      }

      .waitColumn {
          width: 10%
      }

      .activityColumn {
          width: 10%
      }

      .disconnectColumn {
          width: 10%
      }
    </style>

    <script type="text/javascript">
    // Given a time in milliseconds returns a string representation in HH:MM:SS format
    function formatTime(time) {
      var hours = Math.floor(time / 3600);

      if (hours.toString().length == 1) {
        hours = "0" + hours;
      }

      var minutes = Math.floor( (time % 3600) / 60 );

      if (minutes.toString().length == 1) {
        minutes = "0" + minutes;
      }

      var seconds = Math.floor( ((time % 3600) % 60 ) );

      if (seconds.toString().length == 1) {
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

      var stringStart = start.toString("yyyy'-'MM'-'dd HH':'mm':'ss");
      var stringEnd = end.toString("yyyy'-'MM'-'dd HH':'mm':'ss");
      // convert mysql timestamp into javascript date
      var startAsJDate_ = stringStart.split(/[- : T]/);
      var startAsJDate = new Date(startAsJDate_[0], startAsJDate_[1]-1, startAsJDate_[2], startAsJDate_[3], startAsJDate_[4], startAsJDate_[5]);

      // get difference in seconds
      if ( stringEnd == "now" ) { // get difference from current time
        var difference = ((new Date()).valueOf() - startAsJDate.valueOf()) / 1000;
      }
      else { // get difference from specified time
        // convert mysql timestamp into javascript date
        var endAsJDate_ = stringEnd.split(/[- : T]/);
        var endAsJDate = new Date(endAsJDate_[0], endAsJDate_[1]-1, endAsJDate_[2], endAsJDate_[3], endAsJDate_[4], endAsJDate_[5]);

        var difference = (endAsJDate.valueOf() - startAsJDate.valueOf());
        if ( difference > 0 ) {
          difference = Math.floor(difference / 1000);
        }
      }

      return formatTime(difference);
   }
    </script>

  </head>
  <body>

    <div id="callQueue" class="panel">
      <h3>Call Queue:</h3>
      <table id="keywords" class="fixed" cellspacing="0" cellpadding="0">
        <thead>
          <tr>
            <g:sortableColumn class="userColumn" property="user" title="Agent"/>
            <g:sortableColumn class="callerColumn" property="ani" title="Caller"/>
            <g:sortableColumn class="skillColumn" property="skill" title="Skill"/>
            <g:sortableColumn class="statusColumn" property="callStatus" title="Status"/>
            <g:sortableColumn class="waitColumn" property="enqueueTime" title="Wait Time"/>
            <g:sortableColumn class="activityColumn" property="lastModified" title="Last Activity"/>
            <th class="disconnectColumn" width=10%></th>
          </tr>
          %{-- Using hidden input to persist data from grails to ajax requests (see queue.poll) --}%
          <input id="sort" type="hidden" value="${sort}"/>
          <input id="order" type="hidden" value="${order}"/>
          <input id="max" type="hidden" value="${max}"/>
          <input id="offset" type="hidden" value="${offset}"/>
        </thead>
        <tbody>
          <g:set var="row_count" value="${0}"/>
          <g:each in="${calls}" var="call">
            <tr class="${++row_count % 2 == 0 ? 'even' : 'odd'}">
              <td>${call.user != null ? call.user.realName : ''}</td>
              <td>${call.ani}</td>
              <td>${call.skill.description}</td>
              <td>${call.callStatus.viewable()}</td>
              %{-- <td>${call.callStatus.toString()}</td> --}%
              <script type="text/javascript">
                document.write('<td>' + getDifference('${call.enqueueTime}','') + '</td>');
                document.write('<td>' + getDifference('${call.lastModified}','') + '</td>');
              </script>
                <td class="disconnect-button">
                    <button type="button" class="disconnectButton" id="disconnectButton"
                            value="${call.id}"
                            onclick="disconnectClicked(this, this.value)">Voicemail</button>
                </td>
            </tr>
          </g:each>
        </tbody>
      </table>
    </div>

    <div class="pagination" style="display: ${calls.size() > 0 ? 'block' : 'none'};">
      <listen:paginateTotal total="${callTotal}" messagePrefix="paginate.total.callers"/>
      <g:paginate total="${callTotal}" maxsteps="5"/>
    </div>

    <script type="text/javascript">
    $(document).ready( function () {
      setInterval(queue.poll,1000);
    });

    function disconnectClicked(e, callId) {
        alert("Disconnect Call?");
        $.ajax({
            url: '${createLink(action: 'disconnectCaller')}?id=' + callId,
            dataType: 'json',
            cache: false,
            success: function(data)
            {
                if(data && data.success == "true")
                {
                    listen.showSuccessMessage('Call disconnected.')
                }
                else
                {
                    listen.showErrorMessage('Unable to disconnect call.')
                }
            }
        });

        return true;
    }

    var queue = {
      poll: function() {
        $.ajax({
          url: '${createLink(action: 'pollQueue')}?sort=' + $("#sort").val() + '&order=' + $("#order").val() +
                '&max=' + $("#max").val() + '&offset=' + $("#offset").val(),
          dataType: 'json',
          cache: false,
          success: function(data) {
            // update queue table - basically rebuild it...
            var tbody = $("#callQueue > table > tbody");
            tbody.empty();
            var row = "";
            var row_count = 0;
            if(data.calls)
            {
              data.calls.forEach(function(call) {
                row = "";

                row += '<tr class="' + (++row_count % 2 == 0 ? 'even' : 'odd') + '">';
                row += '<td>' + call.user + '</td>';
                row += '<td>' + call.ani + '</td>';
                row += '<td>' + call.skill + '</td>';
                row += '<td>' + call.callStatus + '</td>';
                row += '<td>' + getDifference(call.enqueueTime,'now') + '</td>';
                row += '<td>' + getDifference(call.lastModified,'now') + '</td>';
                row += '<td class="disconnect-button">' +
                        '<button type="button" class="disconnectButton" id="disconnectButton"' +
                        'value="' + call.id + '"onclick="disconnectClicked(this, this.value)">Voicemail</button></td>';
                row += '</tr>';
                tbody.append(row);
              });
            }
          } // success
        }); // $.ajax
      } // queue.poll
    }; // queue
   </script>

  </body>
</html>
