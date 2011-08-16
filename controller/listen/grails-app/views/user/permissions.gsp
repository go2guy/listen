<html>
  <head>
    <title><g:message code="page.user.permissions.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="users"/>
    <meta name="button" content="permissions"/>
    <meta name="page-header" content="${g.message(code: 'page.user.permissions.header')}"/>
    <style type="text/css">
tr.disabled {
    color: #999999;
}

.col-username { width: 20%; }
.col-realname { width: 20%; }
.col-enabled { width: 10%; }
.col-permission { width: ${(50 / (available.size() > 0 ? available.size() : 1)) as int}%; }

table.permissions thead th {
    vertical-align: bottom;
}

.col-enabled {
    text-align: center;
}

tbody .col-permission {
    text-align: center;
}

thead .col-permission {
    font-size: 10px;
    text-align: center;
    padding-bottom: 10px;
}

.col-permission .toggle-button {
    display: block;
    height: 23px;
    margin: 0 auto;
    width: 23px;

    border-radius: 0;
    -moz-border-radius: 0;
    -webkit-border-radius: 0;
}

thead th.col-odd {
    background-color: #EDE8DF;
}

thead th.col-even,
tr.even td.col-even {
    background-color: #DBD6CC;
}

tr.odd td.col-even {
    background-color: #CCC7BE;
}

input.allowed { 
    background-color: #C1E8B7;
}

    </style>
  </head>
  <body>
    <table class="permissions">
      <thead>
        <tr>
          <td colspan="3">
            <listen:infoSnippet summaryCode="page.user.permissions.snippet.summary" contentCode="page.user.permissions.snippet.content"/>
          </td>
          <g:each in="${available}" var="permission" status="i">
            <th rowspan="2" class="col-permission ${i % 2 == 0 ? 'col-even' : 'col-odd'}"><div>${permission.description}</div></th>
          </g:each>
        </tr>
        <tr>
          <g:sortableColumn property="username" title="${g.message(code: 'user.username.label')}" class="col-username"/>
          <g:sortableColumn property="realName" title="${g.message(code: 'user.realName.label')}" class="col-realName"/>
          <g:sortableColumn property="enabled" title="${g.message(code: 'user.enabled.label')}" class="col-enabled"/>
        </tr>
      </thead>
      <tbody>
        <g:each in="${userList}" var="user" status="i">
          <tr class="${i % 2 == 0 ? 'even' : 'odd'}${!user.enabled ? ' disabled' : ''}">
            <td class="col-username">${fieldValue(bean: user, field: 'username')}</td>
            <td class="col-realName">${fieldValue(bean: user, field: 'realName')}</td>
            <td class="col-enabled"><listen:checkMark value="${user.enabled}"/></td>
            <g:each in="${available}" var="permission" status="j">
              <td class="col-permission ${j % 2 == 0 ? 'col-even' : 'col-odd'}">
                <g:set var="hasRole" value="${user.hasRole(permission.authority)}"/>
                <g:if test="${permission.readOnly}">
                  <span title="This permission is not assignable"><listen:checkMark value="${hasRole}"/></span>
                </g:if>
                <g:else>
                  <listen:ifEqualsCurrentUser user="${user}">
                    <span title="You cannot change your own permissions"><listen:checkMark value="${hasRole}"/></span>
                  </listen:ifEqualsCurrentUser>
                  <listen:ifNotEqualsCurrentUser user="${user}">
                    <g:form controller="user" action="togglePermission" method="post" class="toggle-form">
                      <input type="hidden" name="id" value="${user.id}"/>
                      <input type="hidden" name="permission" value="${permission.name()}"/>
                      <input type="submit" class="toggle-button ${hasRole ? 'allowed' : 'disallowed'}" value="${listen.checkMark(value: hasRole)}" title="${hasRole ? 'Revoke' : 'Grant'}"/>
                    </g:form>
                  </listen:ifNotEqualsCurrentUser>
                </g:else>
              </td>
            </g:each>
          </tr>
        </g:each>
      </tbody>
    </table>
    <g:if test="${userListTotal > 0}">
      <listen:paginateTotal total="${userListTotal}" messagePrefix="paginate.total.users"/>
      <div class="pagination">
        <g:paginate total="${userListTotal}" maxSteps="5"/>
      </div>
    </g:if>
    <script type="text/javascript">
$(document).ready(function() {
    $('.toggle-form').submit(function() {
        var form = $(this);
        $('.toggle-button', form).attr('disabled', 'disabled').attr('readonly', 'readonly').addClass('disabled').removeClass('allowed');
        $.ajax({
            type: 'POST',
            url: form.attr('action'),
            data: form.serialize(),
            success: function(data) {
                $('.toggle-button', form).val(data.hasRole ? '\u2713' : '\u00A0').attr('title', data.hasRole ? 'Revoke' : 'Grant');
                if(data.hasRole) {
                    $('.toggle-button', form).addClass('allowed');
                    listen.showSuccessMessage('Granted');
                } else {
                    listen.showSuccessMessage('Revoked');
                }
            },
            complete: function() {
                $('.toggle-button', form).removeAttr('disabled').removeAttr('readonly').removeClass('disabled');
            }
        });
        return false;
    });
});
    </script>
  </body>
</html>