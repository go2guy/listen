<!DOCTYPE html>
<html>
  <head>
    <title>Listen - <g:layoutTitle/> - NewNet Communication Technologies</title>
    <link rel="shortcut icon" href="${resource(dir: 'resources/app/images', file: 'favicon.ico')}">
    <script type="text/javascript" src="${resource(dir: 'resources/jquery', file: 'jquery-1.4.2.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'resources/jquery', file: 'jquery-ui-1.8rc3.custom.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'resources/jquery', file: 'jquery.color.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'resources/json.org', file: 'json2.js')}"></script>
    <script type="text/javascript">
var util = {
    typewatch: (function() {
        var timer = 0;
        return function(callback, ms) {
            clearTimeout(timer);
            timer = setTimeout(callback, ms);
        };
    })(),

    selectFirst: function(selectList) {
        $('option:first', selectList).attr('selected', 'selected');
    }
};

var log = {
    debug: function(message) {
        this.write(message, 'DEBUG');
    },

    error: function(message) {
        this.write(message, 'ERROR');
    },

    write: function(message, severity) {
        var sev = severity || 'LOG  ';
        var content = sev + ': ' + message;
        try {
            console.log(content);
            return true;
        } catch(e) {
            try {
                opera.postError(content);
                return true;
            } catch(e2) {
                // eat the exception, we dont want to crash just because we cant write a log
            }
        }
    }
};

var listen = {
    showSuccessMessage: function(message) {
        var ca = $('#content-area');
        $('.messages.success', ca).remove();
        ca.prepend('<ul class="messages success"><li>' + message + '</li></ul>');
        listen.fadeMessage($('.messages.success', ca));
    },
    showErrorMessage: function(message) {
        var ca = $('#content-area');
        $('.messages.error', ca).remove();
        ca.prepend('<ul class="messages error"><li>' + message + '</li></ul>');
        listen.fadeMessage($('.messages.error', ca));
    },
    fadeMessage: function(element) {
        var el = $(element);
        setTimeout(function() {
          el.fadeOut(4000, 'swing', function() {
            el.remove();
          });
        }, 1000);
    }
};


    /*
     @params required title, body
    */
    function dialogBox(event, title, body, width, height) {
        // is there one already open?
        var isOpen = false;
        if ($( "#dialog" ).dialog( "isOpen" ) === true) {
            isOpen = true;
        }
        
        if (isOpen == false) {
            // Get position
            var posX = event.pageX-width/2;
            var posY = event.pageY;

            // Create dialog box
            $('<div id="dialog" title="'+title+'">'+body+'</div>').appendTo('body');      
            event.preventDefault();
            dialogIsOpen = true;
            $('#dialog').dialog({
                width: width,
                height: height,
                position: [posX, posY],
                modal: false,
                resizable: true,
                dialogClass: 'dialogStyle',
                close: function(event, ui) {
                    $('#dialog').remove();
                    isOpen = false;
                }
            });
        }
    }
    </script>

    <link rel="stylesheet" type="text/css" href="${resource(dir: 'resources/jquery/skin/css/custom-theme', file: 'jquery-ui-1.8.2.custom.css')}">
    <link rel="stylesheet" href="${resource(dir: 'resources/app/css', file: 'common.css')}">
    <style type="text/css">
/* TODO move this into a separate file when development is complete */

/* MAIN LAYOUT */

#container {
    display: block;
    margin: 0 auto;
    width: 950px;
}

#header {
    display: block;
    height: 41px; /* logo height + logo vertical padding + logo vertical margin */
}

#header img {
    background-color: #FFFFFF;
    display: block;
    float: left;
    height: 24px;
    margin: 7px 0 0 10px;
    padding: 5px 10px;
    width: 50px;
}

#user-info {
    display: block;
    float: right;
    margin-top: 5px;
}

#user-info {
    color: #054B7A;
    font: 12px Tahoma, Arial, sans-serif;
}

#user-info li {
    display: inline;
    margin-right: 8px;
}

#user-info a {
    color: #054B7A;
}

#user-info a:hover {
    color: #CCCCCC;
}

ul.tab-menu {
    display: inline-block;
    font: bold 14px Century Gothic, Arial, sans-serif;
    height: 27px;
    padding: 10px 5px 0 5px;
    *display:block; /* IE7 */
    zoom: 1; /* IE7 */
}

ul.tab-menu li {
    display: inline-block;
    margin: 0 2px;
    padding: 0;
    *display:inline; /* IE7 */
    zoom: 1; /* IE7 */
}

ul.tab-menu li a {
    background-color: #e4f0fb;
    border-color: #80b5ed;
    border-style: solid;
    border-width: 2px 2px 0 2px;
    color: #176BA3; /* swatch:dark */
    display: inline-block;
    line-height: 25px; /* same as height */
    padding: 0 5px;
    text-align: center;
    text-decoration: none;

    border-top-left-radius: 5px;
    border-top-right-radius: 5px;
    -webkit-border-top-left-radius: 5px;
    -webkit-border-top-right-radius: 5px;
    -moz-border-radius-topleft: 5px;
    -moz-border-radius-topright: 5px;

    *display:inline; /* IE7 */
    zoom: 1; /* IE7 */
}

ul.tab-menu li a:hover {
    background: #FFFFFF;
    border-color: #9BC3DE; /* swatch:subdued */
    border-style: solid;
    border-width: 2px 2px 0 2px;
    color: #054B7A; /* swatch:text */
}

ul.tab-menu li.current a {
    background: #FFFFFF;
    border-color: #9BC3DE; /* swatch:subdued */
    color: #054B7A; /* swatch:text */
    height: 27px;
}

#meta-menu {
    float: right;
    margin-right: 14px;
}


/* special coloring for meta menus */

#meta-menu li a {
    background: #FAEDD5;
    border-color: #E4A634;
    color: #574D3E;
}

#meta-menu li a:hover,
#meta-menu li.current a {
    color: #574D3E;
    background: #FFFFFF;
}

#content-area.meta {
    border-color: #E4A634;
}

#content-area.meta ul.button-menu li a {
    background: #FFFFFF;
    border-color: #E4A634;
    color: #574D3E;
}

#content-area.meta ul.button-menu li a:hover,
#content-area.meta ul.button-menu li.current a {
    background: #FAEDD5;
    border-color: #574D3E;
    color: #574D3E;
}

#content-area.meta tr.even { background-color: #EDE8DF; }
#content-area.meta tr.odd { background-color: #E3DDD3; }
#content-area.meta tr.highlighted,
#content-area.meta table.highlighted tbody tr {
    background-color: #E4A634;
}

#content-area.meta a,
#content-area.meta table thead th {
    color: #574D3E;
}

#content-area.meta caption,
#content-area.meta h3 {
    border-color: #574D3E;
    color: #574D3E;
}

#content-area.meta input[type=submit],
#content-area.meta input[type=button],
#content-area.meta input[type=reset],
#content-area.meta button {
    border-color: #574D3E;
    color: #574D3E;
}

#content-area.meta input[type=submit]:hover,
#content-area.meta input[type=button]:hover,
#content-area.meta input[type=reset]:hover,
#content-area.meta button:hover {
    background-color: #574D3E;
    color: #FFFFFF;
}

/* end special coloring */

#content-area {
    background-color: #FFFFFF;
    border-color: #9BC3DE; /* swatch:subdued */
    border-style: solid;
    border-width: 2px;
    display: inline-block; /* IE (trigger hasLayout) */
    display: block;
    font-family: Century Gothic, Arial, sans-serif;
    overflow: hidden;
    padding: 10px 10px 10px 10px; /* top is 15 to offset some border widths */
}

ul.button-menu  {
    display: block;
    float: left;
    font: 12px Century Gothic, Arial, sans-serif;
    height: 27px;
    margin-bottom: 10px;
}

ul.button-menu li {
    display: inline-block;
    margin-right: 5px;
    *display:inline; /* IE7 */
    zoom: 1; /* IE7 */
}

ul.button-menu li a {
    border: 2px solid #6DACD6; /* swatch:subdued */
    color: #054B7A; /* swatch:text */
    display: inline-block;
    padding: 2px 10px;
    text-decoration: none;
    
    border-radius: 5px;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
    *display: inline; /* IE7 */
    zoom: 1; /* IE7 */
}

ul.button-menu li a:hover,
ul.button-menu li.current a {
    background-color: #e4f0fb;
    border-color: #176BA3; /* swatch:dark */
    color: #054B7A; /* swatch:text */
}

#content-separator {
    clear: both;
    display: block;
    height: 0;
}

/* END MAIN LAYOUT */

/* MESSAGES */

ul.messages.success {
    float: right;
}

ul.messages.error {
    clear: both;
}

.blocked-number {
    border-style: solid;
    display: inline-block;
    font-size: 12px;
    font-weight: bold;
    height: 21px;
    line-height: 21px;
    margin-left: 5px;
    padding: 0 4px;
    vertical-align: text-top;

    border-radius: 5px;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
}

/* END MESSAGES */

#new-message-count {
    margin-left: 5px;
}
    </style>
    <listen:customApplicationStyles/>
    <g:layoutHead/>
  </head>
  <body>
    <div id="container">
      <div id="header">
        <img src="${resource(dir: 'resources/app/images', file: 'listen_logo_50x24.png')}"/>
        <ul id="user-info">
          <li><listen:userOrg/></li>
          <li><listen:realName/></li>
          <li>[ <g:link controller="logout">Logout</g:link> ]</li>
        </ul>
      </div>

      <ul id="meta-menu" class="tab-menu">
        <sec:ifAllGranted roles="ROLE_ORGANIZATION_ADMIN">
          <li class="administration<g:if test="${pageProperty(name: 'meta.tab') == 'administration'}"> current</g:if>"><g:link controller="administration"><g:message code="tab.menu.administration"/></g:link></li>
          <li class="users<g:if test="${pageProperty(name: 'meta.tab') == 'users'}"> current</g:if>"><g:link controller="user"><g:message code="tab.menu.users"/></g:link></li>
        </sec:ifAllGranted>
        <sec:ifAllGranted roles="ROLE_CUSTODIAN">
          <li class="administration<g:if test="${pageProperty(name: 'meta.tab') == 'custodianAdministration'}"> current</g:if>"><g:link controller="custodianAdministration"><g:message code="tab.menu.custodianAdministration"/></g:link></li>
        </sec:ifAllGranted>
        <li class="right-aligned<g:if test="${pageProperty(name: 'meta.tab') == 'profile'}"> current</g:if>"><g:link controller="profile"><g:message code="tab.menu.profile"/></g:link></li>
      </ul>

      <ul id="application-menu" class="tab-menu">
        <listen:canAccessAny features="VOICEMAIL,FAX">
            <sec:ifAnyGranted roles="ROLE_VOICEMAIL_USER,ROLE_FAX_USER">
              <li class="messages<g:if test="${['messages', 'voicemail', 'fax'].contains(pageProperty(name: 'meta.tab'))}"> current</g:if>"><g:link controller="messages" action="inbox"><g:message code="tab.menu.messages"/><span id="new-message-count">(<listen:newMessageCount/>)</span></g:link></li>
            </sec:ifAnyGranted>
        </listen:canAccessAny>
        <listen:canAccess feature="CONFERENCING">
            <sec:ifAllGranted roles="ROLE_CONFERENCE_USER">
              <li class="conferencing<g:if test="${pageProperty(name: 'meta.tab') == 'conferencing'}"> current</g:if>"><g:link controller="conferencing"><g:message code="tab.menu.conferencing"/></g:link></li>
            </sec:ifAllGranted>
        </listen:canAccess>
        <listen:canAccess feature="FINDME">
            <sec:ifAllGranted roles="ROLE_FINDME_USER">
              <li class="findme<g:if test="${pageProperty(name: 'meta.tab') == 'findme'}"> current</g:if>"><g:link controller="findme"><g:message code="tab.menu.findme"/></g:link></li>
            </sec:ifAllGranted>
        </listen:canAccess>
        <listen:canAccess feature="ATTENDANT">
            <sec:ifAllGranted roles="ROLE_ATTENDANT_ADMIN">
                <li class="attendant<g:if test="${pageProperty(name: 'meta.tab') == 'attendant'}"> current</g:if>"><g:link controller="attendant"><g:message code="tab.menu.attendant"/></g:link></li>
            </sec:ifAllGranted>
        </listen:canAccess>
            <sec:ifAllGranted roles="ROLE_CUSTODIAN">
                <li class="organization<g:if test="${pageProperty(name: 'meta.tab') == 'organization'}"> current</g:if>"><g:link controller="organization"><g:message code="tab.menu.organizations"/></g:link></li>
            </sec:ifAllGranted>
        <listen:canAccess feature="ACD">
            <sec:ifAllGranted roles="ROLE_ACD_USER">
                <li class="acd<g:if test="${pageProperty(name: 'meta.tab') == 'acd'}"> current</g:if>"><g:link controller="acd"><g:message code="tab.menu.acd"/></g:link></li>
            </sec:ifAllGranted>
        </listen:canAccess>

      </ul>

      <g:set var="tab" value="${pageProperty(name: 'meta.tab')}"/>
      <g:set var="isMeta" value="${['administration', 'users', 'profile', 'custodianAdministration'].contains(tab)}"/>
      <div id="content-area" class="${tab}${isMeta ? ' meta' : ''}">
        <g:if test="${flash.successMessage}">
          <ul class="messages success"><li>${flash.successMessage}</li></ul>
        </g:if>

        <listen:buttonMenu tab="${pageProperty(name: 'meta.tab')}" button="${pageProperty(name: 'meta.button')}"/>

        <g:if test="${flash.errorMessage}">
          <ul class="messages error"><li>${flash.errorMessage}</li></ul>
        </g:if>
        <g:else>
          <g:hasErrors>
            <ul class="messages error">
              <g:eachError><li><g:message error="${it}"/></li></g:eachError>
            </ul>
          </g:hasErrors>
        </g:else>

        <div id="content-separator"></div>

        <g:if test="${pageProperty(name: 'meta.page-header')}">
          <h3>${pageProperty(name: 'meta.page-header')}</h3>
        </g:if>

        <g:layoutBody/>
      </div>

      <div id="footer">
        <listen:copyright/>
      </div>

      <script type="text/javascript">
$(document).ready(function() {
    $('.ajax-form').submit(function(e) {
        var form = $(e.target);
        var disable = form.hasClass('disable-on-submit');
        var doConfirm = form.hasClass('confirm-before-submit');

        var button = $('input[type="submit"]', form);
        var delayedEnable = function() {
            setTimeout(function() {
                button.removeAttr('disabled').removeAttr('readonly').removeClass('disabled');
            }, 1500);
        }
        if(disable) {
            button.attr('disabled', 'disabled').attr('readonly', 'readonly').addClass('disabled');
        }

        if(!doConfirm || confirm('Are you sure?')) {
            $.ajax({
                type: 'POST',
                url: form.attr('action'),
                data: form.serialize(),
                complete: function() {
                    if(disable) {
                        delayedEnable();
                    }
                    $('.clear-after-submit', form).val('');
                }
            });
        } else {
            delayedEnable();
        }
        return false; // prevent browser from submitting the form
    });

<sec:ifAnyGranted roles="ROLE_VOICEMAIL_USER,ROLE_FAX_USER">
    setInterval(function() {
        $.ajax({
            url: '${request.contextPath}/messages/newCount',
            dataType: 'json',
            cache: false,
            success: function(data) {
                var displayedCount = $('#new-message-count');
                if(data)
                {
                    var newCount = '(' + data.count + ')';
                }
                else
                {
                    var newCount = 0;
                }

                if(newCount != displayedCount.text()) {
                    displayedCount.text(newCount);
                }
            }
        });
    }, 5000);
</sec:ifAnyGranted>

<sec:ifAnyGranted roles="ROLE_ACD_USER">
    setInterval(function getAcdMessageCount() {
    var displayedCount = $("#new-acd-message-count");
    var newCount = '(0)';
    $.ajax({
      url: '${request.contextPath}/messages/newAcdCount',
      dataType: 'json',
      cache: false,
      success: function(data) {
        if (data && data.count != displayedCount.text() ) {
          displayedCount.text(data.count);
        }
      }
    });
  }, 5000);
</sec:ifAnyGranted>

});

$(document).ready(function() {
  $('.messages.success').each(function(index) {
    listen.fadeMessage(this);
  });
});
      </script>
      <listen:customApplicationScripts/>
    </div>
  </body>
</html>
