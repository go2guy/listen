<html>
  <head>
    <title><g:message code="page.acd.inbox.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="acd"/>
    <meta name="button" content="inbox"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'inbox.css')}" type="text/css">
  </head>

  <body>

    <ul class="button-menu">
      <g:each var="skill" in="${skillList}">
        <li class="${currentSkill == skill ? 'current' : ''}">
          <g:link controller="messages" action="acdInbox" params="[currentSkill: skill]">${skill}
            <span id="${skill.replaceAll(' ','')}-message-count">(0)</span>
          </g:link>
        </li>
      </g:each>
    </ul>
    %{-- Temporary spacing fix until I have time to figure out the styling --}%
    </br>

    <g:if test="${messageList.size() > 0}">
      <div class="listHeader">
        <div class="from columnHeader<g:if test="${params.sort == 'leftBy'}"> sorted ${params.order}</g:if>"><listen:sortLink controller="messages" action="inbox" property="leftBy" title="${g.message(code: 'inboxMessage.leftBy.label')}"/></div><!-- TODO this needs to sort by leftBy.realName, see [http://stackoverflow.com/questions/5708735/how-do-i-sort-by-a-property-on-a-nullable-association-in-grails] -->
        <div class="received columnHeader<g:if test="${params.sort == 'dateCreated'}"> sorted ${params.order}</g:if>"><listen:sortLink controller="messages" action="inbox" property="dateCreated" title="${g.message(code: 'inboxMessage.dateCreated.label')}"/></div>
        <div class="duration columnHeader"></div>
        <div class="status columnHeader<g:if test="${params.sort == 'isNew'}"> sorted ${params.order}</g:if>"><listen:sortLink controller="messages" action="inbox" property="isNew" title="${g.message(code: 'page.messages.inbox.status.column.header')}"/></div>
      </div>
    </g:if>

    <ol>
      <g:if test="${messageList.size() > 0}">
        <g:each in="${messageList}" var="message">
          <li class="<g:if test="${message.isNew}">new </g:if>message-row <listen:ifIsVoicemail message="${message}">voicemail</listen:ifIsVoicemail><listen:ifIsFax message="${message}">fax</listen:ifIsFax>" id="${message.id}">
            <div class="from">${message?.from().encodeAsHTML()}</div>
            <div class="received" title="${joda.format(value: message.dateCreated, style: 'LL')}"><listen:prettytime date="${message.dateCreated}"/></div>
            <listen:ifIsVoicemail message="${message}">
              <div style="display:none;" class="playLink" id="playLink${message.id}"><a href="#" onclick="playVoicemail(${message.id});return false;">Play</a> <listen:prettyduration duration="${message.audio.duration}"/></div>
              <div class="play"></div>
              <div class="duration"><listen:prettyduration duration="${message.audio.duration}"/></div>
            </listen:ifIsVoicemail>
            <listen:ifIsFax message="${message}">
              <div class="playLink" style="display: none;">${message.pages} page${message.pages == 1 ? '' : 's'} (<listen:megabytes file="${message.file}" unavailable="Size Unknown"/>)</div>
              <div class="play"></div>
            </listen:ifIsFax>
            <div class="status">${message.isNew ? 'new' : 'old'}</div>
            <listen:ifIsVoicemail message="${message}">
              <div class="transcription">${fieldValue(bean: message.audio, field: 'transcription')}</div>
            </listen:ifIsVoicemail>
            <div class="options">
              <ul class="type-tags">
                <listen:ifIsVoicemail message="${message}">
                  <li class="voicemail-tag">voicemail</li>
                </listen:ifIsVoicemail>
                <listen:ifIsFax message="${message}">
                  <li class="fax-tag">fax</li>
                </listen:ifIsFax>
              </ul>
              <ul class="option-buttons">
                <li>
                  <g:form action="delete" method="post" params="${params}" class="delete-form">
                    <g:hiddenField name="id" value="${message.id}"/>
                    <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                  </g:form>
                </li>
                <li>
                   <g:form action="toggleStatus" method="post" params="${params}">
                    <g:hiddenField name="id" value="${message.id}"/>
                    <g:if test="${message.isNew}">
                      <g:submitButton name="toggleStatus" value="${g.message(code: 'page.messages.inbox.button.markOld')}"/>
                    </g:if>
                    <g:else>
                       <g:submitButton name="toggleStatus" value="${g.message(code: 'page.messages.inbox.button.markNew')}"/>
                    </g:else>
                  </g:form>
                </li>
                <li>
                  <listen:ifIsVoicemail message="${message}">
                    <g:form controller="voicemail" action="download" method="get">
                      <g:hiddenField name="id" value="${message.id}"/>
                      <g:submitButton name="download" value="${g.message(code: 'default.button.download.label')}"/>
                    </g:form>
                  </listen:ifIsVoicemail>
                  <listen:ifIsFax message="${message}">
                    <g:form controller="fax" action="download" method="get">
                      <g:hiddenField name="id" value="${message.id}"/>
                      <g:submitButton name="download" value="${g.message(code: 'default.button.download.label')}"/>
                    </g:form>
                  </listen:ifIsFax>
                </li>
              </ul>
            </div>
          </li>
        </g:each>
      </g:if>
      <g:else>
        <li class="placeholder"><g:message code="page.messages.inbox.noMessages"/></li>
      </g:else>
    </ol>
    <g:if test="${messageList.size() > 0}">
      <listen:paginateTotal total="${messageTotal}" messagePrefix="paginate.total.messages"/>
      <div class="pagination">
        <g:paginate total="${messageTotal}" maxsteps="5" action="inbox"/>
      </div>
    </g:if>

    <ul class="templates">
      <li id="message-template" class="new">
        <div class="from"></div>
        <div class="received"></div>
        <div style="" class="playLink"><a href="#">Play</a></div>
        <div class="play"></div>
        <div class="status"></div>
        <div class="transcription"></div>
        <div class="options">
          <ul class="type-tags">
          </ul>
          <ul>
            <li>
              <form action="delete" method="post" class="delete-form">
                <input type="hidden" name="id" id="id">
                <input type="submit" name="delete" value="<g:message code="default.button.delete.label"/>" id="delete">
              </form>
            </li>
            <li>
              <form action="toggleStatus" method="post">
                <input type="hidden" name="id" id="id">
                <input type="submit" name="toggleStatus" value="${g.message(code: 'page.messages.inbox.button.markNew')}" id="toggleStatus">
              </form>
            </li>
            <li>
              <form action="download" method="get">
                <input type="hidden" name="id" id="id">
                <input type="submit" name="download" value="<g:message code="default.button.download.label"/>" id="download">
              </form>
            </li>
          </ul>
        </div>
      </li>
      <li class="placeholder-template"><g:message code="page.messages.inbox.noMessages"/></li>
    </ul>



    <script type="text/javascript">
      //remove duration fields since the player will show duration. A degradation of sorts
      $(document).ready(function() {
        $('.playLink').show();
        $('ol .duration').remove();

        $('.delete-form').submit(function(e) {
          return confirm('Are you sure?');
        });
      });

      function playVoicemail(id) {
        $('.playerDiv').remove();
        $('.playLink').show();
        var playerHtml = '<div class="playerDiv"><object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" width="165" height="37" id="player' + id + '" align=""><param name=movie value="${resource(dir: 'resources/audio', file: 'niftyplayer.swf')}?file=${request.contextPath}/voicemail/download/' + id + '&as=1"><param name=quality value=high><param name=bgcolor value=#FFFFFF><embed src="${resource(dir: 'resources/audio', file: 'niftyplayer.swf')}?file=${request.contextPath}/voicemail/download/' + id + '&as=1" quality=high bgcolor=#FFFFFF width="165" height="37" id="player' + id + '" name="player' + id + '" align="" type="application/x-shockwave-flash" swLiveConnect="true" pluginspage="http://www.macromedia.com/go/getflashplayer"></embed></object></div>';
        $('#' + id + ' .play').append(playerHtml);
        $('#playLink' + id).hide()
      }

      function setMessageStatus(id, newStatus) {
         $.ajax({
           url: '${request.contextPath}/messages/setStatus?id=' + id + '&newStatus=' + status,
           dataType: 'json',
           cache: 'false',
           success: function(data, textStatus, xhr) {
                //Shouldn't need to do anything, polling will reflect change
           },
           failure: function(data, textStatus, xhr) {
                //Write a javascript console log saying we failed once we have cross-browser way to log
           }
         });
      }

      setInterval("pollMessages()", 5000);

      function pollMessages() {
        var visibleIds = ''
        $('.message-row').each(function (index, domEle) {
          visibleIds += $(domEle).attr('id') + ',';
        });
        visibleIds = visibleIds.substring(0, visibleIds.length - 1);

        $.ajax({
          url: '${request.contextPath}/messages/acdPollingList?visibleIds=' + visibleIds + '${(request.queryString?.length() > 0) ? "&" + request.queryString : ""}' +
                '&currentSkill=${currentSkill}',
          dataType: 'json',
          cache: false,
          success: function(data, textStatus, xhr) {
            var add = data.add;
            var remove = data.remove;
            var list = data.list;
            var leftByNames = data.leftByNames

            for(var i  = 0; i < remove.length; i++) {
              var removeIndex = remove[i] + 1;
              $("ol li:nth-child(" + removeIndex  + ")").remove();
            }

            for(var j = 0; j < add.length; j++) {
              var message = list[j];
              var clone = $('#message-template').clone(true);

              clone.css('opacity', 0);
              clone.removeAttr('id');
              clone.attr('id', message.id);
              clone.addClass('message-row');
              clone.find('.from').text(message.from);
              clone.find('.received').attr('title', message.dateTitle);
              clone.find('.received').text(message.dateCreated);
              clone.find('.status').text(message.isNew ? 'new' : 'old');

              if(message.type == 'voicemail') {                        
              var playLink = clone.find('.playLink');
              $(playLink).attr('id', 'playLink' + message.id);
              var durationString = ' ' + message.audio.duration;
              $(playLink).append(durationString);
              $(playLink).click(function() {
                playVoicemail(message.id);
                return false;
              });

              clone.find('.transcription').html(message.transcription);
              clone.addClass('voicemail');
              clone.find('.type-tags').append('<li class="voicemail-tag">voicemail</li>');
              }
              else if(message.type == 'fax') {
                clone.find('.transcription').remove();
                clone.find('.playLink a').remove();
                clone.find('.playLink').text(message.pages + ' page' + (message.pages == 1 ? '' : 's') + ' (' + message.size + ')');
                clone.addClass('fax');
                clone.find('.type-tags').append('<li class="fax-tag">fax</li>');
              }
                
              $('input[type="hidden"]', clone).attr('value', message.id);
              $('input[name="toggleStatus"]', clone).attr('value', message.isNew ? '<g:message code="page.messages.inbox.button.markOld"/>' : '<g:message code="page.messages.inbox.button.markNew"/>');

              if(add[j] == 0) {
              $('ol').prepend(clone);
              clone.animate({ opacity: 1 }, 1000);
              } else {
                $('.message-row').eq(j-1).after(clone);
                clone.animate({ opacity: 1 }, 1000);
              }
            }

            for(var k = 0; k < list.length; k++) {
              var screenMessage = $('.message-row').eq(k);
              var listMessage = list[k];

              if(screenMessage.find('.received').text() != listMessage.dateCreated) {
                screenMessage.find('.received').text(listMessage.dateCreated);
              }
                  
              var listMessageStatus = listMessage.isNew ? 'new' : 'old';

              if(screenMessage.find('.status').text() != listMessageStatus) {
                screenMessage.find('.status').text(listMessageStatus);
                if(listMessageStatus != 'new') {
                  $(screenMessage).removeClass('new');
                  $('input[name="toggleStatus"]', screenMessage).attr('value', '<g:message code="page.messages.inbox.button.markNew"/>');
                   
                }
                else {
                  $(screenMessage).addClass('new');
                  $('input[name="toggleStatus"]', screenMessage).attr('value', '<g:message code="page.messages.inbox.button.markOld"/>');
                }
              }

              if(screenMessage.find('.transcription').html() != listMessage.transcription) {
                 screenMessage.find('.transcription').html(listMessage.transcription);
              }
            }

            if(list.length > 0) {
              $('.placeholder').remove();
            }
            else {
              if($('.placeholder') == null) {
                var placeholderTemplate = $('.placeholder-template');
              placeholderTemplate.removeClass('placeholder-template').addClass('placeholder');
              $('ol').prepend(placeholderTemplate);

              }
            }
          } // success
        }); // $.ajax
      } // pollMessages

      function getMessageCountForSkill(skill) {
        var displayedCount = $("#" + skill + "-message-count");
        var newCount = '(0)';
        $.ajax({
          url: '${request.contextPath}/messages/newAcdCount?currentSkill=' + skill,
          dataType: 'json',
          cache: false,
          success: function(data) {
            if ( data && data.count != displayedCount.text() ) {
              displayedCount.text(data.count);
            }
          }
        });
      }

      <%
        def javascript = "\$(document).ready( function () { "
        // javascript += "getMessageCountForSkills(); "
        javascript += "setInterval(getMessageCountForSkills(),5000);"
        javascript += "});\n\n"
        javascript += "function getMessageCountForSkills() {\n"
        skillList.each() { skill ->
          javascript += "getMessageCountForSkill('" + skill.replaceAll(' ','') + "');\n"
        }
        // javascript += "setTimeout(getMessageCountForSkills(),5000);\n"
        javascript += "}\n"
        out << javascript
      %>

      </script>

  </body>
</html>
