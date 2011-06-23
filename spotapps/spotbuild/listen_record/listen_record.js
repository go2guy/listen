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

function getTuiEvent(channel, recEvent) {
    var result;
    if (channel == 'GUI' && recEvent == 'START')
        result = 'IS_RECORDING';
    else if (channel == 'TUI' && recEvent == 'START_ERROR')
        result = 'ERR_REC_START';
    else if (channel == 'TUI' && recEvent == 'STOP_ERROR')
        result = 'ERR_REC_STOP';
    else
        result = '';
    return result;
}
