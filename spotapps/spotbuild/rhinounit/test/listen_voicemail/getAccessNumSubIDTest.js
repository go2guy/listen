/*
NAME: getAppNameTest.js
DESC: This file contains test cases for the getAppName function.
      It loads listen_main.js (which contains getAppName). It
      also defines relevant test cases for this function.
*/

eval(loadFile("../listen_voicemail/listen_voicemail.js"));    //Load lib file containing function to be tested

// Define variables & object used for testing
var appObjIsUndefined;
var testVal = 'testAccessNumSubIDTest';
var emptyObj = '';
var justSpace = '  ';
var setAppObj = {
    appName: function (name) {
        var currTime = new Date().getTime();
        return '{"href":"/accessNumbers?_first=0&_max=100&_fields=subscriber&number=362","count":1,"total":1,"results":[{"href":"/accessNumbers/1","subscriber":{"href":"/subscribers/2"}}]}','Success';
    } // This object contains one member whose value is a function.
};

//Here are the test cases
testCases(test,
    function shouldReturnDefaultAsResult1() {
        assert.that(getAppName(appObjIsUndefined), eq('DEFAULT'));
    },
    function shouldReturnDefaultAsResult2() {
        assert.that(getAppName(emptyObj), eq('DEFAULT'));
    },
    function shouldReturnDefaultAsResult3() {
        assert.that(getAppName(justSpace), eq('DEFAULT'));
    },
    function shouldReturnAnEmptyStringResult() {
        assert.that(getAppName(setAppObj.appName('')), eq(''));
    },
    function shouldReturnTestAppNameAsResult() {
        assert.that(getAppName(setAppObj.appName(testVal)), eq('testAppName'));
    },
    function shouldReturnListen_autoDialAsResult() {
        assert.that(getAppName(setAppObj.appName('AUTO_DIAL')), eq('listen_autoDial'));
    },
    function shouldReturnListen_recordAsResult() {
        assert.that(getAppName(setAppObj.appName('RECORD')), eq('listen_record'));
    },
    function shouldReturnListen_artifactsAsResult() {
        assert.that(getAppName(setAppObj.appName('DEL_ARTIFACT')), eq('listen_artifacts'));
    },
    function shouldReturnListen_confEventsAsResult() {
        assert.that(getAppName(setAppObj.appName('CONF_EVENT')), eq('listen_confEvents'));
    },
    function shouldReturnMsgLightCntrlAsResult() {
        assert.that(getAppName(setAppObj.appName('MSG_LIGHT')), eq('msgLightCntrl'));
    },
    function shouldReturnConferencingAsResult() {
        assert.that(getAppName(setAppObj.appName('conferencing')), eq('listen_conference'));
    },
    function shouldReturnListen_mailboxAsResult1() {
        assert.that(getAppName(setAppObj.appName('mailbox')), eq('listen_mailbox'));
    },
    function shouldReturnListen_mailboxAsResult2() {
        assert.that(getAppName(setAppObj.appName('directVoicemail')), eq('listen_mailbox'));
    },
    function shouldReturnListen_voicemailAsResult() {
        assert.that(getAppName(setAppObj.appName('voicemail')), eq('listen_voicemail'));
    }
);
