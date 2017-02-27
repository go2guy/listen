package com.newnet.test;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.newnet.main.Header;
import com.newnet.main.TestngContext;
import com.newnet.main.VerbActions;

public class ACDAgent {

    String userID = "41";
    int OK = 200;
    int NotAuthenticated = 401;
    //    int BadRequest = 400;
    int AgentNotFound = 404;

    @BeforeClass
    public void setup(ITestContext context) {
        TestngContext.setContext(context);
        Header.setHeader();

    }

    @Test(priority = 10)
    public void getACDAgentOK() {

        String endpoint = "acd/agent/" + userID + "?apiKey=" + TestngContext.getParam("apiKey");

        Assert.assertEquals(VerbActions.get(endpoint).getStatusCode(), OK);
    }

    @Test(priority = 20)
    public void getACDAgentNotAuthenticated() {

        String endpoint = "acd/agent/" + userID + "?apiKey=123456789";

        Assert.assertEquals(VerbActions.get(endpoint).getStatusCode(), NotAuthenticated);
    }

    //        This Request is not possible for a GET
    //    @Test(priority = 30)
    //    public void getBadRequest() {
    //
    //        String endpoint = "acd/agent/" + userID + "?apiKey=" + TestngContext.getParam("apiKey");
    //
    //        Assert.assertEquals(VerbActions.get(endpoint).getStatusCode(), BadRequest);
    //    }

    @Test(priority = 40)
    public void getACDAgentNotFound() {

        String endpoint = "acd/agent/999?apiKey=" + TestngContext.getParam("apiKey");

        Assert.assertEquals(VerbActions.get(endpoint).getStatusCode(), AgentNotFound);
    }

    @Test(priority = 50)
    public void putACDAgentOK() {

        String endpoint = "acd/agent/" + userID + "?apiKey=" + TestngContext.getParam("apiKey") + "&status=available";

        Assert.assertEquals(VerbActions.put(endpoint).getStatusCode(), OK);
    }

    @Test(priority = 60)
    public void putACDAgentNotAuthenticated() {

        String endpoint = "acd/agent/" + userID + "?apiKey=1234567";

        Assert.assertEquals(VerbActions.put(endpoint).getStatusCode(), NotAuthenticated);
    }

    //    @Test(priority = 70)
    //    public void putAgentStatusBadRequest() {
    //
    //        String endpoint = "acd/agent/" + userID + "?apiKey=" + TestngContext.getParam("apiKey") + "&status=available";
    //
    //        Assert.assertEquals(VerbActions.put(endpoint).getStatusCode(), BadRequest);
    //    }

    @Test(priority = 80)
    public void putACDAgentNotFound() {

        String endpoint = "acd/agent/999?apiKey=" + TestngContext.getParam("apiKey") + "&status=available";

        Assert.assertEquals(VerbActions.put(endpoint).getStatusCode(), AgentNotFound);
    }

}
