function getAppName(appName, jsonObj) {
    if ((typeof(jsonObj) != 'undefined') && (jsonObj.length > 0)) {
        var application = new String(appName).replace(/^\s*|\s*$|\s+/g,'');
        application = application.toUpperCase();
        if (application == 'MAILBOX' || application == 'DIRECTMAILBOX')
            application = 'VOICEMAIL';
        else if (application == 'FINDMECONFIG')
            application = 'FINDME';
        else if (application == 'DIRECTINWARDDIAL')
            application = 'IPPBX';
        else if (application == 'MONITORACDCALL')
            application = 'ACD';
			
        if (application != 'DIRECTMESSAGE') {
            var licensed = getJsonVal(jsonObj, application);
            if (!licensed)
                return '';
        }
    }
    switch (appName) {
        case "Attendant":
            appName = "listen_autoAttendant";
            break;
        case "IP PBX":
        case "Direct Inward Dial":
            appName = "ippbx";
            break;
        case "Conferencing":
            appName = "listen_conference";
            break;
        case "Voicemail":
            appName = "listen_voicemail";
            break;
        case "Mailbox":
        case "Direct Mailbox":
            appName = "listen_mailbox";
            break;
		case "ACD":
			appName = "listen_acd";
			break;
		case "Monitor ACD Call":
			appName = "listen_monitorACD";
			break;
        case "AUTO_DIAL":
            appName = "listen_autoDial";
            break;
        case "RECORD":
            appName = "listen_record";
            break;
        case "DEL_ARTIFACT":
            appName = "listen_artifacts";
            break;
        case "CONF_EVENT":
            appName = "listen_confEvents";
            break;
        case "MSG_LIGHT":
            appName = "msgLightCntrl";
            break;
        case "Broadcast":
            appName = "broadcast";
            break;
        case "Find Me Config":
            appName = "listen_findmeAdmin";
            break;
        case "After Hours":
            appName = "listen_afterHours";
            break;
        case "Direct Message":
            appName = "directMessage";
            break;
        default:
    }
    return appName;
}

function extendAppObject(passValues,organization,appToAccess,cntrlURL,hostName,sysAccessTime,HTTPcontroller,sipURL,STATcontroller,artifactsDIR,EXT_LENGTH,pstnLength,EXT_PREFIX,EXT_SUFFIX,licenses,voipChannel,callerID,applicationLabel,faxGateway,pbxObCallerId,specialNumberList) {
    if ((typeof(passValues) == 'undefined') || (passValues.length == 0)) {
        var sipFrom = '';
        var tmpVal = callerID.split('<sip:');
        if ((typeof(tmpVal[0]) == 'undefined') || (tmpVal[0].length == 0))
            sipFrom = tmpVal[1].split('@')[0];
        else
            sipFrom = tmpVal[0].replace(/"|\s$/g,'');
        return "{\"application\":\""+appToAccess+"\", \"sipFrom\":\""+sipFrom+"\", \"licenses\":"+licenses+", \"applicationLabel\":\""+applicationLabel+"\", \"voipChannel\":\""+voipChannel+"\", \"sysAccessTime\": \""+ sysAccessTime+"\", \"EXT_LENGTH\": \""+EXT_LENGTH+"\", \"EXT_PREFIX\": \""+EXT_PREFIX+"\", \"faxGateway \":\""+faxGateway+"\", \"pstnLength\": \""+pstnLength+"\", \"EXT_SUFFIX\": \""+EXT_SUFFIX+"\", \"sipURL\":\""+sipURL+"\", \"cntrlURL\":\""+cntrlURL+"/api\", \"hostName\":\""+hostName+"\", \"HTTPcontroller\": \""+ HTTPcontroller+"\", \"artifactsDIR\": \""+ artifactsDIR+"\", \"organization\": \""+ organization+"\", \"specialNumberList\": \""+specialNumberList+"\", \"pbxObCallerId\": \""+pbxObCallerId+"\", \"STATcontroller\": \""+ STATcontroller+"\"}";
    }
    else {
        var result = eval("("+passValues+")");
        result.cntrlURL = cntrlURL + "/api";
        result.hostName = hostName;
        result.sipURL = sipURL;
        result.faxGateway = faxGateway;
		result.pbxObCallerId = pbxObCallerId;
		result.specialNumberList = specialNumberList;
        if(!result.sysAccessTime) {        
            result.sysAccessTime = sysAccessTime;
        }
        result.HTTPcontroller = HTTPcontroller;
        result.STATcontroller = STATcontroller;
        result.artifactsDIR = artifactsDIR;
        result.EXT_LENGTH = EXT_LENGTH;
        result.pstnLength = pstnLength;
        result.EXT_PREFIX = EXT_PREFIX;
        result.EXT_SUFFIX = EXT_SUFFIX;
        result.application = appToAccess;
        if(organization != '') {
            result.organization = organization;
        }
        return iiStringify(result);
    }
}

function setRouteRequest(ANI, DNIS) {
    var ani = getnum(ANI);
    var dnis = getnum(DNIS);
    if ((ani.length == 0) || (ani == undefined))
        ani = 'anonymous';
    return "?ani=" + escape(ani) + "&dnis=" + escape(dnis);
}

function getOrganization(jsonObj) {
    var result =  eval("("+jsonObj+")");
    var orgHref = result.organization.id;
    if ((orgHref != null) && (orgHref != undefined))
        result = "/organization/" + orgHref;
    else
        result = "";
    return result;
}
