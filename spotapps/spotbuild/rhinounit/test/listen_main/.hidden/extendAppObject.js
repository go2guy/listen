/*
NAME: extendAppObject.js
DESC: This file contains test cases for the extendAppObject function.
      It loads the needed JS lib files and defines the relevant test 
      cases for this function.*/

//Load lib file containing function to be tested
eval(loadFile("../listen_main/listen_main.js"));
eval(loadFile("../../../spotbuild-vip/src/vip/lib/js/lib.js"));
eval(loadFile("../../../spotbuild-vip/src/vip/lib/js/stringify.js"));

// Define variables & object used for testing
var appObjIsUndefined;
var testVal = 'testAppName';
var emptyObj = '';
var justSpace = '  ';
var setAppObj = {
    appName: function (name) {
        var currTime = new Date().getTime();
        return '{"application":"' + name + '", "sysAccessTime": "' + currTime + '", "sipURL":"testServer", "cntrlURL":"http://localhost:9091/api", "hostName":"localhost", "HTTPcontroller": "http://localhost/spot/cgi-bin/spotbuild/listen/controller.php", "artifactsDIR": "/interact/listen/artifacts/", "STATcontroller": "http://localhost:9413/collect/stat"}';
    } // This object contains one member whose value is a function.
};

//Here are the test cases
testCases(test,
    function shouldReturnDefaultAsResult1() {
        assert.that(getAppName(appObjIsUndefined), eq("DEFAULT"));
    },
    function shouldReturnDefaultAsResult2() {
        assert.that(getAppName(emptyObj), eq("DEFAULT"));
    },
    function shouldReturnDefaultAsResult3() {
        assert.that(getAppName(justSpace), eq("DEFAULT"));
    },
    function shouldReturnAnEmptyStringResult() {
        assert.that(getAppName(setAppObj.appName('')), eq(""));
    },
    function shouldReturnTestAppNameAsResult() {
        assert.that(getAppName(setAppObj.appName(testVal)), eq("testAppName"));
    },
    function shouldReturnListen_autoDialAsResult() {
        assert.that(getAppName(setAppObj.appName('AUTO_DIAL')), eq("listen_autoDial"));
    },
    function shouldReturnListen_recordAsResult() {
        assert.that(getAppName(setAppObj.appName('RECORD')), eq("listen_record"));
    },
    function shouldReturnListen_artifactsAsResult() {
        assert.that(getAppName(setAppObj.appName('DEL_ARTIFACT')), eq("listen_artifacts"));
    },
    function shouldReturnListen_confEventsAsResult() {
        assert.that(getAppName(setAppObj.appName('CONF_EVENT')), eq("listen_confEvents"));
    },
    function shouldReturnMsgLightCntrlAsResult() {
        assert.that(getAppName(setAppObj.appName('MSG_LIGHT')), eq("msgLightCntrl"));
    },
    function shouldReturnConferencingAsResult() {
        assert.that(getAppName(setAppObj.appName('conferencing')), eq("listen_conference"));
    },
    function shouldReturnListen_mailboxAsResult1() {
        assert.that(getAppName(setAppObj.appName('mailbox')), eq("listen_mailbox"));
    },
    function shouldReturnListen_mailboxAsResult2() {
        assert.that(getAppName(setAppObj.appName('directVoicemail')), eq("listen_mailbox"));
    },
    function shouldReturnListen_voicemailAsResult() {
        assert.that(getAppName(setAppObj.appName('voicemail')), eq("listen_voicemail"));
    }
);
