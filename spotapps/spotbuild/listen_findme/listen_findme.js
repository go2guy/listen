function changeController(cntrlURL, suffix)
{
    var result = getNextElement('L',cntrlURL,'/');
    result = cntrlURL.split('/'+result);
    return result[0] + "/" + suffix;
}


function setParamsForCallEndCaller(endTime, callType, ANI, DNIS, organization, callResult, callStartTime) {
    var duration = endTime - callStartTime;
    if(callStartTime == '')
    {
    duration = 0;
    }
    return "{\"date\": \""+iiDateToISO(callStartTime)+"\", \"service\": \""+callType+"\", \"duration\":"+duration+", \"ani\":\""+ANI+"\", \"dnis\":\""+DNIS+"\", \"result\":\""+callResult+"\", \"organization\": {\"href\":\""+organization+"\"}}";
}

function checkOutBound (passValues, phoneNumber) {
    var str = new String(phoneNumber);
    var num = /^\d+$/;
    var extLength = getJsonVal(passValues, 'EXT_LENGTH');

    if (phoneNumber.indexOf('.') != -1)
    {
        return"IP";
    }
    if ((num.test(str)) && (phoneNumber.length >= extLength) && (phoneNumber.length > 0))
        return "OUTDIAL";
    else
        return "EXT";
}