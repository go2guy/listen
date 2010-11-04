package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.attendant.Action;
import com.interact.listen.attendant.Menu;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class DeleteAttendantMenuServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.ATTENDANT))
        {
            throw new NotLicensedException(ListenFeature.ATTENDANT);
        }

        ServletUtil.sendStat(request, Stat.GUI_DELETE_ATTENDANT_MENU);

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

        String id = request.getParameter("id");
        if(id == null)
        {
            throw new BadRequestServletException("Missing required parameter [id]");
        }

        Menu menu;

        try
        {
            menu = (Menu)session.get(Menu.class, Long.parseLong(id));
        }
        catch(NumberFormatException e)
        {
            throw new BadRequestServletException("Parameter [id] with value [" + id + "] is not a valid number");
        }

        if(menu == null)
        {
            throw new BadRequestServletException("Menu with id [" + id + "] not found");
        }

        if(menu.getName().equals(Menu.TOP_MENU_NAME))
        {
            throw new BadRequestServletException("Cannot delete Top Menu");
        }

        for(Action action : Action.queryByMenuWithoutDefaultAndTimeout(session, menu))
        {
            session.delete(action);
        }

        session.delete(menu);
    }
}
