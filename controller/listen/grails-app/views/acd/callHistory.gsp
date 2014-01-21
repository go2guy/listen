<html>
  <head>
    <title><g:message code="page.acd.callQueue.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="acd"/>
    <meta name="button" content="callHistory"/>

    <style type="text/css">
      table.fixed {
        table-layout: fixed;
      }

      .td {
        white-space: nowrap;
        overflow: hidden;
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
              return '';
          }

        var stringStart = start.toString("yyyy'-'MM'-'dd HH':'mm':'ss");
        var stringEnd = end.toString("yyyy'-'MM'-'dd HH':'mm':'ss");
        // convert mysql timestamp into javascript date
        var startAsJDate_ = stringStart.split(/[- : T]/);
        var startAsJDate = new Date(startAsJDate_[0], startAsJDate_[1]-1, startAsJDate_[2], startAsJDate_[3], startAsJDate_[4], startAsJDate_[5]);

        // get difference in seconds
        if ( stringEnd == "" ) { // get difference from current time
          var difference = ((new Date()).valueOf() - stringStart.valueOf()) / 1000;
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

    <div id="callHistory">
      <h3>Call History:</h3>
      <table id="keywords" class="fixed" cellspacing="0" cellpadding="0">
        <thead>
          <tr>
            <g:sortableColumn property="callStart" title="Call Start"/>
            <g:sortableColumn property="ani" title="Caller"/>
            <g:sortableColumn property="skill" title="Skill"/>
            <g:sortableColumn property="user" title="Agent"/>
            <g:sortableColumn property="callEnd" title="Time With Agent"/>
            <g:sortableColumn property="dequeueTime" title="Time On Call"/>
          </tr>
          %{-- Using hidden input to persist data from grails to ajax requests (see records.poll) --}%
          <input id="sort" type="hidden" value="${sort}"/>
          <input id="order" type="hidden" value="${order}"/>
          <input id="max" type="hidden" value="${max}"/>
          <input id="offset" type="hidden" value="${offset}"/>
        </thead>
        <tbody>
          <g:set var="row_count" value="${0}"/>
          <g:each in="${calls}" var="call">
            <tr class="${++row_count % 2 == 0 ? 'even' : 'odd'}">
              <td>${call.enqueueTime.toString("yyyy'-'MM'-'dd HH':'mm':'ss")}</td>
              <td>${call.ani}</td>
              <td>${call.skill}</td>
              <td>${call.user != null ? call.user : ''}</td>
              <script type="text/javascript">
                  var callDuration = getDifference('${call.callStart}','${call.callEnd}');
                  document.write('<td>' + callDuration + '</td>');
                  document.write('<td>' + getDifference('${call.enqueueTime.toString("yyyy'-'MM'-'dd HH':'mm':'ss")}',
                          '${call.dequeueTime.toString("yyyy'-'MM'-'dd HH':'mm':'ss")}') + '</td>');
              </script>
            </tr>
          </g:each>
        </tbody>
      </table>
    </div>

    <div class="pagination" style="display: ${calls.size() > 0 ? 'block' : 'none'};">
      <listen:paginateTotal total="${callTotal}" messagePrefix="paginate.total.callHistories"/>
      <g:paginate total="${callTotal}" maxsteps="5"/>
    </div>

    <script type="text/javascript">

    var records = {
      poll: function() {
        $.ajax({
          // passing in params to ensure sort and pagination are kept
          url: '${createLink(action: 'pollHistory')}?sort=' + $("#sort").val() + '&order=' + $("#order").val() +
                '&max=' + $("#max").val() + '&offset=' + $("#offset").val(),
          dataType: 'json',
          cache: false,
          success: function(data) {
            // update history table - basically rebuild it...
            var tbody = $("#callHistory > table > tbody");
            tbody.empty();
            var tr;
            var row_count = 0;
            data.calls.forEach(function(call) {
              tr = "";

              column_count = 0;

              tr += '<tr class="' + (++row_count % 2 == 0 ? 'even' : 'odd') + '">';
              tr += '<td>' + call.enqueueTime.toString("yyyy'-'MM'-'dd HH':'mm':'ss") + '</td>';
              tr += '<td>' + call.ani + '</td>';
              tr += '<td>' + call.skill + '</td>';
              tr += '<td>' + call.user + '</td>';
              tr += '<td>' + getDifference(call.start,call.end) + '</td>';
              tr += '<td>' + getDifference(call.enqueueTime,call.dequeueTime) + '</td></tr>';

              tbody.append(tr);

            });
          } // success
        }); // $.ajax
      } // records.poll
    }; // records
   </script>

  </body>
</html>
