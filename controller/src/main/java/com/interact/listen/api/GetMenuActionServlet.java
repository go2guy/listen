package com.interact.listen.api;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.attendant.Action;
import com.interact.listen.attendant.GoToMenuAction;
import com.interact.listen.attendant.Menu;
import com.interact.listen.command.IvrCommand;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

public class GetMenuActionServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String SINGLE_DIGIT_WILDCARD = "?";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.META_API_GET_MENU_ACTION);

        String menuId = request.getParameter("menuId");
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        IvrCommand action;
        
        //This is a request for the Top Menu
        if(menuId == null || menuId.equals(""))
        {
            Menu menu = Menu.queryTopMenu(session);
            action = Menu.queryById(session, menu.getId());
        }
        else
        {
            Menu menu = Menu.queryById(session, Long.valueOf(menuId));
            if(menu == null)
            {
                throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND);
            }
            
            //Only need keysPressed if a menuId was sent in
            String keysPressed = request.getParameter("keysPressed");
            
            if(keysPressed == null || keysPressed.trim().equals(""))
            {
                //assume a timeout if menuId passed in but no key presses
                action = menu.getTimeoutAction();
            }
            else
            {
                List<Action> menuActions = Action.queryByMenuWithoutDefaultAndTimeout(session, menu);
                Map<String, Action> mappings = new HashMap<String, Action>();
                
                for(Action singleAction : menuActions)
                {
                    mappings.put(singleAction.getKeyPressed(), singleAction);
                }
                
                action = getActionFromKeypress(session, mappings, keysPressed);
                
                if(action == null)
                {
                    // No matching configured menu action, using the default
                    action = menu.getDefaultAction();
                }
            }
        }
        
        if(action instanceof GoToMenuAction)
        {
            GoToMenuAction goToMenuAction = (GoToMenuAction)action;
            action = Menu.queryById(session, goToMenuAction.getGoToMenu().getId());
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, action.toIvrCommandJson(session), "text/plain");
    }

    private Action getActionFromKeypress(Session session, Map<String, Action> mappings, String keysPressed)
    {
        //if we have a specific mapping for the number, simply return it's action
        if(mappings.containsKey(keysPressed))
        {
            return mappings.get(keysPressed);
        }
        
        Map<String, Action> wildcards = new TreeMap<String, Action>();
        
        // strip all of the specific mappings (we didn't find one at this point, so we're looking for a wildcarded one)
        // also take out any mappings that are not equal to the number of keys pressed, they won't match since there
        // is not a "one or more" or "zero or more" wildcard
        for(Map.Entry<String, Action> entry : mappings.entrySet())
        {
            if(entry.getKey().endsWith(SINGLE_DIGIT_WILDCARD) && entry.getKey().length() == keysPressed.length())
            {
                wildcards.put(entry.getKey(), entry.getValue());
            }
        }
        
        for(Map.Entry<String, Action> entry : wildcards.entrySet())
        {
            // if the digits of the key without the wildcard(s) equals the same number of digits (length minus number of wildcards)
            // then we have a most specific match.  All wilcards will match with a 0.
            int keyLength = entry.getKey().length();
            int numWildcards = StringUtils.countMatches(entry.getKey(), SINGLE_DIGIT_WILDCARD);
            
            if(entry.getKey().substring(0, keyLength - numWildcards).equals(keysPressed.substring(0, keyLength - numWildcards)))
            {
                return entry.getValue();
            }
        }
        
        return null;
    }
}
