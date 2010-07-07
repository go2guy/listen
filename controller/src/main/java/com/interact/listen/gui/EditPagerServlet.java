package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class EditPagerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_EDIT_PAGER);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session, currentSubscriber, Channel.GUI);
        HistoryService historyService = new HistoryService(persistenceService);
        
        String alternateNumber = request.getParameter("alternateNumber");
        
        if(alternateNumber == null)
        {
            alternateNumber = "";
        }
        
        alternateNumber = alternateNumber.replaceAll("[-\\.]", "");
        
        if(alternateNumber.length() > 10)
        {
            throw new BadRequestServletException("Please provide an Alternate Number with ten digits or less");
        }
        
        if(!alternateNumber.matches("^[0-9]*$"))
        {
            throw new BadRequestServletException("Alternate number can only contain digits 0-9");
        }
        
        Configuration.set(Property.Key.ALTERNATE_NUMBER, alternateNumber);
        
        historyService.writeChangedAlternatePagerNumber(alternateNumber);
    }
}
