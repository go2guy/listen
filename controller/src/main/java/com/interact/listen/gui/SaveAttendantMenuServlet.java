package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.attendant.*;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SaveAttendantMenuServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(SaveAttendantMenuServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.ATTENDANT))
        {
            throw new NotLicensedException(ListenFeature.ATTENDANT);
        }

        ServletUtil.sendStat(request, Stat.GUI_SAVE_ATTENDANT_MENU);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        if(!subscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Insufficient permissions");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        JSONObject json = (JSONObject)JSONValue.parse(request.getParameter("menu"));
        LOG.debug("Received menu for saving: " + json.toJSONString());

        String id = (String)json.get("id");
        boolean modifying = id != null && !id.trim().equals("");

        Menu menu = new Menu();
        if(modifying)
        {
            menu = Menu.queryById(session, Long.parseLong(id));
        }

        // only set the name if we're not modifying or we're modifying a menu that's not the current Top Menu
        if(!modifying || !menu.getName().equals(Menu.TOP_MENU_NAME))
        {
            menu.setName((String)json.get("name"));
        }
        menu.setAudioFile((String)json.get("audioFile"));

        if(modifying)
        {
            // we'll re-create these later
            session.delete(menu.getDefaultAction());
            session.delete(menu.getTimeoutAction());
        }

        // default action
        JSONObject defaultActionJson = (JSONObject)json.get("defaultAction");
        Action defaultAction = keyToAction((String)defaultActionJson.get("action"));
        populateAction(defaultActionJson, defaultAction, session);
        menu.setDefaultAction(defaultAction);

        // timeout action
        JSONObject timeoutActionJson = (JSONObject)json.get("timeoutAction");
        Action timeoutAction = keyToAction((String)timeoutActionJson.get("action"));
        populateAction(timeoutActionJson, timeoutAction, session);
        menu.setTimeoutAction(timeoutAction);

        session.save(defaultAction);
        session.save(timeoutAction);

        try
        {
            session.save(menu);
        }
        catch(ConstraintViolationException e)
        {
            throw new BadRequestServletException("There is already a menu named '" + menu.getName() + "'");
        }

        List<Action> existingActions = Action.queryByMenuWithoutDefaultAndTimeout(session, menu);
        for(Action action : existingActions)
        {
            session.delete(action);
        }

        for(JSONObject action : (List<JSONObject>)json.get("actions"))
        {
            Action a = keyToAction((String)action.get("action"));
            populateAction(action, a, session);
            a.setMenu(menu);
            session.save(a);
        }
    }

    private Action keyToAction(String key)
    {
        if(key.equals("GoToMenu"))
        {
            return new GoToMenuAction();
        }
        else if(key.equals("DialNumber"))
        {
            return new DialNumberAction();
        }
        else if(key.equals("DialPressedNumber"))
        {
            return new DialPressedNumberAction();
        }
        else if(key.equals("LaunchApplication"))
        {
            return new LaunchApplicationAction();
        }
        
        throw new IllegalArgumentException("Cannot create Action from unknown key [" + key + "]");
    }

    private void populateAction(JSONObject json, Action action, Session session) throws ServletException
    {
        String name = (String)json.get("action");
        JSONObject arguments = (JSONObject)json.get("arguments");
        String keyPressed = (String)json.get("keyPressed");

        if(action instanceof GoToMenuAction)
        {
            Long menuId = Long.parseLong((String)arguments.get("menuId"));
            Menu menu = Menu.queryById(session, menuId);
            if(menu == null)
            {
                throw new BadRequestServletException("Destination menu with id [" + menuId + "] for keypress [" +
                                                     keyPressed + "] does not exist");
            }
            ((GoToMenuAction)action).setGoToMenu(menu);
        }
        else if(action instanceof DialNumberAction)
        {
            ((DialNumberAction)action).setNumber((String)arguments.get("number"));
        }
        else if(action instanceof LaunchApplicationAction)
        {
            ((LaunchApplicationAction)action).setApplicationName((String)arguments.get("applicationName"));
        }

        if(action == null)
        {
            throw new BadRequestServletException("Action [" + name + "] for keypress [" + keyPressed +
                                                 "] is not a valid action");
        }

        action.setKeyPressed(keyPressed);
    }
}
