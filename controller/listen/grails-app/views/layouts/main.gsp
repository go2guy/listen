<!DOCTYPE html>
<html>
  <head>
    <title>Listen - <g:layoutTitle/> - Interact Incorporated</title>
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
    })()
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
    </script>

    <link rel="stylesheet" type="text/css" href="${resource(dir: 'resources/jquery/skin/css/custom-theme', file: 'jquery-ui-1.8.2.custom.css')}">
    <style type="text/css">
/* TODO move this into a separate file when development is complete */

/* MAIN LAYOUT */

html,
body {
    margin: 0;
    padding: 0;
}

a {
    color: #054B7A;
    text-decoration: underline;
}

ul {
    list-style-type: none;
    margin: 0;
    padding: 0;
}

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

#user-info a:hover,
#footer a:hover {
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

h3 {
    margin: 0 0 10px 0;
}

ul.button-menu  {
    display: block;
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

#footer {
    color: #054B7A;
    display: block;
    font: 11px Arial, sans-serif;
    height: 30px;
    line-height: 30px;
    text-align: center;
}

/* END MAIN LAYOUT */

/* MESSAGES */

#content-area ul.messages {
    border-style: dashed;
    border-width: 1px;
    display: block;
    font: 12px Arial, sans-serif;
    margin-bottom: 10px;
    padding: 5px 5px 5px 30px;
}

.messages.success {
    background-color: #D1FFC9;
    border-color: #437A3A;
    color: #437A3A; /* same as border color */
}

.messages.info {
    background-color: #E4F0FB;
    border-color: #176BA3;
    color: #176BA3;
}

.messages.warning {
    background-color: #FFDEB0;
    border-color: #D17B02;
    color: #D17B02;
}

.messages.error {
    border-color: #A81818;
    background-color: #FFC7C7;
    color: #A81818;
}

.field-error {
    background-color: #FFC7C7;
}

.info-snippet {
    background-color: #DED7CA;
    font-size: 13px;
    margin-bottom: 10px;
    padding: 5px;

    border-radius: 5px;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
}

.info-snippet .summary {
    display: block;
    font-weight: bold;
    margin-bottom: 3px;
}

/* END MESSAGES */


/* TABLES, OTHER STUFF */

table {
    border-collapse: collapse;
    width: 100%;
}

th,
td {
    padding: 2px;
}

thead th,
thead th a,
.columnHeader,
.columnHeader a {
    color: #054B7A;
    font-size: 14px;
    font-weight: bold;
    text-align: left;
}

thead th a,
.columnHeader a {
    text-decoration: underline;
}

caption, h3 {
    color: #054B7A;
    font-size: 16px;
    font-weight: normal;
    text-align: left;
    border-bottom: 1px solid #054B7A;
    margin-bottom: 5px;
}

tr.even { background-color: #EBEEF5; }
tr.odd { background-color: #E4E6ED; }
tr.highlighted,
table.highlighted tbody tr {
    background-color: #6DACD6;
}

/* END TABLES, OTHER STUFF */

/* FORMS and FIELDSETS */

fieldset.vertical {
    border-width: 0;
/*    border: 1px solid #6DACD6;*/
    margin: 0;
    padding: 0;
}

fieldset.vertical label {
    display: block;
    margin-top: 8px;
    margin-bottom: 3px;
}

fieldset.vertical .inline-label {
    display: inline-block;
}

fieldset.vertical input[type=text],
fieldset.vertical input[type=password],
fieldset.vertical select {
    display: block;
}

fieldset.vertical input[type=checkbox] {
    display: inline-block;
}

fieldset.vertical ul.form-buttons {
    display: block;
}

fieldset.vertical ul.form-buttons li {
    display: inline-block;
}

fieldset.vertical ul.form-buttons input,
fieldset.vertical ul.form-buttons button {
    font-size: 14px;
    margin-top: 15px;
}

fieldset.vertical h3 {
    border-width: 0;
    display: block;
    font-size: 14px;
    font-weight: bold;
    margin: 15px 0 5px 0;
    padding-bottom: 3px;
}

fieldset.vertical ul.checkbox-list {
    display: block;
    margin: 10px;
}

fieldset.vertical ul.checkbox-list label {
    display: inline;
    margin-left: 10px;
}*/

label {
    font-size: 14px;
}

input[type=text],
input[type=password],
select {
    border: 1px solid #CCCCCC;
    font-family: Helvetica, sans-serif;
    font-size: 16px;
    padding: 2px;
    width: 250px;
}

select {
    width: 256px;
}

input[type=submit],
input[type=button],
input[type=reset],
button {
    background-color: #FAFAFA;
    border: 1px solid #054B7A;
    color: #054B7A;
    cursor: pointer;
    font-family: Century Gothic, sans-serif;
    font-size: 13px;
    padding: 2px 5px;
    white-space: nowrap;

    border-radius: 5px;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
}

input[type=submit]:hover,
input[type=button]:hover,
input[type=reset]:hover,
button:hover {
    background-color: #054B7A;
    color: #FFFFFF;
}

input[type=submit].disabled,
input[type=button].disabled,
input[type=reset].disabled,
button.disabled {
    color: #999999;
    cursor: default;
    background-color: #DDDDDD;
    border-color: #999999;
}

/* SORTING */

.sortable.sorted.asc a:after,
.columnHeader.sorted.asc a:after {
    content: '\25B4';
}

.sortable.sorted.desc a:after,
.columnHeader.sorted.desc a:after {
    content: '\25BE';
}

/* PAGINATION */

div.pagination {
    display: block;
    height: 20px;
}

div.pagination span,
div.pagination a {
    border: 1px solid #CCCCCC;
    color: #666666;
    display: inline-block;
    font-size: 12px;
    height: 20px;
    line-height: 20px;
    margin: 5px;
    padding: 0 5px;
    text-align: center;
    text-decoration: none;
}

div.pagination .currentStep {
    border-width: 0;
    font-weight: bold;
}

div.listTotal {
    color: #666666;
    display: block;
    float: right;
    font-size: 14px;
    height: 20px;
    margin: 5px;
}

#new-voicemail-count {
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
        <listen:canAccess feature="VOICEMAIL">
          <sec:ifAllGranted roles="ROLE_VOICEMAIL_USER">
            <li class="voicemail<g:if test="${pageProperty(name: 'meta.tab') == 'voicemail'}"> current</g:if>"><g:link controller="voicemail"><g:message code="tab.menu.voicemail"/><span id="new-voicemail-count">(<listen:newVoicemailCount/>)</span></g:link></li>
          </sec:ifAllGranted>
        </listen:canAccess>
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
      </ul>

      <g:set var="tab" value="${pageProperty(name: 'meta.tab')}"/>
      <g:set var="isMeta" value="${['administration', 'users', 'profile', 'custodianAdministration'].contains(tab)}"/>
      <div id="content-area" class="${tab}${isMeta ? ' meta' : ''}">
        <listen:buttonMenu tab="${pageProperty(name: 'meta.tab')}" button="${pageProperty(name: 'meta.button')}"/>

        <g:if test="${flash.successMessage}">
          <ul class="messages success"><li>${flash.successMessage}</li></ul>
        </g:if>

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
<sec:ifAllGranted roles="ROLE_VOICEMAIL_USER">
    setInterval(function() {
        $.ajax({
            url: '${request.contextPath}/voicemail/newCount',
            dataType: 'json',
            cache: false,
            success: function(data) {
                var displayedCount = $('#new-voicemail-count');
                var newCount = '(' + data.count + ')';
                if(newCount != displayedCount.text()) {
                    displayedCount.text(newCount);
                }
            }
        });
    }, 5000);
</sec:ifAllGranted>
});

// this is a fun idea, but it impacts usability - when the element is removed,
// the whole content area slides up fast, which could lead to an inadvertent
// button click, or other negative consequences
/*$(document).ready(function() {
  $('.messages.success').each(function(index) {
    var el = $(this);
    setTimeout(function() {
      el.fadeOut(4000, 'swing', function() {
        el.remove();
      });
    }, 1000);
  });
});*/
      </script>
      <listen:customApplicationScripts/>
    </div>
  </body>
</html>