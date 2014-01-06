<html>
  <head>
    <title><g:message code="page.acd.callQueue.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="acd"/>
    <meta name="button" content="callQueue"/>

    <style type="text/css">
      tr.even {
        background-color: #EBEEF5;
      }

      tr.odd {
        background-color: #E4E6ED;
      }

      td.even {
        background-color: #DBD6CC;
      }

      td.odd {
        background-color: #EDE8DF;
      }
    </style>

    <script type="text/javascript">
      // Given a mysql timestamp returns current difference in HH:MM:SS format
      function getWaitTime(date) {
        var enqueueTime = "";
        var timestamp = "";
        var wait = "";
        var wait_sec = "";
        var wait_min = "";
        var wait_hours = "";

        // convert mysql timestamp into javascript date
        timestamp = date.split(/[- : T]/);
        enqueueTime = new Date(timestamp[0], timestamp[1]-1, timestamp[2], timestamp[3], timestamp[4], timestamp[5]);

        wait = ((new Date()).valueOf() - enqueueTime.valueOf()) / 1000;

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
      <table id="keywords" cellspacing="0" cellpadding="0">
        <thead>
          <tr>
            <th><span><a href="#" onclick='$("#orderBy").val("ani");'>Ani</a></span></th>
            <th><span><a href="#" onclick='$("#orderBy").val("skill");'>Skill</a></span></th>
            <th><span><a href="#" onclick='$("#orderBy").val("enqueueTime");'>Wait Time</a></span></th>
          </tr>
          <input id="orderBy" type="hidden" value="enqueueTime"/>
        </thead>
        <tbody>
          <g:set var="row_count" value="${0}"/>
          <g:each in="${calls}" var="call">
            <tr class="${row_count++ % 2 == 0 ? 'even' : 'odd'}">
              <td class="even">${call.ani}</td>
              <td class="odd">${call.skill.skillname}</td>
              <script type="text/javascript">
                document.write('<td calss="even">' + getWaitTime('${call.enqueueTime}') + '</td>');
              </script>
            </tr>
          </g:each>
        </tbody>
      </table>
    </div>

    <script type="text/javascript">
    var queue = {
      poll: function() {
        $.ajax({
          /* We actually relying on grails to do the sorting and we simply pass to grails
            which column we want to order by. The order is set by the onclick function
            configured for each of the column links. */
          url: '${createLink(action: 'pollQueue')}?orderBy=' + $("#orderBy").val(),
          dataType: 'json',
          cache: false,
          success: function(data) {
            // update queue table - basically rebuild it...
            var tbody = $("#callQueue > table > tbody");
            tbody.empty();
            var row;
            var row_count = 0;
            var column_count = 0;
            data.calls.forEach(function(call) {
              row = "";
              /* Grails only populates the skill field with the id when working with json, so we need to
                 loop through the skill to find the corresponding skill name associated with the call */
              data.skills.forEach(function(skill) {
                if (skill.id == call.skill.id) {
                  call.skill.skillname = skill.skillname;
                  return;
                }
              });

              column_count = 0;
              row += '<tr class="' + (row_count++ % 2 == 0 ? 'even' : 'odd') + '">';
              row += '<td class="' + (column_count++ % 2 == 0 ? 'even' : 'odd') + '">' + call.ani + '</td>';
              row += '<td class="' + (column_count++ % 2 == 0 ? 'even' : 'odd') + '">' + call.skill.skillname + '</td>';
              row += '<td class="' + (column_count++ % 2 == 0 ? 'even' : 'odd') + '">' + getWaitTime(call.enqueueTime) + '</td></tr>';

              tbody.append(row);
            });
          }
        });
        setTimeout(queue.poll, 1000);
      }
    };

    $(document).ready(queue.poll);
    </script>

  </body>
</html>
