<html>
  <head>
    <title><g:message code="page.findme.configure.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="findme"/>
    <meta name="button" content="configure"/>
    <style type="text/css">
.templates {
    display: none;
}

fieldset.vertical {
    border-width: 1px;
    padding: 10px;
}

fieldset.vertical #expirationDetails select {
    display: inline;
    width: auto;
}

fieldset.vertical #smsDetails {
    margin-left: 30px;
}

fieldset.vertical #smsDetails label {
    display: inline-block;
}

fieldset.vertical #smsDetails input[type="text"],
fieldset.vertical #smsDetails select {
    display: block;
}

h4.action-description {
    background-color: #E4F0FB;
    margin: 5px 0;
    padding: 10px 5px;
}

h4.action-description select {
    margin-left: 10px;
}

ul#groups > li.group {
    border: 1px solid #80B5ED;
    display: block;
    margin: 5px 0;
    padding: 5px 10px;
}

ul#groups > li.group > h4,
ul.destinations > li  {
    font-size: 15px;
    font-weight: normal;
    margin: 10px 0;
    padding: 0;
}

ul.destinations > li {
    background-color: #80B5ED;
    display: block;
    padding: 4px 5px;

    border-radius: 5px;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
}

ul.destinations li.disabled {
    background-color: #E4F0FB;
}

ul.destinations li.disabled input {
    color: #CCCCCC;
}

div.buttons {
    float: right;
}

span.forwarded-to {
    margin-left: 15px;
    font-size: 14px;
}

div.inputs input {
    margin-left: 5px;
    margin-right: 5px;
}

input.seconds {
    text-align: center;
    width: 75px;
}

.delete-group,
.add-destination {
    display: block;
}

.delete-group {
    float: right;
}

.page-buttons {
    background-color: #EBEEF5;
    margin: 5px auto;
    padding: 5px 0;
    text-align: center;
}
    </style>
  </head>
  <body>
    <listen:infoSnippet summaryCode="page.findme.configure.snippet.summary" contentCode="page.findme.configure.snippet.content"/>

    <g:form controller="findme" action="save" method="post" name="save-form">

    <fieldset class="vertical">
      <g:if test="${preferences?.isActive()}">
        <ul class="messages info"><li>Your Find Me / Follow Me configuration is active.</li></ul>
      </g:if>
      <g:else>
        <ul class="messages warning"><li>Your Find Me / Follow Me configuration has expired.</li></ul>
      </g:else>

      <div id="expirationDetails">
        <label class="inline-label">Configuration expires</label>
        <joda:datePicker name="expires" value="${preferences?.expires}"/> at <listen:timePicker name="expires" value="${preferences?.expires}"/>
        <g:if test="${preferences?.expires}">
          (<listen:prettytime date="${preferences.expires}"/>)
        </g:if>
      </div>

      <g:checkBox name="sendReminder" value="${preferences?.sendReminder}"/>
      <label for="sendReminder" class="inline-label">Shortly before the configuration expires, send me a text message</label>

      <div id="smsDetails">
        <label for="smsNumber">
          Mobile Number
          <g:textField name="smsNumber" value="${preferences?.reminderNumberComponents()?.number?.encodeAsHTML()}"/>
        </label>

        <label for="smsProvider">
          Mobile Provider
          <listen:mobileProviderSelect name="smsProvider" value="${preferences?.reminderNumberComponents()?.provider}"/>
        </label>
      </div>
    </fieldset>

    <h4 class="action-description">When somebody calls me</h4>

    <ul id="groups">
      <g:each in="${groups}" var="group" status="i">
        <g:render template="groupTemplate" model="${[group: group]}"/>
        <g:if test="${i < groups?.size() - 1}">
          <li><h4 class="action-description">If I don't answer</h4></li>
        </g:if>
      </g:each>
    </ul>

    <h4 class="action-description">
      If I don't answer
      <select id="no-answer-action">
        <option selected="selected" value="voicemail">Send the caller to my voicemail</option>
        <option value="dial">Dial...</option>
      </select>
    </h4>

    <div class="page-buttons">
      <button type="button" id="save">Save</button>
    </div>

    <ul class="templates">
      <g:render template="groupTemplate" model="[id: 'group-template', group: null]"/>
      <g:render template="destinationTemplate" model="[id: 'destination-template', findMeNumber: null]"/>
    </ul>

    <g:hiddenField name="jsonGroups"/>
    </g:form>

    <script type="text/javascript">
var findme = {
    forwardedPhones: [],

    deleteGroup: function(group) {
        var groups = group.parent();
        var count = groups.children('li').length;
        
        var li = group.next('li');
        if(li.length > 0) {
            li.remove();
        } else {
            group.prev('li').remove();
        }

        group.remove();
    },

    addDestination: function(group, number, dialDuration, isEnabled) {
        var clone = $('#destination-template').clone(true).removeAttr('id');
        var numberField = $('.number', clone);

        numberField.val(number);
        $('.seconds', clone).val(dialDuration);
        findme.toggleDestination(clone, isEnabled);
        $('ul.destinations', group).append(clone);

        numberField.focus();
    },

    addGroup: function() {
        var clone = $('#group-template').clone(true).removeAttr('id');
        findme.addDestination(clone, '', 20, true);
        $('#groups').append(clone);
        $('.number', clone).focus();
    },

    toggleDestination: function(destination, enable) {
        var button = $('.toggle-destination', destination);
        if(enable === true || destination.hasClass('disabled')) {
            button.text('Disable');
            destination.removeClass('disabled').addClass('enabled');
        } else {
            button.text('Enable');
            destination.removeClass('enabled').addClass('disabled');
        }
    },

    toggleSmsDetails: function() {
        $('#smsDetails').toggle($('#sendReminder').is(':checked'));
    },

    buildJson: function() {
        var groups = [];
        $('.group').each(function(index) {
            var group = [];
            $('.destination', this).each(function() {
                var destination = {
                    number: $('.number', this).val(),
                    dialDuration: $('.seconds', this).val(),
                    isEnabled: $(this).hasClass('enabled')
                };
                if(destination.number != '') {
                    group.push(destination);
                }
            });
            if(group.length > 0) {
                groups.push(group);
            }
        });
        return groups;
    },

    save: function() {
        var groups = findme.buildJson();
        var form = $('#save-form');
        $('#jsonGroups', form).val(JSON.stringify(groups));
        form.submit();
    },

    toggleForwarded: function(destination) {
        var number = $('.number', destination).val();
        var forwardedTo = '';
        for(var i = 0; i < findme.forwardedPhones.length; i++) {
            console.log('Phone [' + findme.forwardedPhones[i].from + '] to [' + findme.forwardedPhones[i].to + ']');
            if(findme.forwardedPhones[i].from == number) {
                forwardedTo = findme.forwardedPhones[i].to;
                break;
            }
        }
        if(forwardedTo != '') {
            $('.inputs', destination).append('<span class="forwarded-to">( <b>' + number + '</b> is forwarded to <b>' + forwardedTo + '</b> )</span>');
        } else {
            $('.forwarded-to', destination).remove();
        }
    }
};

$(document).ready(function() {
    $('.add-destination').click(function(e) {
        var group = $(e.target).closest('.group');
        findme.addDestination(group, '', 20, true);
    });

    $('.delete-group').click(function(e) {
        var group = $(e.target).parent();
        findme.deleteGroup(group);
    });

    $('.delete-destination').click(function(e) {
        var destination = $(e.target).closest('.destination');
        var group = destination.closest('.group');
        destination.remove();
        if($('ul.destinations > li', group).length == 0) {
            findme.deleteGroup(group);
        }
    });

    $('.toggle-destination').click(function(e) {
        findme.toggleDestination($(e.target).closest('.destination'));
    });

    findme.toggleSmsDetails(); // toggle on page load to hide if unchecked
    $('#sendReminder').click(function() {
        findme.toggleSmsDetails();
    });

    $('#no-answer-action').change(function(e) {
        var select = $(e.target);
        if(select.val() == 'dial') {
            var count = $('#groups li').length;
            if(count > 0) {
                $('#groups').append('<li><h4 class="action-description">If I don\'t answer</h4></li>');
            }
            findme.addGroup();
            select.val('voicemail');
        }
    });

    $('#save').click(function() {
        findme.save();
        return false;
    });

    $('.number').change(function(e) {
        findme.toggleForwarded($(e.target).closest('.destination'));
    });

    $.ajax({
        url: '${request.contextPath}/profile/listPhones',
        dataType: 'json',
        cache: false,
        success: function(data) {
            for(var i = 0, len = data.length; i < len; ++i) {
                var number = data[i];
                if(number.forwardedTo) {
                    findme.forwardedPhones.push({
                        from: number.number,
                        to: number.forwardedTo
                    });
                }
            }
        }
    });
});
    </script>
  </body>
</html>