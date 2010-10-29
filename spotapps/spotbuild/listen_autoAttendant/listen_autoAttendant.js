function setControllerURL(appObj) {
    var tmpVal = eval("("+appObj+")");
    var result = tmpVal.cntrlURL.split('/api');
    return result[0] + "/meta";
}
