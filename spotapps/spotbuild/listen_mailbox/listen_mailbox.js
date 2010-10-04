function accessNumSrchString(input, flag) {
    var result;
    if (flag == 'GET_SUB')
        result = '?_fields=subscriber&number=' + input;
    else if (flag = 'GET_ACCESS_NUMS')
        result  = '?subscriber=' + getJsonVal(input, 'href');
    else
        result = '';
    return result;
}

function getSubscriber(returnVal, flag) {
    var result = eval("("+returnVal+")");
    var subscriber = result.results[0].subscriber.href;
    if (flag == 'id')
        subscriber = getNextElement('L',subscriber,'/');

    return subscriber;
}

function getPlayBackOrder (subscriberInfo) {
    var result = eval("("+subscriberInfo+")");
    if (result.voicemailPlaybackOrder == 'NEWEST_TO_OLDEST')
        return "DESCENDING";
    else
        return "ASCENDING";
}

function setVmRetrievalString(subscriber, playbackOrder, flag) {
    return "?subscriber=/subscriber/"+subscriber+"&isNew="+flag+"&_sortBy=dateCreated&_sortOrder="+playbackOrder;
}

function getResourceID(jsonObj, index) {
    var tmpVal = getJsonVal(jsonObj, 'results');
    var result = tmpVal[index].href.split('/');
    return result[result.length-1];
}

function getVmDateTime(result, flag) {
    var result;
    var vmDateCreated = getJsonVal(result, 'dateCreated');
    if (flag == 'date') {
        var dateVal = vmDateCreated.split('T')[0].split('-');
        result = 'YYYY-'+dateVal[1]+'-'+dateVal[2]+'_dd';
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

function updatePin (subscriberInfo, oldPin, newPin) {
    var temp = subscriberInfo.split('voicemailPin":"'+oldPin);
    return temp[0] + 'voicemailPin":"' + newPin + temp[1];
}

function setFileName (path, name, extension){
    return path+name+extension;
}

function createAccessNumberObj(greetingLocation, subID, accessID, hostName) {
    return "{\"subscriber\": {\"href\": \"/subscribers/" + subID +"\"},\"greetingLocation\": \"http://"+hostName+greetingLocation+"\","+ "\"number\": \""+accessID+"\"}";
}

function getTimeDifference(endTime, startTime) {
    return Math.round ((endTime - startTime)/1000);
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
    return "{\"uri\": \"http://"+hostName+FILE2+"\",\"duration\":\""+result.duration+"\",\"fileSize\":\""+result.fileSize+"\",\"leftBy\":\""+result.leftBy+"\",\"subscriber\": { \"href\": \"/subscribers/"+subFwd+"\"},\"forwardedBy\": { \"href\": \"/subscribers/"+subscriber+"\"}}";
}

function updateVmState (vmObj) {
    var strObj = vmObj.split('isNew":true');
    return strObj[0] + 'isNew":false' + strObj[1];
}
