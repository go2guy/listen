package com.interact.listen.gui;

import com.interact.listen.EmailerService;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestNotificationSettingsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_TEST_NOTIFICATION_SETTINGS);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }
        
        String messageType = request.getParameter("messageType");
        if(messageType == null || messageType.equals(""))
        {
            throw new BadRequestServletException("Please provide a message type");
        }
        
        String address = request.getParameter("address");
        if(address == null || address.equals(""))
        {
            throw new BadRequestServletException("Please provide an address");
        }
        
        EmailerService emailService = new EmailerService();
        
        if(!emailService.sendTestNotificationSettingsMessage(messageType, address))
        {
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                             "An error occurred sending the test message", "text/plain");
        }
    }
}
