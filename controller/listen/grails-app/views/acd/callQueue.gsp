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

      td {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
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

      difference = endTime.getTime() - startTime.getTime();

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
                document.write('<td>' + getDifference('${call.enqueueTime}','now') + '</td>');
                document.write('<td>' + getDifference('${call.lastModified}','now') + '</td>');
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

    <div id="pagination" class="pagination" style="display: ${calls.size() > 0 ? 'block' : 'none'};">
      <listen:paginateTotal total="${callTotal}" messagePrefix="paginate.total.callers"/>
      <g:paginate total="${callTotal}" maxsteps="5"/>
    </div>

    <script type="text/javascript">
    $(document).ready( function () {
      setInterval(queue.poll,1000);
    });

    function disconnectClicked(e, callId) {
        alert("Transfer the call to Voicemail?");
        $.ajax({
            url: '${createLink(action: 'disconnectCaller')}?id=' + callId,
            dataType: 'json',
            cache: false,
            success: function(data)
            {
                if(data && data.success == "true")
                {
                    listen.showSuccessMessage('Transferred to Voicemail.')
                }
                else
                {
                    listen.showErrorMessage('Unable to transfer the call to Voicemail.')
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
            if(data && data.calls.length > 0)
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
            } // if ( data ...
            var div = $("#pagination > div");
            div.empty();
            if ( data.callTotal ) {
              if ( data.callTotal == 1 ) {
                div.append(data.callTotal + " Caller");
              }
              else {
                div.append(data.callTotal + " Callers");
              }
              div.show();
            } // if ( data.callTotal ...
            else {
              // div.append("0 Callers");
              div.hide();
            }
          } // success
        }); // $.ajax
      } // queue.poll
    }; // queue
   </script>

  </body>
</html>
