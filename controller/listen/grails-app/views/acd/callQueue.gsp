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
    </style>

    <script type="text/javascript">
      // Given a mysql timestamp returns current difference in HH:MM:SS format
      function getTimeSince(date) {
        var time = "";
        var timestamp = "";
        var wait = "";
        var wait_sec = "";
        var wait_min = "";
        var wait_hours = "";

        // convert mysql timestamp into javascript date
        timestamp = date.split(/[- : T]/);
        time = new Date(timestamp[0], timestamp[1]-1, timestamp[2], timestamp[3], timestamp[4], timestamp[5]);

        wait = ((new Date()).valueOf() - time.valueOf()) / 1000;

        wait_hours = Math.floor(wait / 3600);

        if (wait_hours.toString().length == 1) {
          wait_hours = "0" + wait_hours;
        }

        wait %= 3600;
        wait_min = Math.floor(wait / 60);

        if (wait_min.toString().length == 1) {
          wait_min = "0" + wait_min;
        }

        wait_sec = Math.floor(wait % 60);

        if (wait_sec.toString().length == 1) {
          wait_sec = "0" + wait_sec;
        }

        return (wait_hours + ':' + wait_min + ':' + wait_sec);
      }
    </script>

  </head>
  <body>

    <div id="callQueue" class="panel">
      <h3>Call Queue:</h3>
      <table id="keywords" class="fixed" cellspacing="0" cellpadding="0">
        <thead>
          <tr>
            <g:sortableColumn property="user" title="Agent"/>
            <g:sortableColumn property="ani" title="Caller"/>
            <g:sortableColumn property="skill" title="Skill"/>
            <g:sortableColumn property="callStatus" title="Status"/>
            <g:sortableColumn property="enqueueTime" title="Wait Time"/>
            <g:sortableColumn property="lastModified" title="Last Activity"/>
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
                document.write('<td>' + getTimeSince('${call.enqueueTime}') + '</td>');
                document.write('<td>' + getTimeSince('${call.lastModified}') + '</td>');
              </script>
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

    var queue = {
      poll: function() {
        $.ajax({
          /* We actually relying on grails to do the sorting and we simply pass to grails
            which column we want to order by. The order is set by the onclick function
            configured for each of the column links. */
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
                row += '<td>' + getTimeSince(call.enqueueTime) + '</td>';
                row += '<td>' + getTimeSince(call.lastModified) + '</td></tr>';

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
