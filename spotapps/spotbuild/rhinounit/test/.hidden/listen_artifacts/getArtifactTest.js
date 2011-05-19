/*
NAME: getArtifact.js
DESC: This file contains test cases for the getArtifact function.
      It loads the needed JS lib files and defines the relevant test 
      cases for this function.*/

//Load lib file containing function to be tested
eval(loadFile("../listen_artifacts/listen_artifacts.js"));    //Load lib file containing function to be tested

// Define variables & object used for testing
var objIsUndefined;
var emptyObj = '';
var justSpace = '  ';
var httpDir = '/var/www/html/';
var fileArtifact = 'subID/voicemail/message/fromID-dateStamp-toID.wav';
var subscriberArtifact = 'subID';
var pathToFile = '/var/www/html/interact/listen/artifacts/subID/voicemail/message/fromID-dateStamp-toID.wav';
var pathToSubscriber = '/var/www/html/interact/listen/artifacts/subID';
var setInputObj = {
    createObj: function (action, artifact) {
        return '{"action":"' + action + '", "application":"applicationName", "artifact":"http://localhost/interact/listen/artifacts/"' + artifact + '", "initiatingChannel":"GUI", "initiatingSubscriber":"/subscribers/subID", "cntrlURL":"http://localhost:9091/api", "hostName":"localhost", "sipURL":"testSystem", "sysAccessTime":"currentTime", "HTTPcontroller":"http://localhost/spot/cgi-bin/spotbuild/listen/controller.php", "STATcontroller":"http://localhost:9413/collect/stat", "artifactsDIR":"/interact/listen/artifacts/"}';
    } // This object contains one member whose value is a function.
};

//Here are the test cases
testCases(test,
    function shouldReturnErrorAsResult1() {
        assert.that(getArtifact(objIsUndefined, httpDir), eq('ERROR'));
    },
    function shouldReturnErrorAsResult2() {
        assert.that(getArtifact(emptyObj, httpDir), eq('ERROR'));
    },
    function shouldReturnErrorAsResult3() {
        assert.that(getArtifact(justSpace, httpDir), eq('ERROR'));
    },
    function shouldReturnErrorAsResult4() {
        assert.that(getArtifact(setInputObj.createObj('UNKNOWN', fileArtifact)), httpDir), eq('ERROR'));
    }/*,
    function shouldReturnPathToFileAsResult() {
        assert.that(getArtifact(setInputObj.createObj('FILE', fileArtifact), httpDir), eq(pathToFile));
    },
    function shouldReturnPathToSubscriberDirectoryAsResult() {
        assert.that(getArtifact(setInputObj.createObj('SUB', subscriberArtifact), httpDir), eq(pathToSubscriber));
    }*/
);
