<html>
  <head>
    <title><g:message code="page.conferencing.recordings.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="conferencing"/>
    <meta name="button" content="recordings"/>
    <style type="text/css">
.col-date { width: 45%; }
.col-duration { width: 20%; }
.col-size { width: 15%; }
.col-download { width: 10%; }
.col-delete { width: 10%; }
    </style>
  </head>
  <body>
    <g:if test="${recordingList.size() > 0}">
      <table>
        <thead>
          <tr>
            <g:sortableColumn property="audio.dateCreated" title="${g.message(code: 'audio.dateCreated.label')}" class="col-date"/>
            <g:sortableColumn property="audio.duration" title="${g.message(code: 'audio.duration.label')}" class="col-duration"/>
            <th class="col-size"><g:message code="page.conferencing.recordings.column.fileSize"/></th>
            <th class="col-download"></th>
            <th class="col-delete"></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${recordingList}" var="recording" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <td class="col-date"><joda:format value="${recording.audio.dateCreated}" style="LL"/></td>
              <td class="col-duration"><listen:prettyduration duration="${recording.audio.duration}"/></td>
              <td class="col-size"><listen:megabytes file="${recording.audio.file}"/></td>
              <td class="col-download">
                <g:form action="downloadRecording" method="get">
                  <g:hiddenField name="id" value="${recording.id}"/>
                  <g:submitButton name="download" value="${g.message(code: 'default.button.download.label')}"/>
                </g:form>
              </td>
              <td class="col-delete">
                <g:form action="deleteRecording" method="post">
                  <g:hiddenField name="id" value="${recording.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </g:form>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>
      <listen:paginateTotal total="${recordingTotal}" messagePrefix="paginate.total.recordings"/>
      <div class="pagination">
        <g:paginate total="${recordingTotal}" maxSteps="5"/>
      </div>
    </g:if>
    <g:else>
      You do not have any conference recordings.
    </g:else>
  </body>
</html>