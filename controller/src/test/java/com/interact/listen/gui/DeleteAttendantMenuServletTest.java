package com.interact.listen.gui;

import static org.junit.Assert.assertNull;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;
import com.interact.listen.attendant.Action;
import com.interact.listen.attendant.Menu;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class DeleteAttendantMenuServletTest extends ListenServletTest
{
    private DeleteAttendantMenuServlet servlet = new DeleteAttendantMenuServlet();

    @Test
    public void test_doPost_withoutCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("POST");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doPost_withNonAdministratorCurrentSubscriber_throwsUnauthorized() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        testForListenServletException(servlet, 401, "Unauthorized - Insufficient permissions");
    }

    @Test
    public void test_doPost_withNullIdParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);
        request.setMethod("POST");
        request.setParameter("id", (String)null);
        testForListenServletException(servlet, 400, "Id cannot be null");
    }

    @Test
    public void test_doPost_withNonNumericIdParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);
        request.setMethod("POST");
        request.setParameter("id", "abcd");
        testForListenServletException(servlet, 400, "Id must be a number");
    }

    @Test
    public void test_doPost_withIdForNonexistentMenu_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);
        Long id = System.currentTimeMillis();

        Menu menu = Menu.queryById(session, id);
        assert menu == null;

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(id));
        testForListenServletException(servlet, 400, "Menu with id [" + id + "] not found");
    }

    @Test
    public void test_doPost_withIdForTopMenu_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        Menu menu = createMenu(session);
        menu.setName(Menu.TOP_MENU_NAME);
        session.save(menu);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(menu.getId()));
        testForListenServletException(servlet, 400, "Cannot delete Top Menu");
    }

    @Test
    public void test_doPost_withDeletableMenu_deletesMenu() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        Menu menu = createMenu(session);
        Long id = menu.getId();

        Action action = createAction(session, "DialPressedNumberAction", menu, "123", null);
        Long actionId = action.getId();

        request.setParameter("id", String.valueOf(id));
        servlet.doPost(request, response);

        Menu queryMenu = Menu.queryById(session, id);
        assertNull(queryMenu);

        Action queryAction = (Action)session.get(Action.class, actionId);
        assertNull(queryAction);
    }
}
