package com.interact.listen.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.ListenServletTest;
import com.interact.listen.attendant.*;
import com.interact.listen.exception.ListenServletException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class GetMenuActionServletTest  extends ListenServletTest
{
    private GetMenuActionServlet servlet;
    private Menu topMenu;
    private Menu otherMenu;
    DialNumberAction dialNumberAction;
    DialPressedNumberAction dialPressedNumberAction;
    GoToMenuAction goToMenuAction;
    LaunchApplicationAction launchApplicationAction;
    LaunchApplicationAction topMenuDefaultAction;
    LaunchApplicationAction topMenuTimeoutAction;

    @Before
    public void setUp()
    {
        servlet = new GetMenuActionServlet();
        otherMenu = createMenu(session);
        topMenu = createMenu(session);
        topMenu.setName("Top Menu");
        topMenuDefaultAction = (LaunchApplicationAction)createAction(session, "LaunchApplicationAction", topMenu, "", null);
        topMenuTimeoutAction = (LaunchApplicationAction)createAction(session, "LaunchApplicationAction", topMenu, "", null);
        topMenu.setDefaultAction(topMenuDefaultAction);
        topMenu.setTimeoutAction(topMenuTimeoutAction);
        session.save(topMenu);
        
        dialNumberAction = (DialNumberAction)createAction(session, "DialNumberAction", topMenu, "123", null);
        dialPressedNumberAction = (DialPressedNumberAction)createAction(session,"DialPressedNumberAction", topMenu, "3453", null);
        goToMenuAction = (GoToMenuAction)createAction(session, "GoToMenuAction", topMenu, "6??", otherMenu);
        launchApplicationAction = (LaunchApplicationAction)createAction(session, "LaunchApplicationAction", topMenu, "78?", null);
    }

    @Test
    public void test_doGet_menuIdAndNullKeysPressed_returnsTimeoutAction() throws ServletException, IOException
    {
        JSONObject expectedReturn = new JSONObject();
        expectedReturn.put("action", "LAUNCH_APPLICATION");
        
        JSONObject args = new JSONObject();
        args.put("applicationName", topMenuTimeoutAction.getApplicationName());
        expectedReturn.put("args", args);
        
        testSuccessfulResponse(String.valueOf(topMenu.getId()), null, expectedReturn);
        /*testExpectedListenServletException(String.valueOf(topMenu.getId()), null, HttpServletResponse.SC_BAD_REQUEST, "Please provide the keys pressed",
                                           "text/plain");*/
    }
    
    @Test
    public void test_doGet_entryFound_returnsDialNumberAction() throws ServletException, IOException
    {
        JSONObject expectedReturn = new JSONObject();
        expectedReturn.put("action", "DIAL_NUMBER");
        
        JSONObject args = new JSONObject();
        args.put("number", dialNumberAction.getNumber());
        expectedReturn.put("args", args);
        
        testSuccessfulResponse(String.valueOf(topMenu.getId()), "123", expectedReturn);
    }

    @Test
    public void test_doGet_entryFound_returnsDialPressedNumberAction() throws ServletException, IOException
    {
        JSONObject expectedReturn = new JSONObject();
        expectedReturn.put("action", "DIAL_PRESSED_NUMBER");
        
        testSuccessfulResponse(String.valueOf(topMenu.getId()), "3453", expectedReturn);
    }

    @Test
    public void test_doGet_entryFound_returnsNewMenuWithInformation() throws ServletException, IOException
    {
        DialNumberAction dialNumberAction2 = (DialNumberAction)createAction(session, "DialNumberAction", otherMenu, "1??", null);
        DialNumberAction otherMenuTimeout = (DialNumberAction)createAction(session, "DialNumberAction", otherMenu, null, null);
        DialNumberAction otherMenuDefault = (DialNumberAction)createAction(session, "DialNumberAction", otherMenu, null, null);
        LaunchApplicationAction launchApplicationAction2 = (LaunchApplicationAction)createAction(session, "LaunchApplicationAction",
                                                                                                 otherMenu, "22?", null);
        otherMenu.setDefaultAction(otherMenuDefault);
        otherMenu.setTimeoutAction(otherMenuTimeout);
        session.save(otherMenu);
       
        JSONObject expectedReturn = new JSONObject();
        expectedReturn.put("action", "PROMPT");
        
        JSONObject args = new JSONObject();
        JSONArray keyPresses = new JSONArray();
        keyPresses.add("1??");
        keyPresses.add("22?");
        
        args.put("id", otherMenu.getId());
        args.put("keyPresses", keyPresses);
        args.put("audioFile", otherMenu.getAudioFile());
        
        expectedReturn.put("args", args);
        
        testSuccessfulResponse(String.valueOf(topMenu.getId()), "611", expectedReturn);
    }

    @Test
    public void test_doGet_entryFound_returnsLaunchApplicationAction() throws ServletException, IOException
    {
        JSONObject expectedReturn = new JSONObject();
        expectedReturn.put("action", "LAUNCH_APPLICATION");
        
        JSONObject args = new JSONObject();
        args.put("applicationName", launchApplicationAction.getApplicationName());
        expectedReturn.put("args", args);
        
        testSuccessfulResponse(String.valueOf(topMenu.getId()), "781", expectedReturn);
    }

    @Test
    public void test_doGet_entryNotFound_returnsDefaultAction() throws ServletException,
        IOException
    {
        JSONObject expectedReturn = new JSONObject();
        expectedReturn.put("action", "LAUNCH_APPLICATION");
        
        JSONObject args = new JSONObject();
        args.put("applicationName", topMenuDefaultAction.getApplicationName());
        expectedReturn.put("args", args);
        
        testSuccessfulResponse(String.valueOf(topMenu.getId()), "555", expectedReturn);
    }

    @Test
    public void test_doGet_noKeypress_timeoutTrue_returnsTimeoutAction() throws ServletException,
        IOException
    {
        JSONObject expectedReturn = new JSONObject();
        expectedReturn.put("action", "LAUNCH_APPLICATION");
        
        JSONObject args = new JSONObject();
        args.put("applicationName", topMenuTimeoutAction.getApplicationName());
        expectedReturn.put("args", args);
        
        testSuccessfulResponse(String.valueOf(topMenu.getId()), null, expectedReturn);
    }

    private void testSuccessfulResponse(String menuId, String keysPressed, JSONObject expectedContent) throws IOException,
        ServletException
    {
        performRequest(menuId, keysPressed);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertOutputBufferContentEquals(expectedContent.toString());
        assertOutputBufferContentTypeEquals("text/plain");
    }
    
    private void performRequest(String menuId, String keysPressed) throws IOException, ServletException
    {
        request.setMethod("GET");
        request.setParameter("menuId", menuId);
        request.setParameter("keysPressed", keysPressed);
        servlet.service(request, response);
    }

    private void testExpectedListenServletException(String menuId, String keysPressed, int expectedStatus,
                                                    String expectedContent, String expectedContentType)
        throws IOException, ServletException
    {
        try
        {
            performRequest(menuId, keysPressed);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(expectedStatus, e.getStatus());
            assertEquals(expectedContent, e.getContent());
            assertEquals(expectedContentType, e.getContentType());
        }
    }
}