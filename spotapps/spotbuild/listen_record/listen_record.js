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

function getChannelInfo(argList, key) {
    var result = eval("("+argList+")");
    if (result[key]== 'TUI') 
        return result.adminSID;
    else 
        return "GUI";
}
