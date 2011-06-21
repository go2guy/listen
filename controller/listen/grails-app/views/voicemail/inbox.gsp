<html>
  <head>
    <title><g:message code="page.voicemail.inbox.title"/></title>
    <script type="text/javascript" src="${resource(dir: 'resources/jquery', file: 'jquery-1.4.2.min.js')}"></script>
    <meta name="layout" content="main"/>
    <meta name="tab" content="voicemail"/>
    <meta name="button" content="inbox"/>
    <style type="text/css">

ol {
    margin: 0;
    padding: 0;
    width: 100%;
}

ol > li {
    border: 2px solid #e4f0fb;
    border-radius: 5px;
    display: block;
    margin-top: 10px;
    padding: 5px;
}

ol > li.new {
    background-color: #e4f0fb;
    border: 2px solid #054B7A;
}

ol > li.new div.transcription {
    background-color: #FFFFFF;
}

ol > li.new .from {
    font-weight: bold;
}

div.listHeader {
    margin-left: 7px; /* to offset li padding and borders */
}

div.from,
div.received,
div.duration,
div.status,
div.playLink,
div.play,
div.playerDiv {
    display: inline-block;

    *display: inline; /* IE7 */
    zoom: 1; /* IE7 */
}

div.transcription,
div.options {
    display: block;
}

div.from {
    font-size: 20px;
    width: 450px;
}

div.received { width: 200px; }
div.duration { width: 200px; }
div.playLink { width: 200px; }
div.playerDiv { width: 200px; }
div.status { width: 46px; }

div.transcription {
    border-radius: 5px;
    font-size: 13px;
    padding: 10px;
}

div.transcription,
div.options {
    margin-top: 5px;
}

div.options form,
div.options li {
    display: inline;
}

.listTotal,
.pagination {
    height: 24px;
}
.templates {
    display: none;
}
    </style>
  </head>
  <body>

    <g:if test="${voicemailList.size() > 0}">
      <div class="listHeader">
        <div class="from columnHeader<g:if test="${params.sort == 'leftBy'}"> sorted ${params.order}</g:if>"><listen:sortLink controller="voicemail" action="inbox" property="leftBy" title="${g.message(code: 'voicemail.leftBy.label')}"/></div><!-- TODO this needs to sort by leftBy.realName, see [http://stackoverflow.com/questions/5708735/how-do-i-sort-by-a-property-on-a-nullable-association-in-grails] -->
        <div class="received columnHeader<g:if test="${params.sort == 'dateCreated'}"> sorted ${params.order}</g:if>"><listen:sortLink controller="voicemail" action="inbox" property="dateCreated" title="${g.message(code: 'voicemail.dateCreated.label')}"/></div>
        <div class="duration columnHeader<g:if test="${params.sort == 'audio.duration'}"> sorted ${params.order}</g:if>"><listen:sortLink controller="voicemail" action="inbox" property="audio.duration" title="${g.message(code: 'audio.duration.label')}"/></div>
        <div class="status columnHeader<g:if test="${params.sort == 'isNew'}"> sorted ${params.order}</g:if>"><listen:sortLink controller="voicemail" action="inbox" property="isNew" title="${g.message(code: 'page.voicemail.inbox.status.column.header')}"/></div>
      </div>
    </g:if>

    <ol>
      <g:if test="${voicemailList.size() > 0}">
        <g:each in="${voicemailList}" var="voicemail">
          <li class="<g:if test="${voicemail.isNew}">new </g:if>voicemail-row" id="${voicemail.id}">
            <div class="from">${voicemail?.from().encodeAsHTML()}</div>
            <div class="received" title="${joda.format(value: voicemail.dateCreated, style: 'LL')}"><listen:prettytime date="${voicemail.dateCreated}"/></div>
            <div style="display:none;" class="playLink" id="playLink${voicemail.id}"><a href="#" onclick="playVoicemail(${voicemail.id},'${voicemail.audio.uri}');return false;">Play</a> <listen:prettyduration duration="${voicemail.audio.duration}"/></div>
            <div class="play"></div>
            <div class="duration"><listen:prettyduration duration="${voicemail.audio.duration}"/></div>
            <div class="status">${voicemail.isNew ? 'new' : 'old'}</div>
            <div class="transcription">${fieldValue(bean: voicemail.audio, field: 'transcription')}</div>
            <div class="options">
              <ul>
                <li>
                  <g:form action="delete" method="post" params="${params}" class="delete-form">
                    <g:hiddenField name="id" value="${voicemail.id}"/>
                    <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                  </g:form>
                </li>
                <li>
                   <g:form action="toggleStatus" method="post" params="${params}">
                    <g:hiddenField name="id" value="${voicemail.id}"/>
                    <g:if test="${voicemail.isNew}">
                      <g:submitButton name="toggleStatus" value="${g.message(code: 'page.voicemail.inbox.button.markOld')}"/>
                    </g:if>
                    <g:else>
                       <g:submitButton name="toggleStatus" value="${g.message(code: 'page.voicemail.inbox.button.markNew')}"/>
                    </g:else>
                  </g:form>
                </li>
                <li>
                  <g:form action="download" method="get">
                    <g:hiddenField name="id" value="${voicemail.id}"/>
                    <g:submitButton name="download" value="${g.message(code: 'default.button.download.label')}"/>
                  </g:form>
                </li>
              </ul>
            </div>
          </li>
        </g:each>
      </g:if>
      <g:else>
        <li class="placeholder"><g:message code="page.voicemail.inbox.noVoicemail"/></li>
      </g:else>
    </ol>
    <g:if test="${voicemailList.size() > 0}">
      <listen:paginateTotal total="${voicemailTotal}" messagePrefix="paginate.total.voicemails"/>
      <div class="pagination">
        <g:paginate total="${voicemailTotal}" maxsteps="5" action="inbox"/>
      </div>
    </g:if>

    <ul class="templates">
      <li id="voicemail-template" class="new">
        <div class="from"></div>
        <div class="received"></div>
        <div style="" class="playLink"><a href="#">Play</a></div>
        <div class="play"></div>
        <div class="status"></div>
        <div class="transcription"></div>
        <div class="options">
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
                <input type="submit" name="toggleStatus" value="${g.message(code: 'page.voicemail.inbox.button.markNew')}" id="toggleStatus">
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
      <li class="placeholder-template"><g:message code="page.voicemail.inbox.noVoicemail"/></li>
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

      function playVoicemail(id, uri) {
           //setVoicemailStatus(id, 'old');
           $('.playerDiv').remove();
           $('.playLink').show();
           var playerHtml = '<div class="playerDiv"><object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" width="165" height="37" id="player' + id + '" align=""><param name=movie value="${resource(dir: 'resources/audio', file: 'niftyplayer.swf')}?file=' + uri + '&as=1"><param name=quality value=high><param name=bgcolor value=#FFFFFF><embed src="${resource(dir: 'resources/audio', file: 'niftyplayer.swf')}?file=' + uri + '&as=1" quality=high bgcolor=#FFFFFF width="165" height="37" id="player' + id + '" name="player' + id + '" align="" type="application/x-shockwave-flash" swLiveConnect="true" pluginspage="http://www.macromedia.com/go/getflashplayer"></embed></object></div>';
           $('#' + id + ' .play').append(playerHtml);
           $('#playLink' + id).hide()
      }

      function setVoicemailStatus(id, newStatus) {
           $.ajax({
             url: '${request.contextPath}/voicemail/setStatus?id=' + id + '&newStatus=' + status,
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

      setInterval( "pollVoicemails()", 1000);
      
      function pollVoicemails() {
            var visibleIds = ''
            $('.voicemail-row').each(function (index, domEle) {
                 visibleIds += $(domEle).attr('id') + ',';
            });
            visibleIds = visibleIds.substring(0, visibleIds.length - 1);

            $.ajax({
              url: '${request.contextPath}/voicemail/pollingList?visibleIds=' + visibleIds + '${(request.queryString?.length() > 0) ? "&" + request.queryString : ""}',
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
                        var voicemail = list[j];
                        var clone = $('#voicemail-template').clone(true);

                        clone.css('opacity', 0);
                        clone.removeAttr('id');
                        clone.attr('id', voicemail.id);
                        clone.addClass('voicemail-row');
                        clone.find('.from').text(voicemail.from);
                        clone.find('.received').attr('title', voicemail.dateTitle);
                        clone.find('.received').text(voicemail.dateCreated);
                        
                        var playLink = clone.find('.playLink');
                        $(playLink).attr('id', 'playLink' + voicemail.id);
                        var durationString = ' ' + voicemail.audio.duration;
                        $(playLink).append(durationString);
                        $(playLink).click(function() {
                            playVoicemail(voicemail.id, voicemail.audio.uri);
                            return false;
                        });

                        clone.find('.status').text(voicemail.isNew ? 'new' : 'old');
                        clone.find('.transcription').html(voicemail.transcription);
                        
                        $('input[type="hidden"]', clone).attr('value', voicemail.id);
                        $('input[name="toggleStatus"]', clone).attr('value', voicemail.isNew ? '<g:message code="page.voicemail.inbox.button.markOld"/>' : '<g:message code="page.voicemail.inbox.button.markNew"/>');

                        if(add[j] == 0) {
                             $('ol').prepend(clone);
                             clone.animate({ opacity: 1 }, 1000);
                        } else {
                             $('.voicemail-row').eq(j-1).after(clone);
                             clone.animate({ opacity: 1 }, 1000);
                        }
                   }

                   for(var k = 0; k < list.length; k++) {
                        var screenVoicemail = $('.voicemail-row').eq(k);
                        var listVoicemail = list[k];

                        if(screenVoicemail.find('.received').text() != listVoicemail.dateCreated) {
                             screenVoicemail.find('.received').text(listVoicemail.dateCreated);
                        }
                        
                        var listVoicemailStatus = listVoicemail.isNew ? 'new' : 'old';

                        if(screenVoicemail.find('.status').text() != listVoicemailStatus) {
                             screenVoicemail.find('.status').text(listVoicemailStatus);
                             if(listVoicemailStatus != 'new') {
                                  $(screenVoicemail).removeClass('new');
                                  $('input[name="toggleStatus"]', screenVoicemail).attr('value', '<g:message code="page.voicemail.inbox.button.markNew"/>');
                                  
                             } else {
                                  $(screenVoicemail).addClass('new');
                                  $('input[name="toggleStatus"]', screenVoicemail).attr('value', '<g:message code="page.voicemail.inbox.button.markOld"/>');
                             }
                        }

                        if(screenVoicemail.find('.transcription').html() != listVoicemail.transcription) {
                             screenVoicemail.find('.transcription').html(listVoicemail.transcription);
                        }
                        
                   }

                   if(list.length > 0) {
                        $('.placeholder').remove();
                   } else {
                        if($('.placeholder') == null) {
                             var placeholderTemplate = $('.placeholder-template');
                             placeholderTemplate.removeClass('placeholder-template').addClass('placeholder');
                             $('ol').prepend(placeholderTemplate);

                        }
                   }
              }
            });
      }
    </script>
  </body>
</html>