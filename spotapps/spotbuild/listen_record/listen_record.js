function getArcadeId(argList) {
    var tmpVal = eval("("+argList+")");
    var result = tmpVal.arcadeId;
    if (result == 'null') 
        result =  "GET_ID";
    else {
        if (result == 'GET_ID')
            result = tmpVal.ConferenceID;
    }
    return result;
}

function getSubscriber(returnVal) {
    var result = eval("("+returnVal+")");
    result = result.subscriber.href;
    return getNextElement('L',result,'/');
}

function setRecFileName(argList, recPath, id ) {
    var result = eval("("+argList+")");
    return recPath + id + "-" + result.startTime + "-" + getTimeStamp(1) + ".wav";
}

function getChannelInfo(argList, key) {
    var result = eval("("+argList+")");
    if (result[key]== 'TUI') 
        return result.adminSID;
    else 
        return "GUI";
}

function createConfObject(id,isRecording,isStarted,recSID,arcadeID,confDescription) {
    return "{\"href\": \"/conferences/"+id+"\", \"id\":"+id+", \"description\":\""+confDescription+"\", \"arcadeId\":\""+arcadeID+"\",\"recordingSessionId\":\""+recSID+"\", \"isRecording\":"+isRecording+", \"isStarted\": "+isStarted+"}";
}

function createConfRecObject(id,hostName,recSize,recFile,recLength,recInfo) {
    return "{\"conference\": {\"href\": \"/conferences/"+id+"\"}, \"uri\":\"http://"+hostName+recFile+"\", \"description\":\""+recInfo+"\", \"duration\":\""+recLength+"\", \"fileSize\":\""+recSize+"\"}";
}
