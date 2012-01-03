function getPlayBackOrder (subscriberInfo) {
    var result = eval("("+subscriberInfo+")");
    if (result.voicemailPlaybackOrder == 'NEWEST_TO_OLDEST')
        return "DESCENDING";
    else
        return "ASCENDING";
}

function getVmDateTime(result, flag) {
    var result;
    var vmDateCreated = getJsonVal(result, 'dateCreated');
    if (flag == 'date') {
        var dateVal = vmDateCreated.split('T')[0].split('-');
        result = dateVal[1]+'-'+dateVal[1]+'-'+dateVal[2]+'_dd';
    }
    else if (flag == 'time') {
        var timeVal = vmDateCreated.split('T')[1].split(':');
        switch (timeVal[0]) {
            case '00':
                result = '12:'+timeVal[1]+'am';
                break;
            case '12':
                result = '12:'+timeVal[1]+'pm';
                break;
            default:
                if ((parseInt(timeVal[0],10)) > 12) {
                    result = parseInt(timeVal[0], 10) - 12;
                    result = result+':'+timeVal[1]+'pm';
                }
                else
                    result = timeVal[0]+':'+timeVal[1]+'am';
        }
    }
    else
        result = '';
    return result
}

function setFwdFileName(FILE1, subscriber, subFwd) {
    var fileName = new String();
    var tmpVal = new String(FILE1).split('://');
    if (tmpVal.length > 1) {
        var i = tmpVal[1].indexOf('/');
        fileName = tmpVal[1].slice(i);
    }
    else
        fileName = tmpVal[0];

    tmpVal = fileName.split(subscriber+'.wav');
    fileName = tmpVal[0]+subFwd+'.wav';
    tmpVal = fileName.split('/'+subscriber+'/');
    return tmpVal[0]+'/'+subFwd+'/'+tmpVal[1];
}

function createVoiceMailObj(result, subFwd, hostName, FILE2, subscriber) {
    var result = eval("("+result+")");
    return "{\"uri\": \"file:"+FILE2+"\",\"duration\":\""+result.duration+"\",\"fileSize\":\""+result.fileSize+"\",\"leftBy\":\""+result.leftBy+"\", \"transcription\":\"\", \"subscriber\": { \"href\": \"/subscribers/"+subFwd+"\"},\"forwardedBy\": { \"href\": \"/subscribers/"+subscriber+"\"}}";
}

function setupMbxMenu(enableOutDial, enablePagingSupport)
{
    var menu;
    var used = false;

    if(enableOutDial == true)
    {
        used = true;
            menu = "mbxMenuDial";
    }

    if (enablePagingSupport == true)
    {
        if (used == true) {
            menu = menu + "|PmbxMenuPaging";
        }
        else {
            menu = "mbxMenuPaging";
        }
        used = true;
    }

    if (used == true)
    {
            menu = menu + "|PmbxMenuEnd";
    }
    else
    {
            menu = "mbxMenuEnd";
    }
    return menu;
}

function checkMbxMenuInput(enableOutDial, enablePagingSupport, userInput)
{
    if (userInput == '8')
    {
        if(enableOutDial == true)
        {
            return userInput;
        }
        return 'DISABLED';
    }

    if (userInput == '9')
    {
        if (enablePagingSupport == true)
        {
            return userInput;
        }
        return 'DISABLED';
    }        

    return userInput;
}

function setParamsForCallEndMailbox(passValues, ANI, callResult, organization) {
    var duration = getJsonVal(passValues, 'callTime');
    if(duration == '')
    {
    duration = 0;
    }
    var callStartTime = getJsonVal(passValues, 'callStartTime');
    if(callStartTime == '')
    {
        callStartTime = (new Date().getTime());
    }
    return "{\"date\": \""+iiDateToISO(callStartTime)+"\", \"service\": \""+getJsonVal(passValues, 'callType')+"\", \"duration\":"+duration+", \"ani\":\""+ANI+"\", \"dnis\":\""+getJsonVal(passValues, 'destination')+"\", \"result\":\""+callResult+"\", \"organization\": {\"href\":\""+organization+"\"}}";
}

function setSearchParams (subID, organization) {
    if((subID.length == 0) || (subID == undefined))
        subID = 'Anonymous';

    return '?number=' + subID + '&organization=' + organization;
}
