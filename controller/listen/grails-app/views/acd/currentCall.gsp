<html>
<head>
    <title><g:message code="page.acd.currentCall.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="acd"/>
    <meta name="button" content="currentCall"/>
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

    #currentCall table .col-hold-button
    {
        text-align: center;
        width: 11%;
    }

    .template { display: none; }
    .initially-hidden { display: none; }
    .hidden { display: none; }

    </style>
</head>
<body>

<div id="currentCall" class="panel">
    <h3>Current Call:</h3>
    <form id="callForm" method="POST">
        <div id="callListDiv">
            <table>
                <thead>
                    <tr>
                        <th width=10%>On Hold</th>
                        <th width=25%>Caller</th>
                        <th width=15%>Skill</th>
                        <th id="transferHeader" width=7%></th>
                        <th id="disconnectHeader" width=7%></th>
                        <th width=41%></th>

                    </tr>
                </thead>
                <tbody>
                    <table id="callTable">
                        <g:each in="${calls}" status="i" var="thisCall">
                            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}" data-id="${thisCall.id}">
                                <input id="hiddenCallId" type="hidden" name="id" value="${thisCall.id}"/>
                                <td class="holdBox" width="10%">
                                    <g:checkBox name="holdCheckBox" value="${thisCall.id}" class="case holdCheckbox"
                                                checked="${thisCall.onHold}" onchange="toggleHold(this, this.value)"/>
                                </td>
                                <td class="caller" width=25%>${thisCall.ani}</td>
                                <td class="callerSkill" width=15%>${thisCall.skill.description}</td>
                                <td class="transfer-button"  width=7%>
                                    <button type="button" class="transferButton" id="transferButton"
                                            value="${thisCall.id}"
                                            onclick="transferClicked(this, this.value)">Transfer</button>
                                </td>
                                <td class="disconnect-button">
                                    <button type="button" class="disconnectButton" id="disconnectButton"
                                            value="${thisCall.id}"
                                            onclick="disconnectClicked(this, this.value)">Disconnect</button>
                                </td>
                                <td class="transfer-dropdown hidden">
                                    <div id="transferDropdownDiv">
                                        <select class="transferAgentSelect" id="transferAgents"></select>
                                        <button type="button" class="submitTransferButton" id="submitTransferButton"
                                                onclick="submitTransferClicked(this)">Transfer Call</button>
                                        <button type="button" class="cancelTransferButton" id="cancelTransferButton"
                                                onclick="cancelTransferClicked(this)">Cancel</button>
                                    </div>
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </tbody>
            </table>
        </div>
    </form>
</div>

<table class="template">
    <tr id="call-row-template">
        <input id="hiddenCallId" type="hidden" name="id" value=""/>
        <td class="holdBox" width="10%">
            <g:checkBox name="holdCheckBox" value="" class="case holdCheckbox" checked="" onchange="toggleHold(this, this.value)"/>
        </td>
        <td class="caller" width="25%"></td>
        <td class="callerSkill" width=15%></td>
        <td class="transfer-button" width=7%>
            <button type="button" class="transferButton" id="transferButton" value=""
                    onclick="transferClicked(this, this.value)">Transfer</button>
        </td>
        <td class="disconnect-button" >
            <button type="button" class="disconnectButton" id="disconnectButton"
                    value=""
                    onclick="disconnectClicked(this, this.value)">Disconnect</button>
        </td>
        <td class="transfer-dropdown hidden">
            <div id="transferDropdownDiv">
                 <select class="transferAgentSelect" id="transferAgents"></select>
                <button type="button" class="submitTransferButton" id="submitTransferButton"
                        onclick="submitTransferClicked(this)">Transfer Call</button>
                <button type="button" class="cancelTransferButton" id="cancelTransferButton"
                        onclick="cancelTransferClicked(this)">Cancel</button>
            </div>
        </td>
    </tr>
</table>

<script type="text/javascript">

    function transferClicked(e, callId) {
        $('.transfer-button').addClass('hidden');
        $('.disconnect-button').addClass('hidden');
        $('#transferHeader')[0].innerHTML = "Transfer";

        $.ajax({
            url: '${createLink(action: 'availableTransferAgents')}?id=' + callId,
            dataType: 'json',
            cache: false,
            success: function(data)
            {
                $('.transferAgentSelect').empty();

                if(data && data.users)
                {
                    for(var i = 0; i < data.users.length; i++)
                    {
                        var thisAgent = data.users[i];
                        $('.transferAgentSelect').
                                append('<option value="' + thisAgent.id + '">' + thisAgent.realName + '</option>');
                    }
                }
            }
        });

//        $('transferButton').addClass('disabled');
        $('.transfer-dropdown').removeClass('hidden');
        return true;
    }

    function submitTransferClicked(e) {
        alert("Submit Transfer?");
        var userId = $('.transferAgentSelect')[0].value;
        var callId = $('#hiddenCallId')[0].value;

        $.ajax({
            url: '${createLink(action: 'transferCaller')}?id=' + callId + '&userId=' + userId,
            dataType: 'json',
            cache: false,
            success: function(data)
            {
                if(data && data.success == "true")
                {
                    listen.showSuccessMessage('Call transferred.')
//                    $('.transfer-button').removeClass('hidden');
                    $('.transfer-dropdown').addClass('hidden');
                    $('.transfer-button').removeClass('hidden');
                    $('.transfer-button').disabled = true;
                    $('.disconnect-button').removeClass('hidden');
                    $('.disconnect-button').disabled = true;
                    $('#transferHeader')[0].innerHTML = "";
                }
                else
                {
                    listen.showErrorMessage('Unable to transfer call.')
                    $('.transfer-button').removeClass('hidden');
                    $('.disconnect-button').removeClass('hidden');
                    $('.transfer-dropdown').addClass('hidden');
                    $('#transferHeader')[0].innerHTML = "";
                }
            }
        });

        return true;
    }

    function cancelTransferClicked(e) {
        $('.transfer-button').removeClass('hidden');
        $('.disconnect-button').removeClass('hidden');
        $('.transfer-dropdown').addClass('hidden');
        $('#transferHeader')[0].innerHTML = "";
        return true;
    }

    function disconnectClicked(e) {
        alert("Disconnect Call?");
        var callId = $('#hiddenCallId')[0].value;

        $.ajax({
            url: '${createLink(action: 'disconnectCaller')}?id=' + callId,
            dataType: 'json',
            cache: false,
            success: function(data)
            {
                if(data && data.success == "true")
                {
                    listen.showSuccessMessage('Call disconnected.')
//                    $('.transfer-button').removeClass('hidden');
                    $('.transfer-dropdown').addClass('hidden');
                    $('#transferHeader')[0].innerHTML = "";
                }
                else
                {
                    listen.showErrorMessage('Unable to disconnect call.')
                    $('.transfer-button').removeClass('hidden');
                    $('.transfer-dropdown').addClass('hidden');
                    $('#transferHeader')[0].innerHTML = "";
                }
            }
        });

        return true;
    }

    function toggleHold(element, callId)
    {
        if(element.checked)
        {
            $.ajax({
                url: '${createLink(action: 'callerOnHold')}?id=' + callId,
                dataType: 'json',
                cache: false,
                success: function(data)
                {
                    if(data)
                    {
                        $('.holdCheckbox')[0].checked = data.onHold;
                    }
                }
            });
        }
        else
        {
            $.ajax({
                url: '${createLink(action: 'callerOffHold')}?id=' + callId,
                dataType: 'json',
                cache: false,
                success: function(data)
                {
                    if(data)
                    {
                        $('.holdCheckbox')[0].checked = data.onHold;
                    }
                }
            });
        }
    }

    var callList =
    {
        poll: function() {
            $.ajax({
                url: '${createLink(action: 'polledCalls')}',
                dataType: 'json',
                cache: false,
                success: function(data)
                {
                    // 1. loop through table rows and remove rows that don't exist in the new data

//                    var tbody = $('#currentCall table tbody table');
                    var tbody = $('#callTable');

                    $('tr', tbody).each(function() {
                        var tr = $(this);
                        var rowId = parseInt(tr.attr('data-id'), 10);

                        var exists = false;
                        if(data && data.calls)
                        {
                            for(var i = 0; i < data.calls.length; i++)
                            {
                                if(data.calls[i].id == rowId)
                                {
                                    exists = true;
                                    break;
                                }
                            }
                        }
                        if(!exists) {
                            tr.remove();
                        }
                    });

                    // 2. loop through new rows and move existing rows / add new rows

                    if(data && data.calls && data.calls.length > 0)
                    {
                        for(var i = 0; i < data.calls.length; i++)
                        {
                            var call = data.calls[i];

                            var position = -1;
                            var tr; // will be set if a table row is found for this call
                            $('tr', tbody).each(function(index) {
                                var rowId = parseInt($(this).attr('data-id'), 10);
                                if(rowId === call.id) {
                                    position = index;
                                    tr = $(this);
                                }
                            });

                            if(position === -1)
                            {
                                tr = $('#call-row-template').clone(true).removeAttr('id');
                                callList.populate(tr, call);

                                if(i === 0 || $('tr', tbody).length === 0)
                                {
                                    tbody.prepend(tr);
                                }
                            }
                        }
                    }
                }
            });
//            setTimeout(callList.poll(), 1000);
        },

        populate: function(row, call) {
            callList.updateField($('td.caller', row), call.ani);
            callList.updateField($('td.callerSkill', row), call.skill);
//            if(row.attr('data-id') != call.id) {
                row.attr('data-id', call.id);
//            }

            $('input[name="id"]', row).val(call.id);

            $('.holdCheckbox', row)[0].value=call.id

            if(call.onHold)
            {
                $('.holdCheckbox', row)[0].checked=true
            }
            else
            {
                $('.holdCheckbox', row)[0].checked=false;
            }

            $('.transferButton', row)[0].value = call.id;
            $('.disconnectButton', row)[0].value = call.id;
            $('.transfer-button', row).removeClass('hidden');
            $('.disconnect-button', row).removeClass('hidden');

            return;
        },

        updateField: function(field, content, asHtml) {
            var changed = false;
            if(asHtml === true) {
                if(field.html() != content) {
                    field.html(content);
                    changed = true;
                }
            } else {
                var c = String(content);
                if(field.text() != c) {
                    field.text(c);
                    changed = true;
                }
            }
            return changed;
        }
    };

    $(document).ready(function()
    {
        setInterval(callList.poll, 2000);
    });
</script>
</body>
</html>
