function getArtifact(argList, HTTP_dir) {
    var result ='';
    var tmpVal = new String(argList).replace(/^\s*|\s*$/g,'');
    if((tmpVal == '') || (typeof(argList) == 'undefined'))
        result = "ERROR";
    else {
        tmpVal = eval("("+argList+")");
        switch (tmpVal.action) {
            case "FILE":
                var host = tmpVal.hostName + "/";
                var artifact = tmpVal.artifact;
                result = HTTP_dir + tmpVal.artifactsDIR;
                break;
            case "SUB":
                result = HTTP_dir + tmpVal.artifactsDIR + tmpVal.artifact;
                break;
            default:
                result = "ERROR";
                break;
        }
    }
    return result;
}