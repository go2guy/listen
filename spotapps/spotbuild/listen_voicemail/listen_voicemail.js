function getAccessNumSubID(jsonObj, status) {
    var result = "NOT_FOUND";
    var tmpVal = eval("("+jsonObj+")");
    if ((status == 'Success') && (tmpVal.count== 1))
        result = getNextElement('L',tmpVal.results[0].subscriber.href,'/');
    return result;
}

function setCallerName (jsonObj, status, ANI) {
    var result = getnum(ANI);
    if (status == 'Success') {
        var tmpVal = getJsonVal(jsonObj, 'realName');
        if (tmpVal.length > 0)
            result = tmpVal;
    }
    return result;
}

function createTranscriptionObj(returnVal, fileLocation, passValues, transcriptionOpts) {
    var number = eval("("+returnVal+")");
    var result = eval("("+passValues+")");
    result.fileLocation = fileLocation;
    result.fileNumber = number.id;
    result.phoneNumber = getJsonVal(transcriptionOpts, 'phoneNumber');
    return iiStringify(result);
}

function wavToMp3Status (pcmStatus, mp3Status) {
    var result = 'Failure';
    if (pcmStatus && mp3Status)
        result = 'Success';

    return result;
}

function recordOptions(recDtmf) {
    if ((recDtmf == '*') || (recDtmf == '#'))
       return 'true';
    else
       return 'false';
}

function extendAppObj(jsonObj, recDtmf) {
    jsonObj = extendJsonObject(jsonObj, 'application', 'voicemail');
    if (recDtmf == 0)
       jsonObj = extendJsonObject(jsonObj, 'action', 'operator');
    else
       jsonObj = extendJsonObject(jsonObj, 'action', 'autoAttendant');

    return jsonObj;
}

function getGreetingURL (returnVal) {
    var greeting = getJsonVal (returnVal, 'greetingLocation');
    if (greeting.length > 0)
        greeting = greeting.split('file:')[1];
    return greeting;
}

function vmPostParams (fileLocation,  subID, duration, fileSize, ANI, transcriptionTxt) {
    var ani = getnum(ANI);
    if (ani.length == 0)
        ani = 'Anonymous';
    return "{\"uri\": \"file:"+fileLocation +"\",\"transcription\":\""+transcriptionTxt+"\",\"duration\":\""+duration+"\",\"fileSize\":\""+fileSize+"\",\"leftBy\":\""+ani+"\",\"subscriber\": { \"href\": \"/subscribers/"+subID+"\"}}";
}
