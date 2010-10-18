function getAppName(passValues) {
    var result;
    if (typeof(passValues) == 'undefined')
        result = "DEFAULT";
    else {
        var tmpVal = new String(passValues).replace(/^\s*|\s*$|\s+/g,'');
        if(tmpVal == '')
            result = "DEFAULT";
        else {
            tmpVal = eval("("+passValues+")");
            switch (tmpVal.application) {
                case "AUTO_DIAL":
                    result = "listen_autoDial";
                    break;
                case "RECORD":
                    result = "listen_record";
                    break;
                case "DEL_ARTIFACT":
                    result = "listen_artifacts";
                    break;
                case "CONF_EVENT":
                    result = "listen_confEvents";
                    break;
                case "MSG_LIGHT":
                    result = "msgLightCntrl";
                    break;
                case "conferencing":
                    result = "listen_conference";
                    break;
                case "mailbox":
                    result = "listen_mailbox";
                    break;
                case "directVoicemail":
                    result = "listen_mailbox";
                    break;
                case "voicemail":
                    result = "listen_voicemail";
                    break;
                default:
                    result = tmpVal.application;
            }
        }
    }
    return result;
}

function extendAppObject(passValues,cntrlURL,hostName,sysAccessTime,HTTPcontroller,sipURL,STATcontroller,artifactsDIR) {
    var result = eval("("+passValues+")");
    result.cntrlURL = cntrlURL + "/api";
    result.hostName = hostName;
    result.sipURL = sipURL;
    result.sysAccessTime = sysAccessTime;
    result.HTTPcontroller = HTTPcontroller;
    result.STATcontroller = STATcontroller;
    result.artifactsDIR = artifactsDIR;
    return iiStringify(result);
}

function createHostObject(hostName) {
    return "system=http://"+hostName+"/spot";
}

function createPhoneNumberObject(DNIS,callType) {
    return "phoneNumber="+callType+";"+DNIS;
}

function setDnisSearchSting(DNIS) {
    return "getDnis?number=" + getnum(DNIS);
}

function createAppObject(appToAccess,sysAccessTime,sipURL,cntrlURL,hostName,HTTPcontroller,artifactsDIR,STATcontroller) {
    return "{\"application\":\""+appToAccess+"\", \"sysAccessTime\": \""+ sysAccessTime+"\", \"sipURL\":\""+sipURL+"\", \"cntrlURL\":\""+cntrlURL+"/api\", \"hostName\":\""+hostName+"\", \"HTTPcontroller\": \""+ HTTPcontroller+"\", \"artifactsDIR\": \""+ artifactsDIR+"\", \"STATcontroller\": \""+ STATcontroller+"\"}";
}
