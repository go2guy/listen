function getArtifact(argList) {
    var result ='';
    var tmpVal = new String(argList).replace(/^\s*|\s*$/g,'');
    if((tmpVal == '') || (typeof(argList) == 'undefined'))
        result = "ERROR";
    else {
        tmpVal = eval("("+argList+")");
        switch (tmpVal.action) {
            case "FILE":
                result = tmpVal.artifact.split(tmpVal.hostName)[1];
                break;
            case "SUB":
                result = tmpVal.artifactsDIR + tmpVal.artifact;
                break;
            default:
                result = "ERROR";
                break;
        }
    }
    return result;
}

function findMP3(argList) {
    var result = 'exit';
    var tmpVal = eval("("+argList+")");
    if ((tmpVal.action == 'FILE') && (inString('voicemail',tmpVal.artifact,'/'))) {
        result = tmpVal.artifact.split(tmpVal.hostName)[1].split('.wav')[0] + '.mp3';
    }
    return result
}