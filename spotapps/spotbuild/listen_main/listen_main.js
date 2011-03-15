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

function extendAppObject(passValues,cntrlURL,hostName,sysAccessTime,HTTPcontroller,sipURL,STATcontroller,artifactsDIR,EXT_LENGTH) {
    var result = eval("("+passValues+")");
    result.cntrlURL = cntrlURL + "/api";
    result.hostName = hostName;
    result.sipURL = sipURL;
    result.sysAccessTime = sysAccessTime;
    result.HTTPcontroller = HTTPcontroller;
    result.STATcontroller = STATcontroller;
    result.artifactsDIR = artifactsDIR;
    result.EXT_LENGTH = EXT_LENGTH;
    return iiStringify(result);
}

function setDNIS (DNIS) {
    var dnis = getnum(DNIS);
    if (iiNumber(dnis))
        return "getDnis?number=" + escape(dnis);
    else
        return "ippbx";
}
