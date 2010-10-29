function setControllerURL(appObj) {
    var tmpVal = eval("("+appObj+")");
    var result = tmpVal.cntrlURL.split('/api');
    return result[0] + "/meta";
}

function getMenuHotKeys(menuObj) {
    var result = '';
    var tmpVal = getJsonVal(menuObj, 'args');
    for (var i=0; i<tmpVal.keyPresses.length; i++) {
        if (tmpVal.keyPresses[i].length == 1)
            result = result + tmpVal.keyPresses[i] + ',';
    }
    return result;
}
