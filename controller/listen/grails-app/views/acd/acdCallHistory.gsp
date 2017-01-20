<%@ page import="com.interact.listen.User" %>
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

      td {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .hidden { display: none; }

      .searchfield {
          font-size: 14px;
          display: table-cell;
          padding-left: 10px;
          vertical-align: bottom;
          padding-right: 20px;
      }

      button.link {
          background:none;
          border:none;
          font-size: 16px;
          text-decoration: underline;
      }

      .search {
          border-bottom: 1px solid #054B7A;
          padding-top:  10px;
          padding-bottom:  10px;
          display: table;
          width: 100%;
      }

      .formdate {
          width: 120px;
          height: 25px;
          font-size: 16px;
      }

      .filterDropdown {
          width: 180px;
      }

      .filterButton {
          width: 60px;
          height: 30px;
          vertical-align: middle;
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
            if ( difference > 0 )
            {
                difference = Math.floor(difference / 1000);
            }
            else if(difference < 0)
            {
                return 'N/A';
            }
          }

          return formatTime(difference);
        }
        $(function () {
            ////////////////////////////////////////////////////////////////////////////
            // DatePicker Logic
            ////////////////////////////////////////////////////////////////////////////
            $("#startDate").datepicker({
                onSelect: function () {
                    var startDate = $(this).datepicker('getDate');

                    $("#endDate").datepicker("option", "minDate", startDate);
                }
            });

            $("#endDate").datepicker({
            });

        });

        function resetForm() {
            document.form.startDate.value = '';
            document.form.endDate.value = '';
        }
    </script>

  </head>
  <body>

    <div id="callHistory">
      <div id="callHistoryHeader">
        <h3 style="padding-bottom: 10px">Call History:
              <div style="margin-right: 15px; float: right; padding-bottom: 5px;">
                  <g:link action="exportHistoryToCsv" params="${params}">${message(code:'page.administration.acd.callHistory.exportCSV.label')}</g:link>
              </div>
        </h3>
      </div>

      <div id="searchDiv">
          <div id="searchTable" class="search">
                <g:form controller="acd" action="acdCallHistory" method="get" id="filterForm">
                    <div style="display: table-row">
                      <div id="startDateSearch" class="searchfield">
                        Start Date:
                        <input id="startDate" name="startDate" placeholder="mm/dd/yyyy" value="${startDate}" class="formdate"/>
                      </div>
                      <div id="endDateSearch" class="searchfield">
                          End Date:
                          <input id="endDate" name="endDate" placeholder="mm/dd/yyyy" value="${endDate}" class="formdate"/>
                      </div>
                        <div id="skillSearch" class="searchfield">
                            Skill:
                            <g:select name="skill"
                                      class="filterDropdown"
                                      optionKey="id"
                                      optionValue="skillname"
                                      from="${skillList}"
                                      value="${skill != null ? skill : ''}"
                                      noSelection="['':'-All-']" />
                        </div>
                      <div id="agentSearch" class="searchfield">
                            Agent:
                            <g:select name="agent"
                                      class="filterDropdown"
                                      optionKey="id"
                                      optionValue="realName"
                                      from="${agentList}"
                                      value="${agent != null ? agent : ''}"
                                      noSelection="['':'-All-']" />
                      </div>
                      <div id="searchButton" class="searchfield">
                        <input name="sort" type="hidden" value="${sort}"/>
                        <input name="order" type="hidden" value="${order}"/>
                        <g:submitButton class="filterButton" name="filter" value="Filter"/>
                      </div>
                    </div>
                </g:form>
              </div>
      </div>

      <table id="keywords" class="fixed" cellspacing="0" cellpadding="0">
        <thead>
          <tr>
            <%
              // reassign sort parameters if they were lost in filtering
              params.sort = sort
              params.order = order
            %>
            <g:sortableColumn property="agentCallStart" id="call_start_column" width="20%" title="Call Start"/>
            <g:sortableColumn property="ani" id="caller_column" title="Caller"/>
            <g:sortableColumn property="skill" id="skill_column" title="Skill"/>
            <g:sortableColumn property="user" id="agent_column" title="Agent"/>
            <g:sortableColumn property="agentCallEnd" id="time_with_agent_column" title="Time With Agent"/>
            <g:sortableColumn property="dequeueTime" id="time_on_call_column" title="Time On Call"/>
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
              <td>${call.enqueueTime.toString("MM'/'dd'/'yyyy' 'HH':'mm':'ss")}</td>
              <td>${call.ani}</td>
              <td>${call.skill}</td>
              <td>${call.user != null ? call.user : ''}</td>
              <script type="text/javascript">
                  var callDuration = getDifference('${call.agentCallStart}','${call.agentCallEnd}');
                  document.write('<td>' + callDuration + '</td>');
                  document.write('<td>' + getDifference('${call.enqueueTime.toString("yyyy'-'MM'-'dd HH':'mm':'ss")}',
                          '${call.dequeueTime.toString("yyyy'-'MM'-'dd HH':'mm':'ss")}') + '</td>');
              </script>
            </tr>
          </g:each>
        </tbody>
      </table>
    </div>

    <div class="pagination" style="display: 'block';">
      <listen:paginateTotal total="${callTotal}" messagePrefix="paginate.total.acdCallHistories"/>
      <g:paginate action="acdCallHistory" total="${callTotal}" params="${params}"/>
    </div>

    <script type="text/javascript">
    $(document).ready( function() {
      // preserve filter parameters when using sortable columns
      var params = "&startDate=" + $("#startDate").val() + "&endDate=" + $("#endDate").val() +
                   "&skill=" + $("#skill").val() + "&agent=" + $("#agent").val();

      var anchor = $("#call_start_column > a");
      anchor.attr('href',anchor.attr('href') + params);

      anchor = $("#caller_column > a");
      anchor.attr('href',anchor.attr('href') + params);

      anchor = $("#skill_column > a");
      anchor.attr('href',anchor.attr('href') + params);
 
      anchor = $("#agent_column > a");
      anchor.attr('href',anchor.attr('href') + params);
 
      anchor = $("#time_with_agent_column > a");
      anchor.attr('href',anchor.attr('href') + params);
 
      anchor = $("#time_on_call_column > a");
      anchor.attr('href',anchor.attr('href') + params);
    });

    $(".link").click(function(){
        $('#searchDiv').removeClass('hidden');
    });

    $('.filter').click(function() {
        var groups = attendant.buildJson();
        var form = $('#menu-save-form');
        $('input', form).val(JSON.stringify(groups));
        form.submit();
        return false;
    });

    var records = {
      poll: function() {
        $.ajax({
          // passing in params to ensure sort and pagination are kept
          url: '${createLink(action: 'pollHistory', mapping: 'internalApi')}?sort=' + $("#sort").val() + '&order=' + $("#order").val() +
                '&max=' + $("#max").val() + '&offset=' + $("#offset").val(),
          dataType: 'json',
          cache: false,
          success: function(data) {
            // update history table - basically rebuild it...
            var tbody = $("#acdCallHistory > table > tbody");
            tbody.empty();
            var tr;
            var row_count = 0;
            data.calls.forEach(function(call) {
              tr = "";

              column_count = 0;

              tr += '<tr class="' + (++row_count % 2 == 0 ? 'even' : 'odd') + '">';
              tr += '<td>' + call.enqueueTime.toString("yyyy'-'MM'-'dd HH':'mm':'ss") + '</td>';
              tr += '<td>' + call.callHistory.ani + '</td>';
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
