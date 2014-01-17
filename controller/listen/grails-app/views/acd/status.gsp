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
          float: right;
          margin: 10px 0 0 0;
          padding: 5px;
          -webkit-border-radius: 5px;
          width: 288px;
        }

        td.col-button {
            text-align: left;
            width: 10%;
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

    <table id="main-container">
      <tr id="row-one">
        <td width="70%" class="column-one">
          <div id="agent-queue">
            <h4>Waiting Calls:</h4>
            <table id="callQueue" cellspacing="0" cellpadding="0">

            <thead>
              <tr>
                <g:sortableColumn property="ani" title="Caller"/>
                <g:sortableColumn property="enqueueTime" title="Time On Call"/>
                <g:sortableColumn property="lastModified" title="Last Activity"/>
              </tr>
              <input id="queueSort" type="hidden" value="${queueSort}"/>
              <input id="queueOrder" type="hidden" value="${queueOrder}"/>
              <input id="queueMax" type="hidden" value="${queueMax}"/>
              <input id="queueOffset" type="hidden" value="${queueOffset}"/>
            </thead>

            <tbody>
              <g:set var="row_count" value="${0}"/>
              <g:each in="${calls}" var="call">
                <g:set var="column_count" value="${0}"/>
                <tr class="${++row_count % 2 == 0 ? 'even' : 'odd'}"l>
                  <td>${call.ani}</td>
                  <script type="text/javascript">
                  document.write('<td>' + getTimeSince('${call.enqueueTime}') + '</td>');
                  document.write('<td>' + getTimeSince('${call.lastModified}') + '</td>');
                  </script>
                </tr>
              </g:each>
            </tbody>

            </table> <!-- callQueue -->

            <div class="pagination" style="display: ${calls.size() > 0 ? 'block' : 'none'};">
              <listen:paginateTotal total="${callTotal}" messagePrefix="paginate.total.callers"/>
              <g:paginate total="${callTotal}" max="5" maxsteps="5" params="${[paginateOrigin: 'queue']}"/>
            </div>

          </div> <!-- agent-queue -->

          <div id="agent-history">
            <h4>Call History:</h4>

            <table id="history-table" cellspacing="0" cellpadding="0">

              <thead>
                <tr>
                  <g:sortableColumn property="ani" title="Caller"/>
                  <g:sortableColumn property="status" title="Status"/>
                  <g:sortableColumn property="enqueueTime" title="Time On Call"/>
                  <g:sortableColumn property="lastModified" title="Last Activity"/>
                </tr>
                <input id="historySort" type="hidden" value="${historySort}"/>
                <input id="historyOrder" type="hidden" value="${historyOrder}"/>
                <input id="historyMax" type="hidden" value="${historyMax}"/>
                <input id="historyOffset" type="hidden" value="${historyOffset}"/>
              </thead>

              <tbody>
                <g:set var="row_count" value="${0}"/>
                <g:each in="${callHistory}" var="call">
                  <tr class="${++row_count % 2 == 0 ? 'even' : 'odd'}"/>
                    <td>${call.ani}</td>
                    <td>${call.callStatus.toString()}</td>
                    <script type="text/javascript">
                      document.write('<td>' + getTimeSince('${call.enqueueTime}') + '</td>');
                      document.write('<td>' + getTimeSince('${call.lastModified}') + '</td>');
                    </script>
                  </tr>
                </g:each>
              </tbody>

            </table> <!-- history-table -->
          </div> <!-- agent-history -->

          <div class="pagination" style="display: ${calls.size() > 0 ? 'block' : 'none'};">
            <listen:paginateTotal total="${historyTotal}" messagePrefix="paginate.total.callHistories"/>
            <g:paginate total="${historyTotal}" max="5" maxsteps="5" params="${[paginateOrigin: 'history']}"/>
          </div> <!-- pagination -->

        </td> <!-- column-one -->

        <td class="column-two">
          <div id="agent-status" class="panel">
            <h3><g:message code="page.acd.status.yourStatus.label"/></h3>

            <table>
              <tbody>
                <tr>
                  <td class="col-button">
                    <g:form controller="acd" action="toggleStatus" method="post">
                    <g:set var="titleMessage" value="" />
                    <g:submitButton name="toggle_status" value="${status}" class="${status}"
                                    title="${g.message(code: 'page.acd.status.button.' + status)}"
                                    disabled="${statusDisabled}" />
                    </g:form>
                  </td>
                </tr>
                <g:form controller="acd" action="updateNumber" method="post">
                  <tr><td><h3><g:message code="page.acd.status.yourNumber.label"/></h3></td></tr>
                  <tr><td><g:select name="contactNumber" from="${phoneNumbers}" value="${contactNumber}"/></td></tr>
                  <tr><td><g:submitButton name="update_status" value="${g.message(code: 'default.button.update.label')}"/></td></tr>
                </g:form>
                <tr>
                  <td>
                    <div id="agent-skills">
                      <h3><g:message code="page.acd.status.yourSkills.label"/></h3>
                      <ul>
                        <g:each in="${userSkills}" var="userSkill">
                          <li>${userSkill.skill.description}</li>
                        </g:each>
                      </ul>
                    </div> <!-- agent-skills -->
                  </td>
                </tr>
              </tbody>
            </table>

          </div> <!-- agent-status -->
        </td> <!-- column-two -->
      </tr> <!-- row-one -->
    </table> <!-- main-container -->
  </body>
</html>
