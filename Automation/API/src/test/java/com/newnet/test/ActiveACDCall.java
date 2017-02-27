package com.newnet.test;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.newnet.main.Header;
import com.newnet.main.TestngContext;
import com.newnet.main.VerbActions;

public class ActiveACDCall {

    // insert into acd_call (version, ani, dnis, skill_id, session_id, common_call_id, init_time, call_status, ivr, last_modified, user_id, call_start, on_hold) values (6,'5555','994',5,'session.06F77FE599DF9288454132507914877925644727','ab02555a-3c86-4e55-a06b-c255d3651ee2', '2017-02-23 05:47:13', 'CONNECTED', '50.23.255.203', '2017-02-22 23:47:57', 41, '2017-02-22 23:47:57', 0);

    String userName = "ApiAutomation";
    int OK = 200;
    int NotAuthenticated = 401;
    int AgentNotFound = 404;

    @BeforeClass
    public void setup(ITestContext context) {
        TestngContext.setContext(context);
        Header.setHeader();

    }

    @Test(priority = 10)
    public void getActiveACDCallOK() {

        String endpoint = "acd/call/active?apiKey=" + TestngContext.getParam("apiKey") + "&username=" + userName;

        Assert.assertEquals(VerbActions.get(endpoint).getStatusCode(), OK);
    }

    @Test(priority = 20)
    public void getActiveACDCallNotAuthenticated() {

        String endpoint = "acd/call/active?apiKey=123456789" + "&username=" + userName;

        Assert.assertEquals(VerbActions.get(endpoint).getStatusCode(), NotAuthenticated);
    }

    @Test(priority = 30)
    public void getActiveACDCallNotFound() {

        String endpoint = "acd/call/active?apiKey=" + TestngContext.getParam("apiKey") + "&username=test";

        Assert.assertEquals(VerbActions.get(endpoint).getStatusCode(), AgentNotFound);
    }

}
